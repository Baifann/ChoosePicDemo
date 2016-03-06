package com.example.baifan.choosepicdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baifan.choosepicdemo.adapter.ImageAdapter;
import com.example.baifan.choosepicdemo.dto.FolderDTO;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by baifan on 16/2/4.
 */
public class ChoosePicActivity extends Activity implements ListImageDirPopupWindow.OnDirSelectedListener, ImageAdapter.OnImageAdapterListener{
    /**
     * 显示图片控件
     */
    private GridView mGridView;
    /**
     * 底部布局
     */
    private RelativeLayout mLyBottom;
    /**
     * 文件名
     */
    private TextView mTvDirName;
    /**
     * 文件数目
     */
    private TextView mTvFileNum;
    /**
     * 显示图片路径的集合
     */
    private List<String> mImgList = new ArrayList<String>();
    /**
     * 当前文件夹名称
     */
    private File mCurrentDir;
    /**
     * 文件夹中文件的数量
     */
    private int mMaxCount;

    private List<FolderDTO> mFolderList = new ArrayList<FolderDTO>();
    /**
     * 进度条
     */
    private ProgressDialog mProgress;

    private ImageAdapter mImgAdapter;
    /**
     * 选择文件夹
     */
    private ListImageDirPopupWindow mPopWindow;
    /**
     * 存放照片的文件夹
     */
    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    /**
     * 照相机拍照得到的图片
     */
    private File mCurrentphotoFile;

    private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1022;
    /**
     * 确认按钮
     */
    private Button mbtnOk;
    /**
     * 是否多选
     */
    private boolean isMultiChoose;
    /**
     * 设置选择图片的最大数
     */
    private int mChooseMaxCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pic);
        //从Intent获取数据
        getDataFromIntent();

        initViews();
        //手机图片
        initDatas();
        initEvents();
    }

    /**
     * 初始化控件
     */
    public void initViews() {
        mGridView = (GridView) findViewById(R.id.griview_choosepic);
        mLyBottom = (RelativeLayout) findViewById(R.id.ly_bottom);
        mTvDirName = (TextView) findViewById(R.id.tv_dir_name);
        mTvFileNum = (TextView) findViewById(R.id.tv_dir_num);
        mbtnOk = (Button) findViewById(R.id.btn_ok);
    }

    /**
     * 初始化数据
     */
    public void initDatas() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前存储卡不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgress = ProgressDialog.show(this, null, "正在加载...");

        new Thread() {
            @Override
            public void run() {
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                ContentResolver cr = ChoosePicActivity.this.getContentResolver();
                Cursor cursor = cr.query(mImgUri, null, MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ? ", new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

                Set<String> mDirPaths = new HashSet<String>();
                while (cursor.moveToNext()){
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    File parentFile = new File(path).getParentFile();
                    if(parentFile == null){
                        continue;
                    }

                    String dirPath = parentFile.getAbsolutePath();

                    FolderDTO folder = null;
                    if(mDirPaths.contains(dirPath)){
                        continue;
                    }else{
                        mDirPaths.add(dirPath);

                        folder = new FolderDTO();
                        folder.setDir(dirPath);
                        folder.setFirstImgPath(path);
                    }

                    if(parentFile.list() == null){
                        continue;
                    }

                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if(filename.endsWith("jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")){
                                return true;
                            }

                            return false;
                        }
                    }).length;

                    folder.setCount(picSize);
                    //将图片文件夹存放进集合中
                    mFolderList.add(folder);

                    if(picSize > mMaxCount){
                        mMaxCount = picSize;
                        mCurrentDir = parentFile;
                    }

                }

                cursor.close();
                //通知handle扫描完成
                mHandler.sendEmptyMessage(DATA_LOADED);
            }
        }.start();

    }

    /**
     * 设置确认按钮
     */
    public void setCommitBtnText(){
        mbtnOk.setEnabled(false);
        StringBuilder sb = new StringBuilder("确定");
        if(mImgAdapter == null){
            mbtnOk.setText(sb.toString());
            return;
        }
        int selectPicCount = mImgAdapter.getSelectCount();
        Log.i("!!!", "selectPicCount:" + selectPicCount);
        if(selectPicCount == 0){
            mbtnOk.setText(sb.toString());
        }else {
            mbtnOk.setEnabled(true);
            sb.append("(").append(selectPicCount).append("/").append(mChooseMaxCount).append(")");
            mbtnOk.setText(sb);
        }
        notifyAdapterIsCanSelect(selectPicCount);
    }

    /**
     * 通知adapter是否可以可以点击的状态
     */
    public void notifyAdapterIsCanSelect(int imgSelectCount){
        if(mImgAdapter == null){
            return;
        }
        if(imgSelectCount == mChooseMaxCount){
            mImgAdapter.setIsCanSelect(false);
        }else{
            mImgAdapter.setIsCanSelect(true);
        }
    }

    private static final int DATA_LOADED = 0x110;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == DATA_LOADED){
                mProgress.dismiss();
                //绑定数据到view中
                data2View();

                initPopupWindow();
            }
        }
    };

    /**
     * 初始化popupWindow
     */
    private void initPopupWindow() {
        mPopWindow = new ListImageDirPopupWindow(this, mFolderList);
        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });

        mPopWindow.setOnDirSelectedListener(this);
    }

    /**
     * 内容区域变亮
     */
    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }


    private void data2View() {
        if(mCurrentDir == null){
            Toast.makeText(this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> addList = Arrays.asList(mCurrentDir.list());
        fillFirstItemList();
        mImgList.addAll(addList);

        mImgAdapter = new ImageAdapter(this, mImgList, mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(mImgAdapter);
        mImgAdapter.setOnCameraListener(this);
        mTvFileNum.setText(mMaxCount + "");
        mTvDirName.setText(mCurrentDir.getName());
    }

    /**
     * 初始化事件
     */
    public void initEvents() {
        mLyBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindow.setAnimationStyle(R.style.dir_pupupwindow_anim);
                //设置显示位置
                mPopWindow.showAsDropDown(mLyBottom, 0 ,0);
                lightOff();
            }
        });

        //点击确认后
        mbtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("fileList", mImgAdapter.getSelectedPic());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * 内容区域变暗
     */
    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }


    @Override
    public void onSelected(FolderDTO folder) {
        //更新adapter
        mCurrentDir = new File(folder.getDir());

        List<String> addList = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.endsWith("jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")){
                    return true;
                }

                return false;
            }
        }));
        fillFirstItemList();
        mImgList.addAll(addList);

        //可以用另外的方法
        mImgAdapter.setListAndDir(mImgList, mCurrentDir.getAbsolutePath());

        mTvDirName.setText(mCurrentDir.getName());
        mTvFileNum.setText(mImgList.size() + "");
        mPopWindow.dismiss();

    }

    /**
     * 填充第一个item
     */
    public void fillFirstItemList(){
        if(mImgList != null){
            mImgList.clear();
            mImgList.add("");
        }
    }

    /**
     * 用当前时间给取得的图片命名
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date) + ".jpg";
    }

    /**
     * 进行拍照的方法
     */
    protected void openCamera() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();// 创建照片的存储目录
            mCurrentphotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
            final Intent intent = getTakePickIntent(mCurrentphotoFile);
            startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
        } catch (ActivityNotFoundException e) {
        }
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    @Override
    public void onCamera() {
        //进行拍照
        openCamera();
    }

    @Override
    public void onPhotoSelect() {
        setCommitBtnText();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
            ArrayList<String> fileList = new ArrayList<String>();
            fileList.add(mCurrentphotoFile.getAbsoluteFile() + "");
            finishCurrentActivity(fileList);
        }
    }

    /**
     * 结束当前页面
     * @param fileList
     */
    public void finishCurrentActivity(ArrayList<String> fileList){
        Intent intent = new Intent();
        intent.putStringArrayListExtra("fileList", fileList);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 获取data从intent
     */
    public void getDataFromIntent(){
        Intent intent = getIntent();
        //多选几张
        mChooseMaxCount = intent.getIntExtra("chooseMaxCount", 1);

        //设置是否多选
        setSsMultiChoose(mChooseMaxCount);
    }

    /**
     * 设置是否多选
     */
    private void setSsMultiChoose(int chooseMaxCount){
        if(chooseMaxCount > 1){
            isMultiChoose = true;
        }
    }
}
