package com.yinyoupoet.flags;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFlagActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    //插入图片的Activity的返回的code
    static final int IMAGE_CODE = 99;

    //数据库帮助类
    DataBaseUtil dbUtil;

    //内容对象
    Flag flag;      //传进来的flag
    Date date;      //点进来的时候的时间
    Boolean isChanged = false;      //判断内容是否被修改过


    //控件申明
    Toolbar toolbar;            //ToolBar
    ImageView pic;              //插入图片的imageView
    EditText content;           //内容
    ScrollView scrollView;      //整个view
    EditText title;             //标题
    TextView state;             //底下表示状态的
    Dialog dialog;              //底部弹窗
    View inflate;               //dialog的容器
    TextView continueBtn;       //dialog上的待完成按钮
    TextView complishBtn;       //dialog上的已完成按钮
    ImageView share;            //分享按钮

    View hrView;                //最下方菜单栏的那个横线
    View bottomMenu;            //最下方菜单栏



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit);

        //初始化基本参数
        init();

        //初始化控件各种事件
        initWidget();


    }

    //region 进行初始化
    private void init() {
        //region 绑定控件
        toolbar = findViewById(R.id.toolbar_edit);
        pic = findViewById(R.id.iv_edit_pic);
        content = findViewById(R.id.et_edit_content);
        scrollView = findViewById(R.id.sv_edit_view);
        title = findViewById(R.id.et_edit_title);
        state = findViewById(R.id.tv_edit_state);

        hrView = findViewById(R.id.view_edit_1);
        bottomMenu = findViewById(R.id.rl_edit_bottom);
        share = findViewById(R.id.iv_edit_share);

        //初始化底部弹出框控件
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.bottom_state_popup,null);
        continueBtn = inflate.findViewById(R.id.state_continue);
        complishBtn = inflate.findViewById(R.id.state_complish);



        //endregion

        //region 初始化内容对象并初始化值
        flag = (Flag) getIntent().getSerializableExtra("Flag");
        title.setText(flag.getTitle());

        //region 设置内容
        /*Html.ImageGetter imageGetter = new Html.ImageGetter(){
            @Override
            public Drawable getDrawable(String s) {
                int width = ScreenUtils.getScreenWidth(AddFlagActivity.this);
                int height = ScreenUtils.getScreenHeight(AddFlagActivity.this);
                Bitmap bitmap = ImageUtils.getSmallBitmap(s,width,480);
                Drawable drawable = new BitmapDrawable(bitmap);
                drawable.setBounds(0,0,width,height);
                return drawable;
            }
        };
        content.setText(Html.fromHtml(flag.getContent(), imageGetter,null));*/
        /*if(!content.getText().toString().trim().equals("")){
            content.append("\n");
        }*/

        initContent();
        //endregion

        //设置状态
        state.setText(flag.getState());


        //endregion

        //初始化toolBar
        initToolBar();

        //初始化数据库帮助对象
        dbUtil = new DataBaseUtil(this,"Flags.db",null,1);

        //初始化点击状态后弹出的最下面的dialog
        initDialog();

        //默认让内容获取焦点，但是并不弹出软键盘
        content.setFocusable(true);
        content.setFocusableInTouchMode(true);
        content.requestFocus();
        content.setSelection(content.getText().length());
        AddFlagActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    //endregion


    //region 初始化控件的各种事件
    private void initWidget(){

        //region 插入图片的点击事件
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用图库
                callGallery();
            }
        });

        //endregion

        //region 整个Scrollview的点击事件

        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //通知父控件请勿拦截本控件touch事件
                view.getParent().requestDisallowInterceptTouchEvent(true);
                Log.d("YYPT", "click the scrollView");
                //点击整个页面都会让内容框获得焦点，且弹出软键盘
                content.setFocusable(true);
                content.setFocusableInTouchMode(true);
                content.requestFocus();
                content.setSelection(content.getText().length());
                //AddFlagActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                //显示或隐藏软键盘，如果已显示则隐藏，反之显示
                //参考网址： https://www.jianshu.com/p/dc9387417914
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

            }
        });


        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //scrollView.callOnClick();
                return false;
            }
        });

        scrollView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });


        //endregion

        //region toolbar的菜单的点击事件——即保存按钮的点击事件
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.menu_save){
                    if(flag.getId() == -1){
                        //这是新建的
                        if(isChanged){
                            addFlag();
                            isChanged = false;
                        }

                    }else{
                        //这个flag是进行修改的
                        if(isChanged){
                            //只有当进行了修改了才更新数据库
                            editFlag();
                            isChanged = false;
                        }

                    }
                }
                return true;
            }
        });
        //endregion

        //region 监听内容、标题、状态的变化

        //region 监听title
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("YYPT_TITLE_CHANGE", "onTextChanged: ");
                isChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //endregion

        //region 监听content
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("YYPT_CONTENT_CHANGE", "onTextChanged: ");
                isChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //endregion

        //状态的监听会在状态改变的时候设置

        //endregion

        //region 状态按钮点击触发的事件_弹出框改变状态
        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //设置弹出框字体颜色
                setDialogColor(state.getText().toString().trim());

                //显示dialog
                hrView.setVisibility(View.GONE);
                bottomMenu.setVisibility(View.GONE);
                dialog.show();
            }
        });
        //endregion

        //region 状态更换点击事件
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //不能对flag进行设置，因为还在编辑的，有可能是新增的，这时候还没有id，而设计打算在flag.setState()直接对数据库进行操作
                //flag.setState("待完成");
                if(!state.getText().toString().equals("待完成")){
                    isChanged = true;
                    state.setText("待完成");
                    setDialogColor("待完成");
                }
                dialog.dismiss();
            }
        });
        complishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //不能对flag进行设置，因为还在编辑的，有可能是新增的，这时候还没有id，而设计打算在flag.setState()直接对数据库进行操作
                //flag.setState("已完成");
                if(!state.getText().toString().equals("已完成")){
                    isChanged = true;
                    state.setText("已完成");
                    setDialogColor("已完成");
                }
                dialog.dismiss();
            }
        });
        //endregion

        //region toolbar的返回键点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //自动保存
                if(flag.getId() == -1){
                    //这是新建的
                    if(isChanged){
                        addFlag();
                    }

                }else{
                    //这个flag是进行修改的
                    if(isChanged){
                        //只有当进行了修改了才更新数据库
                        editFlag();
                    }

                }
                finish();
            }
        });
        //endregion

        //region 分享按钮点击事件
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFlagActivity.this,ShareActivity.class);
                intent.putExtra("title",title.getText().toString());
                intent.putExtra("content",content.getText().toString());
                startActivity(intent);

                /*String fname = ScreenShootUtils.savePic(ScreenShootUtils.getBitmapByView(scrollView));
                Toast.makeText(AddFlagActivity.this,fname,Toast.LENGTH_SHORT).show();
                Log.d("YYPT_shotPIC", fname);*/
            }
        });
        //endregion
    }
    //endregion



    //region 初始化toolBar
    private void initToolBar() {

        //设置显示的文字
        date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:MM");
        toolbar.setTitle(sdf.format(date));

        //将toolBar设置为该界面的Bar
        setSupportActionBar(toolbar);

        //设置返回键，要在serSupportAction之后
        toolbar.setNavigationIcon(R.drawable.back);
    }
    //endregion


    //region 设置toolbar的菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit,menu);
        return true;
    }
    //endregion

    //region 调用图库
    private void callGallery(){

        int permission_WRITE = ActivityCompat.checkSelfPermission(AddFlagActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_READ = ActivityCompat.checkSelfPermission(AddFlagActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permission_WRITE != PackageManager.PERMISSION_GRANTED || permission_READ != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddFlagActivity.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }

        //调用系统图库
        //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");  //相片类型
        //startActivityForResult(intent,1);

        Intent getAlbum = new Intent(Intent.ACTION_PICK);
        getAlbum.setType("image/*");
        startActivityForResult(getAlbum,IMAGE_CODE);


    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //参考网址：http://blog.csdn.net/abc__d/article/details/51790806

        Bitmap bm = null;
        // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();
        if(requestCode == IMAGE_CODE){
            try{
                // 获得图片的uri
                Uri originalUri = data.getData();
                bm = MediaStore.Images.Media.getBitmap(resolver,originalUri);
                String[] proj = {MediaStore.Images.Media.DATA};
                // 好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = managedQuery(originalUri,proj,null,null,null);
                // 按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                String path = cursor.getString(column_index);
                //Log.e("insertIMG", "onActivityResult: ");
                insertImg(path);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(AddFlagActivity.this,"图片插入失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //region 插入图片
    private void insertImg(String path){
        //Log.e("插入图片", "insertImg:" + path);
        String tagPath = "<img src=\""+path+"\"/>";//为图片路径加上<img>标签
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap != null){
            SpannableString ss = getBitmapMime(path, tagPath);
            insertPhotoToEditText(ss);
            content.append("\n");
            //Log.e("YYPT_Insert", content.getText().toString());

        }else{
            //Log.d("YYPT_Insert", "tagPath: "+tagPath);
            Toast.makeText(AddFlagActivity.this,"插入失败，无读写存储权限，请到权限中心开启",Toast.LENGTH_LONG).show();
        }
    }
    //endregion

    //region 将图片插入到EditText中
    private void insertPhotoToEditText(SpannableString ss){
        Editable et = content.getText();
        int start = content.getSelectionStart();
        et.insert(start,ss);
        content.setText(et);
        content.setSelection(start+ss.length());
        content.setFocusableInTouchMode(true);
        content.setFocusable(true);
    }
    //endregion

    //region 根据图片路径利用SpannableString和ImageSpan来加载图片
    private SpannableString getBitmapMime(String path,String tagPath) {
        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径

        int width = ScreenUtils.getScreenWidth(AddFlagActivity.this);
        int height = ScreenUtils.getScreenHeight(AddFlagActivity.this);

        Log.d("YYPT_IMG_SCREEN", "高度:"+height+",宽度:"+width);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        Log.d("YYPT_IMG_IMG", "高度:"+bitmap.getHeight()+",宽度:"+bitmap.getWidth());
        bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.8,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.8)));

        //Bitmap bitmap = ImageUtils.getSmallBitmap(path,600,480);


        /*
        //高:754，宽1008
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        */
        Log.d("YYPT_IMG_COMPRESS", "高度："+bitmap.getHeight()+",宽度:"+bitmap.getWidth());


        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        ss.setSpan(imageSpan, 0, tagPath.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;


    }
    //endregion


    //region 新建flag，并将id赋给这个flag
    private void addFlag(){

        //String s = Html.fromHtml(content.getText());
        //Log.d("YYPT_ADD",);
        try {
            Date thisDate = new Date();
            SQLiteDatabase db = dbUtil.getWritableDatabase();
            String sql = "insert into flag(state,color,title,content,date) " +
                    "values('" + state.getText().toString() + "'," +
                    "'" + flag.getColor() + "'," +
                    "'" + title.getText().toString() + "'," +
                    "'" + content.getText().toString() + "'," +
                    "'" + thisDate + "')";
            Log.d("YYPT", sql);
            db.execSQL(sql);
            //Toast.makeText(AddFlagActivity.this,"保存成功",Toast.LENGTH_SHORT).show();

            //保存后必须将这个flag的Id赋给它，不然会一直新建
            setFlagId();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //endregion

    //region 编辑flag
    private void editFlag(){
        int id = flag.getId();
        try {
            Date thisDate = new Date();
            SQLiteDatabase db = dbUtil.getWritableDatabase();
            String sql = "update flag set state='"+state.getText().toString().trim()+"'," +
                    "color='"+flag.getColor()+"'," +
                    "title='"+title.getText().toString()+"'," +
                    "content='"+content.getText().toString()+"'," +
                    "date='"+thisDate+"' where id="+id+"";
            Log.d("YYPT", sql);
            db.execSQL(sql);
            //Toast.makeText(AddFlagActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //endregion


    //region 初始化content内容，参考：
    //  http://blog.sina.com.cn/s/blog_766aa3810100u8tx.html#cmt_523FF91E-7F000001-B8CB053C-7FA-8A0
    //  https://segmentfault.com/q/1010000004268968
    //  http://www.jb51.net/article/102683.htm
    private void initContent(){
        String input = flag.getContent().toString();
        //String regex = "<img src=\\".*?\\"\\/>";
        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        Matcher m = p.matcher(input);
        //List<String> result = new ArrayList<String>();


        SpannableString spannable = new SpannableString(input);
        while(m.find()){
            //Log.d("YYPT_RGX", m.group());
            //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
            String s = m.group();
            int start = m.start();
            int end = m.end();
            //path是去掉<img src=""/>的中间的图片路径
            String path = s.replaceAll("\\<img src=\"|\"\\/>","").trim();
            //Log.d("YYPT_AFTER", path);

            //利用spannableString和ImageSpan来替换掉这些图片
            int width = ScreenUtils.getScreenWidth(AddFlagActivity.this);
            int height = ScreenUtils.getScreenHeight(AddFlagActivity.this);

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.8,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.8)));
                ImageSpan imageSpan = new ImageSpan(this, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        content.setText(spannable);
        //content.append("\n");
        //Log.d("YYPT_RGX_SUCCESS",content.getText().toString());
    }
    //endregion

    //region 初始化dialog,http://blog.csdn.net/Small_Lee/article/details/50602400
    private void initDialog(){
        dialog = new Dialog(this,R.style.StateChangedDialogStyle){
            @Override
            public void dismiss() {
                super.dismiss();
                hrView.setVisibility(View.VISIBLE);
                bottomMenu.setVisibility(View.VISIBLE);
                AddFlagActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        };


        //将布局设置给dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置dialog从底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;  //设置Dialog距离底部的距离
        //将属性设置给窗体
        dialogWindow.setAttributes(lp);


    }
    //endregion

    //region 设置弹出框字体颜色，参数为选中的状态
    private void setDialogColor(String st){
        if(st.equals("待完成")){
            continueBtn.setTextColor(Color.parseColor("#cb4042"));
            complishBtn.setTextColor(Color.parseColor("#000000"));
        }else if(st.equals("已完成")){
            continueBtn.setTextColor(Color.parseColor("#000000"));
            complishBtn.setTextColor(Color.parseColor("#cb4042"));
        }
    }
    //endregion

    //region 一个新的flag，保存一次后，给其设置id
    private void setFlagId(){
        SQLiteDatabase db = dbUtil.getWritableDatabase();
        Cursor cursor = db.query("flag",null,null,null,null,null,null);
        if(cursor == null){
            Log.d("AddFlagActivity", "没有呀");
            return;
        }
        List<Integer> idList = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                //遍历Cursor对象，取出数据并打印
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                idList.add(id);
            }while(cursor.moveToNext());
        }
        cursor.close();
        Collections.sort(idList);
        int theId = idList.get(idList.size()-1);
        Log.d("YYPT_ID", ""+theId);
        flag.setId(theId);
    }
    //endregion


    //region 生命周期结束后自动保存
    @Override
    protected void onStop() {
        super.onStop();
        //自动保存
        if(flag.getId() == -1){
            //这是新建的
            if(isChanged){
                addFlag();
            }

        }else{
            //这个flag是进行修改的
            if(isChanged){
                //只有当进行了修改了才更新数据库
                editFlag();
            }
        }
        isChanged = false;
    }
    //endregion

    //region 点击返回退出时也会自动保存
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            //自动保存
            if(flag.getId() == -1){
                //这是新建的
                if(isChanged){
                    addFlag();
                }

            }else{
                //这个flag是进行修改的
                if(isChanged){
                    //只有当进行了修改了才更新数据库
                    editFlag();
                }
            }
            isChanged = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    //endregion
}