package com.example.cs491_capstone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.services.BackgroundMonitor;
import com.example.cs491_capstone.ui.home.HomeFragment;
import com.example.cs491_capstone.ui.intro.IntroManager;
import com.example.cs491_capstone.ui.parental.ParentalFragment;
import com.example.cs491_capstone.ui.settings.SettingsFragment;
import com.example.cs491_capstone.ui.usage.UsageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.example.cs491_capstone.App.APP_LIST;
import static com.example.cs491_capstone.App.localDatabase;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs = null;
    public static ArrayList<UserUsageInfo> usageInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(App.PACKAGE_NAME, MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            Intent intent = new Intent(this, IntroManager.class);
            prefs.edit().putBoolean("firstrun", false).apply();
            startActivity(intent);
            finish();
        } else {
            // retrieveUsageStats();
        }

        //CREATE BOTTOM NAVIGATION
        BottomNavigationView bottomNaV = findViewById(R.id.bottom_navigation);
        //PASS BOTTOM NAVIGATION OBJECT TO CLICK HANDLER
        bottomNaV.setOnNavigationItemSelectedListener(navListener);
        //SET HOME FRAGMENT TO BE SELECTED BY DEFAULT
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
        ///END CREATE BOTTOM NAVIGATION


        //START BACKGROUND MONITOR SERVICE
        Intent serviceIntent = new Intent(this, BackgroundMonitor.class);//Wifi Listener Intent
        startService(serviceIntent);


        /// END CREATE NOTIFICATION CHANNELS


        ///RETRIEVE TODAY'S USAGE STATS
        ///THIS IS ALL OF TODAY BROKEN INTO HOURS
        usageInfo = getTodaysUsage();

        //Log.i("DB",""+App.localDatabase.getAllData());

//        @SuppressLint("RestrictedApi") PeriodicWorkRequest synclocal = new PeriodicWorkRequest.Builder(SyncFireDBWorker.class, 1, TimeUnit.HOURS)
//                .setPeriodStartTime(App.START_OF_DAY, TimeUnit.MILLISECONDS)
//                .build();
//        WorkManager.getInstance(this).enqueue(synclocal);
//
//        @SuppressLint("RestrictedApi") PeriodicWorkRequest synclocal = new PeriodicWorkRequest.Builder(SyncLocalDBWorker.class, 1, TimeUnit.HOURS)
//                .setPeriodStartTime(App.START_OF_DAY, TimeUnit.MILLISECONDS)
//                .build();
//        WorkManager.getInstance(this).enqueue(synclocal);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //CHECK IF THIS IS THE FIRST TIME THE APP IS BEING RUN, IF IT IS THEN START THE INITIALIZER ACTIVITY TO SET THE REQUIRED PERMISSIONS
        if (prefs.getBoolean("firstrun", true)) {
            Intent intent = new Intent(this, IntroManager.class);
            prefs.edit().putBoolean("firstrun", false).apply();
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            finish();
        } else {
            usageInfo = getTodaysUsage();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        usageInfo.clear();

    }

    public static ArrayList<UserUsageInfo> getTodaysUsage() {
        //FOR EVERY APP IN APP LIST
        //WE USE THIS LIST BECAUSE IT IS ALREADY FILTERED
        //WE DON'T HAVE TO FILTER IT AGAIN
        ArrayList<UserUsageInfo> usageInfo = new ArrayList<>();

        for (int i = 0; i < APP_LIST.size(); i++) {
            String packageName = APP_LIST.get(i).getPackageName();
            String appName = APP_LIST.get(i).getSimpleName();
            Drawable icon = APP_LIST.get(i).getIcon();
            Log.i("TRACK", "PACKAGENAME:" + packageName);
            //THIS IS THE LAST RECORD WE HAVE FOR THIS PARTICULAR PACKAGE
            //WE WANT TO GO BACK AND SEE IF WE ARE MISSING ANY DATA FROM BEFORE
            //THIS WILL RETURN 0 IF EMPTY/NULL, IF IT IS 0 THEN WE WILL JUST SKIP IT
            int lastEntry = localDatabase.getLastEntry(App.DATE, APP_LIST.get(i).getPackageName());
            Log.i("TRACK", "LAST ENTRY:" + lastEntry);
            @SuppressLint("UseSparseArrays") HashMap<Integer, Long> time = new HashMap<>();
            for (int j = 0; j <= Integer.parseInt(App.HOUR); j++) {
                Log.i("TRACK", "ENTRY:" + j);
                //WE GET THE TOTAL TIME USED FOR THE HOUR INTERVAL
                //THIS WILL RETURN 0 IF EMPTY/NULL SO WE DON'T NEED TO WORRY ABOUT PARSE EXCEPTION
                long timeUsed = Long.parseLong(localDatabase.get(DatabaseHelper.USAGE_TIME, packageName, App.DATE, String.valueOf(j)));
                Log.i("TRACK", "TIMEUSED:" + timeUsed);
                //STORE IT IN THE MAP
                // J IS THE HOUR INTERVAL AND TIME USED IS THE THAT THE APP WAS USED DURING THIS INTERVAL
                time.put(j, timeUsed);
            }
            Log.i("TRACK", "TIME:" + time);
            usageInfo.add(new UserUsageInfo(packageName, appName, icon, time));
        }
        Collections.sort(usageInfo);
        return usageInfo;
    }

    //CLICK HANDLER FOR BOTTOM NAVIGATION
    /**
     * on click listener for bottom nav
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_parental:
                            selectedFragment = new ParentalFragment();
                            break;
                        case R.id.nav_usage:
                            selectedFragment = new UsageFragment();
                            break;
                        case R.id.nav_settings:
                            selectedFragment = new SettingsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };
    //
}
