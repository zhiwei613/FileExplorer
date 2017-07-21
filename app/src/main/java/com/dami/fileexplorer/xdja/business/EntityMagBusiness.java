package com.dami.fileexplorer.xdja.business;

import com.xdja.safecenter.ckms.opcode.OpCodeFactory;
import com.xdja.safekeyservice.jarv2.EntityManager;
import com.dami.fileexplorer.xdja.bean.RequestBean;
import com.dami.fileexplorer.xdja.utils.CommonUtils;
import com.dami.fileexplorer.xdja.utils.LogUtil;
import com.dami.fileexplorer.xdja.utils.SignatureOpCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class EntityMagBusiness {

    private EntityManager entityManager;

    public EntityMagBusiness() {
        entityManager = EntityManager.getInstance();
    }


    /**
     * 获取设备标识
     * @return  设备标识的字符串
     */
    public String getDeviceId() {
        //获取设备标识
        JSONObject deviceIDObj = entityManager.getDeviceID();

        //设备标识
        String deviceID;
        try {
            int ret_code = deviceIDObj.getInt("ret_code");
            LogUtil.getUtils().e("ret_code = " + ret_code);
            if (ret_code == 0) {
                JSONObject device_id = deviceIDObj.getJSONObject("result");
                deviceID = device_id.getString("device_id");
                LogUtil.getUtils().d("deviceID = " + deviceID);
            } else {
                LogUtil.getUtils().e("错误代码：" + ret_code + " 错误信息： " + deviceIDObj.getString("err_msg"));
                deviceID = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return deviceID;
    }

    /**
     * 用指定的账号创建entity
     * @param account entity对应的账号
     * @return  创建结果0 创建成功 -1发生异常  其他错误码详见开发指南错误码
     */
    public int createEntity(String account) {
        //生成业务操作码
        String opcodes = OpCodeFactory.Coder().createEntity(getDeviceId(), account);

        //对业务操作码进行签名
        String signOpCode = SignatureOpCode.getSignatureOfOpCode(opcodes);

        //创建entity
        JSONObject obj = entityManager.create(account, signOpCode);
        int ret_code;
        try {
            ret_code = obj.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().d("创建entity成功");
            } else {
                String errorMsg = obj.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }

    /**
     * 获取指定设备关联的entity列表(该方法根据示例代码的业务场景暂时没有用到，第三方业务可根据实际情况使用)
     * @param deviceID 需要查询的指定的设备标识
     * @return 关联的entity列表
     */
    public List<String> getEntities(String deviceID) {
        List<String> devices = new ArrayList<>();
        devices.add(deviceID);
        //CKMS对应的查询接口
        JSONObject getEntities = entityManager.getEntities(devices);

        List<String> entities = new ArrayList<>();
        try {
            int ret_code = getEntities.getInt("ret_code");
            if (ret_code == 0) {
                JSONObject result = getEntities.getJSONObject("result");

                String array = result.getString(devices.get(0));
                JSONArray jsonArray = new JSONArray(array);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String entity = jsonArray.getString(i);
                    LogUtil.getUtils().e("entity = " + entity);
                    entities.add(entity);
                }
            } else {
                String errorMsg = getEntities.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return entities;
    }


    /**
     * 强制把当前设备关联到指定entity
     * @param account  需要关联的entity
     * @return 关联结果 0 关联成功  -1 出现异常  其他错误码意义详见开发指南错误码
     */
    public int addDeviceForcibly(String account) {
        //生成业务操作码
        String opcode = OpCodeFactory.Coder().forceAddDevice(getDeviceId(), account);

        //对业务操作码进行签名
        String signOpCode = SignatureOpCode.getSignatureOfOpCode(opcode);

        //强制把当前设备关联到指定entity
        JSONObject forcibly = entityManager.addDeviceForcibly(account, signOpCode);
        int ret_code;
        try {
            ret_code = forcibly.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().e("强制添加成功" + ret_code);
            } else {
                String errorMsg = forcibly.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }


    /**
     * 向服务器发送添加到指定entity的请求
     * @param account  需要添加到的entity
     * @return  请求码
     */
    public String sendAddingDeviceRequest(String account) {
        JSONObject sendRequest = entityManager.getAddingDeviceRequest(account);
        try {
            int ret_code = sendRequest.getInt("ret_code");
            if (ret_code == 0) {
                JSONObject result = sendRequest.getJSONObject("result");
                return result.getString("adding_dev_req_id");
            } else {
                String errorMsg = sendRequest.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 检测并获取设备添加请求信息
     * @param reqId  添加请求码
     * @return 返回添加设备的相关信息，返回值可根据具体业务做调整
     */
    public RequestBean checkAdding(String reqId) {
        JSONObject checkAdding = entityManager.checkAddingDeviceReq(reqId);
        try {
            int ret_code = checkAdding.getInt("ret_code");
            if (ret_code == 0) {
                JSONObject result = checkAdding.getJSONObject("result");
                String appId = result.getString("app_id");
                String destEntity = result.getString("dest_entity");
                String addingDevReq = result.getString("adding_dev_req");
                String addDevReqId = result.getString("add_dev_req_id");//发起请求码
                String addDevId = result.getString("add_dev_id");//发起请求的ID


                RequestBean bean = new RequestBean();
                bean.setAppId(appId);
                bean.setAddDevId(addDevId);
                bean.setAddDevReqId(addDevReqId);
                bean.setDestEntity(destEntity);
                bean.setAddingDevReq(addingDevReq);
                LogUtil.getUtils().e("appid： " + appId + " destEntity = " + destEntity
                        + "addingDevReq = " + addingDevReq + " addDevReqId = " + addDevReqId + " addDevId = " + addDevId);
                return bean;
            } else {
                String errorMsg = checkAdding.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
                return null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 向指定entity下添加安全设备
     *
     * @param currentEntity  当前entity
     * @param reqId  请求添加的id
     * @param deviceId  请求的添加的设备标识
     */
    public boolean addDeviceToEntity(String currentEntity, String reqId, String deviceId) {

        //根据请求码可以查询请求相关信息，包括设备ID，但在执行添加设备到当前entity时查询接口不是必须的
        //传递reqId和deviceId第三方应用可以直接将内容传递也可通过查询获取
        RequestBean bean = checkAdding(reqId);
        if(bean != null){
            String reqID = bean.getAddingDevReq();
            String deviceID = bean.getAddDevId();
            LogUtil.getUtils().e("reqId = " + reqID + " deviceId = " + deviceID);
            LogUtil.getUtils().e("reqId = " + reqId + " deviceId = " + deviceId);
        }

        //生成业务操作码
        String opcode = OpCodeFactory.Coder().addDevice(getDeviceId(), currentEntity, deviceId);

        //对业务操作码签名
        String signOpCode = SignatureOpCode.getSignatureOfOpCode(opcode);

        //授权添加
        JSONObject addDevice = entityManager.addDevice(currentEntity, reqId, signOpCode);
        try {
            int ret_code = addDevice.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().e("添加设备成功 ");
                return true;
            } else {
                String errorMsg = addDevice.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从entity下移除设备
     * @param inputRvDevID 待移除的设备，根据具体场景如果要移除多个设备，该参数可以修改书string[]
     * @return 移除结果 0 移除成功  -1 有异常  其他具体参见开发指南错误码
     */
    public int removeDevice(String inputRvDevID) {
        String deviceId[] = {inputRvDevID};

        //生成业务操作码
        String opCode = OpCodeFactory.Coder().removeDevice(getDeviceId(), CommonUtils.currentAccount, deviceId);

        //对业务操作码进行签名
        String signOpCode = SignatureOpCode.getSignatureOfOpCode(opCode);

        //
        JSONObject remove = entityManager.removeDevice(CommonUtils.currentAccount, inputRvDevID, signOpCode);
        int ret_code;
        try {
            ret_code = remove.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().e("删除设备成功 ");
            } else {

                String errorMsg = remove.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }

    /**
     * 判断当前设备是否已经添加到指定的entity
     * @param account  需要检测的entity
     * @return  0 当前设备已于指定entity关联， -1，当前设备跟entity没有关联，其他详见开发指南对应的错误码
     */
    public int isCurrentDevAdded2Entity(String account) {
        JSONObject isDevAdd2Entity = entityManager.isCurrentDevAdded2Entity(account);
        int result;
        try {
            int ret_code = isDevAdd2Entity.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().e("当前设备跟指定的账号已关联");
            } else {
                String errorMsg = isDevAdd2Entity.getString("err_msg");
                if (ret_code == -1) {
                    LogUtil.getUtils().e("当前设备跟账号没有关联 ");
                } else {
                    LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
                }
            }
            result = ret_code;
        } catch (JSONException e) {
            e.printStackTrace();
            return -2;
        }
        return result;
    }

    /**
     * 获取指定entity下的所有的安全设备信息
     * @param entity
     * @return
     */
    public List<String> getDeviceFromEntity(String entity) {
        List<String> entities = new ArrayList<>();
        entities.add(entity);

        List<String> deviceList = new ArrayList<>();
        JSONObject getDevice = entityManager.getDevices(entities);
        try {

            // 如果传入的entity list中有一个绑定了设备，则返回数据中只包含了有设备信息entity
            // 如果传入的entity list 都没有绑定设备，则会返回错误“40015”，表示为找到任何设备
            int ret_code = getDevice.getInt("ret_code");
            LogUtil.getUtils().e("william"+" getDeviceFromEntity ret_code is:"+ret_code);
            if (ret_code == 0) {
                JSONObject result = getDevice.getJSONObject("result");
                for (int m = 0; m < entities.size(); m++) {
                    String entity1 = result.getString(entities.get(m));
                    JSONArray entity1Array = new JSONArray(entity1);
                    for (int i = 0; i < entity1Array.length(); i++) {
                        JSONObject entityDev = entity1Array.getJSONObject(i);
                        deviceList.add(entityDev.getString("device_id"));
                    }
                }
            } else if(ret_code == 40015){ //都没有关联的情况下第三方业务根据自身业务做相应的处理
                LogUtil.getUtils().d("查询的账号跟任何设备都没有关联");
            } else {
                String errorMsg = getDevice.getString("err_msg");
                LogUtil.getUtils().e("错误代码：" + ret_code + " 错误信息： " + errorMsg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return deviceList;
    }

    /**
     * 销毁当前设备正在使用的entity
     * @return 销毁的结果
     */
    public int destroyEntity() {
        //获取业务操作码
        String opcode = OpCodeFactory.Coder().destroyEntity(getDeviceId(), CommonUtils.currentAccount);

        //对业务操作码签名
        String signOpcode = SignatureOpCode.getSignatureOfOpCode(opcode);

        //执行销毁的操作
        JSONObject destroyEntity = entityManager.destroy(CommonUtils.currentAccount, signOpcode);
        int ret_code;
        try {
            ret_code = destroyEntity.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().e("销毁entity成功");
            } else {

                String errorMsg = destroyEntity.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }


    /**
     * 发送获取同步解密能力请求
     * @return 返回请求id
     */
    public String sendSyncPowerRequest() {

        JSONObject sendSync = entityManager.getSyncPowerRequest(CommonUtils.currentAccount);
        try {
            int ret_code = sendSync.getInt("ret_code");
            if (ret_code == 0) {

                JSONObject result = sendSync.getJSONObject("result");
                String reqId = result.getString("syncing_power_req_id");
                LogUtil.getUtils().e("发起同步的请求id ： " + reqId);
                return reqId;
            } else {

                String errorMsg = sendSync.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 授权解密能力
     * @param reqId  发起方发送的请求码
     * @param fromDeviceID  发送方的设备标识
     * @return 授权解密能力结果
     */
    public int agreeSyncPower(String reqId, String fromDeviceID) {

        //获取业务操作码
        String opcode = OpCodeFactory.Coder().syncSecPower(getDeviceId(), CommonUtils.currentAccount, fromDeviceID);

        //业务操作码签名
        String signOpcode = SignatureOpCode.getSignatureOfOpCode(opcode);

        //向发起者同步解密能力
        JSONObject agreeSec = entityManager.syncSecPower(CommonUtils.currentAccount, reqId, signOpcode);

        int ret_code;
        try {
            ret_code = agreeSec.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().e("同意同步解密能力 ： ");
            } else {

                String errorMsg = agreeSec.getString("err_msg");
                LogUtil.getUtils().d("错误代码：" + ret_code + " 错误信息： " + errorMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;
    }


}
