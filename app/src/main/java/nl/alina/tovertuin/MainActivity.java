package nl.alina.tovertuin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.window.OnBackInvokedDispatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;

/** Queen Alina: the game from app/src/main/assets/web, fully offline. */
public class MainActivity extends Activity {
    static final String HOST = "appassets.androidplatform.net";
    static final String START = "https://" + HOST + "/index.html";

    WebView web;
    private TextToSpeech tts;
    private volatile boolean ttsReady;

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        w.setStatusBarColor(Color.parseColor("#dff1ff"));
        w.setNavigationBarColor(Color.parseColor("#dff1ff"));

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) return;
            // Loading the Dutch voice can take seconds; on the main thread that freezes the web view too.
            new Thread(() -> {
                int r = tts.setLanguage(new Locale("nl", "NL"));
                ttsReady = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED;
            }, "tts-language").start();
        });

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) WebView.setWebContentsDebuggingEnabled(true);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#dff1ff"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setTextZoom(100);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        web.addJavascriptInterface(new Bridge(), "QAHost");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new AssetClient(getAssets()));
        setContentView(web);
        hideBars();
        if (Build.VERSION.SDK_INT >= 33)
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::goBack);
        if (state == null || web.restoreState(state) == null) web.loadUrl(START);
    }

    @SuppressWarnings("deprecation")
    private void hideBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override protected void onResume() {
        super.onResume();
        web.onResume();
        web.resumeTimers();
    }

    @Override protected void onPause() {
        if (tts != null) tts.stop();
        web.onPause();
        web.pauseTimers();
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (tts != null) tts.shutdown();
        web.destroy();
        super.onDestroy();
    }

    /** Back goes to the game's own back button; on the main menu the app goes to the background. */
    void goBack() {
        web.evaluateJavascript("(function(){var b=document.getElementById('back');if(b){b.click();return true;}return false;})()",
            r -> { if (!"true".equals(r)) moveTaskToBack(true); });
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { goBack(); }

    final class Bridge {
        @JavascriptInterface public void speak(String text, float rate, float pitch) {
            if (!ttsReady || text == null) return;
            tts.setSpeechRate(Math.max(.5f, Math.min(1.5f, rate)));
            tts.setPitch(Math.max(.5f, Math.min(1.5f, pitch)));
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "qa");
        }

        @JavascriptInterface public void stop() { if (tts != null) tts.stop(); }

        @JavascriptInterface public boolean ttsReady() { return ttsReady; }

        /** Stars earned in the earlier native app, so they carry over once. */
        @JavascriptInterface public int legacyStars() { return new GameStore(MainActivity.this).load().stars; }
    }

    /** Serves assets/web as https://appassets.androidplatform.net/ and blocks everything else. */
    static final class AssetClient extends WebViewClient {
        private final AssetManager assets;
        AssetClient(AssetManager assets) { this.assets = assets; }

        @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
            if (!HOST.equals(req.getUrl().getHost())) return missing();
            String path = req.getUrl().getPath();
            if (path == null || path.equals("/")) path = "/index.html";
            try {
                InputStream in = assets.open("web" + path);
                WebResourceResponse r = new WebResourceResponse(mime(path), path.endsWith(".html") || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".json") ? "utf-8" : null, in);
                r.setResponseHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"));
                return r;
            } catch (IOException e) {
                return missing();
            }
        }

        @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
            return !HOST.equals(req.getUrl().getHost());
        }

        private static WebResourceResponse missing() {
            return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found", Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
        }

        static String mime(String p) {
            if (p.endsWith(".html")) return "text/html";
            if (p.endsWith(".js")) return "text/javascript";
            if (p.endsWith(".css")) return "text/css";
            if (p.endsWith(".json")) return "application/json";
            if (p.endsWith(".mp3")) return "audio/mpeg";
            if (p.endsWith(".woff2")) return "font/woff2";
            if (p.endsWith(".png")) return "image/png";
            return "application/octet-stream";
        }
    }
}
