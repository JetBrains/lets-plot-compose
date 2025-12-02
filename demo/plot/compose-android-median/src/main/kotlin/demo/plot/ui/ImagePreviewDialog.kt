package demo.plot.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.letsPlot.compose.PlotFormat

@Composable
fun ImagePreviewDialog(uri: Uri, format: PlotFormat, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf<String?>(null) }
    var resolution by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uri) {
        displayName = getDisplayNameFromUri(context, uri)
    }

    val isWebViewContent = format == PlotFormat.SVG || format == PlotFormat.HTML

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            if (isWebViewContent) {
                var webViewContent by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(uri) {
                    withContext(Dispatchers.IO) {
                        webViewContent =
                            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                }
                if (webViewContent != null) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = object : WebViewClient() {
                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        println("WebView Error: ${error?.description}")
                                    }
                                }
                                settings.javaScriptEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(
                                "file:///android_asset/",
                                webViewContent!!,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier.fillMaxSize().padding(32.dp)
                    )
                } else {
                    CircularProgressIndicator()
                }
            } else {
                var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(uri) {
                    isLoading = true
                    withContext(Dispatchers.IO) {
                        try {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val bmp = BitmapFactory.decodeStream(inputStream)
                                if (bmp != null) {
                                    // Extract resolution from the decoded bitmap
                                    resolution = "${bmp.width} x ${bmp.height} px"
                                    imageBitmap = bmp.asImageBitmap()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    isLoading = false
                }

                when {
                    isLoading -> CircularProgressIndicator()
                    imageBitmap != null -> Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Saved plot preview",
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentScale = ContentScale.Fit
                    )

                    else -> Text("Failed to load image", color = Color.White)
                }
            }

            // Display Info Overlay (Name + Resolution)
            displayName?.let { name ->
                val infoText = if (resolution != null) "$name\n$resolution" else name

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Text(
                        text = infoText,
                        color = Color.White,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Text("Close")
            }
        }
    }
}

private fun getDisplayNameFromUri(context: Context, uri: Uri): String? {
    if (uri.scheme != "content") return null
    var displayName: String? = null
    try {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return displayName
}