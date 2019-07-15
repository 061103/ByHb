package com.zhou.biyongxposed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 11929 on 2018/6/20.
 */

public class CountUnitAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> data;

    public CountUnitAdapter(Context mContext, List<String> data){
        super();
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public int getCount() {    //要绑定的数量
        return data.size();
    }

    @Override
    public Object getItem(int position) {  //根据索引获得该位置的对象
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {  //获取条目的id
        return position;
    }

    /**
     *
     * @param position  本条目在数据集的位置
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {  //获取该条目的布局界面
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.cointype,null);
        TextView textView = view.findViewById(R.id.textView);
        ImageView imageView = view.findViewById(R.id.image);
        imageView.setImageResource(R.mipmap.ic_launcher);
        textView.setText(data.get(position));
        return view;
    }
}
