package com.krysanify.name.android

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.*
import androidx.webkit.WebViewAssetLoader.*
import androidx.webkit.WebViewFeature.*

const val DOMAIN = "template.domain.app"

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false
                allowContentAccess = false
            }

            val loader = Builder().setDomain(DOMAIN)
                .addPathHandler("/assets/", AssetsPathHandler(application))
                .addPathHandler("/res/", ResourcesPathHandler(application))
                .build()
            webViewClient = MainViewClient(loader)
            webChromeClient = MainChromeClient()

            setContentView(this)
        }.loadUrl("https://${DOMAIN}/assets/index.html")
    }
}

class MainViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
        assetLoader.shouldInterceptRequest(request.url)

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (url.endsWith("/assets/index.html")) {
            initChannel(view)
        }
    }

    private fun initChannel(view: WebView) {
        if (isFeatureSupported(CREATE_WEB_MESSAGE_CHANNEL)) {
            WebViewCompat.createWebMessageChannel(view).let {
                ProxyCallback(view.context, it[0]).subscribe(null)

                if (isFeatureSupported(POST_WEB_MESSAGE)) {
                    val message = WebMessageCompat("{'TODO':'FIXME'}", arrayOf(it[1]))
                    val origin = Uri.parse("https://${DOMAIN}")
                    WebViewCompat.postWebMessage(view, message, origin)
                }
            }
        }
    }
}

class ProxyCallback(
    private val context: Context,
    private val reply: WebMessagePortCompat
) : WebMessagePortCompat.WebMessageCallbackCompat() {
    fun subscribe(handler: Handler?) {
        if (isFeatureSupported(WEB_MESSAGE_PORT_SET_MESSAGE_CALLBACK)) {
            reply.setWebMessageCallback(handler, this)
        } else if (isFeatureSupported(WEB_MESSAGE_PORT_CLOSE)) {
            reply.close()
        }
    }

    override fun onMessage(port: WebMessagePortCompat, message: WebMessageCompat?) {
        if (null != message && isFeatureSupported(WEB_MESSAGE_PORT_POST_MESSAGE)) {
            //TODO: replace echo with actual message handling
            reply.postMessage(message)
        } else if (isFeatureSupported(WEB_MESSAGE_PORT_CLOSE)) {
            reply.close()
            port.close()
        }
    }
}

class MainChromeClient : WebChromeClient() {

}