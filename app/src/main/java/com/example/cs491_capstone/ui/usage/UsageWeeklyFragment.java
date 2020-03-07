package com.example.cs491_capstone.ui.usage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.usage.usage_graphs.weekly.WeeklyNotificationGraph;
import com.example.cs491_capstone.ui.usage.usage_graphs.weekly.WeeklyUnlocksGraph;
import com.example.cs491_capstone.ui.usage.usage_graphs.weekly.WeeklyUsageGraph;
import com.google.android.material.tabs.TabLayout;

public class UsageWeeklyFragment extends Fragment {
    public final static String[] week = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabbed_usage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.graph_choice);
        ViewPager viewPager = view.findViewById(R.id.graph_container);
        UsageFragment.ViewPageAdapter adapter = new UsageFragment.ViewPageAdapter(getFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new WeeklyUsageGraph(), "Usage");
        adapter.addFragment(new WeeklyNotificationGraph(), "Notifications");
        adapter.addFragment(new WeeklyUnlocksGraph(), "Unlocks");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}

