package com.yinyoupoet.flags;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    //要显示的是完成的还是每完成的
    Boolean isComplish;

    private String TAG = "YYPT";


    private int delPosition = -1;       //用来删除的一个变量，因为内部类要用

    //属性
    List<Flag> flagList = new ArrayList<>();
    DataBaseUtil dbUtil;
    FlagAdapter adapter;            //listView的adapter


    //控件
    Toolbar toolbar;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Intent intent = getIntent();
        isComplish = intent.getBooleanExtra("isComplish",false);

        init();
    }

    private void init(){
        //初始化参数
        dbUtil = new DataBaseUtil(CategoryActivity.this,"Flags.db",null,1);

        //初始化控件
        toolbar = findViewById(R.id.toolbar_category);
        listView = findViewById(R.id.lv_category);

        //初始化toolBar
        initToolBar();

        //初始化listView
        initListView();

    }

    //初始化toolbar
    private void initToolBar(){
        if(isComplish){
            toolbar.setTitle("已完成");
        }else{
            toolbar.setTitle("待完成");
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //初始化listView
    private void initListView(){
        //获取flags的值，并保存到flagList中
        getFlags();

        //处理listView的点击和长按事件
        handleListViewAction();

        adapter = new FlagAdapter(CategoryActivity.this,R.layout.flag,flagList);
        listView.setAdapter(adapter);
    }

    //获取flags的值，并保存到flagList中
    private void getFlags(){

        //在获取之前要先把flagList清空
        flagList.clear();

        SQLiteDatabase db = dbUtil.getWritableDatabase();
        String sql;
        if(isComplish){
            //如果查询的是已完成的
            sql = "select * from flag where state = '已完成'";
        }else{
            sql = "select * from flag where state = '待完成'";
        }
        Cursor cursor = db.rawQuery(sql,null);
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

            }while(cursor.moveToNext());
        }
        cursor.close();
        Collections.sort(flagList);
    }

    //处理listView的点击和长按事件
    private void handleListViewAction(){

        //region listView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Flag flag = flagList.get(i);
                Intent intent = new Intent(CategoryActivity.this,AddFlagActivity.class);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
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

    //再次进入要刷新一下listView

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
}
