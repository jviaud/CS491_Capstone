package com.example.cs491_capstone.ui.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
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

import static com.example.cs491_capstone.App.DATE;

public class DetailedAppActivity extends AppCompatActivity {

    public static String packageName;
    private TextView usage_val;
    private TextView notification_val;
    private TextView unlocks_val;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_app);

        //INITIALISE ALL OF THE VIEWS
        ImageView appIcon = findViewById(R.id.app_icon);
        TextView appName = findViewById(R.id.app_name);
        TextView appCategory = findViewById(R.id.app_category);

        usage_val = findViewById(R.id.usage_val);
        notification_val = findViewById(R.id.notification_value);
        unlocks_val = findViewById(R.id.unlock_val);

        //INITIALISE THE APP OBJECT
        Intent intent = getIntent();
        UserUsageInfo appInfo = intent.getParcelableExtra("APP");

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

        long usageTime = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(DATE, DatabaseHelper.USAGE_TIME, packageName)) / 60000;
        String usage = "";
        if (usageTime < 60) {
            usage = usageTime + " min(s)";
        } else {
            usageTime = usageTime / 60;
            usage = usageTime + " hr(s)";
        }
        usage_val.setText(usage);

        notification_val.setText(App.localDatabase.getSumTotalStatByPackage(DATE, DatabaseHelper.NOTIFICATIONS_COUNT, packageName));
        unlocks_val.setText(App.localDatabase.getSumTotalStatByPackage(DATE, DatabaseHelper.UNLOCKS_COUNT, packageName));

        TypedArray usage_awards = getResources().obtainTypedArray(R.array.usage_awards);
        TypedArray notification_awards = getResources().obtainTypedArray(R.array.notification_awards);
        TypedArray unlocks_awards = getResources().obtainTypedArray(R.array.unlocks_awards);

        ImageView award_one = findViewById(R.id.award_one);
        ImageView award_two = findViewById(R.id.award_two);
        ImageView award_three = findViewById(R.id.award_three);


        int index;
        //AWARDS
        if (App.localDatabase.getTopThreeRankings(DATE, DatabaseHelper.USAGE_TIME).contains(packageName)) {

            index = App.localDatabase.getTopThreeRankings(DATE, DatabaseHelper.USAGE_TIME).indexOf(packageName);
            award_one.setImageDrawable(usage_awards.getDrawable(index));
            award_one.setVisibility(View.VISIBLE);
        }
//
//
        if (App.localDatabase.getTopThreeRankings(DATE, DatabaseHelper.NOTIFICATIONS_COUNT).contains(packageName)) {
            index = App.localDatabase.getTopThreeRankings(DATE, DatabaseHelper.NOTIFICATIONS_COUNT).indexOf(packageName);
            award_two.setImageDrawable(notification_awards.getDrawable(index));
            award_two.setVisibility(View.VISIBLE);
        }
//
//
        if (App.localDatabase.getTopThreeRankings(DATE, DatabaseHelper.UNLOCKS_COUNT).contains(packageName)) {
            index = App.localDatabase.getTopThreeRankings(DATE, DatabaseHelper.UNLOCKS_COUNT).indexOf(packageName);
            award_three.setImageDrawable(unlocks_awards.getDrawable(index));
            award_three.setVisibility(View.VISIBLE);
        }

        usage_awards.recycle();
        notification_awards.recycle();
        unlocks_awards.recycle();
    }


}
