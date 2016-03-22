package com.example.baifan.choosepicdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.baifan.choosepicdemo.util.BitmapUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

/**
 * Created by baifan on 16/2/4.
 */
public class DemoActivity extends Activity implements View.OnClickListener{
    /**
     * 测试图片
     */
    private ImageView mImgDemo;
    private ImageLoader mImageLoader;
    private Button mBtnChoosePic;
    private final static int REQUEST_CHOOSE_PIC = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mImageLoader = ImageLoader.getInstance();

        initViews();

        initEvents();
    }

    public void initViews(){
        mImgDemo = (ImageView) findViewById(R.id.img_demo);
        mBtnChoosePic = (Button) findViewById(R.id.btn_choose_pic);
    }

    public void initEvents(){
        mBtnChoosePic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(DemoActivity.this, ChoosePicActivity.class);
//        intent.setClassName(DemoActivity.this, "com.example.baifan.choosepicdemo.DemoActivity");
        //传入图片最大的选择数
        intent.putExtra("chooseMaxCount", 9);
        //传递已经选择照片的数目
        intent.putExtra("selectPicCount", 2);
        startActivityForResult(intent, REQUEST_CHOOSE_PIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if(requestCode == REQUEST_CHOOSE_PIC){
            //进行后续操作
            List<String> fileList = data.getStringArrayListExtra("fileList");
            fillPic(fileList);
        }
    }

    /**
     * 填充图片
     * @param fileList
     */
    private void fillPic(List<String> fileList) {
        if(fileList != null && fileList.size() > 0){
            Toast.makeText(this, fileList.get(0), Toast.LENGTH_SHORT).show();
            File file = new File(fileList.get(0));
            Log.i("hehe", "文件大小:" + file.length());
            //800*800差不多够了
            BitmapUtil.handleBitmap(fileList.get(0), fileList.get(0), 800, 800);
            mImageLoader.displayImage(formatPicUrl(fileList.get(0)), mImgDemo);
            Log.i("hehe", "文件大小:" + file.length());
        }
    }

    /**
     * 格式化下url
     * @param picUrl
     * @return
     */
    public String formatPicUrl(String picUrl){
        return "file://" + picUrl;
    }
}
