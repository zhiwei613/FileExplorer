package com.dami.fileexplorer.xdja.business;

import android.content.Context;

import com.xdja.safecenter.ckms.opcode.OpCodeFactory;
import com.xdja.safekeyservice.jarv2.EntityManager;
import com.xdja.safekeyservice.jarv2.SecurityGroupManager;
import com.dami.fileexplorer.xdja.bean.QueryResultBean;
import com.dami.fileexplorer.xdja.utils.LogUtil;
import com.dami.fileexplorer.xdja.utils.SignatureOpCode;
import com.dami.fileexplorer.xdja.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/7.
 */

public class SGroupManagerBusiness {

    private SecurityGroupManager securityGroupManager;
    private Context context;

    public SGroupManagerBusiness(Context context){
        securityGroupManager = SecurityGroupManager.getInstance();
        this.context = context;
    }


    public int createSGroup(List<String> entitiesList, String groupId, String currentEntity){
        String[] entities = new String[entitiesList.size()];
        for (int i = 0; i < entitiesList.size(); i++) {
            entities[i] = entitiesList.get(i);
        }

        int result;
        String opCode = OpCodeFactory.Coder().createGroup(getDeviceId(), groupId, currentEntity, entities);
        JSONObject createSGroup = securityGroupManager.createSGroup(currentEntity, groupId, entitiesList, SignatureOpCode.getSignatureOfOpCode(opCode));

        try {
            int ret_code = createSGroup.getInt("ret_code");
            if(ret_code == 0){
                LogUtil.getUtils().e("创建群组成功");
                ToastUtils.makeText(context, "创建SGROUP成功");
                result = 0;
            } else {
                String errMsg = createSGroup.getString("err_msg");
                LogUtil.getUtils().e("创建群组失败 错误代码 = " + ret_code + " 错误原因 : " + errMsg);
                //ToastUtils.makeText(context, " 创建群组失败错误原因 : " + errMsg);
                result = ret_code;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return result;
    }

    public int addEntityToSGroup(String groupId, String currentEntity, List<String> entitiesList){
        String[] entities = new String[entitiesList.size()];
        for (int i = 0; i < entitiesList.size(); i++) {
            entities[i] = entitiesList.get(i);
        }

        String opCode = OpCodeFactory.Coder().addEntity(getDeviceId(), groupId, currentEntity, entities);

        JSONObject addEntities = securityGroupManager.addEntities(currentEntity, entitiesList, groupId, SignatureOpCode.getSignatureOfOpCode(opCode));
        int ret_code;
        try {
            ret_code = addEntities.getInt("ret_code");
            if(ret_code == 0){
                LogUtil.getUtils().e("添加群成员成功");
                //ToastUtils.makeText(context, "添加群成员成功");
            } else {
                String errMsg = addEntities.getString("err_msg");
                LogUtil.getUtils().e("添加群成员失败 错误代码 = " + ret_code + " 错误原因 : " + errMsg);
                //ToastUtils.makeText(context, "添加群成员失败错误原因 : " + errMsg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }

    public int rvEntityFromSGroup(String groupId, String currentEntity, List<String> entitiesList){
        String[] entities = new String[entitiesList.size()];
        for (int i = 0; i < entitiesList.size(); i++) {
            entities[i] = entitiesList.get(i);
        }

        String opCode = OpCodeFactory.Coder().removeEntity(getDeviceId(), groupId, currentEntity, entities);

        JSONObject removeEntities = securityGroupManager.removeEntities(currentEntity, entitiesList, groupId, SignatureOpCode.getSignatureOfOpCode(opCode));

        int ret_code;
        try {
            ret_code = removeEntities.getInt("ret_code");
            if(ret_code == 0){
                LogUtil.getUtils().e("删除群成员成功");
                ToastUtils.makeText(context, "删除群成员成功");
            } else {
                String errMsg = removeEntities.getString("err_msg");
                LogUtil.getUtils().e("删除群成员失败 错误代码 = " + ret_code + " 错误原因 : " + errMsg);
                ToastUtils.makeText(context, "删除群成员失败错误原因 : " + errMsg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }

    public QueryResultBean getEntityFromSGroup(String groupId){
        JSONObject getEntities = securityGroupManager.getEntities(groupId);
        List<String> entities = new ArrayList<>();
        QueryResultBean bean = new QueryResultBean();
        try {
            int ret_code = getEntities.getInt("ret_code");
            if(ret_code == 0){
                JSONArray result = getEntities.getJSONArray("result");
                for (int i = 0; i < result.length() ; i++) {
                    LogUtil.getUtils().d(result.get(i));
                    entities.add(result.getString(i));
                }
                ToastUtils.makeText(context, "获取群成员成功");
                LogUtil.getUtils().e("获取群成员成功");
            } else {
                String errMsg = getEntities.getString("err_msg");
                LogUtil.getUtils().e("获取群成员失败 错误代码 = " + ret_code + " 错误原因 : " + errMsg);
//                ToastUtils.makeText(context, "获取群成员失败 错误原因 : " + errMsg);
            }
            bean.setEntities(entities);
            bean.setCode(ret_code);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bean;
    }

    public void getSGroup(){
        List<String> entitiesList = new ArrayList<>();
        entitiesList.add("123456");
        entitiesList.add("a123456");
        entitiesList.add("789123");
        JSONObject getSGroup = securityGroupManager.getSGroups(entitiesList);
        try {
            int ret_code = getSGroup.getInt("ret_code");
            if(ret_code == 0){
                JSONObject result = getSGroup.getJSONObject("result");
                LogUtil.getUtils().d(result.get(entitiesList.get(0)));
                LogUtil.getUtils().d(result.get(entitiesList.get(1)));
//                LogUtil.getUtils().d(result.get(entitiesList.get(2)));
                LogUtil.getUtils().e("获取指定entity对应的群信息成功");
                ToastUtils.makeText(context, "获取指定entity对应的群信息");
            } else {
                String errMsg = getSGroup.getString("err_msg");
                LogUtil.getUtils().e("获取指定entity对应的群信息失败 错误代码 = " + ret_code + " 错误原因 : " + errMsg);
                ToastUtils.makeText(context, "获取指定entity对应的群信息失败错误原因 : " + errMsg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int destroySGroup(String groupID, String currentEntity){

        String opCode = OpCodeFactory.Coder().destroyGroup(getDeviceId(), groupID, currentEntity);
        JSONObject destroy = securityGroupManager.destroy(currentEntity, groupID, SignatureOpCode.getSignatureOfOpCode(opCode));
        int ret_code;
        try {
            ret_code = destroy.getInt("ret_code");
            if(ret_code == 0){
                LogUtil.getUtils().e("销毁群成功");
                ToastUtils.makeText(context, "销毁群成功");
            } else {
                String errMsg = destroy.getString("err_msg");
                LogUtil.getUtils().e("销毁群失败 错误代码 = " + ret_code + " 错误原因 : " + errMsg);
                ToastUtils.makeText(context, "销毁群失败错误原因 : " + errMsg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }


    public String getDeviceId() {
        JSONObject deviceIDObj = EntityManager.getInstance().getDeviceID();
        //设备id
        String deviceID = null;
        try {
            int ret_code = deviceIDObj.getInt("ret_code");
            LogUtil.getUtils().e("ret_code = " + ret_code);
            if (ret_code == 0) {
                JSONObject device_id = deviceIDObj.getJSONObject("result");
                deviceID = device_id.getString("device_id");
                LogUtil.getUtils().d("deviceID = " + deviceID);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return deviceID;
    }

}
