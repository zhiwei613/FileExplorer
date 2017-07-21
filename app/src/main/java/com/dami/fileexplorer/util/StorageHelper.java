package com.dami.fileexplorer.util;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import com.dami.fileexplorer.util.Util.MemoryCardInfo;
import com.dami.fileexplorer.util.Util.SDCardInfo;
import com.dami.fileexplorer.util.Util.UsbStrogeInfo;
import com.dami.fileexplorer.util.Util;
import com.dami.fileexplorer.R;

public class StorageHelper {
    private static final String LOG_TAG = "StorageHelper";
    private static Context mContext;
    private static StorageHelper storageHelper;
    private String mCurrentMountPoint;
    private StorageManager mStorageManager;

    public static class MountedStorageInfo {
        public long free;
        public long total;
    }

    private StorageHelper(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Util.setMountedStorageBySubPath(mContext, mStorageManager);
    }

    public static StorageHelper getInstance(Context context) {
        if (storageHelper == null) {
            storageHelper = new StorageHelper(context);
            mContext = context;
        }
        return storageHelper;
    }

    public long destVolumeFreeSpace(String str) {
        return getStorageInfoForVolume(Util.getMountedStorageBySubPath(mContext, str)).free;
    }

    public String getCurrentVolume() {
        return mCurrentMountPoint;
    }

    public StorageVolume getLatestMountedVolume() {
        ArrayList sortedMountVolumeList = getSortedMountVolumeList();
        return (sortedMountVolumeList == null || sortedMountVolumeList.size() == 0) ? null : (StorageVolume) sortedMountVolumeList.get(sortedMountVolumeList.size() - 1);
    }

    public com.dami.fileexplorer.util.StorageHelper.MountedStorageInfo getMountedStorageInfo() {
        throw new UnsupportedOperationException("Method not decompiled: com.dami.fileexplorer.util.StorageHelper.getMountedStorageInfo():com.dami.fileexplorer.util.StorageHelper$MountedStorageInfo");
    }

    public int getMountedVolumeCount() {
        int i = 0;
		StorageVolume[] volumeList = mStorageManager.getVolumeList();
        for (StorageVolume path : volumeList) {
            if (isVolumeMounted(path.getPath())) {
                i++;
            }
        }
        return i;
    }

    public ArrayList<StorageVolume> getMountedVolumeList() {
        StorageVolume[] volumeList = mStorageManager.getVolumeList();
        ArrayList<StorageVolume> arrayList = new ArrayList();
        for (StorageVolume storageVolume : volumeList) {
            if (isVolumeMounted(storageVolume.getPath())) {
                arrayList.add(storageVolume);
            }
        }
        return arrayList;
    }

    public StorageVolume getPrimaryStorageVolume() {
        StorageVolume storageVolume = null;
        ArrayList mountedVolumeList = getMountedVolumeList();
        int size = mountedVolumeList.size();
        if (size <= 0) {
            return null;
        }
        int i = 0;
        while (i < size) {
            StorageVolume storageVolume2 = (StorageVolume) mountedVolumeList.get(i);
            if (isVolumeMounted(storageVolume2.getPath()) && mContext.getString(R.string.storage_phone).equals(((StorageVolume) mountedVolumeList.get(i)).getDescription(mContext))) {
                return (StorageVolume) mountedVolumeList.get(i);
            }
            i++;
            storageVolume = storageVolume2;
        }
        return storageVolume;
    }

    public ArrayList<StorageVolume> getSortedMountVolumeList() {
        StorageVolume[] volumeList = mStorageManager.getVolumeList();
        ArrayList<StorageVolume> arrayList = new ArrayList();
        for (StorageVolume storageVolume : volumeList) {
            if (isVolumeMounted(storageVolume.getPath())) {
                arrayList.add(storageVolume);
            }
        }
        StorageVolume storageVolume2;
        if (arrayList.size() == 2) {
            storageVolume2 = (StorageVolume) arrayList.get(0);
            StorageVolume storageVolume3 = (StorageVolume) arrayList.get(1);
            if (!mContext.getString(R.string.storage_phone).equals(storageVolume2.getDescription(mContext))) {
                arrayList.clear();
                arrayList.add(storageVolume3);
                arrayList.add(storageVolume2);
            }
        } else if (arrayList.size() == 3) {
            ArrayList<StorageVolume> arrayList2 = new ArrayList();
            arrayList2.add(arrayList.get(0));
            arrayList2.add(arrayList.get(1));
            arrayList2.add(arrayList.get(2));
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                storageVolume2 = (StorageVolume) it.next();
                if (!mContext.getString(R.string.storage_phone).equals(storageVolume2.getDescription(mContext)) && mContext.getString(R.string.storage_sd_card).equals(storageVolume2.getDescription(mContext))) {
                }
            }
            return arrayList2;
        }
        return arrayList;
    }

    public MountedStorageInfo getStorageInfoForVolume(StorageVolume storageVolume) {
        MountedStorageInfo mountedStorageInfo = new MountedStorageInfo();
        if (storageVolume == null || storageVolume.getPath() == null) {
            return null;
        }
        String path = storageVolume.getPath();
        if (isVolumeMounted(path)) {
            try {
                StatFs statFs = new StatFs(path);
                long blockSize = (long) statFs.getBlockSize();
                long availableBlocks = (long) statFs.getAvailableBlocks();
                mountedStorageInfo.total = ((long) statFs.getBlockCount()) * blockSize;
                mountedStorageInfo.free = availableBlocks * blockSize;
                return mountedStorageInfo;
            } catch (Throwable e) {
                Log.e(LOG_TAG, "statfs failed", e);
            }
        }
        return null;
    }

    public String getStorageState(String str) {
        try {
            String str2 = (String) StorageManager.class.getMethod("getVolumeState", new Class[]{String.class}).invoke(mStorageManager, new Object[]{str});
            return str2;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isCurrentVolumeMounted() {
        return isVolumeMounted(mCurrentMountPoint);
    }

    public boolean isVolumeMounted(String str) {
        if (VERSION.SDK_INT >= 21) {
            if (!(mStorageManager == null || TextUtils.isEmpty(str) || "mounted".equals(Environment.getExternalStorageState(new File(str))))) {
                return false;
            }
        } else if (!"mounted".equals(getStorageState(str))) {
            return false;
        }
        if (!TextUtils.isEmpty(Util.SD_DIR) && str.startsWith(Util.SD_DIR)) {
            SDCardInfo sDCardInfo = Util.getSDCardInfo();
            if (!(sDCardInfo == null || sDCardInfo.total == sDCardInfo.free)) {
                return true;
            }
        }
        if (str.startsWith(Util.USBOTG_DIR)) {
            UsbStrogeInfo usbStorgeInfo = Util.getUsbStrogeInfo();
            if (!(usbStorgeInfo == null || usbStorgeInfo.total <= 0 || usbStorgeInfo.free == usbStorgeInfo.total)) {
                return true;
            }
        }
        if (!str.startsWith(Util.getDefaultPath())) {
            return false;
        }
        MemoryCardInfo memoryCardInfo = Util.getMemoryCardInfo();
        return memoryCardInfo != null && memoryCardInfo.total > 0;
    }

    public void release() {
        mContext = null;
        storageHelper = null;
    }

    public void setCurrentMountPoint(String str) {
        for (StorageVolume path : mStorageManager.getVolumeList()) {
            if (path.getPath().equals(str)) {
                mCurrentMountPoint = str;
            }
        }
    }
}
