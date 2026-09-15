package com.havn.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.viewinterop.AndroidView
import com.havn.app.ui.theme.HavnTheme
import org.json.JSONObject

/**
 * The 3D pill organiser.
 *
 * This is the most distinctive asset in the product, so it stays — but it is
 * now driven by the theme and torn down properly.
 *
 * Three fixes over the previous version:
 *
 *  • **Theme.** The page painted a fixed `#EDEAE4` body and light-mode slot
 *    labels, so in dark mode it was a bright rectangle in the middle of an ink
 *    canvas. It now receives the resolved palette and repaints on theme change.
 *
 *  • **Lifecycle.** The WebView was never destroyed. Navigating away and back
 *    leaked a renderer each time, and with a WebGL context per instance that
 *    added up quickly. It is now destroyed on dispose.
 *
 *  • **Re-entrancy.** `update` reassigned the captured reference on every
 *    recomposition, which meant the data push could race the page load. Load
 *    state is now explicit.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HavnOrganizerView(
    dataJson: String,
    selectedSlot: Int,
    modifier: Modifier = Modifier,
    onSlotTapped: (Int) -> Unit = {},
) {
    val colors = HavnTheme.colors
    val haptics = LocalHapticFeedback.current
    val currentOnSlotTapped by rememberUpdatedState(onSlotTapped)

    var webView by remember { mutableStateOf<WebView?>(null) }
    var pageReady by remember { mutableStateOf(false) }

    val themeJson = remember(colors) {
        JSONObject()
            .put("isDark", colors.isDark)
            .put("canvas", colors.canvas.hex())
            .put("surface", colors.surface.hex())
            .put("body", colors.surfaceRaised.hex())
            .put("accent", colors.accent.hex())
            .put("accentSoft", colors.accentSoft.hex())
            .put("text", colors.textPrimary.hex())
            .put("textSoft", colors.textTertiary.hex())
            .put("hairline", colors.hairlineStrong.hex())
            .toString()
    }

    LaunchedEffect(pageReady, dataJson, selectedSlot, themeJson) {
        val view = webView ?: return@LaunchedEffect
        if (!pageReady) return@LaunchedEffect
        view.evaluateJavascript(
            """
            HavnOrganizer.setTheme($themeJson);
            HavnOrganizer.setWeekData($dataJson);
            HavnOrganizer.selectDay($selectedSlot);
            """.trimIndent(),
            null,
        )
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    // The page is a local asset and loads no remote content;
                    // leaving file access on would only widen its reach.
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mediaPlaybackRequiresUserGesture = true
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = WebView.OVER_SCROLL_NEVER

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            pageReady = true
                        }
                    }

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onSlotTapped(slotIndex: Int, isOpen: Boolean) {
                                post {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentOnSlotTapped(slotIndex)
                                }
                            }

                            @JavascriptInterface
                            fun onDragStarted() {
                                post {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        },
                        "AndroidOrganizer",
                    )

                    loadUrl("file:///android_asset/organizer/organizer.html")
                    webView = this
                }
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                // Order matters: stop the page before tearing the view down, or
                // the renderer can keep running against a detached host.
                stopLoading()
                loadUrl("about:blank")
                removeJavascriptInterface("AndroidOrganizer")
                (parent as? ViewGroup)?.removeView(this)
                destroy()
            }
            webView = null
        }
    }
}

private fun androidx.compose.ui.graphics.Color.hex(): String =
    String.format("#%06X", 0xFFFFFF and toArgb())
