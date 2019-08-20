package com.neux.androidScreenshot;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;


/**
 * This class echoes a string called from JavaScript.
 */
public class androidScreenshot extends CordovaPlugin {
    public static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;


    private static final String[] MEDIA_PROJECTIONS =  {
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
    };

    private static final String[] KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot", "screen shot",
            "screencapture", "screen_capture", "screen-capture", "screen capture",
            "screencap", "screen_cap", "screen-cap", "screen cap"
    };
    
    private ContentObserver mInternalObserver;

    private ContentObserver mExternalObserver;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private CallbackContext callback;

    private class MediaContentObserver extends ContentObserver {

        private Uri mContentUri;

        public MediaContentObserver(Uri contentUri, Handler handler) {
            super(handler);
            mContentUri = contentUri;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("onChange", mContentUri.toString());
            handleMediaContentChange(mContentUri);
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            Activity ctx = this.cordova.getActivity();

            if(!PermissionHelper.hasPermission(this, READ)) {
                PermissionHelper.requestPermission(this, 0, READ);
            }
            
            mHandlerThread = new HandlerThread("Screenshot_Observer");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());

            // 初始化
            mInternalObserver = new MediaContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, mHandler);
            mExternalObserver = new MediaContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mHandler);
            callback = callbackContext;
        
            // 添加监听
            ctx.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                false,
                mInternalObserver
            );
            ctx.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false,
                mExternalObserver
            );

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "Init");
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);

            // String message = args.getString(0);
            // this.coolMethod(message, callbackContext);
            return true;
        } else if (action.equals("stop")) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "Error");
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }
        return false;
    }
    
    // protected void onCreate(@Nullable Bundle savedInstanceState) {
    //     super.onCreate(savedInstanceState);
    //     setContentView(R.layout.activity_screenshot);
    // }
    
    // protected void onDestroy() {
    //     super.onDestroy();
        
    //     // 注销监听
    //     mInternalObserver.unregisterContentObserver(mInternalObserver);
    //     mExternalObserver.unregisterContentObserver(mExternalObserver);
    // }
    
    private void handleMediaContentChange(Uri contentUri) {
        Cursor cursor = null;
        Activity ctx = this.cordova.getActivity();
        try {
            cursor = ctx.getContentResolver().query(
                    contentUri,
                    MEDIA_PROJECTIONS,
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
            );

            if (cursor == null) {
                return;
            }
            if (!cursor.moveToFirst()) {
                return;
            }

            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);

            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);

            handleMediaRowData(data, dateTaken);

        } catch (Exception e) {
            PluginResult resultTry = new PluginResult(PluginResult.Status.OK, e.toString());
            resultTry.setKeepCallback(true);
            this.callback.sendPluginResult(resultTry);

            e.printStackTrace();

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
    
    private void handleMediaRowData(String data, long dateTaken) {
        if (checkScreenShot(data, dateTaken)) {
            Log.d("screenshot", data + " " + dateTaken);
            if (this.callback != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, "screenshot");
            result.setKeepCallback(true);
            this.callback.sendPluginResult(result);
            }
        } else {
            Log.d("Not", "Not screenshot event");
        }
    }
    
    
    private boolean checkScreenShot(String data, long dateTaken) {

        data = data.toLowerCase();
        for (String keyWork : KEYWORDS) {
            if (data.contains(keyWork)) {
                return true;
            }
        }
        return false;
    }
}
