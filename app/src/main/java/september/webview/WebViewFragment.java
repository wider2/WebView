package september.webview;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


public class WebViewFragment extends DialogFragment {

    private final String TAG = WebViewFragment.class.getSimpleName();
    private View mainView;
    private String mUrl;
    private View spinnerLoader;
    private TextView tvProgress;
    private WebView webView;

    public static WebViewFragment newInstance(String url) {
        WebViewFragment f = new WebViewFragment();
        f.setUrl(url);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_web_view, container, false);

        init();

        return mainView;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setStyle(STYLE_NO_TITLE, 0);
    }

    public void init() {
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

            spinnerLoader = mainView.findViewById(R.id.spinnerLoader);
            tvProgress = (TextView) mainView.findViewById(R.id.tvProgress);
            tvProgress.setVisibility(View.VISIBLE);

            webView = (WebView) mainView.findViewById(R.id.webView);

            webView.setVisibility(View.VISIBLE);
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearHistory();
            webView.clearMatches();
            webView.clearSslPreferences();
            //webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.135 Mobile Safari/537.36");
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            //webView.getSettings().setGeolocationEnabled(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            //webView.measure(100, 100);
            //webView.getSettings().setDefaultFontSize(20);
            webView.getSettings().setAppCacheEnabled(true);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            //webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

            webView.getSettings().setAllowContentAccess(true);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

            //webView.setHorizontalScrollBarEnabled(false);
            webView.setScrollbarFadingEnabled(true);
            webView.setSaveEnabled(true);
            webView.setNetworkAvailable(true);
            webView.getSettings().setBlockNetworkImage(false);

            webView.getSettings().setDatabaseEnabled(true);
            webView.getSettings().setSaveFormData(true);
            webView.getSettings().setSupportMultipleWindows(true);

            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);

            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(true);

            //if SDK version is greater of 19 then activate hardware acceleration otherwise activate software acceleration
            if (Build.VERSION.SDK_INT >= 19) {
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                webView.getSettings().setLoadsImagesAutomatically(true);
            } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                webView.getSettings().setLoadsImagesAutomatically(false);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings()
                        .setMixedContentMode(
                                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        );
            }

            webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int newProgress) {
                    tvProgress.setVisibility(View.VISIBLE);
                    tvProgress.setText("Page loading: " + newProgress + "%");
                    if (newProgress == 100) {
                        tvProgress.setText("Page Loaded.");
                    }
                }
            });

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    tvProgress.setText("Page Started.");
                    spinnerLoader.setVisibility(View.VISIBLE);
                    super.onPageStarted(view, url, favicon);
                }

                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    tvProgress.setVisibility(View.GONE);
                    return true;
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    view.loadUrl(request.getUrl().toString());
                    tvProgress.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (view.isShown()) {
                        view.postInvalidate();
                    }
                    if (android.os.Build.VERSION.SDK_INT >= 19) {
                        view.requestFocus();
                    }
                    spinnerLoader.clearAnimation();
                    spinnerLoader.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    super.onPageFinished(view, url);
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed(); // Ignore SSL certificate errors
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onReceivedError(WebView view, int errorcode, String description, String failingUrl) {
                    super.onReceivedError(view, errorcode, description, failingUrl);
                    tvProgress.setVisibility(View.VISIBLE);
                    tvProgress.setText("Error: " + errorcode + ", " + description + "; " + failingUrl);
                }

                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    tvProgress.setVisibility(View.VISIBLE);
                    tvProgress.setText("The webView with the following url " + request.getUrl().toString() +
                            " failed with the following errorCode " +
                            "" + error.getDescription());
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    super.onReceivedHttpError(view, request, errorResponse);
                    tvProgress.setVisibility(View.VISIBLE);
                    tvProgress.setText("The webView with the following url " + request.getUrl().toString() +
                            " failed with the following errorCode " +
                            "" + errorResponse.getStatusCode());
                }
            });

            webView.loadUrl(mUrl);

        } catch (NullPointerException ex) {
            Log.d("WebView exception", ex.getMessage());
        }
    }

    public void setUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith("www")) {
            mUrl = "http://" + url;
        } else {
            mUrl = url;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDialogToScreenSize(getActivity(), getDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        setDialogToScreenSize(getActivity(), getDialog(), true);
    }

    public static void setDialogToScreenSize(Context context, Dialog dialog) {
        setDialogToScreenSize(context, dialog, false);
    }

    public static void setDialogToScreenSize(Context context, Dialog dialog, boolean forAllDevices) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;

        if ((forAllDevices || !isTablet(context)) && dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(width, height);
        }
    }

    public static boolean isTablet(Context context) {
        final String screenType = context.getResources().getString(R.string.screen_type);
        return "tablet".equalsIgnoreCase(screenType);
    }

}
