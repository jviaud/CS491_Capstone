package com.example.cs491_capstone.ui.goal;

import android.os.Bundle;
import android.util.Log;
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
import com.example.cs491_capstone.ui_helpers.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import static com.example.cs491_capstone.App.currentPeriod;

public class GoalsFragment extends Fragment {

    List<Goal> goalsList = new ArrayList<>();
    private GoalImageAdapter goalAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        goalsList = App.goalDataBase.getAllActiveGoals(currentPeriod.get(0).get(0),currentPeriod.get(0).get(6));
        Log.i("GOAL", "SIZE:" + goalsList.size());

        TabLayout tabLayout = view.findViewById(R.id.graph_choice);
        ViewPager viewPager = view.findViewById(R.id.graph_container);
        ViewPageAdapter adapter = new ViewPageAdapter(getChildFragmentManager(), FragmentPagerAdapter.POSITION_UNCHANGED);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        adapter.addFragment(new PhoneGoalFragment(), "Phone");
        adapter.addFragment(new AppGoalFragment(), "Application");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        RecyclerView goalsRecycler = view.findViewById(R.id.goals_list);

        LinearLayoutManager layoutManagerWeek = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        //ASSIGN EACH LAYOUT MANAGER TO ITS CORRESPONDING RECYCLER
        goalsRecycler.setLayoutManager(layoutManagerWeek);
        goalsRecycler.setHasFixedSize(true);

        goalAdapter = new GoalImageAdapter(getContext(), goalsList);
        goalsRecycler.setAdapter(goalAdapter);


//        goalsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dx > 0 | dx < 0) {
//                    System.out.println("Scrolled Right");
//                    for (Goal goal : goalsList) {
//                        goal.setExpanded(false);
//                        recyclerView.post(new Runnable() {
//                            public void run() {
//                                // There is no need to use notifyDataSetChanged()
//                                goalAdapter.notifyDataSetChanged();
//                            }
//                        });
//
//                    }
//                } else {
//                    return;
//                }
//            }
//        });


        goalAdapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();
        goalsList.clear();
        goalsList.addAll(App.goalDataBase.getAllActiveGoals(currentPeriod.get(0).get(0),currentPeriod.get(0).get(6)));
        goalAdapter.notifyDataSetChanged();
    }
}
