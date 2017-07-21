package com.dami.fileexplorer.xdja.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/12/12.
 */

public class DbHelper extends SQLiteOpenHelper {
    //private static final String DATABASE_NAME = "ckms_demo";
    private static final String DATABASE_NAME = "encryptpassword";
    private static final int DATABASE_VERSION = 1;
    public static final String PASSWORD_TABLE = "encryptpassword";
    public static final String GROUP_TABLE = "group_table";
    public static final String ACCOUNT_TABLE = "account";
    private String account;
    public DbHelper(Context context, String account) {
        super(context, DATABASE_NAME +".db", null, DATABASE_VERSION);
        this.account = account;
    }
	public DbHelper(Context context) {
        super(context, DATABASE_NAME +".db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+
        //        GROUP_TABLE+ "_" + account +" (_id INTEGER PRIMARY KEY AUTOINCREMENT, groupID TEXT, groupName TEXT, groupMember TEXT, type INTEGER)");
        //sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+
        //        ACCOUNT_TABLE +" (_id INTEGER PRIMARY KEY AUTOINCREMENT, account TEXT)");
		PASSWORD_TABLE +" (_id INTEGER PRIMARY KEY AUTOINCREMENT, accountname TEXT, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
