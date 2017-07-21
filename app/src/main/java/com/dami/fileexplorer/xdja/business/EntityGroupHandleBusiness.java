package com.dami.fileexplorer.xdja.business;

import android.content.Context;
import android.util.Log;

import com.dami.fileexplorer.xdja.bean.RequestBean;
import com.dami.fileexplorer.xdja.utils.CommonUtils;
import com.dami.fileexplorer.xdja.utils.LogUtil;
import com.dami.fileexplorer.xdja.utils.SignatureOpCode;
import com.xdja.safecenter.ckms.opcode.OpCodeFactory;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.dami.fileexplorer.xdja.business.EntityMagBusiness;
import com.dami.fileexplorer.xdja.business.SGroupManagerBusiness;
import com.dami.fileexplorer.xdja.business.SecurityMagBusiness;

public class EntityGroupHandleBusiness {

    private EntityMagBusiness entityMag;
    private SGroupManagerBusiness sGroupMag;
    private SecurityMagBusiness securityMag;
    private Context context;

    public EntityGroupHandleBusiness(Context context) {
        this.context = context;
    }


    public void initEntityAndGroup(){
        entityMag = new EntityMagBusiness();
        initEntity(CommonUtils.currentAccount);

        sGroupMag = new SGroupManagerBusiness(this.context);
        initGroup(CommonUtils.currentAccount,CommonUtils.groupId);

    }

    private void initEntity(String account) {

        /**增加未检测到安全芯片异常处理**/
        List<String> curEntityHasDevice;

        try{
            //查询输入entity是否已经绑定安全设备
            curEntityHasDevice = entityMag.getDeviceFromEntity(account);
        }catch (Exception e){
            if(e.getMessage().equals("Unknown URI content://com.xdja.scservice.cp.SksProvider")){
                //Toast.makeText(this,"未检测到安全芯片",Toast.LENGTH_SHORT).show();
                Log.e("william"," initEntity : 未检测到安全芯片");
            }
            return;
        }
        //输入的账号跟任何安全设备没有关联,即输入的账号目前还不是一个entity
        if (curEntityHasDevice.size() == 0) {
            int retCode = entityMag.createEntity(account);
            //Log.d("william","william entityMag.createEntity is sucess");
            Log.e("william"," initEntity retCode is:"+retCode);
            if (retCode == 0) {

            } else if(retCode == 106870){
                //访问权限不存在,重新初始化
                //securityMag.initSecuritySDKManager();
            }
            else {
                //错误码对应的意义详见开发指南
                //ToastUtils.makeText(this, "注册账号失败");
            }
        } else {
            //输入的entity跟当前设备已关联走正常登录流程
            //ToastUtils.makeText(this, "当前账号已经注册过，请正常登录");
        }
    }

    public void initGroup(String account, String groupId){

        //String groupIdStr = CommonUtils.getUUID();
        List<String> myGroupEntity = new ArrayList<>();
        myGroupEntity.add(account);
        Log.d("william"," myGroupEntity is:"+myGroupEntity);

        //创建一个人的sgroup
        int result = sGroupMag.createSGroup(myGroupEntity, groupId, account);
        //创建一个人的sgroup成功，第三方应用进行数据维护
        Log.e("william"," initGroup result is:"+result);
        if (result == 0) {
            //Group group = new Group("1234567890", CommonUtils.currentAccount, CommonUtils.currentAccount, 0);
            Log.d("william"," sgroup is create sucess");
        }else if (result == 70515){
            result = sGroupMag.addEntityToSGroup(groupId, account, myGroupEntity);
            Log.e("william"," addEntityToSGroup result is:"+result);
        }
    }


}
