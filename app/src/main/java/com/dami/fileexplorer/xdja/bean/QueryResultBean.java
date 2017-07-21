package com.dami.fileexplorer.xdja.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/12/27.
 */

public class QueryResultBean {

    private List<String> entities;
    private int code;

    public List<String> getEntities() {
        return entities;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;

    }
}
