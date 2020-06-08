package com.xw.idld.aschwitkey.adapter;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xw.idld.aschwitkey.R;
import com.xw.idld.aschwitkey.entity.AnAuditBean;
import com.youth.banner.util.BannerUtils;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 自定义布局，网络图片
 */
public class ImageNetAdapter extends BannerAdapter<AnAuditBean.Notice,ImageNetAdapter.ImageHolder> {

    public ImageNetAdapter(List<AnAuditBean.Notice> mDatas) {
        super(mDatas);
    }

    @Override
    public ImageHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView = (ImageView) BannerUtils.getView(parent, R.layout.banner_image);
        //通过裁剪实现圆角
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BannerUtils.setBannerRound(imageView,20);
        }
        return new ImageHolder(imageView);
    }

    @Override
    public void onBindView(ImageHolder holder, AnAuditBean.Notice data, int position, int size) {
        Glide.with(holder.itemView)
                .load(data.getCoverImg())
//                .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
                .into(holder.imageView);
    }

    public class ImageHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageHolder(@NonNull View view) {
            super(view);
            this.imageView = (ImageView) view;
        }
    }

}
