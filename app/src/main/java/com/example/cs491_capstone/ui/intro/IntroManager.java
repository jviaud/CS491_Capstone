package com.example.cs491_capstone.ui.intro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui_helpers.LockableViewPager;
import com.example.cs491_capstone.ui_helpers.ZoomOutPageTransformer;
import com.pixelcan.inkpageindicator.InkPageIndicator;

import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class IntroManager extends AppCompatActivity {
    public static LockableViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_manager);

        InkPageIndicator pageIndicator = findViewById(R.id.indicator);
        pager = findViewById(R.id.intro_pager);
        IntroPageAdapter adapter = new IntroPageAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        //pager.setAdapter(adapter);


        adapter.addFragment(new NotificationPermissionFragment(), "Notification Listener");
        adapter.addFragment(new UsagePermissionFragment(), "Usage Stats");
        adapter.addFragment(new GetStartedFragment(), "Get Started");

        pager.setSwipeable(false);
        pager.setAdapter(adapter);
        pageIndicator.setViewPager(pager);
        pager.setPageTransformer(true, new ZoomOutPageTransformer() {
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public static class IntroPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        IntroPageAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }


        @Override
        public int getCount() {
            return fragmentList.size();
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
