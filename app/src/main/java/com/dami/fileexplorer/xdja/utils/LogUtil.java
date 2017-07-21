package com.dami.fileexplorer.xdja.utils;

import android.util.Log;

public class LogUtil {
    private static boolean logFlag = true;

    public final static String tag = "CKMS";
    private final static int logLevel = Log.VERBOSE;
    private final String mClassName;

    private static LogUtil logger;

    private LogUtil(String name) {
        mClassName = name;
    }


    public static LogUtil getUtils() {
        if (logger == null) {
            logger = new LogUtil(tag);
        }
        return logger;
    }

    /**
     * 获取当前方法路径名
     *
     * @return 当前方法路径名
     * @作者 hkb
     * @since 2014年11月12日 下午5:13:27
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return mClassName + "[ " + Thread.currentThread().getName() + ": " + st.getFileName() + ":"
                    + st.getLineNumber() + " " + st.getMethodName() + " ]";
        }
        return null;
    }

    /**
     * Info 级别日志
     *
     * @param str
     * @作者 hkb
     * @since 2014年11月12日 下午5:14:50
     */
    public void i(Object str) {
        if (logFlag) {
            if (logLevel <= Log.INFO) {
                String name = getFunctionName();
                if (name != null) {
                    Log.i(tag, name + " - " + str);
                } else {
                    Log.i(tag, str.toString());
                }
            }
        }

    }

    /**
     * Debug 级别日志
     *
     * @param str
     * @作者 hkb
     * @since 2014年11月12日 下午5:15:27
     */
    public void d(Object str) {
        if (logFlag) {
            if (logLevel <= Log.DEBUG) {
                String name = getFunctionName();
                if (name != null) {
                    Log.d(tag, name + " - " + str);
                } else {
                    Log.d(tag, str.toString());
                }
            }
        }
    }

    /**
     * Verbose 级别日志
     *
     * @param str
     * @作者 hkb
     * @since 2014年11月12日 下午5:16:00
     */
    public void v(Object str) {
        if (logFlag) {
            if (logLevel <= Log.VERBOSE) {
                String name = getFunctionName();
                if (name != null) {
                    Log.v(tag, name + " - " + str);
                } else {
                    Log.v(tag, str.toString());
                }
            }
        }
    }

    /**
     * Warn 级别日志
     *
     * @param str
     * @作者 hkb
     * @since 2014年11月12日 下午5:16:07
     */
    public void w(Object str) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                if (name != null) {
                    Log.w(tag, name + " - " + str);
                } else {
                    Log.w(tag, str.toString());
                }
            }
        }
    }

    /**
     * Error 级别日志
     *
     * @param str
     * @作者 hkb
     * @since 2014年11月12日 下午5:16:55
     */
    public void e(Object str) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                if (name != null) {
                    Log.e(tag, name + " - " + str);
                } else {
                    Log.e(tag, str.toString());
                }
            }
        }
    }

    /**
     * Error 异常信息日志
     *
     * @param ex
     * @作者 hkb
     * @since 2014年11月12日 下午5:17:14
     */
    public void e(Exception ex) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                Log.e(tag, "error", ex);
            }
        }
    }

    /**
     * Error 异常信息日志
     *
     * @param log
     * @param tr
     * @作者 hkb
     * @since 2014年11月12日 下午5:17:14
     */
    public void e(String log, Throwable tr) {
        if (logFlag) {
            String line = getFunctionName();
            Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] " + log
                    + "\n", tr);
        }
    }
}
