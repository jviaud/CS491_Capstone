package com.example.cs491_capstone.ui.usage;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.intro.IntroManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class UsageFragment extends Fragment {


    public final static String[] category = new String[]{"Maps", "Social", "Movies & Video", "Audio", "Game", "Image", "News", "Productivity"};
    public final static int[] categoryKey = new int[]{Color.BLUE, Color.GREEN, Color.LTGRAY, Color.RED, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.DKGRAY};
    public static ArrayList<String> weeksSingleFormat;
    private int indicatorWidth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_usage, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //CREATE TABBED LAYOUT FOR WEEK/DAY CONTENT
        final View indicator = view.findViewById(R.id.indicator);
        final TabLayout tabLayout = view.findViewById(R.id.tabbed_layout);
        IntroManager.LockableViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setSwipeable(false);
        ViewPageAdapter adapter = new ViewPageAdapter(getFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new UsageDailyFragment(), "Day");
        adapter.addFragment(new UsageWeeklyFragment(), "Week");

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                indicatorWidth = tabLayout.getWidth() / tabLayout.getTabCount();

                //Assign new width
                FrameLayout.LayoutParams indicatorParams = (FrameLayout.LayoutParams) indicator.getLayoutParams();
                indicatorParams.width = indicatorWidth;
                indicator.setLayoutParams(indicatorParams);
            }
        });
        //

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) indicator.getLayoutParams();

                //Multiply positionOffset with indicatorWidth to get translation
                float translationOffset = (positionOffset + position) * indicatorWidth;
                params.leftMargin = (int) translationOffset;
                indicator.setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        weeksSingleFormat = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            weeksSingleFormat.addAll(App.currentPeriod.get(i));
        }
        Log.i("WEEK", "" + weeksSingleFormat);


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

            //I DON'T WANT FRAGMENTS TO HAVE A NAME JUST AN ICON
            //TO HAVE A NAME AD THIS BAK TO CONSTRUCTOR
            titles.add(title);
        }
    }


}
