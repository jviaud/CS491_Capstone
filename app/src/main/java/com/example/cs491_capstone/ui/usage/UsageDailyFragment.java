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
import com.example.cs491_capstone.ui.usage.usage_graphs.daily.DailyNotificationGraph;
import com.example.cs491_capstone.ui.usage.usage_graphs.daily.DailyUnlocksGraph;
import com.example.cs491_capstone.ui.usage.usage_graphs.daily.DailyUsageGraph;
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

public class UsageDailyFragment extends Fragment {


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
        ViewPageAdapter adapter = new ViewPageAdapter(getChildFragmentManager(), FragmentPagerAdapter.POSITION_UNCHANGED);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new DailyUsageGraph(), "Usage");
        adapter.addFragment(new DailyNotificationGraph(), "Notifications");
        adapter.addFragment(new DailyUnlocksGraph(), "Unlocks");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}
