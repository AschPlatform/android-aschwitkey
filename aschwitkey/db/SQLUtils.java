package com.xw.idld.aschwitkey.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xw.idld.aschwitkey.entity.DBHelperBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xw on 2020/5/27.
 * 数据库方法类
 * 增删改查
 */

public class SQLUtils {

    private static SQLiteDatabase mDatabase;
    private static DBHelper mDBHelper;

    /**
     * 根据钱包地址查询数据库指定信息
     * @param AddRess  钱包地址
     */
    public static List<DBHelperBean> QuerySQL(Context context, String AddRess){
        mDBHelper = new DBHelper(context);
        mDatabase = mDBHelper.getWritableDatabase();
        List<DBHelperBean> list=new ArrayList<>();
        String querysql="select *from T_ASCHCITY where address in ( "+AddRess+" )";
        DBHelperBean helperBean=null;
        Cursor cursor=mDatabase.rawQuery(querysql,null);
        while (cursor.moveToNext()){
            helperBean=new DBHelperBean();
            helperBean.setAccount(cursor.getString(cursor.getColumnIndex(DBHelper.ACCOUNT)));
            helperBean.setAddress(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)));
            helperBean.setSecret(cursor.getString(cursor.getColumnIndex(DBHelper.SECRET)));
            helperBean.setState(cursor.getString(cursor.getColumnIndex(DBHelper.STATE)));
            list.add(helperBean);
        }
        return list;
    }

    /**
     * 查询数据库
     */
    public static List<DBHelperBean> QuerySQLAll(Context context,String where){
        mDBHelper = new DBHelper(context);
        mDatabase = mDBHelper.getWritableDatabase();
        List<DBHelperBean> list=new ArrayList<>();
        String querysql="select *from T_ASCHCITY "+where;
        DBHelperBean helperBean=null;
        Cursor cursor=mDatabase.rawQuery(querysql,null);
        while (cursor.moveToNext()){
            helperBean=new DBHelperBean();
            helperBean.setAccount(cursor.getString(cursor.getColumnIndex(DBHelper.ACCOUNT)));
            helperBean.setAddress(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)));
            helperBean.setSecret(cursor.getString(cursor.getColumnIndex(DBHelper.SECRET)));
            helperBean.setState(cursor.getString(cursor.getColumnIndex(DBHelper.STATE)));

            list.add(helperBean);
        }
        return list;
    }


    /**
     * 查询数据库
     */
    public static String QueryPublick(Context context,String address){
        mDBHelper = new DBHelper(context);
        mDatabase = mDBHelper.getWritableDatabase();
        List<DBHelperBean> list=new ArrayList<>();
        String querysql="select *from T_ASCHCITY where address='"+address+"'";
        DBHelperBean helperBean=null;
        Cursor cursor=mDatabase.rawQuery(querysql,null);
        while (cursor.moveToNext()){
            helperBean=new DBHelperBean();
            helperBean.setAccount(cursor.getString(cursor.getColumnIndex(DBHelper.ACCOUNT)));
            helperBean.setAddress(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)));
            helperBean.setSecret(cursor.getString(cursor.getColumnIndex(DBHelper.SECRET)));
            helperBean.setState(cursor.getString(cursor.getColumnIndex(DBHelper.STATE)));

            list.add(helperBean);
        }

        String publick="a";
        if (!list.isEmpty()){
           publick=list.get(0).getState();
        }
        return publick;
    }

    /**
     * 增加一条数据
     * @param context   上下文
     * @param Account   账号
     * @param Address   钱包地址
     * @param Secret    加密后的助记词
     * @param state     备用字段
     */
    public static void AddSql(Context context,String Account,String Address,String Secret,String state){
        mDBHelper = new DBHelper(context);
        mDatabase = mDBHelper.getWritableDatabase();
        String addsql="insert into T_ASCHCITY(account,address,secret,state) values('"+
                Account+"','"+Address+"','"+Secret+"','"+state+"')";
        mDatabase.execSQL(addsql);
    }

    /**
     * 删除某一个条数据，根据钱包地址删除
     * @param context     上下文
     * @param AddRess     钱包地址
     */
    public static void DaleteSql(Context context,String AddRess){
        mDBHelper = new DBHelper(context);
        mDatabase = mDBHelper.getWritableDatabase();
        String dalsql="delete from T_ASCHCITY where address='"+AddRess+"'";
        mDatabase.execSQL(dalsql);
    }

    /**
     * 删除数据数据库
     * @param context     上下文
     */
    public static void DaleteSql(Context context){
        mDBHelper = new DBHelper(context);
        mDatabase = mDBHelper.getWritableDatabase();
        String dalsql="delete from T_ASCHCITY ";
        mDatabase.execSQL(dalsql);
    }

}
