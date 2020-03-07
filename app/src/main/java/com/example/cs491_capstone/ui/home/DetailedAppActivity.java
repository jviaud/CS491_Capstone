package com.example.cs491_capstone.ui.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.UserUsageInfo;
import com.example.cs491_capstone.ui.home.detailed_graphs.DetailedNotificationGraphFragment;
import com.example.cs491_capstone.ui.home.detailed_graphs.DetailedUnlocksGraphFragment;
import com.example.cs491_capstone.ui.home.detailed_graphs.DetailedUsageGraphFragment;
import com.google.android.material.tabs.TabLayout;

public class DetailedAppActivity extends AppCompatActivity {
    public final static String[] days = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    /**
     * List of values on a clock from 12AM to 11PM used to label the X-Axis on the graphs
     */
    public final static String[] clock = new String[]{"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM",
            "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM",
            "8PM", "9PM", "10PM", "11PM"};




    private ImageView appIcon;
    private TextView appName, appCategory, usage_val, notification_val, unlocks_val;

    private UserUsageInfo appInfo;

    public static String packageName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_app);


        //INITIALISE ALL OF THE VIEWS
        appIcon = findViewById(R.id.app_icon);
        appName = findViewById(R.id.app_name);
        appCategory = findViewById(R.id.app_category);

        usage_val = findViewById(R.id.usage_val);
        notification_val = findViewById(R.id.notification_value);
        unlocks_val = findViewById(R.id.unlock_val);

        //INITIALISE THE APP OBJECT
        Intent intent = getIntent();
        appInfo = intent.getParcelableExtra("APP");

        //SET APP INFO TEXT VIEWS
        appName.setText(appInfo.getSimpleName());


        packageName = appInfo.getPackageName();
        //SET ICON & CATEGORY
        try {
            Drawable mIcon = getPackageManager().getApplicationIcon(packageName);

            appCategory.setText(App.getAppCategoryName(packageName, this));

            appIcon.setImageDrawable(mIcon);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }



        setBanner();


        TabLayout tabLayout = findViewById(R.id.graph_choice);
        ViewPager viewPager = findViewById(R.id.graph_container);
        HomeFragment.ViewPageAdapter adapter = new HomeFragment.ViewPageAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new DetailedUsageGraphFragment(), "Usage");
        adapter.addFragment(new DetailedNotificationGraphFragment(), "Notifications");
        adapter.addFragment(new DetailedUnlocksGraphFragment(), "Unlocks");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void setBanner() {

        long usageTime = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.USAGE_TIME, packageName)) / 60000;
        String usage = "";
        if (usageTime < 60) {
            usage = usageTime + " min(s)";
        } else {
            usageTime = usageTime / 60;
            usage = usageTime + " hr(s)";
        }
        usage_val.setText(usage);

        notification_val.setText(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.NOTIFICATIONS_COUNT, packageName));
        unlocks_val.setText(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.UNLOCKS_COUNT, packageName));
    }


















}
