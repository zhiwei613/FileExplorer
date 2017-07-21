/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dami.fileexplorer.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.view.FileCategoryActivity;
import com.dami.fileexplorer.view.FileViewActivity;
import com.dami.fileexplorer.view.Settings;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.util.Iterator;


public class Util {
    private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

    private static final String LOG_TAG = "Util";
	public static String MEMORY_DIR = Environment.getExternalStorageDirectory().getPath();
    public static String PATH = "/storage/emulated/0";
    public static String SD_DIR = "/storage/sdcard1";
    public static String USBOTG_DIR = "/storage/usbotg";
    private static String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath();  
    private static File sdFile = new File(SD_DIR);
    public static FileCategoryActivity mFileCategoryActivity;
    private static StorageVolume[] storageVolumes;
    private static ArrayList<StorageVolume> mountVolumeList;
    private static StorageVolume storageVolume1;
    private static StorageVolume storageVolume = null;
    
    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // if path1 contains path2
    public static boolean containsPath(String path1, String path2) {
        String path = path2;
        while (path != null) {
            if (path.equalsIgnoreCase(path1))
                return true;

            if (path.equals(GlobalConsts.ROOT_PATH))
                break;
            path = new File(path).getParent();
        }

        return false;
    }

    public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator))
            return path1 + path2;

        return path1 + File.separator + path2;
    }

    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static boolean isNormalFile(String fullName) {
        return !fullName.equals(ANDROID_SECURE);
    }

    public static FileInfo GetFileInfo(String filePath) {
        File lFile = new File(filePath);
        if (!lFile.exists())
            return null;

        FileInfo lFileInfo = new FileInfo();
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = Util.getNameFromFilepath(filePath);
        lFileInfo.ModifiedDate = lFile.lastModified();
        lFileInfo.IsDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        lFileInfo.fileSize = lFile.length();
        return lFileInfo;
    }

    public static FileInfo GetFileInfo(File f, FilenameFilter filter, boolean showHidden) {
        FileInfo lFileInfo = new FileInfo();
        String filePath = f.getPath();
        File lFile = new File(filePath);
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = f.getName();
        lFileInfo.ModifiedDate = lFile.lastModified();
        lFileInfo.IsDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        if (lFileInfo.IsDir) {
            int lCount = 0;
            File[] files = lFile.listFiles(filter);

            // null means we cannot access this dir
            if (files == null) {
                return null;
            }

            for (File child : files) {
                if ((!child.isHidden() || showHidden)
                        && Util.isNormalFile(child.getAbsolutePath())) {
                    lCount++;
                }
            }
            lFileInfo.Count = lCount;

        } else {

            lFileInfo.fileSize = lFile.length();

        }
        return lFileInfo;
    }

    /*
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
     * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return null;
    }

    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }

    public static String getPathFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(0, pos);
        }
        return "";
    }

    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }
    
    public static String getRealNameFromFilepath(String filepath) {
    	String filename = getNameFromFilepath(filepath);
        int pos = filename.lastIndexOf('.');
        if (pos != -1) {
        	filename = filename.substring(0, pos);
        	pos = filename.lastIndexOf('.');
        	if(pos != -1){
        		return filename = filename.substring(0, pos);
        	}
        }
        return "";
    }    
     
    // return new file path if successful, or return null
    public static String copyFile(String src, String dest) {
        File file = new File(src);
        if (!file.exists() || file.isDirectory()) {
            Log.v(LOG_TAG, "copyFile: file not exist or is directory, " + src);
            return null;
        }
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(file);
            File destPlace = new File(dest);
            if (!destPlace.exists()) {
                if (!destPlace.mkdirs())
                    return null;
            }

            String destPath = Util.makePath(dest, file.getName());
            File destFile = new File(destPath);
            int i = 1;
            while (destFile.exists()) {
                String destName = Util.getNameFromFilename(file.getName()) + " " + i++ + "."
                        + Util.getExtFromFilename(file.getName());
                destPath = Util.makePath(dest, destName);
                destFile = new File(destPath);
            }

            if (!destFile.createNewFile())
                return null;

            fo = new FileOutputStream(destFile);
            int count = 102400;
            byte[] buffer = new byte[count];
            int read = 0;
            while ((read = fi.read(buffer, 0, count)) != -1) {
                fo.write(buffer, 0, read);
            }

            // TODO: set access privilege

            return destPath;
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "copyFile: file not found, " + src);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "copyFile: " + e.toString());
        } finally {
            try {
                if (fi != null)
                    fi.close();
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // does not include sd card folder
    private static String[] SysFileDirs = new String[] {
        "miren_browser/imagecaches"
    };

    public static boolean shouldShowFile(String path) {
        return shouldShowFile(new File(path));
    }

    public static boolean shouldShowFile(File file) {
        boolean show = Settings.instance().getShowDotAndHiddenFiles();
        if (show)
            return true;

        if (file.isHidden())
            return false;

        if (file.getName().startsWith("."))
            return false;

        String sdFolder = getSdDirectory();
        for (String s : SysFileDirs) {
            if (file.getPath().startsWith(makePath(sdFolder, s)))
                return false;
        }

        return true;
    }

    public static ArrayList<FavoriteItem> getDefaultFavorites(Context context) {
        ArrayList<FavoriteItem> list = new ArrayList<FavoriteItem>();
        list.add(new FavoriteItem(context.getString(R.string.favorite_photo), makePath(getSdDirectory(), "DCIM/Camera")));
        list.add(new FavoriteItem(context.getString(R.string.favorite_sdcard), getSdDirectory()));
        //list.add(new FavoriteItem(context.getString(R.string.favorite_root), getSdDirectory()));
        list.add(new FavoriteItem(context.getString(R.string.favorite_screen_cap), makePath(getSdDirectory(), "MIUI/screen_cap")));
        list.add(new FavoriteItem(context.getString(R.string.favorite_ringtone), makePath(getSdDirectory(), "MIUI/ringtone")));
        return list;
    }

    public static boolean setText(View view, int id, String text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView == null)
            return false;

        textView.setText(text);
        return true;
    }

    public static boolean setText(View view, int id, int text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView == null)
            return false;

        textView.setText(text);
        return true;
    }

    // comma separated number
    public static String convertNumber(long number) {
        return String.format("%,d", number);
    }

    // storage, G M K B
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static class MemoryCardInfo {
        public long total;
        public long free;
    }
    
    public static class SDCardInfo {
        public long free;
        public long total;
    }

    public static class UsbStrogeInfo {
        public long free;
        public long total;
    }

    public static StorageVolume getMountedStorageBySubPath(Context context, String str) {
        if (!TextUtils.isEmpty(str)) {
            Iterator it = StorageHelper.getInstance(context).getMountedVolumeList().iterator();
            while (it.hasNext()) {
                StorageVolume storageVolume = (StorageVolume) it.next();
                if (str.startsWith(storageVolume.getPath())) {
                    return storageVolume;
                }
            }
        }
        return null;
    }
    
    public static void setMountedStorageBySubPath(Context context, StorageManager storageManager) {
        storageVolumes = storageManager.getVolumeList();
        mountVolumeList = new ArrayList();
        for (StorageVolume storageVolume : storageVolumes) {
            storageVolume1 = storageVolume;
            if (VERSION.SDK_INT >= 19) {
                if (!(storageVolume1.getStorageId() == 0 || storageVolume1.getState().equals("removed"))) {
                    mountVolumeList.add(storageVolume1);
                }
            } else if (storageVolume1.getStorageId() != 0) {
                mountVolumeList.add(storageVolume1);
            }
        }
        if (mountVolumeList.size() == 2) {
            SD_DIR = getSdPath();
        }
        if (mountVolumeList.size() == 1) {
            SD_DIR = null;
        }
        if (mountVolumeList.size() > 2) {
            SD_DIR = getSdPath();
            storageVolume = storageVolumes[2];
            USBOTG_DIR = storageVolume.getPath();
        }
    }    
    
    public static String getSdPath() {
        if (PATH.equals(defaultPath) || !((StorageVolume) mountVolumeList.get(0)).isRemovable()) {
            storageVolume = storageVolumes[1];
        } else {
            storageVolume = storageVolumes[0];
        }
        return storageVolume.getPath();
    }    
    
    public static String getDefaultPath() {
        return PATH.equals(defaultPath) ? defaultPath : mountVolumeList.size() == 1 ? ((StorageVolume) mountVolumeList.get(0)).getPath() : (mountVolumeList.size() != 2 || ((StorageVolume) mountVolumeList.get(0)).isRemovable()) ? ((StorageVolume) mountVolumeList.get(1)).getPath() : ((StorageVolume) mountVolumeList.get(0)).getPath();
    }
    
    public static MemoryCardInfo getMemoryCardInfo(){
    	defaultPath = getDefaultPath();
        try {
            StatFs statFs = new StatFs(defaultPath);
            long blockCount = statFs.getBlockCount();
            long blockSize = statFs.getBlockSize();
            long freeBlocks = statFs.getFreeBlocks();
            freeBlocks = statFs.getAvailableBlocks();
            MemoryCardInfo memoryCardInfo = new MemoryCardInfo();
            memoryCardInfo.total = blockCount * blockSize;
            memoryCardInfo.free = freeBlocks * blockSize;
            return memoryCardInfo;
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, e.toString());
            return null;
        }
    }
    
    /*
    public static SDCardInfo getSDCardInfo() {
        if (!(SD_DIR == null || SD_DIR.length() == 0)) {
            sdFile = new File(SD_DIR);
            try {
                StatFs statFs = new StatFs(sdFile.getPath());
                long blockCount = (long) statFs.getBlockCount();
                long blockSize = (long) statFs.getBlockSize();
                long freeBlocks = (long) statFs.getFreeBlocks();
                freeBlocks = (long) statFs.getAvailableBlocks();
                SDCardInfo sDCardInfo = new SDCardInfo();
                sDCardInfo.total = blockCount * blockSize;
                sDCardInfo.free = freeBlocks * blockSize;
                return sDCardInfo;
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return null;
    }
    */
    
    public static SDCardInfo getSDCardInfo() {
    	if (!(SD_DIR == null || SD_DIR.length() == 0)) {
    		SD_DIR = FileCategoryActivity.SDCardPath;
    		sdFile = new File(SD_DIR);
            try {
                StatFs statFs = new StatFs(SD_DIR);
                long blockCount = statFs.getBlockCount();
                long blockSize = statFs.getBlockSize();
                long freeBlocks = statFs.getFreeBlocks();
                freeBlocks = statFs.getAvailableBlocks();
                SDCardInfo sDCardInfo = new SDCardInfo();
                sDCardInfo.total = blockCount * blockSize;
                sDCardInfo.free = freeBlocks * blockSize;
                return sDCardInfo;
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
    	return null;
    }
    
    public static UsbStrogeInfo getUsbStrogeInfo() {
    	return null;
    } 

    public static void showNotification(Context context, Intent intent, String title, String body, int drawableId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Notification notification = new Notification(drawableId, body, System.currentTimeMillis());
        if (intent == null) {
            // FIXEME: category tab is disabled
            intent = new Intent(context, FileViewActivity.class);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        //notification.setLatestEventInfo(context, title, body, contentIntent);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        manager.notify(drawableId, notification);
    }

    public static String formatDateString(Context context, long time) {
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(context);
        Date date = new Date(time);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    public static void updateActionModeTitle(ActionMode mode, Context context, int selectedNum) {
        if (mode != null) {
            mode.setTitle(context.getString(R.string.multi_select_title,selectedNum));
            if(selectedNum == 0){
                mode.finish();
            }
        }
    }

    public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
        {
            add("text/plain");
            add("text/html");
            add("application/pdf");
            add("application/msword");
            add("application/vnd.ms-excel");
            add("application/vnd.ms-powerpoint");
            add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            add("application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        }
    };

    public static String sZipFileMimeType = "application/zip";

    public static int CATEGORY_TAB_INDEX = 0;
    public static int SDCARD_TAB_INDEX = 1;
}
