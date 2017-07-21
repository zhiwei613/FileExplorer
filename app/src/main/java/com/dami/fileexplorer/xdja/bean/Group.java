package com.dami.fileexplorer.xdja.bean;

import android.content.ContentValues;

/**
 * Created by Administrator on 2016/12/19.
 */

public class Group {

    private String groupID;
    private String groupName;
    private String groupMember;
    private int groupType;

    public Group(String groupID, String groupName, String groupMember, int groupType){
        this.groupID = groupID;
        this.groupMember = groupMember;
        this.groupName = groupName;
        this.groupType = groupType;
    }


    public String getID() {
        return groupID;
    }

    public void setID(String mID) {
        this.groupID = mID;
    }

    public String getName() {
        return groupName;
    }

    public void setName(String mName) {
        this.groupName = mName;
    }

    public String getMember() {
        return groupMember;
    }

    public void setMember(String mMember) {
        this.groupMember = mMember;
    }

    public int getType() {
        return groupType;
    }

    public void setType(int mType) {
        this.groupType = mType;
    }

    public ContentValues getContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put("groupID", groupID);
        contentValues.put("groupName", groupName);
        contentValues.put("groupMember", groupMember);
        contentValues.put("type", groupType);
        return contentValues;
    }

}
