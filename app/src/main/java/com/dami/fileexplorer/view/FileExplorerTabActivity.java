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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.util.Util;

import com.dami.fileexplorer.xdja.bean.Group;
import com.dami.fileexplorer.xdja.business.EntityGroupHandleBusiness;
import com.dami.fileexplorer.xdja.business.EntityMagBusiness;
import com.dami.fileexplorer.xdja.business.SGroupManagerBusiness;
import com.dami.fileexplorer.xdja.business.SecurityMagBusiness;
import com.dami.fileexplorer.xdja.interfaces.VerifySafeKeyResult;
import com.dami.fileexplorer.xdja.utils.CommonUtils;
import com.dami.fileexplorer.xdja.business.DBBusiness;
//import com.dami.fileexplorer.xdja.utils.DBBusiness;
import com.dami.fileexplorer.xdja.utils.ToastUtils;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class FileExplorerTabActivity extends Activity implements VerifySafeKeyResult{
    private static final String INSTANCESTATE_TAB = "tab";
    private static final int DEFAULT_OFFSCREEN_PAGES = 2;
    public ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    ActionMode mActionMode;
    private RadioGroup mTabHost;
    private static RadioButton mTabBtnOne;
    private static RadioButton mTabBtnTwo;
    private static RadioButton mTabBtnThree;
    
    private SecurityMagBusiness securityMag;
    private EntityMagBusiness entityMag;
    private SGroupManagerBusiness sGroupMag;
    private EntityGroupHandleBusiness entityGroupHandle;
    //private final Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pager);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);
        
        mTabHost = (RadioGroup) findViewById(R.id.home_group);
        mTabBtnOne = (RadioButton) findViewById(R.id.home_radio_one);
        mTabBtnTwo = (RadioButton) findViewById(R.id.home_radio_two);
        mTabBtnThree = (RadioButton) findViewById(R.id.home_radio_three);
        
        mTabsAdapter = new TabsAdapter(FileExplorerTabActivity.this, mViewPager);
        mTabsAdapter.addTab(FileCategoryActivity.class, null);
        mTabsAdapter.addTab(FileViewActivity.class, null);
        mTabsAdapter.addTab(ServerControlActivity.class, null);
        
        mTabHost.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.home_radio_one:
					mViewPager.setCurrentItem(0);
					break;
				case R.id.home_radio_two:
					mViewPager.setCurrentItem(1);
					break;
				case R.id.home_radio_three:
					mViewPager.setCurrentItem(2);
					break;		
				default:
					break;
				}
			}
		});
        getDeviceIMEI();
        new MyAsyncTask().execute("start");
        //securityMag = new SecurityMagBusiness(this, this);
        //securityMag.initSecuritySDKManager();

        /*
        entityMag = new EntityMagBusiness();
        register(CommonUtils.currentAccount);
        
        List<String> myGroupEntity = new ArrayList<>();
        myGroupEntity.add(CommonUtils.currentAccount);
        Log.d("william","william myGroupEntity is:"+myGroupEntity);

        sGroupMag = new SGroupManagerBusiness(this);
        int result = sGroupMag.createSGroup(myGroupEntity, "1234567890", CommonUtils.currentAccount);
        if (result == 0) {
        	Log.d("william","william sgroup is create sucess");
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(INSTANCESTATE_TAB, getActionBar().getSelectedNavigationIndex());
        editor.commit();
    } 

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (getActionBar().getSelectedNavigationIndex() == Util.CATEGORY_TAB_INDEX) {
            FileCategoryActivity categoryFragement =(FileCategoryActivity) mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX);
            if (categoryFragement.isHomePage()) {
                reInstantiateCategoryTab();
            } else {
                categoryFragement.setConfigurationChanged(true);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    public void reInstantiateCategoryTab() {
        mTabsAdapter.destroyItem(mViewPager, Util.CATEGORY_TAB_INDEX,
                mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX));
        mTabsAdapter.instantiateItem(mViewPager, Util.CATEGORY_TAB_INDEX);
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) mTabsAdapter
                .getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }

    public interface IBackPressedListener {
        /**
         * 处理back事件。
         * @return True: 表示已经处理; False: 没有处理，让基类处理。
         */
        boolean onBack();
    }

    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public Fragment getFragment(int tabIndex) {
        return mTabsAdapter.getItem(tabIndex);
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab( Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            mTabs.add(info);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            if (info.fragment == null) {
                info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }
            return info.fragment;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        	
        }

        @Override
        public void onPageSelected(int position) {
          
        	switch (position) {
			case 0:
				mTabBtnOne.setChecked(true);
		        mTabBtnTwo.setChecked(false);
		        mTabBtnThree.setChecked(false);
				break;
			case 1:
				mTabBtnOne.setChecked(false);
		        mTabBtnTwo.setChecked(true);
		        mTabBtnThree.setChecked(false);				
				break;
			case 2:
				mTabBtnOne.setChecked(false);
		        mTabBtnTwo.setChecked(false);
		        mTabBtnThree.setChecked(true);				
				break;		

			default:
				break;
			}
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        	
        }

    }

	@Override
	public void safeKeyVerifyFail() {
		// TODO Auto-generated method stub
		
	}
	
    private void register(String account) {

        /**增加未检测到安全芯片异常处理**/
        List<String> curEntityHasDevice;

        try{
            //查询输入entity是否已经绑定安全设备
            curEntityHasDevice = entityMag.getDeviceFromEntity(account);
        }catch (Exception e){
            if(e.getMessage().equals("Unknown URI content://com.xdja.scservice.cp.SksProvider")){
                Toast.makeText(this,"未检测到安全芯片",Toast.LENGTH_SHORT).show();
            }
            return;
        }
        //输入的账号跟任何安全设备没有关联,即输入的账号目前还不是一个entity
        if (curEntityHasDevice.size() == 0) {
            int retCode = entityMag.createEntity(account);
            //Log.d("william","william entityMag.createEntity is sucess");
            Log.d("william","william register retCode is:"+retCode);
            if (retCode == 0) {

            } else if(retCode == 106870){
                //访问权限不存在,重新初始化
                securityMag.initSecuritySDKManager();
            }
            else {
                //错误码对应的意义详见开发指南
                ToastUtils.makeText(this, "注册账号失败");
            }
        } else {
            //输入的entity跟当前设备已关联走正常登录流程
            //ToastUtils.makeText(this, "当前账号已经注册过，请正常登录");
        }
    }

    public String getDeviceIMEI(){
        String imei1 ="";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        TelephonyManager telephonyManager =
                (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null){
            imei1= telephonyManager.getDeviceId(0);
            //imei2 = telephonyManager.getDeviceId(1);
            editor.putString(CommonUtils.KEY_DEFAULT_DEVICE, imei1);
            editor.apply();
        } else {
            imei1 = prefs.getString(CommonUtils.KEY_DEFAULT_DEVICE,"");
        }
        if (imei1 != ""){
            CommonUtils.currentAccount = imei1;
            CommonUtils.groupId = imei1;
        }
        Log.e("william","getDeviceIMEI imei1: "+imei1);
        return imei1;
    }

    public class MyAsyncTask extends AsyncTask<String, Integer, Integer>{

        @Override
        protected Integer doInBackground(String... params) {  //三个点，代表可变参数
            int ret_code = -1;
            try {
                securityMag = new SecurityMagBusiness(FileExplorerTabActivity.this, FileExplorerTabActivity.this);
                securityMag.initSecuritySDKManager();
                for (int i = 0; i < 5 ; i++) {
                    Thread.sleep(1000);
                    ret_code = securityMag.getInitResult();
                    if (ret_code == 0){ break;}
                }
                Log.e("william","doInBackground ret_code: "+ret_code);
            }  catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return ret_code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            int ret= result;
            Log.e("william","onPostExecute ret: "+ret);
            if (ret == 0){
                entityGroupHandle = new EntityGroupHandleBusiness(FileExplorerTabActivity.this);
                entityGroupHandle.initEntityAndGroup();
            }
        }
    }

}
