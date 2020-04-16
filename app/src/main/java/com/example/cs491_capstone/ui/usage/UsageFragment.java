package com.example.cs491_capstone.ui.usage;

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

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui_helpers.LockableViewPager;
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import static com.example.cs491_capstone.App.CATEGORY_AUDIO;
import static com.example.cs491_capstone.App.CATEGORY_GAME;
import static com.example.cs491_capstone.App.CATEGORY_IMAGE;
import static com.example.cs491_capstone.App.CATEGORY_MAPS;
import static com.example.cs491_capstone.App.CATEGORY_NEWS;
import static com.example.cs491_capstone.App.CATEGORY_OTHER;
import static com.example.cs491_capstone.App.CATEGORY_PRODUCTIVITY;
import static com.example.cs491_capstone.App.CATEGORY_SOCIAL;
import static com.example.cs491_capstone.App.CATEGORY_VIDEO;

public class UsageFragment extends Fragment {


    public final static String[] category = new String[]{CATEGORY_MAPS, CATEGORY_SOCIAL, CATEGORY_VIDEO, CATEGORY_AUDIO, CATEGORY_GAME, CATEGORY_IMAGE, CATEGORY_NEWS, CATEGORY_PRODUCTIVITY, CATEGORY_OTHER};
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


        final TabLayout tabLayout = view.findViewById(R.id.graph_choice);
        LockableViewPager viewPager = view.findViewById(R.id.graph_container);
        viewPager.setScrollDuration(200);
        ViewPageAdapter adapter = new ViewPageAdapter(getChildFragmentManager(), FragmentPagerAdapter.POSITION_UNCHANGED);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new UsageDailyFragment(), "Day");
        adapter.addFragment(new UsageWeeklyFragment(), "Week");

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


        ///WE TAKE THE LIST OF LIST AND MAKE IT EASIER TO TRAVERSE BY PUTTING IT INTO A SINGLE LIST IN THE REVERSE ORDER
        //ONLY THE ORDER OF THE OUTER LIST IS REVERSED, THE ORDER OF THE STRINGS IN THE INNER LIST IS KEPT
        //THIS WAY WE CAN GOO BACK AND FORWARD WITHOUT HAVING TO SWITCH TO DIFFERENT INNER LIST
        weeksSingleFormat = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            weeksSingleFormat.addAll(App.currentPeriod.get(i));
        }

    }


}
