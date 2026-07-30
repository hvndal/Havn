package com.havn.app.ui.screens.splash

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * WebGL Ambient Atmosphere Shader — Directly compiled from stitch_extracted/shader/code.html
 * Soft moving warm waves of Ivory (#FAF8F4), Sand (#F5F3E6), and Sage (#8DA08C) with vignette.
 */
@Composable
fun HavnShaderBackground(
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val context = LocalContext.current

    val shaderHtml = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          html, body { width: 100%; height: 100%; overflow: hidden; background: #FAF8F4; }
          canvas { display: block; width: 100%; height: 100%; }
        </style>
        </head>
        <body>
        <canvas id="c"></canvas>
        <script>
        (function() {
          const canvas = document.getElementById('c');
          function syncSize() {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
          }
          syncSize();
          window.addEventListener('resize', syncSize);

          const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
          if (!gl) return;

          const vs = `
            attribute vec2 a_position;
            varying vec2 v_texCoord;
            void main() {
              v_texCoord = a_position * 0.5 + 0.5;
              gl_Position = vec4(a_position, 0.0, 1.0);
            }
          `;

          const fs = `
            precision highp float;
            varying vec2 v_texCoord;
            uniform float u_time;
            uniform vec2 u_resolution;

            void main() {
                vec2 uv = v_texCoord;
                
                // Base colors from the Hävn palette (from stitch_extracted/shader/code.html)
                vec3 ivory = vec3(0.984, 0.976, 0.961); // #FAF8F4
                vec3 sand = vec3(0.950, 0.930, 0.900);  // Warm Sand
                vec3 sage = vec3(0.553, 0.627, 0.549);  // #8DA08C (Muted Sage)
                
                // Soft slow-moving noise waves
                float wave1 = sin(uv.x * 2.0 + u_time * 0.1) * 0.5 + 0.5;
                float wave2 = cos(uv.y * 3.0 - u_time * 0.15) * 0.5 + 0.5;
                float noise = wave1 * wave2;
                
                // Blend colors for subtle warm atmosphere
                vec3 color = mix(ivory, sand, noise * 0.35);
                color = mix(color, sage, noise * 0.08);
                
                // Vignette
                float vignette = length(uv - 0.5);
                color *= 1.0 - vignette * 0.12;
                
                gl_FragColor = vec4(color, 1.0);
            }
          `;

          function cs(type, src) {
            const s = gl.createShader(type);
            gl.shaderSource(s, src);
            gl.compileShader(s);
            return s;
          }

          const prog = gl.createProgram();
          gl.attachShader(prog, cs(gl.VERTEX_SHADER, vs));
          gl.attachShader(prog, cs(gl.FRAGMENT_SHADER, fs));
          gl.linkProgram(prog);
          gl.useProgram(prog);

          const buf = gl.createBuffer();
          gl.bindBuffer(gl.ARRAY_BUFFER, buf);
          gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1,-1, 1,-1, -1,1, 1,1]), gl.STATIC_DRAW);

          const pos = gl.getAttribLocation(prog, 'a_position');
          gl.enableVertexAttribArray(pos);
          gl.vertexAttribPointer(pos, 2, gl.FLOAT, false, 0, 0);

          const uTime = gl.getUniformLocation(prog, 'u_time');
          const uRes = gl.getUniformLocation(prog, 'u_resolution');

          function render(t) {
            gl.viewport(0, 0, canvas.width, canvas.height);
            if (uTime) gl.uniform1f(uTime, t * 0.001);
            if (uRes) gl.uniform2f(uRes, canvas.width, canvas.height);
            gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
            requestAnimationFrame(render);
          }
          render(0);
        })();
        </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = WebViewClient()
                loadDataWithBaseURL(null, shaderHtml, "text/html", "UTF-8", null)
            }
        }
    )
}
