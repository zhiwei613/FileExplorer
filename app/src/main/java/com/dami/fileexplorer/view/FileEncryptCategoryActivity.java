package com.dami.fileexplorer.view;


import java.util.Collection;
import java.util.HashMap;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.adapter.FileListCursorAdapterEncrypt;
import com.dami.fileexplorer.interfaces.IFileInteractionListener;
import com.dami.fileexplorer.util.FileCategoryHelper;
import com.dami.fileexplorer.util.FileCategoryHelper.CategoryInfo;
import com.dami.fileexplorer.util.FileCategoryHelper.FileCategory;
import com.dami.fileexplorer.util.FileIconHelper;
import com.dami.fileexplorer.util.FileInfo;
import com.dami.fileexplorer.util.FileSortHelper;
import com.dami.fileexplorer.view.FileCategoryActivity.ViewPage;
import com.dami.fileexplorer.view.FileViewInteractionHubEncrypt.Mode;
import com.dami.fileexplorer.xdja.utils.CommonUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FileEncryptCategoryActivity extends Activity implements IFileInteractionListener, OnClickListener {
	
	private Button password_setting_btn, encrypt_btn;
	private FileCategoryHelper mFileCagetoryHelper;
	private FileViewInteractionHubEncrypt mFileViewInteractionHub;
	private FileListCursorAdapterEncrypt mAdapter;
	private FileIconHelper mFileIconHelper;
	private ScannerReceiver mScannerReceiver;
	private ListView fileListView;
	private Activity mActivity;
	private static HashMap<Integer, FileCategory> buttonEncryptCategory = new HashMap<Integer, FileCategory>();
	
    static {
    	buttonEncryptCategory.put(R.id.category_encrypt_music, FileCategory.EcnryptMusic);
    	buttonEncryptCategory.put(R.id.category_encrypt_video, FileCategory.EcnryptVideo);
    	buttonEncryptCategory.put(R.id.category_encrypt_picture, FileCategory.EcnryptPicture);
    	buttonEncryptCategory.put(R.id.category_encrypt_doc, FileCategory.EcnryptDoc);
    	buttonEncryptCategory.put(R.id.category_encrypt_apk, FileCategory.EcnryptApk);
    	buttonEncryptCategory.put(R.id.category_encrypt_other, FileCategory.EcnryptOther);
    }
    
    View.OnClickListener onEncryptCategoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = buttonEncryptCategory.get(v.getId());
            if (f != null) {
            	Log.d("william","william onEncryptCategoryClickListener xxxxFileCategory is click"+f);
            	onEncryptCategorySelected(f);
            	//setHasOptionsMenu(true);
            }
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_encrypt_category);
		mActivity = FileEncryptCategoryActivity.this;
		
		password_setting_btn = (Button) findViewById(R.id.password_setting_btn);
		encrypt_btn = (Button) findViewById(R.id.encrypt_btn);
		password_setting_btn.setOnClickListener(this);
		encrypt_btn.setOnClickListener(this);

		mFileCagetoryHelper = new FileCategoryHelper(FileEncryptCategoryActivity.this);
        mFileViewInteractionHub = new FileViewInteractionHubEncrypt(FileEncryptCategoryActivity.this);
        mFileViewInteractionHub.setMode(Mode.View);
        mFileViewInteractionHub.setRootPath("/");
        
        mFileIconHelper = new FileIconHelper(FileEncryptCategoryActivity.this);
        mAdapter = new FileListCursorAdapterEncrypt(FileEncryptCategoryActivity.this, null, mFileViewInteractionHub, mFileIconHelper);
        fileListView = (ListView) findViewById(R.id.file_path_list);
        fileListView.setAdapter(mAdapter);
        Log.d("william","william FileEncryptCategoryActivity fileListView is:"+fileListView);
        
		setupEncryptCategoryClick();
		
		refreshCategoryInfo();
		
		registerScannerReceiver();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fileListView.setAdapter(mAdapter);
	}
	
    private void setupEncryptCategoryClick() {
    	setupEncryptCategoryClick(R.id.category_encrypt_music);
    	setupEncryptCategoryClick(R.id.category_encrypt_video);
    	setupEncryptCategoryClick(R.id.category_encrypt_picture);
    	setupEncryptCategoryClick(R.id.category_encrypt_doc);
    	setupEncryptCategoryClick(R.id.category_encrypt_apk);
    	setupEncryptCategoryClick(R.id.category_encrypt_other);
    }
    
    private void setupEncryptCategoryClick(int id) {
        View button = findViewById(id);
        button.setOnClickListener(onEncryptCategoryClickListener);

    }
    
    private void onEncryptCategorySelected(FileCategory f) {
        if (mFileCagetoryHelper.getCurCategory() != f) {
            mFileCagetoryHelper.setCurCategory(f);
            mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()
                    + getString(mFileCagetoryHelper.getCurCategoryNameResId()));
            mFileViewInteractionHub.refreshFileList();
        }
		showView(R.id.category_page, false);
        showView(R.id.file_path_list, true);
        showView(R.id.password_setting_btn, false);
        showView(R.id.encrypt_btn, true);
    }    
	
    public void refreshCategoryInfo() {
    	 mFileCagetoryHelper.refreshCategoryInfo(0);

         for (FileCategory fc : FileCategoryHelper.sCategories) {
             CategoryInfo categoryInfo = mFileCagetoryHelper.getCategoryInfos().get(fc);
             setCategoryCount(fc, categoryInfo.count);
         }
    }
    
    private void setCategoryCount(FileCategory fc, long count) {
        int id = getCategoryCountId(fc);
        if (id == 0)
            return;

        setTextView(id, "(" + count + ")");
    }
    
    private static int getCategoryCountId(FileCategory fc) {
        switch (fc) {
            case EcnryptMusic:
                return R.id.category_encrypt_music_count;
            case EcnryptVideo:
                return R.id.category_encrypt_video_count;
            case EcnryptPicture:
                return R.id.category_encrypt_picture_count;
            case EcnryptDoc:
                return R.id.category_encrypt_doc_count;
            case EcnryptApk:
                return R.id.category_encrypt_apk_count;    
            case EcnryptOther:
                 return R.id.category_encrypt_other_count;                
        }

        return 0;
    }
    
    private void setTextView(int id, String t) {
        TextView text = (TextView) findViewById(id);
        text.setText(t);
    }   
    
    private void showView(int id, boolean show) {
        View view = findViewById(id);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }    
    
    private void registerScannerReceiver() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        registerReceiver(mScannerReceiver, intentFilter);
    }
    
    private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        }
    }    
    
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		//Intent intent = new Intent(FileEncryptCategoryActivity.this, FileExplorerTabActivity.class);
		//startActivity(intent);
	}

	@Override
	public View getViewById(int id) {
		// TODO Auto-generated method stub
		return findViewById(id);
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return mActivity;
	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPick(FileInfo f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean shouldShowOperationPane() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onOperation(int id) {
		// TODO Auto-generated method stub
        mFileViewInteractionHub.addContextMenuSelectedItem();
        return true;
	}

	@Override
	public String getDisplayPath(String path) {
		// TODO Auto-generated method stub
		return getString(R.string.tab_category) + path;
	}

	@Override
	public String getRealPath(String displayPath) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public boolean onNavigation(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldHideMenu(int menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FileIconHelper getFileIconHelper() {
		// TODO Auto-generated method stub
		return mFileIconHelper;
	}

	@Override
	public FileInfo getItem(int pos) {
		// TODO Auto-generated method stub
		return mAdapter.getFileItem(pos);
	}

	@Override
	public void sortCurrentList(FileSortHelper sort) {
		// TODO Auto-generated method stub
        refreshList();
    }

    private void refreshList() {
        mFileViewInteractionHub.refreshFileList();
    }

	@Override
	public Collection<FileInfo> getAllFiles() {
		// TODO Auto-generated method stub
		return mAdapter.getAllFiles();
	}

	@Override
	public void addSingleFile(FileInfo file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {
		// TODO Auto-generated method stub
        FileCategory curCategory = mFileCagetoryHelper.getCurCategory();
        if (curCategory == FileCategory.Favorite || curCategory == FileCategory.All)
            return false;

        Cursor c = mFileCagetoryHelper.query(curCategory, sort.getSortMethod());
        mAdapter.changeCursor(c);

        return true;
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return mAdapter.getCount();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		case R.id.password_setting_btn:
			Intent intent = new Intent(FileEncryptCategoryActivity.this, FileEncryptOriginPasswordActivity.class);
			startActivity(intent);
			break;
		case R.id.encrypt_btn:
			
			break;
		default:
			break;
		}
	}
}
