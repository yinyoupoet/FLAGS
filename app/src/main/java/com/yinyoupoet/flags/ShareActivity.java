package com.yinyoupoet.flags;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ShareActivity extends AppCompatActivity {

    ScrollView scrollView;
    Toolbar toolbar;
    RelativeLayout rl_content;
    RelativeLayout rl_share_head;
    TextView tv_title;
    RelativeLayout rl_share_bottom;
    TextView tv_content;

    String title;
    String content;

    String TAG = "Share：";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        init();

        //initWidget();
    }

    private void init(){
        toolbar = findViewById(R.id.toolbar_share);
        rl_content = findViewById(R.id.rl_share_content);
        tv_content = findViewById(R.id.tv_share_content);
        rl_share_head = findViewById(R.id.rl_share_head);
        rl_share_bottom = findViewById(R.id.rl_share_bottom);
        tv_title = findViewById(R.id.tv_share_title);
        scrollView = findViewById(R.id.sv_share);


        //初始化toolbar
        initToolBar();

        //初始化toolbar上面确定按钮的点击事件
        initShare();

        //初始化内容
        initTitleAndContent();

        //初始化布局,让显示内容的高度至少要填充满屏幕
        //initShow();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initShow();
    }

    private void initToolBar(){
        toolbar.setTitle("分享预览图");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    //region 设置toolbar的菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share,menu);
        return true;
    }
    //endregion

    //初始化toolbar上面确定按钮的点击事件
    private void initShare(){
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //点击分享按钮后触发的事件
                if(item.getItemId() == R.id.menu_share){
                    //进行分享
                    doshare();
                }
                return true;
            }
        });
    }

    //初始化布局,让显示内容的高度至少要填充满屏幕
    private void initShow(){
        //获取屏幕高度
        int winHeight = getWindowHeight();
        //获取toolbar高度
        int toolbarHeight = toolbar.getHeight();

        TypedValue typedValue = new TypedValue();
        getApplicationContext().getTheme().resolveAttribute(R.attr.actionBarSize,typedValue,true);
        int[] attribute = new int[] {android.R.attr.actionBarSize};
        TypedArray array = getApplicationContext().obtainStyledAttributes(typedValue.resourceId,attribute);
        toolbarHeight = array.getDimensionPixelSize(0,0);
        array.recycle();

        //获取顶部显示flag的那一行的高度
        int topHeight = rl_share_head.getHeight();
        //获取底部显示flag的那一行的高度
        int bottomHeight = rl_share_bottom.getHeight();
        //获取标题栏的高度
        int titleHeight = tv_title.getHeight();
        //获取顶部状态栏的高度
        int stateHeight =getStateHeight();


        Log.d(TAG, "winHeight:"+winHeight+"   toolBarHeight:"+toolbarHeight);
        //设置content布局高度
        if(rl_content.getHeight() < winHeight-toolbarHeight-stateHeight){
            rl_content.setMinimumHeight(winHeight-toolbarHeight-stateHeight);
            //tv_content.setMinimumHeight(winHeight-toolbarHeight-topHeight-bottomHeight-titleHeight);
            //Log.d(TAG, "initShow: "+rl_content.getHeight());
        }

    }

    //获取屏幕高度
    private int getWindowHeight(){
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    //获取顶部状态栏高度
    private int getStateHeight(){
        int statusBarHeight = 1;
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if(resourceId > 0){
            statusBarHeight = getResources().getDimensionPixelOffset(resourceId);
        }
        Log.d(TAG, "getStateHeight: "+statusBarHeight);
        return statusBarHeight;
    }

    //初始化内容
    private void initTitleAndContent(){
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");

        tv_title.setText(title);
        //tv_content.setText(content);
        initContent();
    }

    //region 初始化content内容，参考：
    //  http://blog.sina.com.cn/s/blog_766aa3810100u8tx.html#cmt_523FF91E-7F000001-B8CB053C-7FA-8A0
    //  https://segmentfault.com/q/1010000004268968
    //  http://www.jb51.net/article/102683.htm
    private void initContent(){
        String input = content;
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
            int width = ScreenUtils.getScreenWidth(ShareActivity.this);
            int height = ScreenUtils.getScreenHeight(ShareActivity.this);

            try {
                Bitmap bitmap = ImageUtils.getSmallBitmap(path, width, 480);
                ImageSpan imageSpan = new ImageSpan(this, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        tv_content.setText(spannable);
        //content.append("\n");
        //Log.d("YYPT_RGX_SUCCESS",content.getText().toString());
    }
    //endregion


    //进行分享
    private void doshare(){
        try {
            Bitmap bitmap = ScreenShootUtils.getBitmapByView(scrollView);
            //String uri = ScreenShootUtils.savePic(bitmap);
            //Log.d(TAG, uri);

            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"title",null);
            Uri bitmapUri = Uri.parse(bitmapPath);


            Intent imageIntent = new Intent(Intent.ACTION_SEND);
            imageIntent.setType("image/*");
            imageIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            startActivity(Intent.createChooser(imageIntent, "分享"));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
