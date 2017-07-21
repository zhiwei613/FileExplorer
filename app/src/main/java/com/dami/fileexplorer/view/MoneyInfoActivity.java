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

import java.util.HashMap;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.FileManagerApplication.ScannerReceiver;
import com.dami.fileexplorer.util.FileCategoryHelper;
import com.dami.fileexplorer.util.Util;
import com.dami.fileexplorer.util.FileCategoryHelper.CategoryInfo;
import com.dami.fileexplorer.util.FileCategoryHelper.FileCategory;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MoneyInfoActivity extends Activity {
    public static final String TAG = "MoneyInfoActivity";
    private static HashMap<Integer, FileCategory> button3Category = new HashMap();
    private TextView infoAvailable;
    private TextView infoMemory;
    private int isSdCard = 1;
    private LinearLayout mApkLinearLayout;
    private LinearLayout mDcLinearLayout;
    private FileCategoryHelper mFileCagetoryHelper;
    private LinearLayout mMusicLinearLayout;
    private LinearLayout mOtherLinearLayout;
    private LinearLayout mPictureLinearLayout;
    private AsyncTask<Void, Void, Object> mRefreshCategoryInfoTask;
    private RoundProgressBar mRoundProgressBar1;
    private RoundProgressBar mRoundProgressBar2;
    private RoundProgressBar mRoundProgressBar3;
    private RoundProgressBar mRoundProgressBar4;
    private RoundProgressBar mRoundProgressBar5;
    private ScannerReceiver mScannerReceiver;
    private LinearLayout mVideoLinearLayout;
    private long progress = 0;
    
    private long sdMemoryCardTotal;
    private long sdMemoryCardTotalss;
    private long sdMemoryCardFree;
    private long sdMemoryCardUsed;    
    private long sdMemoryCardUsedss;

    static {
        button3Category.put(Integer.valueOf(R.id.category_music_small), FileCategory.Music);
        button3Category.put(Integer.valueOf(R.id.category_video_small), FileCategory.Video);
        button3Category.put(Integer.valueOf(R.id.category_picture_small), FileCategory.Picture);
        button3Category.put(Integer.valueOf(R.id.category_document_small), FileCategory.Doc);
        button3Category.put(Integer.valueOf(R.id.category_apk_small), FileCategory.Apk);
        button3Category.put(Integer.valueOf(R.id.category_favorite_small), FileCategory.Other);
    }

    private void onItemClick(LinearLayout linearLayout) {
        linearLayout.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View view) {
            	/*
                FileCategory fileCategory = MoneyInfoActivity.button3Category.get(Integer.valueOf(view.getId()));
                Intent intent = new Intent(MoneyInfoActivity.this, MenoryInfoFileListActivity.class);
                intent.putExtra("category_card", MoneyInfoActivity.this.isSdCard);
                intent.putExtra("category", fileCategory);
                MoneyInfoActivity.this.startActivity(intent);
                */
            }
        });
    }

    private void setCategoryInfo() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            long j = 0;
            if (FileCategoryHelper.sCategories != null) {
                long j2;
                FileCategory[] fileCategoryArr = FileCategoryHelper.sCategories;
                int length = fileCategoryArr.length;
                int i = 0;
                while (i < length) {
                    FileCategory fileCategory = fileCategoryArr[i];
                    CategoryInfo categoryInfo = this.mFileCagetoryHelper.getCategoryInfos().get(fileCategory);
                    if (fileCategory == FileCategory.Other) {
                        j2 = j;
                    } else {
                        setCategorySize(fileCategory, categoryInfo.size);
                        j2 = categoryInfo.size + j;
                    }
                    i++;
                    j = j2;
                }
                j2 = sdMemoryCardUsed;
                Util.convertStorage(j);
                setCategorySize(FileCategory.Other, j2 - j);
            }
        }
    }

    private void setCategorySize(FileCategory fileCategory, long j) {
        int txtId = 0;
        switch (fileCategory) {
            case Music:
                mMusicLinearLayout = (LinearLayout) findViewById(R.id.category_music_small);
                onItemClick(mMusicLinearLayout);
                txtId = R.id.category_legend_music_small;
                break;
            case Video:
                mVideoLinearLayout = (LinearLayout) findViewById(R.id.category_video_small);
                onItemClick(mVideoLinearLayout);
                txtId = R.id.category_legend_video_small;
                break;
            case Picture:
                mPictureLinearLayout = (LinearLayout) findViewById(R.id.category_picture_small);
                onItemClick(mPictureLinearLayout);
                txtId = R.id.category_legend_picture_small;
                break;
            case Doc:
                mDcLinearLayout = (LinearLayout) findViewById(R.id.category_document_small);
                onItemClick(mDcLinearLayout);
                txtId = R.id.category_legend_document_small;
                break;
            case Apk:
                mApkLinearLayout = (LinearLayout) findViewById(R.id.category_apk_small);
                onItemClick(mApkLinearLayout);
                txtId = R.id.category_legend_apk_small;
                break;
            case Other:
                mOtherLinearLayout = (LinearLayout) findViewById(R.id.category_favorite_small);
                mOtherLinearLayout.setVisibility(View.GONE);
                txtId = R.id.category_legend_other_small;
                break;
            default:
                break;
        }
        if (txtId != 0) {
            setTextView(txtId, Util.convertStorage(j));
        }
    }

    private void setTextView(int i, String str) {
        ((TextView) findViewById(i)).setText(str);
    }

    @Override
	public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
	protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_cricle_progress);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayShowHomeEnabled(false);
        infoAvailable = (TextView) findViewById(R.id.info_available);
        infoMemory = (TextView) findViewById(R.id.info_memory);
        sdMemoryCardTotalss = getIntent().getLongExtra("SDMenoryCardTotalss", 0);
        sdMemoryCardUsedss = getIntent().getLongExtra("SDMenoryCardUsedss", 0);
        sdMemoryCardTotal = getIntent().getLongExtra("SDMenoryCardTotal", 0);
        sdMemoryCardUsed = getIntent().getLongExtra("SDMenoryCardUsed", 0);
        sdMemoryCardFree = getIntent().getLongExtra("SDMenoryCardFree", 0);
        isSdCard = getIntent().getIntExtra("IsSDCard", 1);
        if (isSdCard == 1) {
            setTitle(getResources().getString(R.string.sd_info_storage));
            infoAvailable.setText(getResources().getString(R.string.sd_card_available, new Object[]{Util.convertStorage(sdMemoryCardFree)}));
            infoMemory.setText(getResources().getString(R.string.sd_card_size, new Object[]{Util.convertStorage(sdMemoryCardTotal)}));
        } else {
        	infoAvailable.setText(getResources().getString(R.string.memory_available, new Object[]{Util.convertStorage(sdMemoryCardFree)}));
        	infoMemory.setText(getResources().getString(R.string.memory_size, new Object[]{Util.convertStorage(sdMemoryCardTotal)}));
            setTitle(getResources().getString(R.string.interior_info_storage));
        }
        mFileCagetoryHelper = new FileCategoryHelper(this);
        refreshCategoryInfo();
        mRoundProgressBar3 = (RoundProgressBar) findViewById(R.id.roundProgressBar3);
        mRoundProgressBar3.setMax(sdMemoryCardTotalss, sdMemoryCardTotal, sdMemoryCardFree, isSdCard, true);
        new Thread(new Runnable() {
            @Override
			public void run() {
                while (progress <= sdMemoryCardUsedss) {
                    progress = progress + 10;
                    mRoundProgressBar3.setProgress(progress);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        onBackPressed();
        return true;
    }

    @Override
	public void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);
    }

    @Override
	protected void onResume() {
        super.onResume();
        //MobclickAgent.onResume(this);
        refreshCategoryInfo();
    }

    public void refreshCategoryInfo() {
        mRefreshCategoryInfoTask = new AsyncTask() {
            @Override
			protected Object doInBackground(Object... objArr) {
            	mFileCagetoryHelper.refreshCategoryInfo(isSdCard);
                return null;
            }

            @Override
			protected void onPostExecute(Object obj) {
                setCategoryInfo();
            }
        };
        mRefreshCategoryInfoTask.execute(new Void[0]);
    }
}
