package com.yinyoupoet.flags;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;


/**
 * Created by hasee on 2018/2/15.
 */

public class EditFlagActivity extends Activity {
    //控件申明
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //初始化基本参数
        init();

        //初始化控件各种事件
        initWidget();
    }

    //region 初始化基本参数
    private void init(){
        //绑定控件
        toolbar = findViewById(R.id.toolbar_edit);


    }
    //endregion

    //region 初始化控件的各种事件
    private void initWidget(){

    }
    //endregion

    //region 设置toolBar为


    //region 设置toolbar的菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit,menu);
        return true;
    }
    //endregion

}
