package com.dami.fileexplorer.xdja.business;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.dami.fileexplorer.xdja.utils.CommonUtils;
import com.dami.fileexplorer.xdja.utils.DbHelper;
import com.dami.fileexplorer.xdja.utils.LogUtil;

import static com.dami.fileexplorer.xdja.utils.DbHelper.PASSWORD_TABLE;;
/**
 * Created by Administrator on 2016/12/12.
 */

public class DBBusiness {

    protected static SQLiteDatabase db;

    private DbHelper dbHelper;

    public DBBusiness(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void createGroupTable(){
    	db = dbHelper.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS "+
        		PASSWORD_TABLE +" (_id INTEGER PRIMARY KEY AUTOINCREMENT, accountname TEXT, password TEXT)");
    }

    public long insertPassword(ContentValues contentValues) {
    	db = dbHelper.getWritableDatabase();
        if (db == null) {
            return -1;
        }
        return db.insert(PASSWORD_TABLE, null, contentValues);
    }    
    
    public String queryPassword(String accountName) {
    	db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(PASSWORD_TABLE, null, null, null, null, null, null);
        Log.d("william","william DBBusiness.queryPassword cursor is:"+cursor);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String account = cursor.getString(cursor.getColumnIndex("accountname"));
                if (!TextUtils.isEmpty(accountName) && account.equals(accountName)) {
                    return cursor.getString(cursor.getColumnIndex("password"));
                }
                cursor.moveToNext();
            }
        }
        return null;
    }    
    
    public int delete(String accountName) {
    	db = dbHelper.getWritableDatabase();
        if  (db == null || TextUtils.isEmpty(PASSWORD_TABLE)) {
            return -1;
        }
        String whereClause = "accountName=?";
        String[] whereArgs = {accountName};
        return db.delete(PASSWORD_TABLE, whereClause, whereArgs);
    }

    public int update(ContentValues contentValues, String passWord) {
    	db = dbHelper.getWritableDatabase();
        String whereClause = "passWord=?";
        String[] whereArgs = {passWord};
        return db.update(PASSWORD_TABLE, contentValues, whereClause, whereArgs);
    }
    

    public static void closeDB(){
        if(db != null){
            db.close();
        }
    }
}
