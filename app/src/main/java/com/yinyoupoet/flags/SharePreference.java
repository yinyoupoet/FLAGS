package com.yinyoupoet.flags;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hasee on 2018/2/26.
 */

public class SharePreference {
    Context context;
    public SharePreference(Context context){
        this.context = context;
    }

    //设置状态，true为第一次打开
    public void setState(){
        SharedPreferences sp = context.getSharedPreferences("save.himi",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isNew",false);
        editor.commit();
    }

    //获取状态
    public boolean getState(){
        SharedPreferences sp = context.getSharedPreferences("save.himi",Context.MODE_PRIVATE);
        Boolean b = sp.getBoolean("isNew",true);
        return b;
    }
}
