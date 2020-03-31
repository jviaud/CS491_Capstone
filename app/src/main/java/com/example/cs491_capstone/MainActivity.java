package com.example.cs491_capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.cs491_capstone.services.BackgroundMonitor;
import com.example.cs491_capstone.ui.goal.GoalsFragment;
import com.example.cs491_capstone.ui.goal.NewGoal;
import com.example.cs491_capstone.ui.home.HomeFragment;
import com.example.cs491_capstone.ui.intro.IntroManager;
import com.example.cs491_capstone.ui.settings.SettingsFragment;
import com.example.cs491_capstone.ui.usage.UsageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class MainActivity extends AppCompatActivity {
    //public static ArrayList<UserUsageInfo> usageInfo;
    SharedPreferences prefs = null;
    FloatingActionButton fab;
    /**
     * Broadcaster receiver that listens when new apps are installed to the system
     */

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
                            fab.setVisibility(View.VISIBLE);
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_parental:
                            fab.setVisibility(View.VISIBLE);
                            selectedFragment = new GoalsFragment();
                            break;
                        case R.id.nav_usage:
                            fab.setVisibility(View.VISIBLE);
                            selectedFragment = new UsageFragment();
                            break;
                        case R.id.nav_settings:
                            fab.setVisibility(View.GONE);
                            selectedFragment = new SettingsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //STATUS BAR COLOR
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorAccent, this.getTheme()));
        //

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //CREATE BOTTOM NAVIGATION
        BottomNavigationView bottomNaV = findViewById(R.id.bottom_navigation);
        //PASS BOTTOM NAVIGATION OBJECT TO CLICK HANDLER
        bottomNaV.setOnNavigationItemSelectedListener(navListener);
        //SET HOME FRAGMENT TO BE SELECTED BY DEFAULT
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();


        fab = findViewById(R.id.new_goal);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewGoal.class);
                startActivity(intent);
            }
        });


        ///END CREATE BOTTOM NAVIGATION


        //START BACKGROUND MONITOR SERVICE
        Intent serviceIntent = new Intent(this, BackgroundMonitor.class);//Wifi Listener Intent
        startService(serviceIntent);


        ///RETRIEVE TODAY'S USAGE STATS
        //usageInfo = getUsageToday();
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
            App.SPECIAL_APPS = prefs.getStringSet("exclusion_list", null);
        }
    }
}
