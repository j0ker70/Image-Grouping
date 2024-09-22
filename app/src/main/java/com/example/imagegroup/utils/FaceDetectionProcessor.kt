package com.example.imagegroup.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FaceDetectionProcessor {
    private lateinit var faceNet: FaceNet

    fun initializeFaceNet(context: Context) {
        faceNet = FaceNet(context)
    }

    suspend fun detectFacesInImages(
        images: List<Bitmap>,
        onDetectionComplete: (List<Bitmap>, Map<Bitmap, List<Bitmap>>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val uniqueFaces =
                mutableListOf<Pair<Bitmap, FloatArray>>() // Store face and its embedding
            val faceToImagesMap = mutableMapOf<Bitmap, MutableList<Bitmap>>()

            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build()

            val detector = FaceDetection.getClient(options)

            for (image in images) {
                val inputImage = InputImage.fromBitmap(image, 0)
                val faces = detectFaces(detector, inputImage)

                val faceBitmaps = extractFaces(image, faces)
                for (faceBitmap in faceBitmaps) {
                    val embedding = faceNet.getFaceEmbedding(faceBitmap)

                    // Check if the face is already in the uniqueFaces list
                    val existingFace = uniqueFaces.find { (_, existingEmbedding) ->
                        faceNet.calculateSimilarity(
                            existingEmbedding,
                            embedding
                        ) > FaceNet.SIMILARITY_THRESHOLD
                    }

                    if (existingFace == null) {
                        // Add new unique face and its embedding
                        uniqueFaces.add(Pair(faceBitmap, embedding))
                        faceToImagesMap[faceBitmap] = mutableListOf(image)
                    } else {
                        // Add the image to the existing face's list
                        faceToImagesMap[existingFace.first]?.add(image)
                    }
                }
            }

            val uniqueFaceBitmaps = uniqueFaces.map { it.first }
            onDetectionComplete(uniqueFaceBitmaps, faceToImagesMap)
        }
    }

    // Coroutine-friendly face detection
    private suspend fun detectFaces(
        detector: FaceDetector,
        image: InputImage
    ): List<Face> {
        return suspendCoroutine { continuation ->
            detector.process(image)
                .addOnSuccessListener { faces ->
                    continuation.resume(faces)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    private fun extractFaces(bitmap: Bitmap, faces: List<Face>): List<Bitmap> {
        val faceBitmaps = mutableListOf<Bitmap>()
        for (face in faces) {
            val bounds = face.boundingBox
            val faceBitmap = cropBitmap(bitmap, bounds)
            faceBitmaps.add(faceBitmap)
        }
        return faceBitmaps
    }

    private fun cropBitmap(bitmap: Bitmap, rect: Rect): Bitmap {
        // Ensure the cropping bounds do not exceed the bitmap dimensions
        val left = rect.left.coerceAtLeast(0)
        val top = rect.top.coerceAtLeast(0)
        val right = rect.right.coerceAtMost(bitmap.width)
        val bottom = rect.bottom.coerceAtMost(bitmap.height)

        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }
}