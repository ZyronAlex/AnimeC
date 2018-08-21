package com.example.adapter;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.item.ItemCategory;
import com.squareup.picasso.Picasso;
import com.tecapps.AnimeC.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends ArrayAdapter<ItemCategory> {

    private Activity activity;
    private List<ItemCategory> items;
    private ItemCategory objAllBean;
    private int row;
    private ArrayList<ItemCategory> arraylist;

    public CategoryAdapter(Activity act, int resource, List<ItemCategory> arrayList) {
        super(act, resource, arrayList);
        this.activity = act;
        this.row = resource;
        this.items = arrayList;
        this.arraylist = new ArrayList<ItemCategory>();
        this.arraylist.addAll(items);

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);

            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if ((items == null) || ((position + 1) > items.size()))
            return view;

        objAllBean = items.get(position);

        holder.txt = (TextView) view.findViewById(R.id.text);
        holder.img_cat = (ImageView) view.findViewById(R.id.image);
        holder.txt.setText(objAllBean.getCategoryName().toString());
        Picasso.with(activity).load(objAllBean.getCategoryImageurl()).placeholder(R.drawable.placeholder).into(holder.img_cat);

         return view;

    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        items.clear();
        if (charText.length() == 0) {
            items.addAll(arraylist);
        } else {
            for (ItemCategory wp : arraylist) {
                if (wp.getCategoryName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    items.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
    public class ViewHolder {

        public TextView txt;
        public ImageView img_cat;
    }

}
