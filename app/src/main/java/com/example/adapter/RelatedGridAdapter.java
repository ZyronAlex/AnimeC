package com.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.item.ItemRelated;
import com.example.util.Constant;
import com.squareup.picasso.Picasso;
import com.tecapps.AnimeC.R;
import com.tecapps.AnimeC.VideoPlay;

import java.util.ArrayList;

public class RelatedGridAdapter extends RecyclerView.Adapter<RelatedGridAdapter.ItemRowHolder> {

    private ArrayList<ItemRelated> dataList;
    private Context mContext;


    public RelatedGridAdapter(Context context, ArrayList<ItemRelated> dataList) {
        this.dataList = dataList;
        this.mContext = context;

    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_row_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemRowHolder holder, final int position) {
        final ItemRelated singleItem = dataList.get(position);

        if (singleItem.getRVideoType().equals("local")) {
            Picasso.with(mContext).load(singleItem.getRImageUrl()).placeholder(R.drawable.placeholder).into(holder.imgv_latetst);
        } else if (singleItem.getRVideoType().equals("server_url")) {
            Picasso.with(mContext).load(singleItem.getRImageUrl()).placeholder(R.drawable.placeholder).into(holder.imgv_latetst);
        } else if (singleItem.getRVideoType().equals("youtube")) {
            Picasso.with(mContext).load(Constant.YOUTUBE_IMAGE_FRONT + singleItem.getRVideoId() + Constant.YOUTUBE_SMALL_IMAGE_BACK).placeholder(R.drawable.placeholder).into(holder.imgv_latetst);
        } else if (singleItem.getRVideoType().equals("dailymotion")) {
            Picasso.with(mContext).load(Constant.DAILYMOTION_IMAGE_PATH + singleItem.getRVideoId()).placeholder(R.drawable.placeholder).into(holder.imgv_latetst);
        } else if (singleItem.getRVideoType().equals("vimeo")) {
            Picasso.with(mContext).load("" + singleItem.getRImageUrl().toString()).placeholder(R.drawable.placeholder).into(holder.imgv_latetst);
        }

        holder.name.setText(singleItem.getRVideoName().toString());
        holder.txt_time.setText("(" + singleItem.getRDuration().toString() + ")");
        holder.txt_category.setText(singleItem.getRCategoryName().toString());
        holder.txt_rate.setText(singleItem.getRVideoRate());

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Constant.VIDEO_IDD=singleItem.getRvid();
                Intent intplay=new Intent(mContext,VideoPlay.class);
                intplay.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intplay);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        public ImageView imgv_latetst;
        public TextView name, txt_time, txt_category,txt_rate;
        public LinearLayout lyt_parent;

        public ItemRowHolder(View itemView) {
            super(itemView);
            imgv_latetst = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.text);
            txt_time = (TextView) itemView.findViewById(R.id.textView2);
            txt_category = (TextView) itemView.findViewById(R.id.textcatname);
             lyt_parent = (LinearLayout) itemView.findViewById(R.id.rootLayout);
            txt_rate=(TextView)itemView.findViewById(R.id.textView_view);
        }
    }


}
