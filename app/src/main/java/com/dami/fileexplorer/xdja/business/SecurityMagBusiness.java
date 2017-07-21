package com.dami.fileexplorer.xdja.business;

import android.content.Context;
import android.util.Log;

import com.xdja.safekeyservice.jarv2.SecurityConstants;
import com.xdja.safekeyservice.jarv2.SecuritySDKManager;
import com.xdja.safekeyservice.jarv2.bean.IVerifyPinResult;
import com.dami.fileexplorer.xdja.interfaces.VerifySafeKeyResult;
import com.dami.fileexplorer.xdja.utils.LogUtil;
import com.dami.fileexplorer.xdja.utils.SignatureOpCode;
import com.dami.fileexplorer.xdja.business.EntityGroupHandleBusiness;

import org.json.JSONException;
import org.json.JSONObject;



public class SecurityMagBusiness {

    private final int SAFEKEY_INEXISTENCE = 50008;
    private final int RET_OK = 0;
    private SecuritySDKManager securitySDKManager;
    private Context context;
    private VerifySafeKeyResult result;
    private EntityGroupHandleBusiness entityGroupHandle;
    private int initRet = -1;

    public SecurityMagBusiness(Context context, VerifySafeKeyResult result) {
        securitySDKManager = SecuritySDKManager.getInstance();
        this.context = context;
        this.result = result;
        entityGroupHandle = new EntityGroupHandleBusiness(context);
    }

    /**
     * 启动安全口令检验界面
     * @return 启动安全口令结果 0 验证成功 -1出现异常  其他错误码详见开发指南错误码，第三方应用可根据实际情况确定返回值
     */
    public int startVerifySafeKey() {

        JSONObject startVerifyPinObj = securitySDKManager.startVerifyPinActivity(context, new IVerifyPinResult() {
                    @Override
                    public void onResult(int i, String s) {
                        LogUtil.getUtils().e("i = " + i + " s = " + s);
                        if (i == RET_OK) {
                            LogUtil.getUtils().e("安全口令验证成功");
                            //PIN码验证成功需要重新初始化SecuritySDKManager,因为只有初始化SecuritySDKManager失败的情况下才应该调用输入PIN码界面
                            initSecuritySDKManager();
                        } else {
                            LogUtil.getUtils().e("安全口令验证失败原因: " + s);
                            result.safeKeyVerifyFail();
                        }
                    }
                }
        );

        int ret_code;
        try {
            ret_code = startVerifyPinObj.getInt("ret_code");
            if (ret_code == 0) {
                LogUtil.getUtils().d("验证安全口令成功");
            } else {
                LogUtil.getUtils().e("错误代码：" + ret_code + " 错误信息： " + startVerifyPinObj.getString("err_msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
        return ret_code;

    }


    private String signatureOfChallenge() {
        try {
            return SignatureOpCode.getSignatureOfOpCode(getChallenge());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化CKMS
     */
    public void initSecuritySDKManager() {
        try {
            //1、获取挑战值
            String challenge = getChallenge();
            //2、对挑战值进行签名
            String sigChallenge = SignatureOpCode.getSignatureOfOpCode(challenge);

            securitySDKManager.init(context, sigChallenge, new SecuritySDKManager.InitCallBack() {
                @Override
                public void onInitComplete(JSONObject jsonObject) {
                    try {
                        int ret_code = jsonObject.getInt("ret_code");
                        Log.d("william","william initSecuritySDKManager ret_code is:"+ret_code);
                        if (ret_code == RET_OK) {
                            LogUtil.getUtils().e("初始化SecuritySDKManger成功");
                            //entityGroupHandle.initEntityAndGroup();
                            setInitResult(ret_code);
                        } else {
                            String errorMsg = jsonObject.getString("err_msg");
                            LogUtil.getUtils().d("errorMsg = " + errorMsg);

                            //ret_code == 50008 安全口令不存在，需要调用输入安全口令界面;其他错误码意义可参见开发指南
                            if (ret_code == SAFEKEY_INEXISTENCE) {
                                //启动安全口令检验界面
                                startVerifySafeKey();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
            LogUtil.getUtils().e("initRet = " + initRet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getChallenge() {
        JSONObject challenge = securitySDKManager.getChallenge(context);
        Log.d("william","william getChallenge challenge is:"+challenge);
        try {
            int challenge_ret_code = challenge.getInt("ret_code");
            Log.d("william","william getChallenge challenge_ret_code is:"+challenge_ret_code);
            if (challenge_ret_code == RET_OK) {
                JSONObject jsonResult = challenge.getJSONObject("result");
                String challengeStr = jsonResult.getString("challenge");
                LogUtil.getUtils().d("获取挑战值成功");
                return challengeStr;
            } else {
                String errMsg = challenge.getString("err_msg");
                LogUtil.getUtils().d("获取挑战值错误代码：" + challenge_ret_code + "获取挑战值失败原因：" + errMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void refresh() {
        //续期操作需要在有效期内操作，否则ticket超期，需要重新初始化获取新的ticket
        JSONObject refresh = securitySDKManager.refresh();
        try {
            int code = refresh.getInt("ret_code");
            if (code == RET_OK) {

            } else {
                LogUtil.getUtils().d("续租失败错误代码：" + code);
                String errMsg = refresh.getString("err_msg");
                LogUtil.getUtils().d("续租失败原因：" + errMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void sdkRelease() {
        securitySDKManager.release();
    }


    public void sdkStatus() {
        JSONObject status = securitySDKManager.getStatus(context, SecurityConstants.FLAG_VERSION);
        try {
            int ret_code = status.getInt("ret_code");
            if (ret_code == RET_OK) {
                JSONObject jsonResult = status.getJSONObject("result");
                String flagVersionStatus = jsonResult.getString("status" + SecurityConstants.FLAG_VERSION);
                LogUtil.getUtils().d("flagVersionStatus：" + flagVersionStatus);
            } else {
                String errMsg = status.getString("err_msg");
                LogUtil.getUtils().d("获取状态失败代码：" + ret_code + " 获取状态失败：" + errMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getInitResult() {
        Log.e("william","getInitResult ret_code: "+initRet);
        return initRet;
    }

    public void setInitResult(int ret_code) {
        initRet = ret_code;
        Log.e("william","setInitResult ret_code: "+initRet);
    }

}
