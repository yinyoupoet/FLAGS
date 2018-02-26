package com.yinyoupoet.flags;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by hasee on 2018/2/23.
 */

public class DataBaseUtil extends SQLiteOpenHelper{
    public static final String CREATE_FLAGS = "create table flag(" +
            "id integer primary key autoincrement, " +
            "state text, " +
            "color text, " +
            "title text, " +
            "content text, " +
            "date date)";

    private Context mContext;

    public DataBaseUtil(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_FLAGS);
        Toast.makeText(mContext,"Create successed",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }




}
