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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.util.Log;


import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.util.FileSortHelper.SortMethod;
import com.dami.fileexplorer.util.MediaFile.MediaFileType;
import com.dami.fileexplorer.view.FileCategoryActivity;

public class FileCategoryHelper {
    public static final int COLUMN_ID = 0;

    public static final int COLUMN_PATH = 1;

    public static final int COLUMN_SIZE = 2;

    public static final int COLUMN_DATE = 3;

    private static final String LOG_TAG = "FileCategoryHelper";

    public enum FileCategory {
        All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite, Ecnrypt, EcnryptMusic, EcnryptVideo, EcnryptPicture, EcnryptDoc, EcnryptApk, EcnryptOther,
    }

    private static String APK_EXT = "apk";
    private static String THEME_EXT = "mtz";
    private static String[] ZIP_EXTS  = new String[] {
            "zip", "rar"
    };

    public static HashMap<FileCategory, FilenameExtFilter> filters = new HashMap<FileCategory, FilenameExtFilter>();

    public static HashMap<FileCategory, Integer> categoryNames = new HashMap<FileCategory, Integer>();

    static {
        categoryNames.put(FileCategory.All, R.string.category_all);
        categoryNames.put(FileCategory.Music, R.string.category_music);
        categoryNames.put(FileCategory.Video, R.string.category_video);
        categoryNames.put(FileCategory.Picture, R.string.category_picture);
        categoryNames.put(FileCategory.Theme, R.string.category_theme);
        categoryNames.put(FileCategory.Doc, R.string.category_document);
        categoryNames.put(FileCategory.Zip, R.string.category_zip);
        categoryNames.put(FileCategory.Apk, R.string.category_apk);
        categoryNames.put(FileCategory.Other, R.string.category_other);
        categoryNames.put(FileCategory.Favorite, R.string.category_favorite);
        categoryNames.put(FileCategory.Ecnrypt, R.string.encrypt);
        categoryNames.put(FileCategory.EcnryptMusic, R.string.category_encrypt_music);
        categoryNames.put(FileCategory.EcnryptVideo, R.string.category_encrypt_video);
        categoryNames.put(FileCategory.EcnryptPicture, R.string.category_encrypt_picture);
        categoryNames.put(FileCategory.EcnryptDoc, R.string.category_encrypt_document);
        categoryNames.put(FileCategory.EcnryptApk, R.string.category_encrypt_apk);
        categoryNames.put(FileCategory.EcnryptOther, R.string.category_encrypt_other);
    }

    public static FileCategory[] sCategories = new FileCategory[] {
            FileCategory.Music, FileCategory.Video, FileCategory.Picture, FileCategory.Theme,
            FileCategory.Doc, FileCategory.Zip, FileCategory.Apk, FileCategory.Other ,FileCategory.Ecnrypt, FileCategory.EcnryptMusic, FileCategory.EcnryptVideo,   FileCategory.EcnryptPicture, FileCategory.EcnryptDoc, FileCategory.EcnryptApk, FileCategory.EcnryptOther
    };

    private FileCategory mCategory;

    private Context mContext;

    public FileCategoryHelper(Context context) {
        mContext = context;

        mCategory = FileCategory.All;
    }

    public FileCategory getCurCategory() {
        return mCategory;
    }

    public void setCurCategory(FileCategory c) {
        mCategory = c;
    }

    public int getCurCategoryNameResId() {
        return categoryNames.get(mCategory);
    }

    public void setCustomCategory(String[] exts) {
        mCategory = FileCategory.Custom;
        if (filters.containsKey(FileCategory.Custom)) {
            filters.remove(FileCategory.Custom);
        }

        filters.put(FileCategory.Custom, new FilenameExtFilter(exts));
    }

    public FilenameFilter getFilter() {
        return filters.get(mCategory);
    }

    private HashMap<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();

    public HashMap<FileCategory, CategoryInfo> getCategoryInfos() {
        return mCategoryInfo;
    }

    public CategoryInfo getCategoryInfo(FileCategory fc) {
        if (mCategoryInfo.containsKey(fc)) {
            return mCategoryInfo.get(fc);
        } else {
            CategoryInfo info = new CategoryInfo();
            mCategoryInfo.put(fc, info);
            return info;
        }
    }

    public class CategoryInfo {
        public long count;

        public long size;
    }

    private void setCategoryInfo(FileCategory fc, long count, long size) {
        CategoryInfo info = mCategoryInfo.get(fc);
        if (info == null) {
            info = new CategoryInfo();
            mCategoryInfo.put(fc, info);
        }
        info.count = count;
        info.size = size;
    }


    private String buildDocSelection() {
        StringBuilder selection = new StringBuilder();
        Iterator<String> iter = Util.sDocMimeTypesSet.iterator();
        while(iter.hasNext()) {
            selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next() + "') OR ");
        }
        return  selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    private String buildSelectionByCategory(FileCategory cat) {
        String selection = null;
        switch (cat) {
            case Theme:
                selection = MediaColumns.DATA + " LIKE '%.mtz'";
                break;
            case Doc:
                selection = buildDocSelection();
                break;
            case Zip:
                selection = "(" + FileColumns.MIME_TYPE + " == '" + Util.sZipFileMimeType + "')";
                break;
            case Apk:
                selection = MediaColumns.DATA + " LIKE '%.apk'";
                break;
		    case Ecnrypt:
                selection = "_data LIKE '%.encryptaudio' OR _data LIKE '%.encryptvideo' OR _data LIKE '%.encryptimage' OR _data LIKE '%.encryptfile' OR _data LIKE '%.encryptapk' OR _data LIKE '%.encryptother'";    
                break;
            case EcnryptMusic:
                selection = "_data LIKE '%.encryptaudio'";
                break;
            case EcnryptVideo:
                selection = "_data LIKE '%.encryptvideo'";
                break; 
            case EcnryptPicture:
                selection = "_data LIKE '%.encryptimage'";
                break; 
            case EcnryptDoc:
                selection = "_data LIKE '%.encryptfile'";
                break; 
            case EcnryptApk:
                selection = "_data LIKE '%.encryptapk'";
                break; 
            case EcnryptOther:
                selection = "_data LIKE '%.encryptother'";
                break;     
            default:
                selection = null;
        }
        return selection;
    }

    private String buildSelectionByCategoryM(FileCategory fileCategory) {
        switch (fileCategory) {
            case Doc:
                return "(" + buildDocSelection() + ") AND " + "_data" + " LIKE '" + Util.getDefaultPath() + "%'";
            case Apk:
                return "_data LIKE '%.apk' AND _data LIKE '" + Util.getDefaultPath() + "%'";
            default:
                return "_data LIKE '" + Util.getDefaultPath() + "%'";
        }
    }    
    
    private String buildSelectionByCategoryS(FileCategory fileCategory) {
        switch (fileCategory) {
            case Doc:
                return "(" + buildDocSelection() + ") AND " + "_data" + " LIKE '" + FileCategoryActivity.SDCardPath + "%'";
            case Apk:
                return "_data LIKE '%.apk' AND _data LIKE '" + FileCategoryActivity.SDCardPath + "%'";
            case Other:
                return "(media_type not in ( 'text/plain','text/html','application/pdf','application/msword','application/vnd.ms-excel','application/vnd.ms-powerpoint','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet','application/vnd.openxmlformats-officedocument.wordprocessingml.document','application/vnd.openxmlformats-officedocument.wordprocessingml.template')  or media_type is null) and media_type = 0";
            default:
                return "_data LIKE '" + FileCategoryActivity.SDCardPath + "%'";
        }
    }   
    
    private Uri getContentUriByCategory(FileCategory cat) {
        Uri uri;
        String volumeName = "external";
        switch(cat) {
            case Theme:
            case Doc:
            case Zip:
            case Apk:
            case Ecnrypt:
            case EcnryptApk:
            case EcnryptDoc:
            case EcnryptMusic:
            case EcnryptOther:
            case EcnryptPicture:
            case EcnryptVideo:
                uri = Files.getContentUri(volumeName);
                break;
            case Music:
                uri = Audio.Media.getContentUri(volumeName);
                break;
            case Video:
                uri = Video.Media.getContentUri(volumeName);
                break;
            case Picture:
                uri = Images.Media.getContentUri(volumeName);
                break;
           default:
               uri = null;
        }
        return uri;
    }

    private String buildSortOrder(SortMethod sort) {
        String sortOrder = null;
        switch (sort) {
            case name:
                sortOrder = FileColumns.TITLE + " asc";
                break;
            case size:
                sortOrder = MediaColumns.SIZE + " asc";
                break;
            case date:
                sortOrder = MediaColumns.DATE_MODIFIED + " desc";
                break;
            case type:
                sortOrder = FileColumns.MIME_TYPE + " asc, " + FileColumns.TITLE + " asc";
                break;
        }
        return sortOrder;
    }

    public Cursor query(FileCategory fc, SortMethod sort) {
        Uri uri = getContentUriByCategory(fc);
        String selection = buildSelectionByCategory(fc);
        String sortOrder = buildSortOrder(sort);

        if (uri == null) {
            Log.e(LOG_TAG, "invalid uri, category:" + fc.name());
            return null;
        }

        String[] columns = new String[] {
                BaseColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED
        };

        return mContext.getContentResolver().query(uri, columns, selection, null, sortOrder);
    }    
    
    public Cursor query(FileCategory fc, SortMethod sort, int i) {
        Uri uri = getContentUriByCategory(fc);
        String selection = i == 0 ? buildSelectionByCategory(fc) : i == 1 ? buildSelectionByCategoryS(fc) : buildSelectionByCategoryM(fc);
        String sortOrder = buildSortOrder(sort);

        if (uri == null) {
            Log.e(LOG_TAG, "invalid uri, category:" + fc.name());
            return null;
        }

        String[] columns = new String[] {
                BaseColumns._ID, MediaColumns.DATA, MediaColumns.SIZE, MediaColumns.DATE_MODIFIED
        };

        return mContext.getContentResolver().query(uri, columns, selection, null, sortOrder);
    }

    public void refreshCategoryInfo(int i) {
        // clear
        for (FileCategory fc : sCategories) {
            setCategoryInfo(fc, 0, 0);
        }

        // query database
        String volumeName = "external";

        Uri uri = Audio.Media.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Music, uri, i);

        uri = Video.Media.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Video, uri, i);

        uri = Images.Media.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Picture, uri, i);

        uri = Files.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Theme, uri, i);
        refreshMediaCategory(FileCategory.Doc, uri, i);
        refreshMediaCategory(FileCategory.Zip, uri, i);
        refreshMediaCategory(FileCategory.Apk, uri, i);
        refreshMediaCategory(FileCategory.Ecnrypt, uri, i);
        refreshMediaCategory(FileCategory.EcnryptApk, uri, i);
        refreshMediaCategory(FileCategory.EcnryptDoc, uri, i);
        refreshMediaCategory(FileCategory.EcnryptMusic, uri, i);
        refreshMediaCategory(FileCategory.EcnryptOther, uri, i);
        refreshMediaCategory(FileCategory.EcnryptPicture, uri, i);
        refreshMediaCategory(FileCategory.EcnryptVideo, uri, i);
    }

    private boolean refreshMediaCategory(FileCategory fc, Uri uri, int i) {
        String[] columns = new String[] {
                "COUNT(*)", "SUM(_size)"
        };
        //Cursor c = mContext.getContentResolver().query(uri, columns, buildSelectionByCategory(fc), null, null);
        Cursor c = null;
        if(i == 0){
        	c = mContext.getContentResolver().query(uri, columns, buildSelectionByCategory(fc), null, null);
        }else if(i == 1){
        	c = mContext.getContentResolver().query(uri, columns, buildSelectionByCategoryS(fc), null, null);
        }else{
        	c = mContext.getContentResolver().query(uri, columns, buildSelectionByCategoryM(fc), null, null);
        }
        
        if (c == null) {
            Log.e(LOG_TAG, "fail to query uri:" + uri);
            return false;
        }

        if (c.moveToNext()) {
            setCategoryInfo(fc, c.getLong(0), c.getLong(1));
            Log.v(LOG_TAG, "Retrieved " + fc.name() + " info >>> count:" + c.getLong(0) + " size:" + c.getLong(1));
            c.close();
            return true;
        }

        return false;
    }

    public static FileCategory getCategoryFromPath(String path) {
        MediaFileType type = MediaFile.getFileType(path);
        if (type != null) {
            if (MediaFile.isAudioFileType(type.fileType)) return FileCategory.Music;
            if (MediaFile.isVideoFileType(type.fileType)) return FileCategory.Video;
            if (MediaFile.isImageFileType(type.fileType)) return FileCategory.Picture;
            if (Util.sDocMimeTypesSet.contains(type.mimeType)) return FileCategory.Doc;
        }

        int dotPosition = path.lastIndexOf('.');
        if (dotPosition < 0) {
            return FileCategory.Other;
        }

        String ext = path.substring(dotPosition + 1);
        if (ext.equalsIgnoreCase(APK_EXT)) {
            return FileCategory.Apk;
        }
        if (ext.equalsIgnoreCase(THEME_EXT)) {
            return FileCategory.Theme;
        }

        if (matchExts(ext, ZIP_EXTS)) {
            return FileCategory.Zip;
        }

        return FileCategory.Other;
    }

    private static boolean matchExts(String ext, String[] exts) {
        for (String ex : exts) {
            if (ex.equalsIgnoreCase(ext))
                return true;
        }
        return false;
    }
}
