package com.example.cs491_capstone.ui.detailed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.UserUsageInfo;
import com.example.cs491_capstone.ui.detailed.detailed_category_graphs.DetailedNotificationGraphCategoryFragment;
import com.example.cs491_capstone.ui.detailed.detailed_category_graphs.DetailedUnlocksGraphCategoryFragment;
import com.example.cs491_capstone.ui.detailed.detailed_category_graphs.DetailedUsageGraphCategoryFragment;
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import static com.example.cs491_capstone.App.DATE;

public class DetailedCategoryActivity extends AppCompatActivity {

    public static String categoryName;
    private TextView usage_val;
    private TextView notification_val;
    private TextView unlocks_val;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = pref.getBoolean("dark_mode", true);
        if (darkMode) {
            getTheme().applyStyle(R.style.GoalTheme_Dark, true);
        } else {
            getTheme().applyStyle(R.style.GoalTheme_Light, true);
        }
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
        UserUsageInfo appInfo = intent.getParcelableExtra("CATEGORY");

        //SET APP INFO TEXT VIEWS
        appName.setText(appInfo.getCategory());

        categoryName = App.getAppCategoryName(appInfo.getPackageName(), this);

        switch (appInfo.getCategory()) {
            case App.CATEGORY_GAME:
                appIcon.setImageResource(R.drawable.category_game);
                break;
            case App.CATEGORY_AUDIO:
                appIcon.setImageResource(R.drawable.category_audio);
                break;
            case App.CATEGORY_VIDEO:
                appIcon.setImageResource(R.drawable.categoroy_video);
                break;
            case App.CATEGORY_IMAGE:
                appIcon.setImageResource(R.drawable.category_image);
                break;
            case App.CATEGORY_SOCIAL:
                appIcon.setImageResource(R.drawable.categoory_social);
                break;
            case App.CATEGORY_NEWS:
                appIcon.setImageResource(R.drawable.category_news);
                break;
            case App.CATEGORY_MAPS:
                appIcon.setImageResource(R.drawable.categoory_maps);
                break;
            case App.CATEGORY_PRODUCTIVITY:
                appIcon.setImageResource(R.drawable.category_productivity);
                break;
            default:
                appIcon.setImageResource(R.drawable.category_other);
        }

        appCategory.setText(" ");


        setBanner();


        TabLayout tabLayout = findViewById(R.id.graph_choice);
        ViewPager viewPager = findViewById(R.id.graph_container);
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new DetailedUsageGraphCategoryFragment(), "Usage");
        adapter.addFragment(new DetailedNotificationGraphCategoryFragment(), "Notifications");
        adapter.addFragment(new DetailedUnlocksGraphCategoryFragment(), "Unlocks");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void setBanner() {

        long usageTime = Long.parseLong(App.localDatabase.getSumTotalStatByCategory(DATE, DatabaseHelper.USAGE_TIME, categoryName)) / 60000;
        String usage = "";
        if (usageTime < 60) {
            usage = usageTime + " min(s)";
        } else {
            usageTime = usageTime / 60;
            usage = usageTime + " hr(s)";
        }
        usage_val.setText(usage);

        notification_val.setText(App.localDatabase.getSumTotalStatByCategory(DATE, DatabaseHelper.NOTIFICATIONS_COUNT, categoryName));
        unlocks_val.setText(App.localDatabase.getSumTotalStatByCategory(DATE, DatabaseHelper.UNLOCKS_COUNT, categoryName));
    }

}
