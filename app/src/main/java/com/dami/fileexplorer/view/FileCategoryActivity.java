
package com.dami.fileexplorer.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;


import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.swiftp.Globals;

import com.dami.fileexplorer.FileManagerApplication;
import com.dami.fileexplorer.R;
import com.dami.fileexplorer.FileManagerApplication.SDCardChangeListener;
import com.dami.fileexplorer.adapter.FileListCursorAdapter;
import com.dami.fileexplorer.interfaces.IFileInteractionListener;
import com.dami.fileexplorer.util.FileCategoryHelper;
import com.dami.fileexplorer.util.FileIconHelper;
import com.dami.fileexplorer.util.FileInfo;
import com.dami.fileexplorer.util.FileSortHelper;
import com.dami.fileexplorer.util.GlobalConsts;
import com.dami.fileexplorer.util.Util;
import com.dami.fileexplorer.util.FavoriteDatabaseHelper.FavoriteDatabaseListener;
import com.dami.fileexplorer.util.FileCategoryHelper.CategoryInfo;
import com.dami.fileexplorer.util.FileCategoryHelper.FileCategory;
import com.dami.fileexplorer.util.Util.MemoryCardInfo;
import com.dami.fileexplorer.util.Util.SDCardInfo;
import com.dami.fileexplorer.view.FileExplorerTabActivity.IBackPressedListener;
import com.dami.fileexplorer.view.FileViewInteractionHub.Mode;
import com.dami.fileexplorer.xdja.business.DBBusiness;
import com.dami.fileexplorer.xdja.utils.CommonUtils;

public class FileCategoryActivity extends Fragment implements IFileInteractionListener,
        FavoriteDatabaseListener, IBackPressedListener, OnClickListener, SDCardChangeListener {

    public static final String EXT_FILETER_KEY = "ext_filter";

    private static final String LOG_TAG = "FileCategoryActivity";

    private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();

    private HashMap<FileCategory, Integer> categoryIndex = new HashMap<FileCategory, Integer>();

    private FileListCursorAdapter mAdapter;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private CategoryBar mCategoryBar;

    private ScannerReceiver mScannerReceiver;

    private FavoriteList mFavoriteList;

    private ViewPage curViewPage = ViewPage.Invalid;

    private ViewPage preViewPage = ViewPage.Invalid;

    private Activity mActivity;
    
    private FileManagerApplication mApplication;

    private View mRootView;

    private FileViewActivity mFileViewActivity;
    
    private RoundProgressBar mRoundProgressBar2;
    private RoundProgressBar mRoundProgressBar3;
    private TextView sdCardView;
    private TextView sdCardinfoView;
    private TextView sdPercentView;
    private TextView memoryCardView;
    private TextView memoryCardinfoView;
    private TextView memoryPercentView;
    public static String SDCardPath;
    private long progress1 = 0;
    private long progress2 = 0;
    private long memoryCardTotal;
    private long memoryCardTotalss;
    private long memoryCardFree;    
    private long memoryCardUsed;
    private long memoryCardUsedss;
    private long sdCardTotal;
    private long sdCardTotalss;
    private long sdCardFree;
    private long sdCardUsed;    
    private long sdCardUsedss;
    private boolean noSdCard = false;
    
    public static DBBusiness mDBBusiness;
    
    private AsyncTask<Void, Void, Void> mRefreshCategoryInfoTask;

    private boolean mConfigurationChanged = false;

    public void setConfigurationChanged(boolean changed) {
        mConfigurationChanged = changed;
    }

    static {
        button2Category.put(R.id.category_music, FileCategory.Music);
        button2Category.put(R.id.category_video, FileCategory.Video);
        button2Category.put(R.id.category_picture, FileCategory.Picture);
        button2Category.put(R.id.category_document, FileCategory.Doc);
        button2Category.put(R.id.category_apk, FileCategory.Apk);
        //button2Category.put(R.id.category_favorite, FileCategory.Favorite);
        button2Category.put(R.id.category_encrypt, FileCategory.Ecnrypt);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        mFileViewActivity = (FileViewActivity) ((FileExplorerTabActivity) mActivity)
                .getFragment(Util.SDCARD_TAB_INDEX);
        mRootView = inflater.inflate(R.layout.file_explorer_category, container, false);
        
        mRoundProgressBar2 = (RoundProgressBar) mRootView.findViewById(R.id.mainroundProgressBar2);
        mRoundProgressBar3 = (RoundProgressBar) mRootView.findViewById(R.id.mainroundProgressBar3);
        sdCardView = (TextView) mRootView.findViewById(R.id.sd_card);
        sdCardinfoView = (TextView) mRootView.findViewById(R.id.sd_card_info);
        sdPercentView = (TextView) mRootView.findViewById(R.id.sd_percent);
        memoryCardView = (TextView) mRootView.findViewById(R.id.memory_card);
        memoryCardinfoView = (TextView) mRootView.findViewById(R.id.memory_card_info);
        memoryPercentView = (TextView) mRootView.findViewById(R.id.memory_percent);
        
        mApplication = (FileManagerApplication) mActivity.getApplication();
        mApplication.addSDCardChangeListener(this);
        
        mRoundProgressBar2.setOnClickListener(this);
        mRoundProgressBar3.setOnClickListener(this);
        
        curViewPage = ViewPage.Invalid;
        mFileViewInteractionHub = new FileViewInteractionHub(this, 0);
        mFileViewInteractionHub.setMode(Mode.View);
        mFileViewInteractionHub.setRootPath("/");
        mFileIconHelper = new FileIconHelper(mActivity);
        mFavoriteList = new FavoriteList(mActivity, (ListView) mRootView.findViewById(R.id.favorite_list), this, mFileIconHelper);
        mFavoriteList.initList();
        mAdapter = new FileListCursorAdapter(mActivity, null, mFileViewInteractionHub, mFileIconHelper);

        ListView fileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
        fileListView.setAdapter(mAdapter);
        Log.d("william","william FileCategoryActivity fileListView is:"+fileListView);
        
        Context myContext = mActivity.getApplicationContext();
    	mDBBusiness = new DBBusiness(myContext);
    	mDBBusiness.createGroupTable();
    	//ContentValues account = new ContentValues();
    	//account.put("accountname", CommonUtils.currentAccount);
        //account.put("password", "123456");
        //mDBBusiness.insertPassword(account);
        
        setupClick();
        setupCategoryInfo();
        setSDcardPath();
        updateUI();
        registerScannerReceiver();
        return mRootView;
    }
    
    @Override
	public void onResume() {
        super.onResume();
		refreshCategoryInfo();
		mFileViewInteractionHub.refreshFileList();
        String SDcardPath = setSDcardPath();
        if (SDcardPath != null) {
        	mRoundProgressBar2.setOnClickListener(this);
        } else {
        	mRoundProgressBar2.setOnClickListener(null);
            }
    }
    
    public String setSDcardPath(){
    	String path = "/storage/sdcard1";
    	SDCardPath = path;
    	mActivity = getActivity();
        Context myContext = Globals.getContext();
        if (myContext == null) {
            myContext = mActivity.getApplicationContext();
            if (myContext == null) {
                throw new NullPointerException("Null context!?!?!?");
            }
            Globals.setContext(myContext);
        }
        StorageManager mStorageManager = (StorageManager) myContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;  
        try {
			storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
			Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
			Object result;
			result = getVolumeList.invoke(mStorageManager);
		    final int length = Array.getLength(result);  
		    for (int i = 0; i < length; i++) {  
		    	Object storageVolumeElement = Array.get(result, i);  
		    	path = (String) getPath.invoke(storageVolumeElement);  
		    	boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);  
		        if (removable) {  
		        	SDCardPath = path;  
		        } 
		    }
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        } catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        } catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        } 
    	return SDCardPath;
    }

    private void registerScannerReceiver() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        mActivity.registerReceiver(mScannerReceiver, intentFilter);
    }

    private void setupCategoryInfo() {
        mFileCagetoryHelper = new FileCategoryHelper(mActivity);

        mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        int[] imgs = new int[] {
                R.drawable.category_bar_music, R.drawable.category_bar_video,
                R.drawable.category_bar_picture, R.drawable.category_bar_theme,
                R.drawable.category_bar_document, R.drawable.category_bar_zip,
                R.drawable.category_bar_apk, R.drawable.category_bar_other
        };

        for (int i = 0; i < imgs.length; i++) {
            mCategoryBar.addCategory(imgs[i]);
        }

        for (int i = 0; i < FileCategoryHelper.sCategories.length; i++) {
            categoryIndex.put(FileCategoryHelper.sCategories[i], i);
        }
    }

    public void refreshCategoryInfo() {
        MemoryCardInfo memoryCardInfo = Util.getMemoryCardInfo();
        if (memoryCardInfo != null) {
            //mCategoryBar.setFullValue(memoryCardInfo.total);
            //setTextView(R.id.sd_card_capacity, getString(R.string.sd_card_size, Util.convertStorage(memoryCardInfo.total)));
            //setTextView(R.id.sd_card_available, getString(R.string.sd_card_available, Util.convertStorage(memoryCardInfo.free)));
            memoryCardTotal = memoryCardInfo.total;
            memoryCardFree = memoryCardInfo.free;
            memoryCardUsed = memoryCardInfo.total-memoryCardInfo.free;
            memoryCardTotalss = memoryCardInfo.total/100000;
            memoryCardUsedss = (memoryCardInfo.total-memoryCardInfo.free)/100000;
            memoryCardinfoView.setText(Util.convertStorage(memoryCardUsed)+"/"+Util.convertStorage(memoryCardTotal));
        }else{
        	memoryCardTotalss = 0;
        	memoryCardUsedss = 0;
        	memoryCardTotal = 0;
        	memoryCardFree = 0;
        	progress1 = -1;
        }
        mRoundProgressBar3.setMax(memoryCardTotalss, memoryCardTotal, memoryCardFree, 2, false);
        new Thread(new Runnable() {
            @Override
			public void run() {
                while (progress2 <= memoryCardUsedss) {
                	progress2 = progress2 + 5;
                    mRoundProgressBar3.setProgress(progress2);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if(sdCardInfo != null){
        	sdCardTotal = sdCardInfo.total;
            sdCardFree = sdCardInfo.free;
            sdCardUsed = sdCardInfo.total-sdCardInfo.free;
            sdCardTotalss = sdCardInfo.total/100000;
            sdCardUsedss = (sdCardInfo.total-sdCardInfo.free)/100000;
        	sdCardinfoView.setText(Util.convertStorage(sdCardUsed)+"/"+Util.convertStorage(sdCardTotal));
            sdCardinfoView.setTextColor(getResources().getColor(R.color.textColor));
            sdCardView.setTextColor(getResources().getColor(R.color.textColor));
        }else{
        	sdCardinfoView.setText(R.string.enable_sd_card);
            sdPercentView.setTextSize(R.dimen.sd_percent_textsize);
            //sdPercentView.setVisibility(View.GONE);
            sdPercentView.setTextColor(getResources().getColor(R.color.notextColor));
            sdCardinfoView.setTextColor(getResources().getColor(R.color.notextColor));
            sdCardView.setTextColor(getResources().getColor(R.color.notextColor));
        	sdCardTotalss = 0;
        	sdCardUsedss = 0;
        	sdCardTotal = 0;
        	sdCardFree = 0;
        	progress1 = -1;
        }
        mRoundProgressBar2.setMax(sdCardTotalss, sdCardTotal, sdCardFree, 1, false);
        new Thread(new Runnable() {
            @Override
			public void run() {
                while (progress1 <= sdCardUsedss) {
                	progress1 = progress1 + 5;
                	mRoundProgressBar2.setProgress(progress1);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        mFileCagetoryHelper.refreshCategoryInfo(0);

        // the other category size should include those files didn't get scanned.
        long size = 0;
        for (FileCategory fc : FileCategoryHelper.sCategories) {
            CategoryInfo categoryInfo = mFileCagetoryHelper.getCategoryInfos().get(fc);
            setCategoryCount(fc, categoryInfo.count);
            Log.d("william", "william refreshCategoryInfo fc is:"+fc);
            Log.d("william", "william refreshCategoryInfo categoryInfo.count is:"+categoryInfo.count);
            // other category size should be set separately with calibration
            if(fc == FileCategory.Other)
                continue;

            setCategorySize(fc, categoryInfo.size);
            setCategoryBarValue(fc, categoryInfo.size);
            size += categoryInfo.size;
        }

        if (memoryCardInfo != null) {
            long otherSize = memoryCardInfo.total - memoryCardInfo.free - size;
            setCategorySize(FileCategory.Other, otherSize);
            setCategoryBarValue(FileCategory.Other, otherSize);
        }

        setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());

        if (mCategoryBar.getVisibility() == View.VISIBLE) {
            mCategoryBar.startAnimation();
        }
    } 

    public enum ViewPage {
        Home, Favorite, Category, NoSD, Invalid
    }

    private void showPage(ViewPage p) {
        if (curViewPage == p) return;

        curViewPage = p;

        showView(R.id.file_path_list, false);
        showView(R.id.navigation_bar, false);
		showView(R.id.gallery_navigationbar, false);
        showView(R.id.category_page, false);
        showView(R.id.operation_bar, false);
        showView(R.id.sd_not_available_page, false);
        mFavoriteList.show(false);
        showEmptyView(false);

        switch (p) {
            case Home:
                showView(R.id.category_page, true);
                if (mConfigurationChanged) {
                    ((FileExplorerTabActivity) mActivity).reInstantiateCategoryTab();
                    mConfigurationChanged = false;
                }
                break;
            case Favorite:
                showView(R.id.navigation_bar, true);
				showView(R.id.gallery_navigationbar, true);
                mFavoriteList.show(true);
                showEmptyView(mFavoriteList.getCount() == 0);
                break;
            case Category:
                showView(R.id.navigation_bar, true);
				showView(R.id.gallery_navigationbar, true);
                showView(R.id.file_path_list, true);
                showEmptyView(mAdapter.getCount() == 0);
                break;
            case NoSD:
                showView(R.id.sd_not_available_page, true);
                break;
        }
    }

    private void showEmptyView(boolean show) {
        View emptyView = mActivity.findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showView(int id, boolean show) {
        View view = mRootView.findViewById(id);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = button2Category.get(v.getId());
            if (f != null) {
                onCategorySelected(f);
                if (f != FileCategory.Favorite) {
                    setHasOptionsMenu(true);
                }
            }
        }

    };

    private void setCategoryCount(FileCategory fc, long count) {
        int id = getCategoryCountId(fc);
        if (id == 0)
            return;

        setTextView(id, "(" + count + ")");
    }

    private void setTextView(int id, String t) {
        TextView text = (TextView) mRootView.findViewById(id);
        text.setText(t);
    }

    private void onCategorySelected(FileCategory f) {
        if (mFileCagetoryHelper.getCurCategory() != f) {
            mFileCagetoryHelper.setCurCategory(f);
            mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()
                    + getString(mFileCagetoryHelper.getCurCategoryNameResId()));
            mFileViewInteractionHub.refreshFileList();
        }

        if (f == FileCategory.Favorite) {
            showPage(ViewPage.Favorite);
        } else {
            showPage(ViewPage.Category);
        }
    }

    private void setupClick(int id) {
        View button = mRootView.findViewById(id);
        if(button.getId() == R.id.category_encrypt){
        	button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String pwd = mDBBusiness.queryPassword(CommonUtils.currentAccount);
		        	Log.d("william","william setupClick mDBBusiness.queryPassword pwd is:"+pwd);
		        	if(pwd == null){
		        		Intent intent = new Intent(mActivity, FileEncryptFistStartActivity.class);
		        		startActivity(intent);
		        	}else{
		        		Intent intent = new Intent(mActivity, FileEncryptLoginActivity.class);
		        		startActivity(intent);
		        	}
				}
			});
        	
        }else{
        	button.setOnClickListener(onClickListener);
        }
        
    }

    private void setupClick() {
        setupClick(R.id.category_music);
        setupClick(R.id.category_video);
        setupClick(R.id.category_picture);
        setupClick(R.id.category_document);
        setupClick(R.id.category_apk);
        //setupClick(R.id.category_favorite);
        setupClick(R.id.category_encrypt);
    }

	@Override
    public boolean onBack() {
        if (isHomePage() || curViewPage == ViewPage.NoSD || mFileViewInteractionHub == null) {
            return false;
        }

        return mFileViewInteractionHub.onBackPressed();
    }

    public boolean isHomePage() {
        return curViewPage == ViewPage.Home;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (curViewPage != ViewPage.Category && curViewPage != ViewPage.Favorite) {
            return;
        }
        mFileViewInteractionHub.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!isHomePage() && mFileCagetoryHelper.getCurCategory() != FileCategory.Favorite) {
            mFileViewInteractionHub.onPrepareOptionsMenu(menu);
        }
    }

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {
        FileCategory curCategory = mFileCagetoryHelper.getCurCategory();
        if (curCategory == FileCategory.Favorite || curCategory == FileCategory.All)
            return false;

        Cursor c = mFileCagetoryHelper.query(curCategory, sort.getSortMethod());
        showEmptyView(c == null || c.getCount() == 0);
        mAdapter.changeCursor(c);

        return true;
    }

	@Override
    public View getViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
	@SuppressLint("Override") public Context getContext() {
        return mActivity;
    }

	@Override
    public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
            	refreshCategoryInfo();
                mAdapter.notifyDataSetChanged();
                mFavoriteList.getArrayAdapter().notifyDataSetChanged();
                showEmptyView(mAdapter.getCount() == 0);
            }

        });
    }

	@Override
    public void onPick(FileInfo f) {
        // do nothing
    }

	@Override
    public boolean shouldShowOperationPane() {
        return true;
    }

	@Override
    public boolean onOperation(int id) {
        mFileViewInteractionHub.addContextMenuSelectedItem();
        switch (id) {
			case 4:
				//getFragmentManager().popBackStack();
				//((FileExplorerTabActivity)mActivity).mViewPager.setCurrentItem(Util.CATEGORY_TAB_INDEX);
				onBack();
				break;
            case R.id.button_operation_copy:
            case GlobalConsts.MENU_COPY:
                copyFileInFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case R.id.button_operation_move:
            case GlobalConsts.MENU_MOVE:
                startMoveToFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case GlobalConsts.OPERATION_UP_LEVEL:
                setHasOptionsMenu(false);
                showPage(ViewPage.Home);
                break; 
            default:
                return false;
        }
        return true;
    }

	@Override
    public String getDisplayPath(String path) {
        return getString(R.string.tab_category) + path;
    }

	@Override
    public String getRealPath(String displayPath) {
        return "";
    }

	@Override
    public boolean onNavigation(String path) {
        showPage(ViewPage.Home);
        return true;
    }
	
	@Override
    public boolean shouldHideMenu(int menu) {
        return (menu == GlobalConsts.MENU_NEW_FOLDER || menu == GlobalConsts.MENU_FAVORITE
                || menu == GlobalConsts.MENU_PASTE || menu == GlobalConsts.MENU_SHOWHIDE);
    }

	@Override
    public void addSingleFile(FileInfo file) {
        refreshList();
    }

	@Override
    public Collection<FileInfo> getAllFiles() {
        return mAdapter.getAllFiles();
    }

	@Override
    public FileInfo getItem(int pos) {
        return mAdapter.getFileItem(pos);
    }

	@Override
    public int getItemCount() {
        return mAdapter.getCount();
    }

	@Override
    public void sortCurrentList(FileSortHelper sort) {
        refreshList();
    }

    private void refreshList() {
        mFileViewInteractionHub.refreshFileList();
    }

    private void copyFileInFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.copyFile(files);
        //mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
		((FileExplorerTabActivity)mActivity).mViewPager.setCurrentItem(Util.SDCARD_TAB_INDEX);
    }

    private void startMoveToFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.moveToFile(files);
        //mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
		((FileExplorerTabActivity)mActivity).mViewPager.setCurrentItem(Util.SDCARD_TAB_INDEX);
    }

	@Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    private static int getCategoryCountId(FileCategory fc) {
        switch (fc) {
            case Music:
                return R.id.category_music_count;
            case Video:
                return R.id.category_video_count;
            case Picture:
                return R.id.category_picture_count;
            case Doc:
                return R.id.category_document_count;
            case Apk:
                return R.id.category_apk_count;    
            //case Favorite:
            //    return R.id.category_favorite_count;
            case Ecnrypt:
                 return R.id.category_encrypt_count;                
        }

        return 0;
    }

    private void setCategorySize(FileCategory fc, long size) {
        int txtId = 0;
        int resId = 0;
        switch (fc) {
            case Music:
                txtId = R.id.category_legend_music;
                resId = R.string.category_music;
                break;
            case Video:
                txtId = R.id.category_legend_video;
                resId = R.string.category_video;
                break;
            case Picture:
                txtId = R.id.category_legend_picture;
                resId = R.string.category_picture;
                break;
            case Doc:
                txtId = R.id.category_legend_document;
                resId = R.string.category_document;
                break;
            case Zip:
                txtId = R.id.category_legend_zip;
                resId = R.string.category_zip;
                break;
            case Apk:
                txtId = R.id.category_legend_apk;
                resId = R.string.category_apk;
                break;
            case Other:
                txtId = R.id.category_legend_other;
                resId = R.string.category_other;
                break;
        }

        if (txtId == 0 || resId == 0)
            return;

        setTextView(txtId, getString(resId) + ":" + Util.convertStorage(size));
    }

    private void setCategoryBarValue(FileCategory f, long size) {
        if (mCategoryBar == null) {
            mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        }
        mCategoryBar.setCategoryValue(categoryIndex.get(f), size);
    }

    @Override
	public void onDestroy() {
        super.onDestroy();
        if (mActivity != null) {
            mActivity.unregisterReceiver(mScannerReceiver);
        }
    }

    private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast: " + action.toString());
            // handle intents related to external storage
            if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) || action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                notifyFileChanged();
            }
        }
    }

    private void updateUI() {
        boolean sdCardReady = Util.isSDCardReady();
        if (sdCardReady) {
            if (preViewPage != ViewPage.Invalid) {
                showPage(preViewPage);
                preViewPage = ViewPage.Invalid;
            } else if (curViewPage == ViewPage.Invalid || curViewPage == ViewPage.NoSD) {
                showPage(ViewPage.Home);
            }
            refreshCategoryInfo();
            // refresh file list
            mFileViewInteractionHub.refreshFileList();
            // refresh file list view in another tab
            mFileViewActivity.refresh();
        } else {
            preViewPage = curViewPage;
            showPage(ViewPage.NoSD);
        }
    }

    // process file changed notification, using a timer to avoid frequent
    // refreshing due to batch changing on file system
    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
			public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER;
                handler.sendMessage(message);
            }

        }, 1000);
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;

    private Timer timer;

    private Handler handler = new Handler() {
        @Override
		public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    // update the count of favorite
	@Override
    public void onFavoriteDatabaseChanged() {
        setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());
    }

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.mainroundProgressBar3:
			Intent intentMenoryCard = new Intent(mActivity, MoneyInfoActivity.class);
			intentMenoryCard.putExtra("SDMenoryCardTotalss", memoryCardTotalss);
			intentMenoryCard.putExtra("SDMenoryCardUsedss", memoryCardUsedss);
			intentMenoryCard.putExtra("SDMenoryCardTotal", memoryCardTotal);
			intentMenoryCard.putExtra("SDMenoryCardUsed", memoryCardUsed);
			intentMenoryCard.putExtra("SDMenoryCardFree", memoryCardFree);
			if(noSdCard){
				intentMenoryCard.putExtra("IsSDCard", 0);
			}else{
				intentMenoryCard.putExtra("IsSDCard", 2);
			}
			startActivity(intentMenoryCard);
			break;
		case R.id.mainroundProgressBar2:
			Intent intentSDCard = new Intent(mActivity, MoneyInfoActivity.class);
			intentSDCard.putExtra("SDMenoryCardTotalss", sdCardTotalss);
			intentSDCard.putExtra("SDMenoryCardUsedss", sdCardUsedss);
			intentSDCard.putExtra("SDMenoryCardTotal", sdCardTotal);
			intentSDCard.putExtra("SDMenoryCardUsed", sdCardUsed);
			intentSDCard.putExtra("SDMenoryCardFree", sdCardFree);
			intentSDCard.putExtra("IsSDCard", 1);
			startActivity(intentSDCard);
			break;	
		default:
			break;		
		}
	}

	@Override
	public void onMountStateChange(int i) {
		// TODO Auto-generated method stub
		refreshCategoryInfo();
        if (i == 1) {
            mRoundProgressBar2.setOnClickListener(this);
        } else if (i == 2) {
            mRoundProgressBar2.setOnClickListener(null);
        }
	}
    
    
    /*public void setStorageDeviceInfo() {
        if (isAdded()) {
            SDCardInfo sDCardInfo = Util.getSDCardInfo();
            Log.i("liuhaoran1", "sdCardInfo=" + sDCardInfo);
            if (sDCardInfo == null || sDCardInfo.total == sDCardInfo.free) {
                noSdCard = true;
                sdCardinfoView.setText(this.mActivity.getResources().getString(R.string.enable_sd_card));
                sdCardView.setText(this.mActivity.getResources().getString(R.string.sd_info_storage));
                sdPercentView.setTextSize(getResources().getDimension(R.dimen.sd_percent_textsize));
                sdPercentView.setVisibility(0);
                sdCardUsed = 0;
                sdCard = 0;
                sdPercentView.setTextColor(this.mActivity.getResources().getColor(R.color.notextColor));
                sdCardinfoView.setTextColor(this.mActivity.getResources().getColor(R.color.notextColor));
                sdCardView.setTextColor(this.mActivity.getResources().getColor(R.color.notextColor));
            } else {
                sdCardinfoView.setText(Util.convertStorage(sDCardInfo.total - sDCardInfo.free) + "/" + Util.convertStorage(sDCardInfo.total));
                sdCardinfoView.setTextScaleX(0.9f);
                sdPercentView.setVisibility(8);
                sdCardinfoView.setTextColor(this.mActivity.getResources().getColor(R.color.textColor));
                sdCardView.setTextColor(this.mActivity.getResources().getColor(R.color.textColor));
                sdCardView.setText(this.mActivity.getResources().getString(R.string.sd_info_storage));
                noSdCard = false;
                sdCardUsedl = sDCardInfo.total - sDCardInfo.free;
                sdCardss = sDCardInfo.total;
                sdCards = Util.convertStorage(sdCardss);
                sdCardFree = sDCardInfo.free;
                sdCardFrees = Util.convertStorage(sdCardFree);
                if (sDCardInfo.total - sDCardInfo.free > 100000) {
                    sdCardUsed = (sDCardInfo.total - sDCardInfo.free) / 100000;
                }
                if (sDCardInfo.total > 100000) {
                    sdCard = sDCardInfo.total / 100000;
                }
                if (progress > sdCardUsed) {
                    progress = 0;
                }
            }
            if (noSdCard) {
                sdCard = 0;
            }
            Log.i("xueweili", "sdCard = " + sdCard + " sdCards = " + sdCards + " sdCardFrees = " + sdCardFrees);
            mRoundProgressBar2.setMax(sdCard, sdCardss, sdCardFree, 1, false);
            new Thread(new Runnable() {
                public void run() {
                    while (progress <= sdCardUsed) {
                        progress = progress + 5;
                        mRoundProgressBar2.setProgress(progress);
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            mRoundProgressBar2.invalidate();
            memoryCardInfo = Util.getMemoryCardInfo();
            if (memoryCardInfo != null) {
                memoryPercentView.setVisibility(8);
                if (mMemoryCardInfo == 0) {
                    memoryCardinfoView.setText(Util.convertStorage(memoryCardInfo.total - memoryCardInfo.free) + "/" + Util.convertStorage(memoryCardInfo.total));
                    memoryCard = memoryCardInfo.total / 100000;
                } else if (!FileManagerApplication.mIsFeiMa.equals("true")) {
                    memoryCardinfoView.setText(Util.convertStorage(memoryCardInfo.total - memoryCardInfo.free) + "/" + FileManagerApplication.mMemoryCardInfo + " GB");
                    memoryCard = (((FileManagerApplication.mMemoryCardInfo * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / 100000;
                } else if (FeatureOption.TYD_TOOL_TEST_ROM_16G || FeatureOption.TYD_TOOL_TEST_ROM_32G || FeatureOption.TYD_TOOL_TEST_ROM_64G || FeatureOption.TYD_TOOL_TEST_ROM_128G) {
                    memoryCardinfoView.setText(Util.convertStorage(memoryCardInfo.total - memoryCardInfo.free) + "/" + FeatureOption.FAKE_ROM_SIZE + " GB");
                    memoryCard = (((Long.parseLong(FeatureOption.FAKE_ROM_SIZE) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / 100000;
                    Log.i("FeatureOption", "fake");
                } else {
                    memoryCardinfoView.setText(Util.convertStorage(memoryCardInfo.total - memoryCardInfo.free) + "/" + Util.convertStorage(memoryCardInfo.total));
                    memoryCard = memoryCardInfo.total / 100000;
                    Log.i("FeatureOption", "real ");
                }
                memoryCardView.setText(this.mActivity.getResources().getString(R.string.interior_info_storage));
                memoryCardss = memoryCardInfo.total;
                memoryCards = Util.convertStorage(memoryCardss);
                memoryFree = memoryCardInfo.free;
                memoryFrees = Util.convertStorage(memoryFree);
                memoryCardUsed = (memoryCardInfo.total - memoryCardInfo.free) / 100000;
                memoryCardUsedl = memoryCardInfo.total - memoryCardInfo.free;
                memoryCardinfoView.setTextScaleX(0.9f);
                memoryCardinfoView.setTextColor(this.mActivity.getResources().getColor(R.color.textColor));
                memoryCardView.setTextColor(this.mActivity.getResources().getColor(R.color.textColor));
                if (progress1 > memoryCardUsed) {
                    progress1 = 0;
                }
            } else {
                sdCardView.setText(mActivity.getResources().getString(R.string.sd_info_storage));
                memoryCardView.setText(mActivity.getResources().getString(R.string.interior_info_storage));
                memoryCardinfoView.setTextColor(mActivity.getResources().getColor(R.color.notextColor));
                memoryCardView.setTextColor(mActivity.getResources().getColor(R.color.notextColor));
                memoryPercentView.setTextColor(mActivity.getResources().getColor(R.color.notextColor));
                memoryPercentView.setVisibility(0);
            }
            Log.i(LOG_TAG, "sdCard = " + sdCard + " sdCards = " + sdCards + " sdCardFrees = " + sdCardFrees);
            mRoundProgressBar3.setMax(memoryCard, memoryCardss, memoryFree, 2, false);
            new Thread(new Runnable() {
                public void run() {
                    while (progress1 <= memoryCardUsed) {
                        progress1 = progress1 + 5;
                        mRoundProgressBar3.setProgress(progress1);
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            mRoundProgressBar3.invalidate();
        }
    }*/    
    
}
