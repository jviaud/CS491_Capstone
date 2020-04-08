package com.example.cs491_capstone.ui.goal;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.goal.adapter.GoalImageAdapter;
import com.example.cs491_capstone.ui.goal.tabs.AppGoalFragment;
import com.example.cs491_capstone.ui.goal.tabs.PhoneGoalFragment;
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import static com.example.cs491_capstone.App.currentPeriod;

public class GoalsFragment extends Fragment {

    public static String startDate = currentPeriod.get(0).get(0);
    public static String endDate = currentPeriod.get(0).get(6);
    private static List<Goal> goalsList = new ArrayList<>();
    private GoalImageAdapter goalAdapter;
    private boolean pageStart = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        goalsList = App.goalDataBase.getAllActiveGoals(startDate, endDate);

        TabLayout tabLayout = view.findViewById(R.id.graph_choice);
        ViewPager viewPager = view.findViewById(R.id.graph_container);
        ViewPageAdapter adapter = new ViewPageAdapter(getChildFragmentManager(), FragmentPagerAdapter.POSITION_UNCHANGED);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new PhoneGoalFragment(), "Phone");
        adapter.addFragment(new AppGoalFragment(), "Application");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        final RecyclerView goalsRecycler = view.findViewById(R.id.goals_list);

        LinearLayoutManager layoutManagerWeek = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);


        //ASSIGN EACH LAYOUT MANAGER TO ITS CORRESPONDING RECYCLER
        goalsRecycler.setLayoutManager(layoutManagerWeek);
        goalsRecycler.setHasFixedSize(true);

        goalAdapter = new GoalImageAdapter(getContext(), goalsList);


        goalsRecycler.setAdapter(goalAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }


            @Override
            public void onPageSelected(int position) {
                goalsList.clear();
                if (position == 0) {
                    pageStart = true;
                    goalsList.addAll(App.goalDataBase.getAllActiveGoals(startDate, endDate));

                } else {
                    pageStart = false;
                    goalsList.addAll(App.goalDataBase.getUniquePackageGoals(startDate, endDate));

                }
                goalAdapter.holderList.clear();
                goalAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //COLLAPSES THE VIEWS WHEN SCROLL IS DETECTED, PURELY AESTHETIC
        goalsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0 | dx < 0) {
                    goalAdapter.collapseAll();
                }
            }
        });
    }




    @Override
    public void onResume() {
        super.onResume();
        goalsList.clear();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                if (pageStart) {

                    goalsList.addAll(App.goalDataBase.getAllActiveGoals(startDate, endDate));

                } else {

                    goalsList.addAll(App.goalDataBase.getUniquePackageGoals(startDate, endDate));

                }


            }
        });
        goalAdapter.holderList.clear();
        goalAdapter.notifyDataSetChanged();

    }
}
