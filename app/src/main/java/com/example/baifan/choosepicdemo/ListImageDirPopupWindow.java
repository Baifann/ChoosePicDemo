package com.example.baifan.choosepicdemo;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.baifan.choosepicdemo.adapter.ListDirPopAdapter;
import com.example.baifan.choosepicdemo.dto.FolderDTO;

import java.util.List;

/**
 * Created by baifan on 16/2/4.
 */
public class ListImageDirPopupWindow extends PopupWindow implements AdapterView.OnItemClickListener{
    private int mWidth;
    private int mHeight;
    private View mContvertView;
    private ListView mLv;
    /**
     * popListview的适配器
     */
    private ListDirPopAdapter mAdapter;

    private List<FolderDTO> mDatas;

    /**
     * 文件夹选中监听器
     */
    public interface OnDirSelectedListener{
            void onSelected(FolderDTO folder);
    }

    public OnDirSelectedListener mListener;

    public void setOnDirSelectedListener(OnDirSelectedListener mListener) {
        this.mListener = mListener;
    }

    public ListImageDirPopupWindow(Context context, List<FolderDTO> datas){
        calWidthAndHeight(context);

        mContvertView = LayoutInflater.from(context).inflate(R.layout.pop_choose_dir, null);
        mDatas = datas;

        setContentView(mContvertView);
        setWidth(mWidth);
        setHeight(mHeight);

        setFocusable(true);
        setTouchable(true);
        //点击外部可以消失
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE){
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        initViews(context);
        initEvent();

    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        mLv.setOnItemClickListener(this);
    }

    /**
     * 初始化控件
     */
    private void initViews(Context context) {
        mLv = (ListView) mContvertView.findViewById(R.id.lv_choose_dir);
        mAdapter = new ListDirPopAdapter(context, mDatas);
        mLv.setAdapter(mAdapter);
    }

    /**
     * 计算宽度和高度
     * @param context
     */
    private void calWidthAndHeight(Context context) {
        //设置宽度是屏幕宽度，高度为屏幕高度的70%
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        mWidth = outMetrics.widthPixels;
        mHeight = (int) (outMetrics.heightPixels * 0.7);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null){
            mListener.onSelected(mDatas.get(position));
            mAdapter.setSelectionChoose(position);
        }
    }
}
