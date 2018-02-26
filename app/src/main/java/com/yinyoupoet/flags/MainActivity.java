package com.yinyoupoet.flags;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextPaint;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    int delPosition=-1;            //用来删除的一个变量，因为内部类要用

    private String TAG = "YYPT";

    DataBaseUtil dbUtil;

    //region 数据声明
    int wholeCount = 0;              //flags的总条数
    int complishCount = 0;          //已完成的flags数量
    int continueCount = 0;          //尚未完成的flags数量

    List<Flag> flagList = new ArrayList<>();    //保存flags的列表

    FlagAdapter adapter;            //listView的adapter
    //endregion


    //region 控件声明
    TextView bgTv;          //列表的背景TextView
    ListView listView;      //flags列表
    Toolbar toolbar;                    //toolbar
    ImageView addBtn;          //新建flag的按钮
    //endregion

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }

        //初始化参数
        init();

        //初始化控件
        initWidget();

    }

    //region 初始化参数
    private void init(){
        dbUtil = new DataBaseUtil(MainActivity.this,"Flags.db",null,1);

        //region 控件初始化
        addBtn = findViewById(R.id.iv_add_flag);
        //endregion


        //判断是否为第一次打开APP并植入引导页
        importGuide();


        //region 初始化toolbar
        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle("FLAGS");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.flag4_2);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                String msg = "";
                switch (item.getItemId()){
                    case R.id.menu_search:
                        msg="Click search";
                        break;
                    case R.id.menu_complish:
                        //msg = "Click complish";
                        Intent intent = new Intent(MainActivity.this,CategoryActivity.class);
                        intent.putExtra("isComplish",true);
                        startActivity(intent);
                        break;
                    case R.id.menu_continue:
                        //msg = "Click continue";
                        Intent intent1 = new Intent(MainActivity.this,CategoryActivity.class);
                        intent1.putExtra("isComplish",false);
                        startActivity(intent1);
                        break;
                    case R.id.menu_about:
                        //msg = "Click about";
                        Intent intent2 = new Intent(MainActivity.this,AboutActivity.class);
                        startActivity(intent2);
                        break;
                }
                /*if(!msg.equals("")){
                    Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                }*/
                return true;
            }
        });
        //endregion



        //region 获取listview的值
        initFlags();
        //endregion

        //region 初始化ListView
        adapter = new FlagAdapter(MainActivity.this,R.layout.flag,flagList);
        listView = findViewById(R.id.lv_flags);
        //listView.setMenu(menuList);
        listView.setAdapter(adapter);
        //endregion


        //region 初始化下拉背景
        bgTv = findViewById(R.id.bg_tv);
        String bgText = "您已经立了<font color='#000'>"+wholeCount+"</font>个FLAG<br>" +
                "完成了<font color='#000'>"+complishCount+"</font>个<br>" +
                "未完成<font color='#000'>"+continueCount+"</font>个<br><br>" +
                "Developed By <font color='#999999'>YinyouPoet</font><br>" +
                "本项目由<font color='#999999'>吟游诗人</font>开发";
        bgTv.setText(Html.fromHtml(bgText));
        //endregion

    }
    //endregion

    //region 初始化控件
    private void initWidget(){


        //region 新建文章的点击
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AddFlagActivity.class);
                Flag flag = new Flag();
                intent.putExtra("Flag",flag);
                startActivity(intent);
            }
        });
        //endregion

        //region listView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Flag flag = flagList.get(i);
                Intent intent = new Intent(MainActivity.this,AddFlagActivity.class);
                intent.putExtra("Flag",flag);
                startActivity(intent);
            }
        });


        //endregion

        //region listView长按事件
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

                delPosition = position;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.drawable.flag4_2);
                builder.setTitle("凡事预则立，不预则废");
                builder.setMessage("是否要删除该条FLAG？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //确定删除
                        try {
                            Log.d(TAG, "onClick: "+delPosition);
                            SQLiteDatabase db = dbUtil.getWritableDatabase();
                            db.delete("flag", "id = ?", new String[]{"" + flagList.get(delPosition).getId()});
                            init();
                            //listView.setAdapter(adapter);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //取消
                    }
                });
                builder.show();
                return true;
            }
        });

        //endregion
    }
    //endregion

    //region 获取flags的值
    private void initFlags(){
        /*Flag flag_1 = new Flag("待完成","#000","胖十斤","我爱吃东西<img src=\"/storage/emulated/0/DCIM/Camera/IMG_20180220_184453.jpg\"/>胖啊胖啊abc<img src=\"/storage/emulated/0/DCIM/Camera/IMG_20180224_100848.jpg\"/>",new Date());
        Flag flag_2 = new Flag("待完成","#000","瘦十斤","明天我要瘦十斤",new Date());
        Flag flag_3 = new Flag("已完成","#000","举起一个锤子","一定一定要在十年，不，二十年之后，或者五十年后，还能举起一个锤子",new Date());
        flagList.add(flag_1);
        flagList.add(flag_2);
        flagList.add(flag_3);*/

        //获取flag条数，完成和未完成条数
        wholeCount = 0;
        complishCount = 0;
        continueCount = 0;
        //在获取之前将其清空，以免重复添加
        flagList.clear();


        SQLiteDatabase db = dbUtil.getWritableDatabase();
        Cursor cursor = db.query("flag",null,null,null,null,null,null);
        if(cursor == null){
            Log.d(TAG, "没有呀");
            return;
        }
        if(cursor.moveToFirst()){
            do{

                //遍历Cursor对象，取出数据并打印
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String state = cursor.getString(cursor.getColumnIndex("state"));
                String color = cursor.getString(cursor.getColumnIndex("color"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String sdate = cursor.getString(cursor.getColumnIndex("date"));
                Date date = new Date(sdate);
                //Log.d(TAG, date.toString());

                Flag flag = new Flag(state,color,title,content,date);
                flag.setId(id);
                flagList.add(flag);

                wholeCount++;
                if(state.equals("待完成")){
                    continueCount++;
                }else{
                    complishCount++;
                }

            }while(cursor.moveToNext());
        }
        cursor.close();
        Collections.sort(flagList);
    }
    //endregion

    //region 设置状态栏菜单
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    //endregion

    //region 让菜单能显示图标
    @Override
    protected boolean onPrepareOptionsPanel(View view, android.view.Menu menu) {
        if(menu != null){
            if(menu.getClass() == MenuBuilder.class){
                try{
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible",Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return super.onPrepareOptionsPanel(view,menu);
    }

    //endregion


    //region 当重新进入该activity后，需要重新初始化一下
    @Override
    protected void onResume() {
        super.onResume();
        init();
        //listView.setAdapter(adapter);
    }
    //endregion

    //判断是否为第一次打开APP并植入引导页
    private void importGuide(){
        SharePreference sp = new SharePreference(MainActivity.this);
        if(sp.getState()){
            //是第一次打开
            addGuide();

            sp.setState();
        }
    }



    //使用引导
    private void addGuide(){
        Date thisDate = new Date();
        doInsert("教程一","点击右下角加号可以编写新Flag，长按可以删除Flag",thisDate,0);
        doInsert("教程二","首页列表根据最近一次更新的时间进行排序",thisDate,-1);
        doInsert("教程三","可以插入图片，且保存按钮很鸡肋，不点击也会自动保存的",thisDate,-2);
        doInsert("教程四","首页下拉可以看到你所有Flag的状态哦",thisDate,-3);
        doInsert("教程五","想联系作者，点击\"关于\"试试看吧",thisDate,-4);
        doInsert("教程六","删除所有教程，开始你的立Flag之旅吧",thisDate,-5);
    }


    //插入默认往数据库里一些数据，引导用户使用
    private void doInsert(String title,String content,Date date,int second){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND,second);
        Date thisDate = calendar.getTime();

        try {
            SQLiteDatabase db = dbUtil.getWritableDatabase();
            String sql = "insert into flag(state,color,title,content,date) " +
                    "values('待完成'," +
                    "'#000000'," +
                    "'" + title.toString() + "'," +
                    "'" + content.toString() + "'," +
                    "'" + thisDate + "')";
            Log.d("YYPT", sql);
            db.execSQL(sql);
            //Toast.makeText(AddFlagActivity.this,"保存成功",Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }




}
