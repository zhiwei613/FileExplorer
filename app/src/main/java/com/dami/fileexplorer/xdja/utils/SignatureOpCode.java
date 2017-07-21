package com.dami.fileexplorer.xdja.utils;

import com.xdja.cssp.was.open.auth.sdk.OpenAuthUtil;

/**
 * Created by Administrator on 2016/12/5.
 */

public class SignatureOpCode {
    /**
     *
     * @param sourceData  签名源数据
     * @return
     * @throws Exception
     */
    public static String getSignatureOfOpCode(String sourceData){
        OpenAuthUtil openAuthUtil = new OpenAuthUtil();

        //secretKey由信大捷安颁发给第三方应用
        String secretKey = "5709f87ffe9bbd5e7409dbb19dae3d0c";
        String result = null;
        try {
            result = openAuthUtil.getBase64SignatureBySecretKey(secretKey, sourceData);

            LogUtil.getUtils().d("签名结果：" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
