package com.dami.fileexplorer.xdja.business;

import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.xdja.safekeyservice.jarv2.SecurityConstants;
import com.xdja.safekeyservice.jarv2.SecuritySDKManager;
import com.xdja.scservice_domain.encrypt.PlainDataBean;
import com.xdja.sks.IEncDecListener;
import com.dami.fileexplorer.xdja.utils.CommonUtils;
import com.dami.fileexplorer.xdja.utils.LogUtil;

import java.io.File;

/**
 * Created by Administrator on 2016/12/8.
 */

public class EncDecBusiness {

    private SecuritySDKManager securitySDKManager;

    public EncDecBusiness() {
        securitySDKManager = SecuritySDKManager.getInstance();
    }


    /**
     * 加密普通消息
     *
     * @param groupId   待加密的SGROUP id
     * @param encSource 待加密的内容，不可以为空，否则CKMS业务会报错误
     * @return 加密有的byte[]
     */
    public byte[] encData(String groupId, String encSource) {
        if (!TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(encSource)) {
            if (securitySDKManager == null) {
                securitySDKManager = SecuritySDKManager.getInstance();
                LogUtil.getUtils().e("加密消息内部");
            }
            byte[] result;
            try {
                result = securitySDKManager.encryptData(CommonUtils.currentAccount, groupId, SecurityConstants.ENCRYPT_TYPE_STANDARD, encSource.getBytes());
            } catch (SecurityException ex) {
                LogUtil.getUtils().e("SecurityException = " + ex.toString());
                return null;
            }

            return result;
        } else {
            return null;
        }
    }

    /**
     * 解密普通消息
     *
     * @param decSource 待解密的内容
     * @return  解密后的字符串
     */
    public String decData(byte[] decSource) {

        PlainDataBean bean;
        try {
            bean = securitySDKManager.decryptData(CommonUtils.currentAccount, decSource);
        } catch (SecurityException ex) {
            LogUtil.getUtils().e("SecurityException = " + ex.toString());
            return null;
        }
        byte[] source = bean.getPlainData();
        LogUtil.getUtils().e("decRes = " + (new String(source)).toString());
        return (new String(source)).toString();
    }


    public void encFile(String groupID, String sourceFile, String encFile) {
        LogUtil.getUtils().e("待加密路径sourceFile: " + sourceFile);
        File fileFis = new File(sourceFile);
        File fileFos = new File(encFile);
        Log.d("william","william fileFis.getAbsolutePath() is:"+fileFis.getAbsolutePath());
        Log.d("william","william fileFos.getAbsolutePath() is:"+fileFos.getAbsolutePath());
        securitySDKManager.encryptFile(CommonUtils.currentAccount, groupID, fileFis.getAbsolutePath(), fileFos.getAbsolutePath(), new IEncDecListener() {
            @Override
            public void onOperStart() throws RemoteException {

            }

            @Override
            public void onOperProgress(long l, long l1) throws RemoteException {

            }

            @Override
            public void onOperComplete(int i) throws RemoteException {
                //该方法参数“i”标识接口执行结果：0表示加密文件成功，否则表示执行出错，该值表示错误码
                LogUtil.getUtils().e("加密文件完毕");
            }

            @Override
            public IBinder asBinder() {
                return null;
            }
        });
    }

    public void decFile(String sourceFile, String decFile) {
        File fileFis = new File(sourceFile);
        File fileFos = new File(decFile);
        securitySDKManager.decryptFile(CommonUtils.currentAccount, fileFos.getAbsolutePath(), fileFis.getAbsolutePath(), new IEncDecListener() {
            @Override
            public void onOperStart() throws RemoteException {

            }

            @Override
            public void onOperProgress(long l, long l1) throws RemoteException {

            }

            @Override
            public void onOperComplete(int i) throws RemoteException {
                //该方法参数“i”标识接口执行结果：0表示解密文件成功，否则表示执行出错，该值表示错误码
                LogUtil.getUtils().e("解密文件完毕");
            }

            @Override
            public IBinder asBinder() {
                return null;
            }
        });
    }
}
