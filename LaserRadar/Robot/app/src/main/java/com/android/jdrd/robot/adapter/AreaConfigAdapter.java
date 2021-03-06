package com.android.jdrd.robot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.activity.AreaConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by jdrd on 2017/6/15.
 *
 */

public class AreaConfigAdapter extends BaseAdapter {
    Context context;
    List<Map> list;

    public AreaConfigAdapter(Context _context, List<Map>  _list) {
        this.list = _list;
        this.context = _context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            final LayoutInflater inflater = (LayoutInflater) context
                           .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_card, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.imageview = (ImageView) convertView.findViewById(R.id.imageview);
            convertView.setTag(viewHolder);//讲ViewHolder存储在View中

        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
            viewHolder.text.setText(list.get(position).get("name").toString());
        if(position == AreaConfig.Current_position){
            viewHolder.imageview.setVisibility(View.VISIBLE);
        }else{
            viewHolder.imageview.setVisibility(View.GONE);
        }
        return convertView;
    }

    //内部类
     class ViewHolder{
        TextView text;
        ImageView imageview;
    }

}

