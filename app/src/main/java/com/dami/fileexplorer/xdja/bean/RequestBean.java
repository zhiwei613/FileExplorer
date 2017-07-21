package com.dami.fileexplorer.xdja.bean;

/**
 * Created by Administrator on 2017/1/3.
 */

public class RequestBean {

    String appId ;
    //请求加入的entity
    String destEntity ;
    //添加请求的描述
    String addingDevReq;
    //发起请求码
    String addDevReqId ;
    //发起添加请求的设备ID
    String addDevId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDestEntity() {
        return destEntity;
    }

    public void setDestEntity(String destEntity) {
        this.destEntity = destEntity;
    }

    public String getAddingDevReq() {
        return addingDevReq;
    }

    public void setAddingDevReq(String addingDevReq) {
        this.addingDevReq = addingDevReq;
    }

    public String getAddDevReqId() {
        return addDevReqId;
    }

    public void setAddDevReqId(String addDevReqId) {
        this.addDevReqId = addDevReqId;
    }

    public String getAddDevId() {
        return addDevId;
    }

    public void setAddDevId(String addDevId) {
        this.addDevId = addDevId;
    }
}
