package com.example.cs491_capstone.ui.goal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cs491_capstone.InstalledAppInfo;
import com.example.cs491_capstone.R;

import java.util.List;

public class InstalledAppsListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<InstalledAppInfo> installedAppInfoList;

    //CONSTRUCTOR
    InstalledAppsListAdapter(Context context, List<InstalledAppInfo> installedAppInfoList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.installedAppInfoList = installedAppInfoList;
    }


    @Override
    public int getCount() {
        return installedAppInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return installedAppInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        InstalledAppsViewHolder listHolder;

        if (convertView == null) {
            //CREATE VIEWHOLDER AND INFLATE LAYOUT
            listHolder = new InstalledAppsViewHolder();
            convertView = inflater.inflate(R.layout.new_goal_app_card, parent, false);

            //ASSIGN VIEW HOLDER CLASS VARIABLE TO LAYOUT
            listHolder.icon = convertView.findViewById(R.id.icon);
            listHolder.name = convertView.findViewById(R.id.name);

            convertView.setTag(listHolder);

        } else {
            listHolder = (InstalledAppsViewHolder) convertView.getTag();
        }

        listHolder.icon.setImageDrawable(installedAppInfoList.get(position).getIcon());
        listHolder.name.setText(installedAppInfoList.get(position).getSimpleName());

        return convertView;
    }




    static class InstalledAppsViewHolder {
        TextView name;
        ImageView icon;
    }

}
