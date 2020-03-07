package com.example.cs491_capstone.ui.usage.usage_graphs.weekly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.R;

import lecho.lib.hellocharts.view.ColumnChartView;

public class WeeklyNotificationGraph extends Fragment {
    private ColumnChartView barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabbed_usage_graphs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.daily_chart);
    }

    @Override
    public void onResume() {
        super.onResume();
        //createUsageChart();
    }
}

