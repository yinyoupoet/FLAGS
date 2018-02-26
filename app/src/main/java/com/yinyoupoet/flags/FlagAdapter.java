package com.yinyoupoet.flags;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by hasee on 2018/2/14.
 */

public class FlagAdapter extends ArrayAdapter<Flag>{

    private static final int CONTINUE = 1;
    private static final int COMPLISH = 0;

    private int resourceId;

    public FlagAdapter(Context context,int textViewResourceId,List<Flag> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Flag flag = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,null);

        TextView title = view.findViewById(R.id.tv_flag_title);
        TextView content = view.findViewById(R.id.tv_flag_content);
        TextView date = view.findViewById(R.id.tv_flag_date);

        title.setText("["+flag.getState()+"] "+flag.getTitle());
        content.setText(flag.getContent());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        date.setText(dateFormat.format(flag.getDate()));
        return view;
    }


    //region 设置菜单

    @Override
    public int getItemViewType(int position) {
        Flag flag = getItem(position);
        if(flag.getState().toString().trim().equals("待完成")){
            return COMPLISH;
        }else{
            return CONTINUE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    //endregion
}
