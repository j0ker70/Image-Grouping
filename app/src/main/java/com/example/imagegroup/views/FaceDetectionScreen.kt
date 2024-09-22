package com.example.imagegroup.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.imagegroup.R
import com.example.imagegroup.ui.theme.ImageGroupTheme
import com.example.imagegroup.utils.FaceDetectionProcessor.detectFacesInImages

@Composable
fun FaceDetectionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Suppose we have a list of images in the drawable resources
    val imageResources = listOf(
        R.drawable.image1, // Add your image references here
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4,
        R.drawable.image5,
        R.drawable.image6,
        R.drawable.image7,
        R.drawable.image8,
        R.drawable.image9,
        R.drawable.image10,
        R.drawable.image11,
        R.drawable.image12,
        R.drawable.image13,
        R.drawable.image14,
        R.drawable.image15,
        R.drawable.image16,
        R.drawable.image17,
        R.drawable.image18,
        R.drawable.image19,
        R.drawable.image20
    )

    // Load the images from resources
    val images = imageResources.map { resId ->
        BitmapFactory.decodeResource(context.resources, resId)
    }

    // State for holding unique faces and their associated images
    var uniqueFaces by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var faceToImagesMap by remember { mutableStateOf<Map<Bitmap, List<Bitmap>>>(emptyMap()) }

    // State for holding the currently selected face and its associated images
    var selectedFaceImages by remember { mutableStateOf<List<Bitmap>?>(null) }

    // Detect faces across all images once they are loaded
    LaunchedEffect(images) {
        detectFacesInImages(images) { faces, faceMap ->
            uniqueFaces = faces
            faceToImagesMap = faceMap
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Horizontal list of unique faces
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uniqueFaces) { faceBitmap ->
                Image(
                    bitmap = faceBitmap.asImageBitmap(),
                    contentDescription = "Face",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape) // Make the images round
                        .clickable {
                            // Show the list of images for the clicked face
                            selectedFaceImages = faceToImagesMap[faceBitmap]
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vertical list of images corresponding to the selected face
        selectedFaceImages?.let { images ->
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { imageBitmap ->
                    Image(
                        bitmap = imageBitmap.asImageBitmap(),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImageGroupTheme {
        FaceDetectionScreen()
    }
}