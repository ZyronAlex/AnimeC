package com.example.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.item.ItemComment;


import com.squareup.picasso.Picasso;
import com.tecapps.AnimeC.R;

import java.util.ArrayList;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ItemRowHolder> {

    private ArrayList<ItemComment> dataList;
    private Context mContext;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;


    public CommentAdapter(Context context, ArrayList<ItemComment> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment_header, parent, false);
            return new VHHeader(v);
        } else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment, parent, false);
            return new VHItem(v);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(ItemRowHolder viewHolder, int position) {

        if (viewHolder.getItemViewType() == TYPE_HEADER) {
            VHHeader VHheader = (VHHeader) viewHolder;
            VHheader.avatar.setContentDescription(mContext.getString(R.string.app_name));
//            if (MyApplication.getInstance().getIsLogin()) {
                Picasso.with(mContext).load(R.mipmap.app_icon).into(VHheader.avatar);
//            }
        } else if (viewHolder.getItemViewType() == TYPE_ITEM) {
            final VHItem holder = (VHItem) viewHolder;
            final ItemComment singleItem = dataList.get(position);
            holder.itemTitle.setText(singleItem.getUserName());
            holder.itemDesc.setText(singleItem.getCommentMsg());
            //Picasso.with(mContext).load(singleItem.getImageIcon()).into(holder.itemImage);

        }
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public int getItemViewType(int position) {
        return dataList.get(position) != null ? TYPE_ITEM : TYPE_HEADER;
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        public ItemRowHolder(View itemView) {
            super(itemView);
        }
    }

    class VHHeader extends ItemRowHolder {
        ShapedImageView avatar;

        public VHHeader(View itemView) {
            super(itemView);
            this.avatar = (ShapedImageView) itemView.findViewById(R.id.avatar);
        }
    }

    class VHItem extends ItemRowHolder {
        protected TextView itemTitle, itemDesc;
        protected ShapedImageView itemImage;

        public VHItem(View itemView) {
            super(itemView);
            this.itemTitle = (TextView) itemView.findViewById(R.id.name);
            this.itemDesc = (TextView) itemView.findViewById(R.id.comment);
            this.itemImage = (ShapedImageView) itemView.findViewById(R.id.avatar);
        }
    }
}
