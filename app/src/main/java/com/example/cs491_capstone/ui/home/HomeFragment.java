package com.example.cs491_capstone.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.UserUsageInfo;
import com.example.cs491_capstone.services.BackgroundMonitor;
import com.example.cs491_capstone.ui.home.home_graphs.HomeNotificationGraphFragment;
import com.example.cs491_capstone.ui.home.home_graphs.HomeUnlocksGraphFragment;
import com.example.cs491_capstone.ui.home.home_graphs.HomeUsageGraphFragment;
import com.google.android.material.tabs.TabLayout;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.cs491_capstone.App.setListViewHeightBasedOnChildren;
import static com.example.cs491_capstone.MainActivity.usageInfo;


public class HomeFragment extends Fragment {
    /**
     * This is the maximum number of apps we want to show total usage time for, because we will allow the user to set the maximum number themselves we have a variable for it here
     */
    private static int maximumOnDisplay = 5;
    private ListView mostUsedListView;
    private MostUsedAppsListAdapter listAdapter;
    private ArrayList<UserUsageInfo> topItems = new ArrayList<>();
    //TIMER
    private Long timeLeft = 0L;
    private TextView hour_timer;
    private TextView minutes_timer;
    private TextView seconds_timer;


    private TextView totalUsage, percentDelta;

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

        //HEADER
        totalUsage = view.findViewById(R.id.total_usage);
        percentDelta = view.findViewById(R.id.percent_delta);
        setPercentDelta();

        String time = App.timeFormatter(Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)));
        totalUsage.setText(time);


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


        //SORT THE LIST OF APP USAGE INFO, THIS IS SORTED BY TOTAL TIME USED FOR THE DAY

        /*
        EVEN THOUGH WE WANT TO DISPLAY UP TO X AMOUNT OF APPS ON THE HOME PAGE, THERE IS NO REASON TO DISPLAY USAGE FOR APPS WITH 0 USAGE TIME
        SO WE LOOK AT THE TOP X AMOUNT OF APPS AND IF THEY DON'T HAVE A TIME GREATER THAN 0 WE DON'T INCLUDE IT IN THE DISPLAY
         */

        for (int i = 0; i <= maximumOnDisplay; i++) {
            if (usageInfo.get(i).getDailyUsage() > 0) {
                topItems.add(usageInfo.get(i));
            } else
                break;
        }
        //NOW WE FORM A SUBLIST OF THE TOP X APPS AND PASS IT TO THE ADAPTER
        listAdapter = new MostUsedAppsListAdapter(getContext(), topItems);
        mostUsedListView.setAdapter(listAdapter);
        setListViewHeightBasedOnChildren(mostUsedListView);


        TabLayout tabLayout = view.findViewById(R.id.graph_choice);
        ViewPager viewPager = view.findViewById(R.id.graph_container);
        ViewPageAdapter adapter = new ViewPageAdapter(getFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new HomeUsageGraphFragment(), "Usage");
        adapter.addFragment(new HomeNotificationGraphFragment(), "Notifications");
        adapter.addFragment(new HomeUnlocksGraphFragment(), "Unlocks");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        updateTimer();

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
        super.onResume();
        //WE MUST REFRESH THE ADAPTER CONTAINING THE LIST VIEW
        //AND RECREATE THE LIST
        topItems.clear();
        for (int i = 0; i <= maximumOnDisplay; i++) {
            if (usageInfo.get(i).getDailyUsage() > 0) {
                topItems.add(usageInfo.get(i));
            } else
                break;
        }
        listAdapter.notifyDataSetChanged();

//        switch (selectedGraph) {
//            case "USAGE":
//                createUsageChart();
//                break;
//            case "NOTIFICATIONS":
//                createNotificationChart();
//                break;
//            case "UNLOCKS":
//                createUnlocksChart();
//                break;
//            default:
//                break;
//        }

        String time = App.timeFormatter(Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)));
        totalUsage.setText(time);

        setPercentDelta();
    }


    private void setPercentDelta() {
        int todayIndex = App.currentPeriod.indexOf(App.DATE);

        try {
            String yesterdayDate = App.currentPeriod.get(0).get(todayIndex - 1);

            double todayUsage = Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)) / 60000;
            double yesterdayUsage = Long.parseLong(App.localDatabase.getSumTotalStat(yesterdayDate, DatabaseHelper.USAGE_TIME)) / 60000;

            String text;

            if (todayUsage == 0) {
                text = "+" + 0 + "%";
                percentDelta.setText(text);
            } else if (yesterdayUsage == 0) {
                text = "+" + 100 + "%";
                percentDelta.setText(text);
            } else {

                double delta = ((todayUsage - yesterdayUsage) / yesterdayUsage) * 100;
                // Log.i("DELTA", "" + todayUsage + ":" + yesterdayUsage + ":" + ":" + delta);

                if (delta > 0) {
                    text = String.format(Locale.ENGLISH, "%s%.2f%s", "+", delta, "%");
                    // "+" + delta + "%";
                    percentDelta.setText(text);

                } else {
                    text = String.format(Locale.ENGLISH, "%.2f%s", delta, "%");
                    percentDelta.setText(text);
                }

            }

        } catch (IndexOutOfBoundsException OoBE) {

            String yesterdayDate = App.currentPeriod.get(1).get(App.currentPeriod.get(1).size() - 1);

            double todayUsage = Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)) / 60000;
            double yesterdayUsage = Long.parseLong(App.localDatabase.getSumTotalStat(yesterdayDate, DatabaseHelper.USAGE_TIME)) / 60000;

            String text;

            //Log.i("DELTA", "today: " + todayUsage + "   yesterday:" + yesterdayUsage);
            if (todayUsage == 0) {
                text = "+" + 0 + "%";
                percentDelta.setText(text);
            } else if (yesterdayUsage == 0) {
                text = "+" + 100 + "%";
                percentDelta.setText(text);
            } else {

                double delta = ((todayUsage - yesterdayUsage) / yesterdayUsage) * 100;
                // Log.i("DELTA", "" + todayUsage + ":" + yesterdayUsage + ":" + ":" + delta);

                if (delta > 0) {
                    text = String.format(Locale.ENGLISH, "%s%.2f%s", "+", delta, "%");
                    // "+" + delta + "%";
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
        CountDownTimer timer = new CountDownTimer(App.START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                timeLeft = BackgroundMonitor.timeLeft;

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
                convertView = inflater.inflate(R.layout.home_most_used_list_layout, parent, false);

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

            String time = installedAppInfoList.get(position).formatTime(installedAppInfoList.get(position).getDailyUsage());
            //long time = installedAppInfoList.get(position).getDailyUsage();
            Log.i("TRACK", "" + time);
            listHolder.time.setText(String.valueOf(time));


            return convertView;
        }


    }

    ///ADAPTER CLASSS FOR TABBED LAYOUT
    public static class ViewPageAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        ViewPageAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {

            return titles.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            titles.add(title);
        }
    }


}
