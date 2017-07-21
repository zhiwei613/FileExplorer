package com.dami.fileexplorer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.dami.fileexplorer.util.StorageHelper;


public class FileManagerApplication extends Application {
    public static final String LOG_TAG = "FileManagerApplication";
    private static final int MSG_SDCARD_CHANGED = 1;
    private static final String TAG = "Config";
    public static String mIsDaMi;
    public static String mIsFeiMa;
    public static String mIsHideFTP;
    public static String mIsNeedRingTone;
    public static String mIsTest;
    public static long mMemoryCardInfo;
    private List<SDCardChangeListener> listeners = new ArrayList();
    Handler mHandler = new Handler() {
        @Override
		public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    int i = message.arg1;
                    Log.i("liuhaoran1", "flag = " + i);
                    for (int i2 = 0; i2 < listeners.size(); i2++) {
                        SDCardChangeListener sDCardChangeListener = listeners.get(i2);
                        if (sDCardChangeListener != null) {
                            sDCardChangeListener.onMountStateChange(i);
                        }
                    }
                    break;
                default:
                	break;
            }
        }
    };
    private InputStream mInput;
    private ScannerReceiver mScannerReceiver;
    private InputStream mVersionConfig;

    public interface SDCardChangeListener {
        public static final int flag_INJECT = 1;
        public static final int flag_UMMOUNT = 2;

        void onMountStateChange(int i);
    }

    public class ScannerReceiver extends BroadcastReceiver {
        @Override
		public void onReceive(Context context, Intent intent) {
            Log.i(FileManagerApplication.LOG_TAG, "FilecategoryACtivity, ScannerReceiver onReceive(), intent:  " + intent);
            String action = intent.getAction();
            if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                StorageHelper.getInstance(FileManagerApplication.this).release();
                StorageHelper.getInstance(FileManagerApplication.this);
                mHandler.obtainMessage(1, 1, 0).sendToTarget();
            } else if (action.equals("android.intent.action.MEDIA_EJECT") || action.equals("android.intent.action.MEDIA_UNMOUNTED") || action.equals("android.intent.action.MEDIA_BAD_REMOVAL")) {
                StorageHelper.getInstance(FileManagerApplication.this).release();
                StorageHelper.getInstance(FileManagerApplication.this);
                mHandler.obtainMessage(1, 2, 0).sendToTarget();
            }
        }
    }

    public void addSDCardChangeListener(SDCardChangeListener sDCardChangeListener) {
        listeners.add(sDCardChangeListener);
    }

    @Override
	public void onCreate() {
        super.onCreate();
        registMountListener();
    }

    @Override
	public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mScannerReceiver);
    }

    public void registMountListener() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(1000);
        intentFilter.addAction("android.intent.action.MEDIA_SCANNER_FINISHED");
        intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addAction("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intentFilter.addAction("android.intent.action.MEDIA_SCANNER_STARTED");
        intentFilter.addAction("android.intent.action.MEDIA_CHECKING");
        intentFilter.addAction("android.intent.action.MEDIA_EJECT");
        intentFilter.addDataScheme("file");
        registerReceiver(mScannerReceiver, intentFilter);
    }

    public void removeSDCardChangeListener(SDCardChangeListener sDCardChangeListener) {
        listeners.remove(sDCardChangeListener);
    }
}
