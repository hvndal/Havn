package com.havn.app.ui.components

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HavnOrganizerView(
    weekDataJson: String,
    selectedDay: Int,
    modifier: Modifier = Modifier,
    onSlotTapped: (Int) -> Unit = {},
    onWebViewReady: (WebView) -> Unit = {},
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var pageReady by remember { mutableStateOf(false) }

    LaunchedEffect(weekDataJson, selectedDay, pageReady) {
        if (!pageReady) return@LaunchedEffect
        val quotedJson = JSONObject.quote(weekDataJson)
        webView?.evaluateJavascript(
            "HavnOrganizer.setWeekData(JSON.parse($quotedJson)); HavnOrganizer.selectDay($selectedDay);",
            null,
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        pageReady = true
                        val quotedJson = JSONObject.quote(weekDataJson)
                        view?.evaluateJavascript(
                            "HavnOrganizer.setWeekData(JSON.parse($quotedJson)); HavnOrganizer.selectDay($selectedDay);",
                            null,
                        )
                    }
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onSlotTapped(slotIndex: Int, isOpen: Boolean) {
                        onSlotTapped(slotIndex)
                    }
                }, "AndroidOrganizer")
                loadUrl("file:///android_asset/organizer/organizer.html")
                webView = this
                onWebViewReady(this)
            }
        },
        update = { view ->
            webView = view
        },
    )
}
