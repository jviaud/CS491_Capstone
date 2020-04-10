package com.example.cs491_capstone.ui.goal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.goal.tabs.AppGoalFragment;
import com.example.cs491_capstone.ui.goal.tabs.PhoneGoalFragment;
import com.example.cs491_capstone.ui_helpers.LockableViewPager;
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import static com.example.cs491_capstone.App.currentPeriod;

public class GoalsFragment extends Fragment {

    public static String startDate = currentPeriod.get(0).get(0);
    public static String endDate = currentPeriod.get(0).get(6);
    public static String highlight;
    private int indicatorWidth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        highlight = getResources().getString(0+R.color.highlight);

        final TabLayout tabLayout = view.findViewById(R.id.graph_choice);
        LockableViewPager viewPager = view.findViewById(R.id.graph_container);
        viewPager.setScrollDuration(200);
        ViewPageAdapter adapter = new ViewPageAdapter(getChildFragmentManager(), FragmentPagerAdapter.POSITION_UNCHANGED);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new PhoneGoalFragment(), "Phone");
        adapter.addFragment(new AppGoalFragment(), "Application");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        final View mIndicator = view.findViewById(R.id.indicator);


        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                indicatorWidth = tabLayout.getWidth() / tabLayout.getTabCount();

                //Assign new width
                FrameLayout.LayoutParams indicatorParams = (FrameLayout.LayoutParams) mIndicator.getLayoutParams();
                indicatorParams.width = indicatorWidth;
                mIndicator.setLayoutParams(indicatorParams);
            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float positionOffset, int positionOffsetPx) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIndicator.getLayoutParams();

                //Multiply positionOffset with indicatorWidth to get translation
                float translationOffset = (positionOffset + i) * indicatorWidth;
                params.leftMargin = (int) translationOffset;
                mIndicator.setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

}