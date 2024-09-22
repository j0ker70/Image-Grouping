package com.example.imagegroup.utils

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class FaceNet(context: Context) {

    private val modelFile = "facenet.tflite"
    private val interpreter: Interpreter
    private val inputImageWidth = 160
    private val inputImageHeight = 160
    private val embeddingSize = 128

    init {
        val model = loadModelFile(context)
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(context: Context): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelFile)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun getFaceEmbedding(bitmap: Bitmap): FloatArray {
        val resizedBitmap =
            Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        val tensorImage = TensorImage.fromBitmap(resizedBitmap)

        val outputBuffer =
            TensorBuffer.createFixedSize(intArrayOf(1, embeddingSize), DataType.FLOAT32)

        interpreter.run(tensorImage.buffer, outputBuffer.buffer.rewind())

        return outputBuffer.floatArray
    }

    fun calculateSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
        // Use cosine similarity between embeddings
        val dotProduct = embedding1.zip(embedding2).sumOf { (a, b) -> (a * b).toDouble() }
        val magnitude1 = sqrt(embedding1.sumOf { (it * it).toDouble() })
        val magnitude2 = sqrt(embedding2.sumOf { (it * it).toDouble() })
        return (dotProduct / (magnitude1 * magnitude2)).toFloat()
    }

    companion object {
        const val SIMILARITY_THRESHOLD = 0.6f // Adjust threshold as needed
    }
}
