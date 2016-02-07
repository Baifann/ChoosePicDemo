package com.example.baifan.choosepicdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.baifan.choosepicdemo.R;
import com.example.baifan.choosepicdemo.dto.FolderDTO;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by baifan on 16/2/4.
 */
public class ListDirPopAdapter extends ArrayAdapter<FolderDTO>{
    private LayoutInflater mInflater;
    private List<FolderDTO> mDatas;
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    public ListDirPopAdapter(Context context, List<FolderDTO> objects) {
        super(context,0, objects);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null){
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_choose_dir, null);

            vh.mImg = (ImageView) convertView.findViewById(R.id.img_choose_dir);
            vh.mTvDirCount = (TextView) convertView.findViewById(R.id.tv_dir_num);
            vh.mTvDirName = (TextView) convertView.findViewById(R.id.tv_dir_name);

            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }
        FolderDTO folderDTO = getItem(position);
        //初始化
        vh.mImg.setImageResource(R.drawable.pictures_no);

        mImageLoader.displayImage(formatPicUrl(folderDTO.getFirstImgPath()), vh.mImg);

        vh.mTvDirName.setText(folderDTO.getName());
        vh.mTvDirCount.setText(folderDTO.getCount() + "");

        return convertView;
    }

    private class ViewHolder{
        ImageView mImg;
        TextView mTvDirName;
        TextView mTvDirCount;
    }

    /**
     * 格式化下路径
     * @param picUrl
     * @return
     */
    public String formatPicUrl(String picUrl){
        return "file://" + picUrl;
    }
}
