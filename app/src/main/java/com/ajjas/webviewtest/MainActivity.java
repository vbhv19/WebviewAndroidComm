package com.ajjas.webviewtest;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getCanonicalName();
  private WebView webView;
  private Button sendByteArrayButton;
  private Button sendHexByteArrayButton;
  private Button sendDirectByteArrayButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    webView = findViewById(R.id.webView);
    sendByteArrayButton = findViewById(R.id.sendByteArrayButton);
    sendHexByteArrayButton = findViewById(R.id.sendHexByteArrayButton);
    sendDirectByteArrayButton = findViewById(R.id.sendDirectByteArrayButton);

    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(new WebViewClient());
    webView.setWebChromeClient(new WebChromeClient());

    webView.addJavascriptInterface(new WebAppInterface(this), "Android");

    webView.loadUrl("file:///android_asset/vbhv.html");

    sendByteArrayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendBase64ByteArrayToWebView();
      }
    });

    sendHexByteArrayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendHexByteArrayToWebView();
      }
    });

    sendDirectByteArrayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendDirectByteArrayToWebView();
      }
    });
  }

  private void sendBase64ByteArrayToWebView() {
    byte[] byteArray = "Vbhv WebView!".getBytes(StandardCharsets.UTF_8);
    String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

    webView.post(new Runnable() {
      @Override
      public void run() {
        webView.evaluateJavascript("javascript:handleBase64ByteArrayFromAndroid('" + base64String + "')", null);
      }
    });
  }

  private void sendHexByteArrayToWebView() {
    byte[] byteArray = "Vbhv WebView!".getBytes(StandardCharsets.UTF_8);
    String hexString = bytesToHex(byteArray);

    webView.post(new Runnable() {
      @Override
      public void run() {
        webView.evaluateJavascript("javascript:handleHexByteArrayFromAndroid('" + hexString + "')", null);
      }
    });
  }

  private void sendDirectByteArrayToWebView() {
    byte[] byteArray = "Hello, WebView!".getBytes(StandardCharsets.UTF_8); // Your binary data
    String jsArrayString = bytesToJavaScriptArray(byteArray);

    Log.d(TAG, "sendDirectByteArrayToWebView: " + jsArrayString);
    webView.post(new Runnable() {
      @Override
      public void run() {
        webView.evaluateJavascript("javascript:handleDirectByteArrayFromAndroid(" + jsArrayString + ")", null);
      }
    });
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xFF & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  private String bytesToJavaScriptArray(byte[] byteArray) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < byteArray.length; i++) {
      sb.append(byteArray[i]);
      if (i < byteArray.length - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  public class WebAppInterface {
    private Context context;

    WebAppInterface(Context c) {
      context = c;
    }

    @JavascriptInterface
    public void receiveBase64Data(String base64String) {
      byte[] byteArray = Base64.decode(base64String, Base64.DEFAULT);
      Log.d(TAG, "receiveBase64Data: " + Arrays.toString(byteArray));
      Toast.makeText(context, "Received Base64 binary data from WebView", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void receiveHexData(String hexString) {
      byte[] byteArray = hexStringToByteArray(hexString);
      Log.d(TAG, "receiveBase64Data: " + Arrays.toString(byteArray));
      Toast.makeText(context, "Received Hex binary data from WebView", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void receiveDirectByteArray(int[] intArray) {
      byte[] byteArray = new byte[intArray.length];
      for (int i = 0; i < intArray.length; i++) {
        byteArray[i] = (byte) intArray[i];
      }
      Log.d(TAG, "receiveDirectByteArray: " + Arrays.toString(byteArray));
      Toast.makeText(context, "Received Direct binary data from WebView", Toast.LENGTH_SHORT).show();
    }

    private byte[] hexStringToByteArray(String hexString) {
      int len = hexString.length();
      byte[] data = new byte[len / 2];
      for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
          + Character.digit(hexString.charAt(i+1), 16));
      }
      return data;
    }
  }
}
