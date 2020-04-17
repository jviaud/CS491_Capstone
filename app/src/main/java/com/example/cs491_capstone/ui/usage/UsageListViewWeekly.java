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
import com.example.cs491_capstone.UserUsageInfo;

import java.util.List;
import java.util.Locale;

public class UsageListViewWeekly extends BaseAdapter {
    private LayoutInflater inflater;
    private List<UserUsageInfo> usedList;
    private boolean byCategory;
    private PackageManager packageManager;
    private String date;
    private String column;

    //CONSTRUCTOR
    public UsageListViewWeekly(Context context, List<UserUsageInfo> usedList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.usedList = usedList;
        packageManager = context.getPackageManager();
    }

    public void setByCategory(boolean byCategory) {
        this.byCategory = byCategory;
    }

    public void setDay(String date) {
        this.date = date;
    }

    public void setColumn(String column) {
        this.column = column;
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
        com.example.cs491_capstone.ui.usage.UsageListViewAdapter.UsageListViewHolder listHolder;

        if (convertView == null) {
            //CREATE VIEW HOLDER AND INFLATE LAYOUT
            listHolder = new com.example.cs491_capstone.ui.usage.UsageListViewAdapter.UsageListViewHolder();
            convertView = inflater.inflate(R.layout.usage_card_layout, parent, false);

            //ASSIGN VIEW HOLDER CLASS VARIABLE TO LAYOUT
            listHolder.icon = convertView.findViewById(R.id.icon);
            listHolder.name = convertView.findViewById(R.id.name);
            listHolder.time = convertView.findViewById(R.id.time);

            convertView.setTag(listHolder);

        } else {
            listHolder = (com.example.cs491_capstone.ui.usage.UsageListViewAdapter.UsageListViewHolder) convertView.getTag();
        }

        if (byCategory) {
            String category = usedList.get(position).getCategory();

            listHolder.name.setText(category);
            String formattedVal = "";
            if (column.equals(DatabaseHelper.USAGE_TIME)) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStatByCategory(date, column, category)) / 60000;
                int hours = (int) (value / (60) % 24);
                int minutes = (int) (value % 60);


                if (hours == 0) {
                    formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
                } else {
                    formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
                }
            } else {
                long value = Long.parseLong(App.localDatabase.getSumTotalStatByCategory(date, column, category));
                formattedVal = " " + value;
            }
            listHolder.time.setText(formattedVal);


            switch (category) {
                case App.CATEGORY_GAME:
                    listHolder.icon.setImageResource(R.drawable.category_game);
                    break;
                case App.CATEGORY_MEDIA:
                    listHolder.icon.setImageResource(R.drawable.categoroy_video);
                    break;
                case App.CATEGORY_SOCIAL:
                    listHolder.icon.setImageResource(R.drawable.categoory_social);
                    break;
                case App.CATEGORY_TOOLS:
                    listHolder.icon.setImageResource(R.drawable.category_news);
                    break;
                case App.CATEGORY_PRODUCTIVITY:
                    listHolder.icon.setImageResource(R.drawable.category_productivity);
                    break;
                default:
                    listHolder.icon.setImageResource(R.drawable.category_other);
            }


        } else {
            String packageName = usedList.get(position).getPackageName();
            try {
                ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
                listHolder.name.setText(ai.loadLabel(packageManager).toString());
                listHolder.icon.setImageDrawable(ai.loadIcon(packageManager));

                String formattedVal = "";
                if (column.equals(DatabaseHelper.USAGE_TIME)) {
                    long value = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(date, column, packageName)) / 60000;
                    int hours = (int) (value / (60) % 24);
                    int minutes = (int) (value % 60);


                    if (hours == 0) {
                        formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
                    } else {
                        formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
                    }
                } else {
                    long value = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(date, column, packageName));
                    formattedVal = " " + value;

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

