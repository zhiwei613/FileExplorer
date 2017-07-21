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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.dami.fileexplorer.view.Settings;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.dami.fileexplorer.xdja.business.DBBusiness;
import com.dami.fileexplorer.xdja.business.EncDecBusiness;
import com.dami.fileexplorer.xdja.utils.CommonUtils;
import com.dami.fileexplorer.util.FileCategoryHelper;
import com.dami.fileexplorer.util.FileCategoryHelper.FileCategory;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.dami.fileexplorer.R;

public class FileOperationHelper {
    private static final String LOG_TAG = "FileOperation";

    private ArrayList<FileInfo> mCurFileNameList = new ArrayList<FileInfo>();

    private boolean mMoving;

    private IOperationProgressListener mOperationListener;

    private FilenameFilter mFilter;
    private EncDecBusiness mEncDecBusiness;
    private File mEncryptFolder;
    private File mDecryptFolder;

    public interface IOperationProgressListener {
        void onFinish();

        void onFileChanged(String path);
    }

    public FileOperationHelper(IOperationProgressListener l) {
        mOperationListener = l;
        mEncDecBusiness = new EncDecBusiness();
    }

    public void setFilenameFilter(FilenameFilter f) {
        mFilter = f;
    }

    public boolean CreateFolder(String path, String name) {
        Log.v(LOG_TAG, "CreateFolder >>> " + path + "," + name);

        File f = new File(Util.makePath(path, name));
        if (f.exists())
            return false;

        return f.mkdir();
    }

    public void Copy(ArrayList<FileInfo> files) {
        copyFileList(files);
    }

    public boolean Paste(String path) {
        if (mCurFileNameList.size() == 0)
            return false;

        final String _path = path;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    CopyFile(f, _path);
                }

                mOperationListener.onFileChanged(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath());

                clear();
            }
        });

        return true;
    }

    public boolean canPaste() {
        return mCurFileNameList.size() != 0;
    }

    public void StartMove(ArrayList<FileInfo> files) {
        if (mMoving)
            return;

        mMoving = true;
        copyFileList(files);
    }

    public boolean isMoveState() {
        return mMoving;
    }

    public boolean canMove(String path) {
        for (FileInfo f : mCurFileNameList) {
            if (!f.IsDir)
                continue;

            if (Util.containsPath(f.filePath, path))
                return false;
        }

        return true;
    }

    public void clear() {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
        }
    }

    public boolean EndMove(String path) {
        if (!mMoving)
            return false;
        mMoving = false;

        if (TextUtils.isEmpty(path))
            return false;

        final String _path = path;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                    for (FileInfo f : mCurFileNameList) {
                        MoveFile(f, _path);
                    }

                    mOperationListener.onFileChanged(Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath());

                    clear();
                }
        });

        return true;
    }

    public ArrayList<FileInfo> getFileList() {
        return mCurFileNameList;
    }

    private void asnycExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                synchronized(mCurFileNameList) {
                    _r.run();
                }
                if (mOperationListener != null) {
                    mOperationListener.onFinish();
                }

                return null;
            }
        }.execute();
    }

    public boolean isFileSelected(String path) {
        synchronized(mCurFileNameList) {
            for (FileInfo f : mCurFileNameList) {
                if (f.filePath.equalsIgnoreCase(path))
                    return true;
            }
        }
        return false;
    }

    public boolean Rename(FileInfo f, String newName) {
        if (f == null || newName == null) {
            Log.e(LOG_TAG, "Rename: null parameter");
            return false;
        }

        File file = new File(f.filePath);
        String newPath = Util.makePath(Util.getPathFromFilepath(f.filePath), newName);
        final boolean needScan = file.isFile();
        try {
            boolean ret = file.renameTo(new File(newPath));
            if (ret) {
                if (needScan) {
                    mOperationListener.onFileChanged(f.filePath);
                }
                mOperationListener.onFileChanged(newPath);
            }
            return ret;
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Fail to rename file," + e.toString());
        }
        return false;
    }

    public boolean Delete(ArrayList<FileInfo> files) {
        copyFileList(files);
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    DeleteFile(f);
                }

                mOperationListener.onFileChanged(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath());

                clear();
            }
        });
        return true;
    }

    public boolean Encrypt(ArrayList<FileInfo> files,File destFolder) {
        copyFileList(files);
        mEncryptFolder = destFolder;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    EncryptFile(f);
                }

                mOperationListener.onFileChanged(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath());

                clear();
            }
        });
        return true;
    }

    public boolean Decrypt(ArrayList<FileInfo> files,File destFolder) {
        copyFileList(files);
        mDecryptFolder = destFolder;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    DecryptFile(f);
                }

                mOperationListener.onFileChanged(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath());

                clear();
            }
        });
        return true;
    }

    private String getEncryptFileName(FileInfo f) {
        File file = new File(f.filePath);
        String path = f.filePath.toString();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        FileCategory mFileCategory = FileCategoryHelper.getCategoryFromPath(path);
        String filename = Util.getNameFromFilepath(path);
        if(mFileCategory == FileCategory.Music){
            filename = filename + "." + str +".encryptaudio";
        }else if(mFileCategory == FileCategory.Video){
            filename = filename + "." + str +".encryptvideo";
        }else if(mFileCategory == FileCategory.Picture){
            filename = filename + "." + str +".encryptimage";
        }else if(mFileCategory == FileCategory.Doc){
            filename = filename + "." + str +".encryptfile";
        }else if(mFileCategory == FileCategory.Apk){
            filename = filename + "." + str +".encryptapk";
        }else{
            filename = filename + "." + str +".encryptother";
        }
        return filename;
    }

    protected void EncryptFile(FileInfo f) {
        if (f == null) {
            Log.e(LOG_TAG, "EncryptFile: null parameter");
            return;
        }
        Log.e(LOG_TAG, "EncryptFile start:  " + f.filePath.toString());
        File file = new File(f.filePath);
        boolean directory = file.isDirectory();
        if (!directory) {
            String path = f.filePath.toString();
            String filename = getEncryptFileName(f);
            mEncDecBusiness.encFile(CommonUtils.groupId, path, mEncryptFolder.getPath() + "/" + filename);
            file.delete();
        }

        Log.e(LOG_TAG, "EncryptFile >>> " + f.filePath);
    }

    protected void DecryptFile(FileInfo f) {
        if (f == null) {
            Log.e(LOG_TAG, "DecryptFile: null parameter");
            return;
        }
        Log.e(LOG_TAG, "DecryptFile start: " + f.filePath.toString());
        File file = new File(f.filePath);
        boolean directory = file.isDirectory();
        if (!directory) {
            String path = f.filePath.toString();
            String realfilename = Util.getRealNameFromFilepath(path);
            mEncDecBusiness.decFile(mDecryptFolder.getPath() + "/" + realfilename, path);
            file.delete();
        }

        Log.e(LOG_TAG, "DecryptFile >>> " + f.filePath);
    }

    protected void DeleteFile(FileInfo f) {
        if (f == null) {
            Log.e(LOG_TAG, "DeleteFile: null parameter");
            return;
        }

        File file = new File(f.filePath);
        boolean directory = file.isDirectory();
        if (directory) {
            for (File child : file.listFiles(mFilter)) {
                if (Util.isNormalFile(child.getAbsolutePath())) {
                    DeleteFile(Util.GetFileInfo(child, mFilter, true));
                }
            }
        }

        file.delete();

        Log.v(LOG_TAG, "DeleteFile >>> " + f.filePath);
    }

    private void CopyFile(FileInfo f, String dest) {
        if (f == null || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter");
            return;
        }

        File file = new File(f.filePath);
        if (file.isDirectory()) {

            // directory exists in destination, rename it
            String destPath = Util.makePath(dest, f.fileName);
            File destFile = new File(destPath);
            int i = 1;
            while (destFile.exists()) {
                destPath = Util.makePath(dest, f.fileName + " " + i++);
                destFile = new File(destPath);
            }

            for (File child : file.listFiles(mFilter)) {
                if (!child.isHidden() && Util.isNormalFile(child.getAbsolutePath())) {
                    CopyFile(Util.GetFileInfo(child, mFilter, Settings.instance().getShowDotAndHiddenFiles()), destPath);
                }
            }
        } else {
            String destFile = Util.copyFile(f.filePath, dest);
        }
        Log.v(LOG_TAG, "CopyFile >>> " + f.filePath + "," + dest);
    }

    private boolean MoveFile(FileInfo f, String dest) {
        Log.v(LOG_TAG, "MoveFile >>> " + f.filePath + "," + dest);

        if (f == null || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter");
            return false;
        }

        File file = new File(f.filePath);
        String newPath = Util.makePath(dest, f.fileName);
        try {
            return file.renameTo(new File(newPath));
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Fail to move file," + e.toString());
        }
        return false;
    }

    private void copyFileList(ArrayList<FileInfo> files) {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
            for (FileInfo f : files) {
                mCurFileNameList.add(f);
            }
        }
    }

}
