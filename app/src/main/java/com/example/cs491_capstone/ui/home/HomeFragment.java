package com.example.cs491_capstone.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.UserUsageInfo;
import com.example.cs491_capstone.ui.detailed.DetailedAppActivity;
import com.example.cs491_capstone.ui.home.home_graphs.HomeNotificationGraphFragment;
import com.example.cs491_capstone.ui.home.home_graphs.HomeUnlocksGraphFragment;
import com.example.cs491_capstone.ui.home.home_graphs.HomeUsageGraphFragment;
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

import static com.example.cs491_capstone.App.localDatabase;
import static com.example.cs491_capstone.App.setListViewHeightBasedOnChildren;
import static com.example.cs491_capstone.services.BackgroundMonitor.timeLeft;
import static com.example.cs491_capstone.ui.settings.SettingsFragment.parentalControls;
import static com.example.cs491_capstone.ui.settings.SettingsFragment.trackedAppsChanged;


public class HomeFragment extends Fragment {
    private ListView mostUsedListView;
    private MostUsedAppsListAdapter listAdapter;
    //TIMER
    private TextView hour_timer;
    private TextView minutes_timer;
    private TextView seconds_timer;
    private List<UserUsageInfo> userInfo;
    private TextView totalUsage, percentDelta;
    private CardView phoneTimerCard;
    private SharedPreferences preferences;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mostUsedListView = view.findViewById(R.id.most_used_list);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userInfo = localDatabase.topAppsUsedToday(App.DATE);


        //HEADER
        totalUsage = view.findViewById(R.id.total_usage);
        percentDelta = view.findViewById(R.id.percent_delta);

        String time = App.timeFormatter(Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)));
        totalUsage.setText(time);

        //PARENTAL
        phoneTimerCard = view.findViewById(R.id.time_card_container);
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());


        ///VIEWS
        TextView date = view.findViewById(R.id.date_actual);
        DateTimeZone timeZone = DateTimeZone.forID("America/Montreal");
        DateTime now = DateTime.now(timeZone);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy");
        date.setText(dtf.print(now));


        ///TIMER
        hour_timer = view.findViewById(R.id.timer_hours);
        minutes_timer = view.findViewById(R.id.timer_minutes);
        seconds_timer = view.findViewById(R.id.timer_seconds);


        //NOW WE FORM A LIST OF THE TOP X APPS AND PASS IT TO THE ADAPTER
        listAdapter = new MostUsedAppsListAdapter(getContext(), userInfo);
        mostUsedListView.setAdapter(listAdapter);
        setListViewHeightBasedOnChildren(mostUsedListView);


        TabLayout tabLayout = view.findViewById(R.id.graph_choice);
        ViewPager viewPager = view.findViewById(R.id.graph_container);
        ViewPageAdapter adapter = new ViewPageAdapter(getFragmentManager(), FragmentPagerAdapter.POSITION_UNCHANGED);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new HomeUsageGraphFragment(), "Usage");
        adapter.addFragment(new HomeNotificationGraphFragment(), "Notifications");
        adapter.addFragment(new HomeUnlocksGraphFragment(), "Unlocks");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        mostUsedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserUsageInfo item = (UserUsageInfo) parent.getItemAtPosition(position);

                Intent intent = new Intent(getContext(), DetailedAppActivity.class);
                intent.putExtra("APP", item);
                startActivity(intent);

            }
        });
    }


    @Override
    public void onResume() {
        getContext().getTheme().applyStyle(R.style.AppTheme, true);
        super.onResume();
        parentalControls = preferences.getBoolean("parental_controls", false);

        //RESIZE EVERY TIME ON ONRESUME IS INEFFICIENT, WE KNOW CHANGES WILL ONLY OCCUR IF AN APP HAS BEEN REMOVED FROM TRACK LIST
        if (trackedAppsChanged) {
            userInfo = localDatabase.topAppsUsedToday(App.DATE);
            setListViewHeightBasedOnChildren(mostUsedListView);
        } else {
            userInfo = localDatabase.topAppsUsedToday(App.DATE);
        }
        listAdapter.notifyDataSetChanged();

        String time = App.timeFormatter(Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)));
        totalUsage.setText(time);

        if (parentalControls) {
            phoneTimerCard.setVisibility(View.VISIBLE);
            if (timeLeft > 0) {
                updateTimer();
            }

        } else {
            phoneTimerCard.setVisibility(View.GONE);
        }

        setPercentDelta();



    }

    private void setPercentDelta() {
        int todayIndex = App.currentPeriod.get(0).indexOf(App.DATE);


        if (todayIndex != 0) {
            String yesterdayDate = App.currentPeriod.get(0).get(todayIndex - 1);

            double todayUsage = (double) Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)) / 60000;
            double yesterdayUsage = (double) Long.parseLong(App.localDatabase.getSumTotalStat(yesterdayDate, DatabaseHelper.USAGE_TIME)) / 60000;

            String text;
            if (yesterdayUsage == 0) {
                text = "+" + 100 + "%";
                percentDelta.setText(text);
            } else {
                double delta = ((todayUsage - yesterdayUsage) / yesterdayUsage) * 100;
                if (delta > 0) {
                    text = String.format(Locale.ENGLISH, "%s%.2f%s", "+", delta, "%");
                    percentDelta.setText(text);
                } else {
                    text = String.format(Locale.ENGLISH, "%.2f%s", delta, "%");
                    percentDelta.setText(text);
                }
            }

        } else {
            String yesterdayDate = App.currentPeriod.get(1).get(6);

            double todayUsage = (double) Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)) / 60000;
            double yesterdayUsage = (double) Long.parseLong(App.localDatabase.getSumTotalStat(yesterdayDate, DatabaseHelper.USAGE_TIME)) / 60000;

            String text;
            if (yesterdayUsage == 0) {
                text = "+" + 100 + "%";
                percentDelta.setText(text);
            } else {
                double delta = ((todayUsage - yesterdayUsage) / yesterdayUsage) * 100;
                if (delta > 0) {
                    text = String.format(Locale.ENGLISH, "%s%.2f%s", "+", delta, "%");
                    percentDelta.setText(text);
                } else {
                    text = String.format(Locale.ENGLISH, "%.2f%s", delta, "%");
                    percentDelta.setText(text);
                }
            }
        }

    }


    private void updateTimer() {
        ///TIMER
        CountDownTimer timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                int hours = (int) (timeLeft / (1000 * 60 * 60) % 24);
                int minutes = (int) (timeLeft / (1000 * 60) % 60);
                int seconds = (int) (timeLeft / 1000) % 60;

                if (hours < 10) {
                    hour_timer.setText(String.format(Locale.ENGLISH, "0%d", hours));
                } else {
                    hour_timer.setText(String.format(Locale.ENGLISH, "%d", hours));
                }
                if (minutes < 10) {
                    minutes_timer.setText(String.format(Locale.ENGLISH, "0%d", minutes));
                } else {
                    minutes_timer.setText(String.format(Locale.ENGLISH, "%d", minutes));
                }
                if (seconds < 10) {
                    seconds_timer.setText(String.format(Locale.ENGLISH, "0%d", seconds));
                } else {
                    seconds_timer.setText(String.format(Locale.ENGLISH, "%d", seconds));
                }

            }

            @Override
            public void onFinish() {
            }
        }.start();
    }


    static class MostUsedAppsViewHolder {
        TextView name;
        ImageView icon;
        TextView time;
    }

    private static class MostUsedAppsListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<UserUsageInfo> installedAppInfoList;

        //CONSTRUCTOR
        MostUsedAppsListAdapter(Context context, List<UserUsageInfo> installedAppInfoList) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            MostUsedAppsViewHolder listHolder;

            if (convertView == null) {
                //CREATE VIEWHOLDER AND INFLATE LAYOUT
                listHolder = new MostUsedAppsViewHolder();
                convertView = inflater.inflate(R.layout.usage_card_layout, parent, false);

                //ASSIGN VIEW HOLDER CLASS VARIABLE TO LAYOUT
                listHolder.icon = convertView.findViewById(R.id.icon);
                listHolder.name = convertView.findViewById(R.id.name);
                listHolder.time = convertView.findViewById(R.id.time);

                convertView.setTag(listHolder);

            } else {
                listHolder = (MostUsedAppsViewHolder) convertView.getTag();
            }

            listHolder.icon.setImageDrawable(installedAppInfoList.get(position).getIcon());
            listHolder.name.setText(installedAppInfoList.get(position).getSimpleName());
            ///

            //String packageName = installedAppInfoList.get(position).getPackageName();

            //String time = installedAppInfoList.get(position).formatTime(installedAppInfoList.get(position).getUsage());
            //long time = installedAppInfoList.get(position).getDailyUsage();
            // Log.i("TRACK", "" + time);
            listHolder.time.setText(String.valueOf(installedAppInfoList.get(position).formatTime()));


            return convertView;
        }


    }


}
