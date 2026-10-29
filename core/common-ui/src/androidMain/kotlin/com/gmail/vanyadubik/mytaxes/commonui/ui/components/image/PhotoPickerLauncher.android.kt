package com.gmail.vanyadubik.myassistant.commonui.ui.components.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.choose_from_gallery
import com.gmail.vanyadubik.myassistant.resource.select_photo_source
import com.gmail.vanyadubik.myassistant.resource.take_photo
import org.jetbrains.compose.resources.stringResource
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalMaterial3Api::class)
@Composable
actual fun rememberPhotoPickerLauncher(
    onPhotoPicked: (String) -> Unit,
): PhotoPickerLauncher {
    val context = LocalContext.current
    var showChoiceDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { processBitmap(it, onPhotoPicked) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val originalBitmap = BitmapFactory.decodeStream(stream) ?: return@use
                    processBitmap(originalBitmap, onPhotoPicked)
                }
            }
        }
    }

    if (showChoiceDialog) {
        ModalBottomSheet(
            onDismissRequest = { showChoiceDialog = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(Res.string.select_photo_source),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppButton(
                        text = stringResource(Res.string.take_photo),
                        onClick = {
                            showChoiceDialog = false
                            cameraLauncher.launch(null)
                        },
                        leftIcon = Icons.Default.AddAPhoto,
                    )
                    AppButton(
                        text = stringResource(Res.string.choose_from_gallery),
                        onClick = {
                            showChoiceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        leftIcon = Icons.Default.Photo,
                        isOutlinedButton = true,
                    )
                }
            }
        }
    }

    return remember {
        object : PhotoPickerLauncher {
            override fun launch() {
                showChoiceDialog = true
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun processBitmap(originalBitmap: Bitmap, onPhotoPicked: (String) -> Unit) {
    val maxDimension = 800
    val width = originalBitmap.width
    val height = originalBitmap.height
    val (targetWidth, targetHeight) = if (width > maxDimension || height > maxDimension) {
        if (width > height) {
            maxDimension to (height * maxDimension / width)
        } else {
            (width * maxDimension / height) to maxDimension
        }
    } else {
        width to height
    }
    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
    val compressedBytes = outputStream.toByteArray()
    val base64 = Base64.encode(compressedBytes)
    onPhotoPicked("data:image/jpeg;base64,$base64")
}
