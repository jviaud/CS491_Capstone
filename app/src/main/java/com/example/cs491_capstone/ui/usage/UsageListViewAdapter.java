package com.example.cs491_capstone.ui.usage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;

import java.util.List;
import java.util.Locale;

public class UsageListViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<String> usedList;
    private boolean byCategory;
    private PackageManager packageManager;
    private String hour;
    private String date;

    //CONSTRUCTOR
    public UsageListViewAdapter(Context context, List<String> usedList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.usedList = usedList;
        packageManager = context.getPackageManager();
    }

    public void setByCategory(boolean byCategory) {
        this.byCategory = byCategory;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setDay(String date) {
        this.date = date;
    }

    @Override
    public int getCount() {
        return usedList.size();
    }

    @Override
    public Object getItem(int position) {
        return usedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UsageListViewHolder listHolder;

        if (convertView == null) {
            //CREATE VIEWHOLDER AND INFLATE LAYOUT
            listHolder = new UsageListViewHolder();
            convertView = inflater.inflate(R.layout.usage_card_layout, parent, false);

            //ASSIGN VIEW HOLDER CLASS VARIABLE TO LAYOUT
            listHolder.icon = convertView.findViewById(R.id.icon);
            listHolder.name = convertView.findViewById(R.id.name);
            listHolder.time = convertView.findViewById(R.id.time);

            convertView.setTag(listHolder);

        } else {
            listHolder = (UsageListViewHolder) convertView.getTag();
        }

        if (byCategory) {


        } else {
            String packageName = usedList.get(position);
            try {
                ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
                listHolder.name.setText(ai.loadLabel(packageManager).toString());
                listHolder.icon.setImageDrawable(ai.loadIcon(packageManager));

                long value = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(date, hour, DatabaseHelper.PACKAGE_NAME, packageName));
                int hours = (int) (value / (60) % 24);
                int minutes = (int) (value % 60);
                String formattedVal;
                if (hours == 0) {
                    formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
                } else {
                    formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
                }
                listHolder.time.setText(formattedVal);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }


        return convertView;
    }


    static class UsageListViewHolder {
        ImageView icon;
        TextView name;
        TextView time;
    }
}
