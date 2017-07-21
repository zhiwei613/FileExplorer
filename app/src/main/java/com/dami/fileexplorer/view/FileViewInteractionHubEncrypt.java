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

package com.dami.fileexplorer.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.interfaces.IFileInteractionListener;
import com.dami.fileexplorer.util.FavoriteDatabaseHelper;
import com.dami.fileexplorer.util.FileCategoryHelper;
import com.dami.fileexplorer.util.FileCategoryHelper.FileCategory;
import com.dami.fileexplorer.util.FileInfo;
import com.dami.fileexplorer.util.FileOperationHelper;
import com.dami.fileexplorer.util.FileSortHelper;
import com.dami.fileexplorer.util.GlobalConsts;
import com.dami.fileexplorer.util.InformationDialog;
import com.dami.fileexplorer.util.IntentBuilder;
import com.dami.fileexplorer.util.TextInputDialog;
import com.dami.fileexplorer.util.Util;
import com.dami.fileexplorer.util.FileOperationHelper.IOperationProgressListener;
import com.dami.fileexplorer.util.FileSortHelper.SortMethod;
import com.dami.fileexplorer.util.TextInputDialog.OnFinishListener;
import com.dami.fileexplorer.view.FileListItemEncrypt.ModeCallback;
import com.dami.fileexplorer.view.FileViewActivity.SelectFilesCallback;
import com.dami.fileexplorer.xdja.business.DBBusiness;
import com.dami.fileexplorer.xdja.business.EncDecBusiness;
import com.dami.fileexplorer.xdja.utils.CommonUtils;

import android.R.drawable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class FileViewInteractionHubEncrypt implements IOperationProgressListener {
    private static final String LOG_TAG = "FileViewInteractionHubEncrypt";

    private IFileInteractionListener mFileViewListener;

    private ArrayList<FileInfo> mCheckedFileNameList = new ArrayList<FileInfo>();

    private FileOperationHelper mFileOperationHelper;

    private FileSortHelper mFileSortHelper;

    private ProgressDialog progressDialog;

    private Context mContext;
    private EncDecBusiness mEncDecBusiness;
    private DBBusiness mDBBusiness;
    private FileCategoryHelper mFileCategory;

    public enum Mode {
        View, Pick
    };

    public FileViewInteractionHubEncrypt(IFileInteractionListener fileViewListener) {
        assert (fileViewListener != null);
        mFileViewListener = fileViewListener;
        setup();
        mFileOperationHelper = new FileOperationHelper(this);
        mFileSortHelper = new FileSortHelper();
        mContext = mFileViewListener.getContext();
		
		mEncDecBusiness = new EncDecBusiness();
    }	
    
    private void showProgress(String msg) {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void sortCurrentList() {
        mFileViewListener.sortCurrentList(mFileSortHelper);
    }

    public boolean canShowCheckBox() {
        return true;
    }

    public void addContextMenuSelectedItem() {
        if (mCheckedFileNameList.size() == 0) {
            int pos = mListViewContextMenuSelectedItem;
            if (pos != -1) {
                FileInfo fileInfo = mFileViewListener.getItem(pos);
                if (fileInfo != null) {
                    mCheckedFileNameList.add(fileInfo);
                }
            }
        }
    }

    public ArrayList<FileInfo> getSelectedFileList() {
        return mCheckedFileNameList;
    }

    public boolean canPaste() {
        return mFileOperationHelper.canPaste();
    }

    // operation finish notification
	@Override
    public void onFinish() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        mFileViewListener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clearSelection();
                refreshFileList();
            }
        });
    }

    public FileInfo getItem(int pos) {
        return mFileViewListener.getItem(pos);
    }

    public boolean isInSelection() {
        return mCheckedFileNameList.size() > 0;
    }

    public boolean isMoveState() {
        return mFileOperationHelper.isMoveState() || mFileOperationHelper.canPaste();
    }

    private void setup() {
        setupFileListView();
    }
  
    public void onSortChanged(SortMethod s) {
        if (mFileSortHelper.getSortMethod() != s) {
            mFileSortHelper.setSortMethog(s);
            sortCurrentList();
        }
    }

    public void refreshFileList() {
        clearSelection();
        
        // onRefreshFileList returns true indicates list has changed
        mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper);
    }

    private void notifyFileSystemChanged(String path) {
        if (path == null)
            return;
        final File f = new File(path);
        final Intent intent;
        if (f.isDirectory()) {
            intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
            intent.setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
            intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            Log.v(LOG_TAG, "directory changed, send broadcast:" + intent.toString());
        } else {
            intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(new File(path)));
            Log.v(LOG_TAG, "file changed, send broadcast:" + intent.toString());
        }
        mContext.sendBroadcast(intent);
    }

    private void doOperationDelete(final ArrayList<FileInfo> selectedFileList) {
        final ArrayList<FileInfo> selectedFiles = new ArrayList<FileInfo>(selectedFileList);
        Dialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.operation_delete_confirm_message))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int whichButton) {
                        if (mFileOperationHelper.Delete(selectedFiles)) {
                            showProgress(mContext.getString(R.string.operation_deleting));
                        }
                        clearSelection();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearSelection();
                    }
                }).create();
        dialog.show();
    }

    // File List view setup
    private ListView mFileListView;

    private int mListViewContextMenuSelectedItem;

    private void setupFileListView() {
        mFileListView = (ListView) mFileViewListener.getViewById(R.id.file_path_list);
        mFileListView.setLongClickable(false);
        mFileListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(parent, view, position, id);
            }
        });
    }

    private com.dami.fileexplorer.view.FileViewInteractionHubEncrypt.Mode mCurrentMode;

    private String mCurrentPath;

    private String mRoot;

    private SelectFilesCallback mSelectFilesCallback;

    public boolean isFileSelected(String filePath) {
        return mFileOperationHelper.isFileSelected(filePath);
    }

    public void setMode(Mode m) {
        mCurrentMode = m;
    }

    public Mode getMode() {
        return mCurrentMode;
    }

    public void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo lFileInfo = mFileViewListener.getItem(position);

        if (lFileInfo == null) {
            Log.e(LOG_TAG, "file does not exist on position:" + position);
            return;
        }

        if (isInSelection()) {
            boolean selected = lFileInfo.Selected;
            ImageView checkBox = (ImageView) view.findViewById(R.id.file_checkbox);
            if (selected) {
                mCheckedFileNameList.remove(lFileInfo);
                checkBox.setImageResource(R.drawable.btn_radio_to_on_mtrl_000);
            } else {
                mCheckedFileNameList.add(lFileInfo);
                checkBox.setImageResource(R.drawable.btn_radio_to_off_mtrl_000);
            }

            lFileInfo.Selected = !selected;

            return;
        }

        if (!lFileInfo.IsDir) {
            if (mCurrentMode == Mode.Pick) {
                mFileViewListener.onPick(lFileInfo);
            } else {
            	String orginFilePath = lFileInfo.filePath;
            	String orginFileName = lFileInfo.fileName;
            	String realFilePath = Util.getPathFromFilepath(orginFilePath);
            	String realFileName = Util.getRealNameFromFilepath(orginFilePath);
            	
            	File dirFirstFolder = new File(mContext.getResources().getString(R.string.decrypt_ccache_path));
            	if(!dirFirstFolder.exists())  
                { //如果该文件夹不存在，则创建文件夹    
            		dirFirstFolder.mkdirs();
                }    
            	
            	mEncDecBusiness.decFile(CommonUtils.SD_PATH + mContext.getResources().getString(R.string.decrypt_ccache) + realFileName, orginFilePath);
            	lFileInfo.filePath = CommonUtils.SD_PATH + mContext.getResources().getString(R.string.decrypt_ccache) + realFileName;            	
            	lFileInfo.fileName = realFileName;
            	
            	viewFile(lFileInfo);
            }
            return;
        }

        mCurrentPath = getAbsoluteName(mCurrentPath, lFileInfo.fileName);
        refreshFileList();
    }

    public void setRootPath(String path) {
        mRoot = path;
        mCurrentPath = path;
    }

    public String getRootPath() {
        return mRoot;
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }

    public void setCurrentPath(String path) {
        mCurrentPath = path;
    }

    private String getAbsoluteName(String path, String name) {
        return path.equals(GlobalConsts.ROOT_PATH) ? path + name : path + File.separator + name;
    }

    // check or uncheck
    public boolean onCheckItem(FileInfo f, View v) {
        if (isMoveState())
            return false;

        if(isSelectingFiles() && f.IsDir)
            return false;

        if (f.Selected) {
            mCheckedFileNameList.add(f);
        } else {
            mCheckedFileNameList.remove(f);
        }
        return true;
    }

    private boolean isSelectingFiles() {
        return mSelectFilesCallback != null;
    }

    public boolean isSelectedAll() {
        return mFileViewListener.getItemCount() != 0 && mCheckedFileNameList.size() == mFileViewListener.getItemCount();
    }
    
    public boolean isSelected() {
        return mCheckedFileNameList.size() != 0;
    }

    public void clearSelection() {
        if (mCheckedFileNameList.size() > 0) {
            for (FileInfo f : mCheckedFileNameList) {
                if (f == null) {
                    continue;
                }
                f.Selected = false;
            }
            mCheckedFileNameList.clear();
            mFileViewListener.onDataChanged();
        }
    }

    private void viewFile(FileInfo lFileInfo) {
        try {
            IntentBuilder.viewFile((FileEncryptCategoryActivity)mContext, lFileInfo.filePath);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "fail to view file: " + e.toString());
        }
    }

    public boolean onBackPressed() {
		if (isInSelection()) {
            clearSelection();
        }
        return true;
    }

    public void copyFile(ArrayList<FileInfo> files) {
        mFileOperationHelper.Copy(files);
    }

    public void moveFileFrom(ArrayList<FileInfo> files) {
        mFileOperationHelper.StartMove(files);
        // refresh to hide selected files
        refreshFileList();
    }

    @Override
    public void onFileChanged(String path) {
        notifyFileSystemChanged(path);
    }

    public void startSelectFiles(SelectFilesCallback callback) {
        mSelectFilesCallback = callback;
    }
}
