package com.xw.idld.aschwitkey.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xw on 2020/5/27.
 * 数据库帮助类
 */

public class DBHelper extends SQLiteOpenHelper {

    /**
     * 数据库文件名
     */
    public static final String DB_NAME = "asch_city.db";

    /**
     * 数据库表名
     */
    public static final String TABLE_NAME = "T_ASCHCITY";

    /**
     * 数据库版本号
     */
    public static final int DB_VERSION = 1;

    /**
     * 字段
     * account       账号
     * address       钱包地址
     * secret        助记词加密结果
     * state         状态  （备用）
     */
    public static final String ACCOUNT = "account";
    public static final String ADDRESS = "address";
    public static final String SECRET = "secret";
    public static final String STATE = "state";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " +
                TABLE_NAME +
                "(_id integer primary key autoincrement, " +
                ACCOUNT + " varchar ," +
                ADDRESS + " varchar ," +
                SECRET + " varchar ," +
                STATE + " varchar " +
                ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //更新表结构或删除旧的表结构
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
