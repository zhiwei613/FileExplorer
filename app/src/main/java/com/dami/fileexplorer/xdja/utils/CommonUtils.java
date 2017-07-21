package com.dami.fileexplorer.xdja.utils;

import android.content.Context;
import android.os.Environment;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Administrator on 2016/12/13.
 */

public class CommonUtils {

    public static String currentAccount = "dami";
    public static String groupId = "dami";
    public static final String KEY_DEFAULT_DEVICE = "default_device";

	public final static String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	//public final static String ENCRYPT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/加密/";
	//public final static String DECRYPT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/解密/";
    public static void copyBigDataToSD(Context context, String fromFileName) {
        String strOutFileName = SD_PATH + fromFileName;
        InputStream myInput;
        try {
            OutputStream myOutput = new FileOutputStream(strOutFileName);
            myInput = context.getResources().getAssets().open(fromFileName);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }

            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (Exception e) {

        }

    }

    public static String getUUID(){
        return UUID.randomUUID().toString();
    }

    public static boolean accountIsValid(String str){
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }
}
