package org.banrural;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.provider.MediaStore.Images;

import java.io.OutputStream;
import java.util.Date;

public class ScreenshotPG extends CordovaPlugin {
  private static final String TAG = "ScreenshotPG";

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Inicializando ScreenshotPG");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    String mensaje = "";
    PluginResult result;
    if ("captureScreen".equals(action)) {
        mensaje = captureScreen(args);
    }
    result = new PluginResult(PluginResult.Status.OK, mensaje);
    callbackContext.sendPluginResult(result);
    return true;
  }

  public String captureScreen(JSONArray args) throws JSONException {
    String ret = "";
    String nombre = args.getString(0);
    Date date = new Date();
    CharSequence format = DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
    Bitmap b = getScreenShot();
    boolean val = insertImage(webView.getContext().getContentResolver(), b, nombre + format + ".jpg", "description");
    if(val) {
        ret = "Se ha realizado la captura de pantalla.";
    } else {
        ret = "La captura de pantalla falló.";
    }
    return ret;
  }

  public Bitmap getScreenShot() {
    Bitmap bitmap = null;
    boolean isCrosswalk = false;
    try {
      Class.forName("org.crosswalk.engine.XWalkWebViewEngine");
      isCrosswalk = true;
    } catch (Exception e) {
    }
    if (isCrosswalk) {
      webView.getPluginManager().postMessage("captureXWalkBitmap", this);
    } else {
      View view = webView.getView();
      view.setDrawingCacheEnabled(true);
      bitmap = Bitmap.createBitmap(view.getDrawingCache());
      view.setDrawingCacheEnabled(false);
    }
    return bitmap;
  }

  public final boolean insertImage(ContentResolver cr,
                                   Bitmap source,
                                   String title,
                                   String description) {
    boolean valRetorno = false;

    ContentValues values = new ContentValues();
    values.put(Images.Media.TITLE, title);
    values.put(Images.Media.DISPLAY_NAME, title);
    values.put(Images.Media.DESCRIPTION, description);
    values.put(Images.Media.MIME_TYPE, "image/jpeg");
    // Add the date meta data to ensure the image is added at the front of the gallery
    values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());

    Uri url = null;
    try {
      url = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);

      if (source != null) {
        OutputStream imageOut = cr.openOutputStream(url);
        try {
          source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
        } finally {
          imageOut.close();
        }
      } else {
        cr.delete(url, null, null);
        url = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (url != null) {
        cr.delete(url, null, null);
        url = null;
      }
    }
    if (url != null) {
      valRetorno = true;
    }
    return valRetorno;
  }
}
