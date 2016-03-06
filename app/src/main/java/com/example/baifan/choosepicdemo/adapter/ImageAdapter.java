package com.example.baifan.choosepicdemo.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.baifan.choosepicdemo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 选择图片adapter
 * Created by baifan on 16/2/4.
 */
public class ImageAdapter extends BaseAdapter {
    private String mDirPath;
    private List<String> mImgPaths;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private Context mContext;
    /**
     * 是否可以选中图片
     */
    private boolean isCanSelect = true;


    public interface OnImageAdapterListener{
        void onCamera();
        void onPhotoSelect();
    }

    private OnImageAdapterListener mListener;


    public void setOnCameraListener(OnImageAdapterListener listener){
        mListener = listener;
    }

    /**
     * 改变集合
     * @param imgPaths
     */
    public void setListAndDir(List<String> imgPaths, String dirPath){
        mImgPaths = imgPaths;
        mDirPath = dirPath;
        this.notifyDataSetChanged();
    }

    /**
     * 选中的图片
     */
    private Set<String> mSelectedList = new HashSet<String>();

    public ImageAdapter(Context context, List<String> datas, String dirPath){
        this.mDirPath = dirPath;
        this.mImgPaths = datas;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return mImgPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mImgPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_gri_pic, null);
            vh = new ViewHolder();
            vh.mImgItem = (ImageView) convertView.findViewById(R.id.img_item_img);
            vh.mImgBtnSelected = (ImageButton) convertView.findViewById(R.id.imgBtn_item_select);

            convertView.setTag(vh);
        }else{
            vh = (ViewHolder) convertView.getTag();
        }

        if(position == 0){
            vh.mImgBtnSelected.setVisibility(View.GONE);
            vh.mImgItem.setImageResource(R.drawable.photoadd);
//            mImageLoader.displayImage(formatRUrl("(R.drawable.photoadd"), vh.mImgItem);
        }else{
            //重置状态
            vh.mImgBtnSelected.setVisibility(View.VISIBLE);
            vh.mImgItem.setImageResource(R.drawable.pictures_no);
            vh.mImgBtnSelected.setImageResource(R.drawable.picture_unselected);
            vh.mImgItem.setColorFilter(null);
            //加载图片
            mImageLoader.displayImage(formatPicUrl(mDirPath + "/" + mImgPaths.get(position)), vh.mImgItem);


        }
        fillClickEvent(vh, position);
        return convertView;
    }

    /**
     * 添加点击事件
     * @param vh
     */
    public void fillClickEvent(final ViewHolder vh, final int position){
        final String filePath = mDirPath + "/" + mImgPaths.get(position);
        vh.mImgItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0){
                    //第一个点击进入拍摄
                    if(mListener != null){
                        mListener.onCamera();
                    }
                }else{
                    //进行其他操作
                    if(mSelectedList.contains(filePath)){
                        //已经选择
                        mSelectedList.remove(filePath);
                        vh.mImgItem.setColorFilter(null);
                        vh.mImgBtnSelected.setImageResource(R.drawable.picture_unselected);
                    }else{
                        if(!isCanSelect){
                            //如果不能选回去
                            return;
                        }
                        //未被选择
                        mSelectedList.add(filePath);
                        vh.mImgItem.setColorFilter(Color.parseColor("#77000000"));
                        vh.mImgBtnSelected.setImageResource(R.drawable.pictures_selected);
                    }
                    if(mListener != null){
                        mListener.onPhotoSelect();
                    }
                }
            }
        });

        if(mSelectedList.contains(filePath)){
            vh.mImgItem.setColorFilter(Color.parseColor("#77000000"));
            vh.mImgBtnSelected.setImageResource(R.drawable.pictures_selected);
        }
    }

    /**
     * 设置是否可以选中图片
     */
    public void setIsCanSelect(boolean isCanSelect){
        this.isCanSelect = isCanSelect;
    }

    /**
     * 获取选中数量
     */
    public int getSelectCount(){
        return mSelectedList.size();
    }

    class ViewHolder{
        ImageView mImgItem;
        ImageButton mImgBtnSelected;
    }

    /**
     * 格式化本地图片资源
     * @param picUrl
     * @return
     */
    public String formatPicUrl(String picUrl){
        return "file://" + picUrl;
    }

    /**
     * 格式化R文件中图片资源
     * @param resId
     * @return
     */
    public String formatRUrl(String resId){
        return "drawable://" + resId;
    }

    /**
     * 返回选中的list
     * @return
     */
    public ArrayList<String> getSelectedPic(){
        ArrayList<String> selectedList = new ArrayList<String>();
        selectedList.addAll(mSelectedList);
        return selectedList;
    }


}
