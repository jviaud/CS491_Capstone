package com.example.cs491_capstone.ui.goal.tabs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.GoalDataBaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.goal.Goal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

import static com.example.cs491_capstone.App.currentPeriod;
import static com.example.cs491_capstone.App.goalDataBase;
import static com.example.cs491_capstone.App.week;
import static com.example.cs491_capstone.ui.goal.GoalsFragment.endDate;
import static com.example.cs491_capstone.ui.goal.GoalsFragment.startDate;

public class AppGoalFragment extends Fragment {
    static RecyclerView appsRecycler;
    static List<Goal> goals;
    public ImageAdapter adapter;
    String packageName;
    private ComboLineColumnChartView usageComboChart;
    private ComboLineColumnChartData usageComboData;
    private PieChartView usagePie;
    private PieChartView unlockPie;
    private ComboLineColumnChartView unlockComboChart;
    private ComboLineColumnChartData unlockComboData;
    private CardView usageCard, unlockCard;
    private TextView graph_app;
    private float maxUsageValue, maxUnlockValue;

    private long[][] usageStatusValues = new long[week.length][2];

    private long[][] unlockStatusValues = new long[week.length][2];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goals_graph_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goals = App.goalDataBase.getUniquePackageGoals(startDate, endDate);
        appsRecycler = view.findViewById(R.id.appsWithGoals);
        usageComboChart = view.findViewById(R.id.graph);
        unlockComboChart = view.findViewById(R.id.unlock_graph);
        usagePie = view.findViewById(R.id.usage_pie);
        unlockPie = view.findViewById(R.id.unlock_pie);
        usageCard = view.findViewById(R.id.usage_card);
        graph_app = view.findViewById(R.id.graph_app);
        usageCard = view.findViewById(R.id.usage_card);
        unlockCard = view.findViewById(R.id.unlock_card);

        graph_app.setVisibility(View.VISIBLE);


        unlockPie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockCard.setVisibility(View.VISIBLE);
                usageCard.setVisibility(View.GONE);
            }
        });

        usagePie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockCard.setVisibility(View.GONE);
                usageCard.setVisibility(View.VISIBLE);

            }
        });

        ///
        if (goals.isEmpty()) {
            appsRecycler.setVisibility(View.GONE);
            graph_app.setVisibility(View.GONE);
        } else {
            graph_app.setVisibility(View.VISIBLE);
            appsRecycler.setVisibility(View.VISIBLE);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        appsRecycler.setLayoutManager(layoutManager);
        appsRecycler.setHasFixedSize(true);

        adapter = new ImageAdapter(getContext(), goals);
        appsRecycler.setAdapter(adapter);
        ///


        try {
            packageName = goals.get(0).getPackageName();
        } catch (IndexOutOfBoundsException ibe) {
            packageName = "null";
        }
        graph_app.setText(getProperName(packageName));

        //PIE CHARTS
        generateUsagePie(packageName);
        generateUnlockPie(packageName);


        //GRAPH DATA


        adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ImageView v) {
                final String packageName = goals.get(position).getPackageName();
                graph_app.setText(getProperName(packageName));

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        generateUnlockPie(packageName);
                        generateUsagePie(packageName);
                    }
                });

                adapter.getSelected(position);

                usageComboData = new ComboLineColumnChartData(generateUsageColumnData(packageName), generateUsageLineData(packageName));
                labelUsageAxis();
                usageComboChart.setComboLineColumnChartData(usageComboData);
                setUsageViewPortWidth(usageComboChart);


                unlockComboData = new ComboLineColumnChartData(generateUnlockColumnData(packageName), generateUnlockLineData(packageName));
                labelUnlockAxis();
                unlockComboChart.setComboLineColumnChartData(unlockComboData);
                setUnlockViewPortWidth(unlockComboChart);

                setStatusChecker(packageName);

            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();

        goals.clear();
        goals.addAll(App.goalDataBase.getUniquePackageGoals(startDate, endDate));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {


                usageComboData = new ComboLineColumnChartData(generateUsageColumnData(packageName), generateUsageLineData(packageName));
                labelUsageAxis();
                usageComboChart.setComboLineColumnChartData(usageComboData);
                setUsageViewPortWidth(usageComboChart);


                unlockComboData = new ComboLineColumnChartData(generateUnlockColumnData(packageName), generateUnlockLineData(packageName));
                labelUnlockAxis();
                unlockComboChart.setComboLineColumnChartData(unlockComboData);
                setUnlockViewPortWidth(unlockComboChart);


            }
        });
        adapter.imageList.clear();
        adapter.notifyDataSetChanged();

        appsRecycler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    appsRecycler.findViewHolderForAdapterPosition(0).itemView.performClick();
                } catch (NullPointerException ignored) {
                }
            }
        }, 100);


    }

    private void setStatusChecker(String packageName) {
        boolean[][] goalStatusFailed = new boolean[7][2];
        Log.i("VALUES", "TRIGGERED:" + usageStatusValues.length);
        for (int i = 0; i < 7; i++) {
            Log.i("VALUES", "USAGE:" + i + "|" + usageStatusValues[i][0] + "|" + usageStatusValues[i][1]);

            if (usageStatusValues[i][0] > usageStatusValues[i][1]) {
                String id = goalDataBase.getAppGoalId(currentPeriod.get(0).get(i), packageName);
                goalDataBase.setUsageStatus(id, "1");
                goalStatusFailed[i][0] = true;
            }
        }

        for (int i = 0; i < 7; i++) {
            Log.i("VALUES", "UNLOCK:" + i + "|" + unlockStatusValues[i][0] + "|" + unlockStatusValues[i][1]);

            if (unlockStatusValues[i][0] > unlockStatusValues[i][1]) {
                String id = goalDataBase.getAppGoalId(currentPeriod.get(0).get(i), packageName);
                goalDataBase.setUnlockStatus(id, "1");
                goalStatusFailed[i][1] = true;
            }
        }

        for (int i = 0; i < 7; i++) {
            Log.i("VALUES", "STATUS:" + i + "|" + goalStatusFailed[i][1] + "|" + goalStatusFailed[i][0]);

            if (goalStatusFailed[i][1] || goalStatusFailed[i][0]) {
                String id = goalDataBase.getAppGoalId(currentPeriod.get(0).get(i), packageName);
                Log.i("VALUES", "STATUS:" + i + "|FAILED");
                goalDataBase.setStatus(id, "1");
            }
        }

    }

    private String getProperName(String packageName) {
        String name = "null";
        PackageManager packageManager = getContext().getPackageManager();
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            name = ai.loadLabel(packageManager).toString();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    private void labelUsageAxis() {
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);

        axisX.setName("Day of Week");
        axisY.setName("Usage vs. Goal Value (minutes)");
        axisY.setTextColor(R.color.black);
        axisX.setTextColor(R.color.black);


        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        for (int i = 0; i < week.length; i++) {
            axisValues.add(new AxisValue(i).setLabel(week[i]));
        }
        axisX.setValues(axisValues);

        usageComboData.setAxisXBottom(axisX);
        usageComboData.setAxisYLeft(axisY);

    }

    private void setUsageViewPortWidth(ComboLineColumnChartView chart) {
        if (maxUsageValue < 10) {
            Viewport v = new Viewport(chart.getMaximumViewport());
            v.top = 10;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
            chart.setViewportCalculationEnabled(false);
        } else {
            Viewport v = new Viewport(chart.getMaximumViewport());
            v.top = ((maxUsageValue + 4) / 5) * 5; //NEXT MULTIPLE OF 5;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
            chart.setViewportCalculationEnabled(false);
        }


        chart.setValueSelectionEnabled(true);
        chart.setZoomEnabled(false);
        chart.setInteractive(true);
        chart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
    }

    private void labelUnlockAxis() {
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);

        axisX.setName("Day of Week");
        axisY.setName("Unlocks vs. Goal Value");
        axisY.setTextColor(R.color.black);
        axisX.setTextColor(R.color.black);
        String black = "black";


        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        for (int i = 0; i < week.length; i++) {
            axisValues.add(new AxisValue(i).setLabel(week[i]));
        }
        axisX.setValues(axisValues);

        unlockComboData.setAxisXBottom(axisX);
        unlockComboData.setAxisYLeft(axisY);

    }

    private void setUnlockViewPortWidth(ComboLineColumnChartView chart) {
        if (maxUnlockValue < 10) {
            Viewport v = new Viewport(chart.getMaximumViewport());
            v.top = 10;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
            chart.setViewportCalculationEnabled(false);
        } else {
            Viewport v = new Viewport(chart.getMaximumViewport());
            v.top = ((maxUsageValue + 4) / 5) * 5; //NEXT MULTIPLE OF 5;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
            chart.setViewportCalculationEnabled(false);
        }


        chart.setValueSelectionEnabled(true);
        chart.setZoomEnabled(false);
        chart.setInteractive(true);
        chart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
    }

    ///
    private ColumnChartData generateUsageColumnData(String packageName) {
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        maxUsageValue = 0;
        for (int i = 0; i < week.length; ++i) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; ++j) {
                long val = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(i), DatabaseHelper.USAGE_TIME, packageName)) / 60000;
                //long val = 10L;

                usageStatusValues[i][0] = val;

                if (val == 0) {
                    values.add(new SubcolumnValue(val, Color.TRANSPARENT));
                    break;
                } else {
                    SubcolumnValue subcolumnValue = new SubcolumnValue(val, Color.DKGRAY);

                    int hours = (int) (val / (60) % 24);
                    int minutes = (int) (val % 60);


                    if (hours == 0) {
                        subcolumnValue.setLabel(String.format(Locale.ENGLISH, "%d%s", minutes, "m"));
                    } else {
                        subcolumnValue.setLabel(String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m"));
                    }

                    values.add(subcolumnValue);
                }

                if (maxUsageValue < val) {
                    maxUsageValue = val;
                }
            }

            columns.add(new Column(values));
        }


        return new ColumnChartData(columns);
    }

    private LineChartData generateUsageLineData(String packageName) {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < 1; ++i) {
            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < week.length; ++j) {

                long val = Long.parseLong(App.goalDataBase.get(App.currentPeriod.get(0).get(j), GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_USAGE)) / 60000;

                usageStatusValues[i][1] = val;

                PointValue pointValue;
                if (val == 0) {
                    pointValue = new PointValue(j, 0);
                    pointValue.setLabel("n/a");
                } else {
                    pointValue = new PointValue(j, val);

                    int hours = (int) (val / (60) % 24);
                    int minutes = (int) (val % 60);


                    if (hours == 0) {
                        pointValue.setLabel(String.format(Locale.ENGLISH, "%d%s", minutes, "m"));
                    } else {
                        pointValue.setLabel(String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m"));
                    }


                }
                values.add(pointValue);

                if (maxUsageValue < val) {
                    maxUsageValue = val;
                }

            }

            Line line = new Line(values);
            line.setColor(R.color.gold);
            line.setCubic(false);
            line.setHasLabelsOnlyForSelected(true);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        return new LineChartData(lines);

    }


    private ColumnChartData generateUnlockColumnData(String packageName) {
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        maxUnlockValue = 0;
        for (int i = 0; i < week.length; ++i) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; ++j) {
                long val = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(i), DatabaseHelper.UNLOCKS_COUNT, packageName));
                //long val = 10L;
                unlockStatusValues[i][0] = val;
                if (val == 0) {
                    values.add(new SubcolumnValue(val, Color.TRANSPARENT));
                    break;
                } else {
                    values.add(new SubcolumnValue(val, Color.DKGRAY));
                }

                if (maxUsageValue < val) {
                    maxUsageValue = val;
                }
            }

            columns.add(new Column(values));
        }


        ColumnChartData columnChartData = new ColumnChartData(columns);


        return columnChartData;
    }

    private LineChartData generateUnlockLineData(String packageName) {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < 1; ++i) {
            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < week.length; ++j) {

                long val = Long.parseLong(App.goalDataBase.get(App.currentPeriod.get(0).get(j), GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_UNLOCKS));
                PointValue pointValue;

                unlockStatusValues[i][1] = val;
                if (val == 0) {
                    pointValue = new PointValue(j, 0);
                    pointValue.setLabel("n/a");
                } else {
                    pointValue = new PointValue(j, val);

                }

                if (maxUsageValue < val) {
                    maxUsageValue = val;
                }
                values.add(pointValue);

            }

            Line line = new Line(values);
            line.setColor(R.color.gold);
            line.setCubic(false);
            line.setHasLabelsOnlyForSelected(true);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        return new LineChartData(lines);

    }


    ///
    private void generateUnlockPie(String packageName) {
        List<SliceValue> values = new ArrayList<>();


        long goalValue = Long.parseLong(App.goalDataBase.get(App.DATE, GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_UNLOCKS));
        long actualValue = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.UNLOCKS_COUNT, packageName));

        float full = 100;
        float remaining = 0;
        String formatRemaining;

        if (actualValue > goalValue & goalValue > 0) {
            formatRemaining = "100%";

        } else if (goalValue == 0) {
            formatRemaining = "~%";
        } else {
            remaining = ((float) actualValue / goalValue * 100);
            full = 100 - remaining;
            formatRemaining = String.format(Locale.ENGLISH, "%.1f%%", remaining);
        }


        SliceValue goal = new SliceValue(remaining, R.color.gold);
        SliceValue actual = new SliceValue(full, Color.LTGRAY);

        values.add(goal);
        values.add(actual);


        PieChartData unlockPieData = new PieChartData(values);
        unlockPieData.setHasLabels(false);
        unlockPieData.setHasCenterCircle(true);

        unlockPieData.setCenterText1(formatRemaining);
        unlockPieData.setCenterText1Typeface(Typeface.DEFAULT_BOLD);
        unlockPieData.setCenterText1Color(R.color.gold);
        unlockPieData.setCenterText1FontSize(25);


        unlockPie.setChartRotationEnabled(false);
        unlockPie.setPieChartData(unlockPieData);

    }

    private void generateUsagePie(String packageName) {
        List<SliceValue> values = new ArrayList<>();

        long goalValue = Long.parseLong(App.goalDataBase.get(App.DATE, GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_USAGE)) / 60000;
        long actualValue = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.USAGE_TIME, packageName)) / 60000;

        float full = 100;
        float remaining = 0;
        String formatRemaining;

        if (actualValue > goalValue & goalValue > 0) {
            formatRemaining = "100%";

        } else if (goalValue == 0) {
            formatRemaining = "~%";
        } else {
            remaining = ((float) actualValue / goalValue * 100);
            full = 100 - remaining;
            formatRemaining = String.format(Locale.ENGLISH, "%.1f%%", remaining);
        }


        SliceValue goal = new SliceValue(remaining, R.color.gold);
        SliceValue actual = new SliceValue(full, Color.LTGRAY);

        values.add(goal);
        values.add(actual);


        PieChartData usagePieData = new PieChartData(values);
        usagePieData.setHasLabels(false);
        usagePieData.setHasCenterCircle(true);

        usagePieData.setCenterText1(formatRemaining);
        usagePieData.setCenterText1Typeface(Typeface.DEFAULT_BOLD);
        usagePieData.setCenterText1Color(R.color.gold);
        usagePieData.setCenterText1FontSize(25);

        usagePie.setChartRotationEnabled(false);
        usagePie.setPieChartData(usagePieData);
    }

    public static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        List<ImageAdapter.ImageViewHolder> imageList;
        private ImageAdapter.OnItemClickListener mListener;//CUSTOM ON CLICK LISTENER
        private Context context;
        private List<Goal> subscriptions;
        private PackageManager packageManager;


        ImageAdapter(Context context, List<Goal> subscriptions) {
            this.context = context;
            this.subscriptions = subscriptions;
            packageManager = context.getPackageManager();
            imageList = new ArrayList<>();


        }

        void getSelected(int position) {
            for (ImageViewHolder holder : imageList) {
                holder.icon.setAlpha(.4f);
            }

            imageList.get(position).icon.setAlpha(1f);
        }

        @NonNull
        @Override
        public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.app_recycler_icons, parent, false);

            return new ImageViewHolder(v, mListener);
        }


        @Override
        public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {
            Goal goal = subscriptions.get(position);
            imageList.add(holder);


            try {
                ApplicationInfo ai = packageManager.getApplicationInfo(goal.getPackageName(), 0);
                Drawable icon = ai.loadIcon(packageManager);
                holder.icon.setImageDrawable(icon);
                holder.icon.setAlpha(.4f);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


        }

        @Override
        public int getItemCount() {
            return subscriptions.size();
        }

        void setOnItemClickListener(ImageAdapter.OnItemClickListener listener) {
            mListener = listener;
        }

        //CUSTOM ONCLICK LISTENER INTERFACE
        public interface OnItemClickListener {
            void onItemClick(int position, ImageView v);
        }
        ////END CUSTOM ONCLICK LISTENER INTERFACE

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;

            ImageViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);

                //INITIALIZE ON CLICK LISTENER IN VIEW HOLDER
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                listener.onItemClick(position, icon);

                            }
                        }
                    }
                });
            }
        }
    }

}


