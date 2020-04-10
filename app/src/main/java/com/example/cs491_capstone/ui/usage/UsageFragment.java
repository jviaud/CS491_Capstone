package com.example.cs491_capstone.ui.usage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui_helpers.LockableViewPager;
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class UsageFragment extends Fragment {


    public final static String[] category = new String[]{"Maps", "Social", "Movies & Video", "Audio", "Game", "Image", "News", "Productivity", "Other"};
    public final static int[] categoryKey = new int[]{R.color.maps, R.color.social, R.color.video, R.color.audio, R.color.game, R.color.image, R.color.news, R.color.productivity,R.color.other,};
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
        LockableViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setSwipeable(true);
        viewPager.setScrollDuration(200);
        ViewPageAdapter adapter = new ViewPageAdapter(getFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new UsageDailyFragment(), "Day");
        adapter.addFragment(new UsageWeeklyFragment(), "Week");

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

//        tabLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                indicatorWidth = tabLayout.getWidth() / tabLayout.getTabCount();
//
//                //Assign new width
//                FrameLayout.LayoutParams indicatorParams = (FrameLayout.LayoutParams) indicator.getLayoutParams();
//                indicatorParams.width = indicatorWidth;
//                indicator.setLayoutParams(indicatorParams);
//            }
//        });
//        //

        ///WE TAKE THE LIST OF LIST AND MAKE IT EASIER TO TRAVERSE BY PUTTING IT INTO A SINGLE LIST IN THE REVERSE ORDER
        //ONLY THE ORDER OF THE OUTER LIST IS REVERSED, THE ORDER OF THE STRINGS IN THE INNER LIST IS KEPT
        //THIS WAY WE CAN GOO BACK AND FORWARD WITHOUT HAVING TO SWITCH TO DIFFERENT INNER LIST
        weeksSingleFormat = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            weeksSingleFormat.addAll(App.currentPeriod.get(i));
        }

    }


}
