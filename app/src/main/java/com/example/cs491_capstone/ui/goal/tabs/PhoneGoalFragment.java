package com.example.cs491_capstone.ui.goal.tabs;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.GoalDataBaseHelper;
import com.example.cs491_capstone.R;

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

public class PhoneGoalFragment extends Fragment {
    private ComboLineColumnChartView usageComboChart;
    private ComboLineColumnChartData usageComboData;
    private PieChartView usagePie;
    private PieChartView unlockPie;

    private ComboLineColumnChartView unlockComboChart;
    private ComboLineColumnChartData unlockComboData;

    private CardView usageCard, unlockCard;

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
        usageComboChart = view.findViewById(R.id.graph);
        unlockComboChart = view.findViewById(R.id.unlock_graph);
        usagePie = view.findViewById(R.id.usage_pie);
        unlockPie = view.findViewById(R.id.unlock_pie);
        usageCard = view.findViewById(R.id.usage_card);
        unlockCard = view.findViewById(R.id.unlock_card);

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


    }

    private void setStatusChecker() {
        boolean[][] goalStatusFailed = new boolean[7][2];
        Log.i("VALUES", "TRIGGERED:" + usageStatusValues.length);
        for (int i = 0; i < 7; i++) {
            Log.i("VALUES", "USAGE:" + i + "|" + usageStatusValues[i][0] + "|" + usageStatusValues[i][1]);

            if (usageStatusValues[i][0] > usageStatusValues[i][1]) {
                String id = goalDataBase.getPhoneGoalId(currentPeriod.get(0).get(i));
                goalDataBase.setUsageStatus(id, "1");
                goalStatusFailed[i][0] = true;
            }
        }

        for (int i = 0; i < 7; i++) {
            Log.i("VALUES", "UNLOCK:" + i + "|" + unlockStatusValues[i][0] + "|" + unlockStatusValues[i][1]);

            if (unlockStatusValues[i][0] > unlockStatusValues[i][1]) {
                String id = goalDataBase.getPhoneGoalId(currentPeriod.get(0).get(i));
                goalDataBase.setUnlockStatus(id, "1");
                goalStatusFailed[i][1] = true;
            }
        }

        for (int i = 0; i < 7; i++) {
            Log.i("VALUES", "STATUS:" + i + "|" + goalStatusFailed[i][1] + "|" + goalStatusFailed[i][0]);

            if (goalStatusFailed[i][1] || goalStatusFailed[i][0]) {
                String id = goalDataBase.getPhoneGoalId(currentPeriod.get(0).get(i));
                Log.i("VALUES", "STATUS:" + i + "|FAILED");
                goalDataBase.setStatus(id, "1");
            }
        }

    }

    private void generateUnlockPie() {
        List<SliceValue> values = new ArrayList<>();


        long goalValue = Long.parseLong(goalDataBase.get(App.DATE, GoalDataBaseHelper.GOAL_PHONE, GoalDataBaseHelper.GOAL_UNLOCKS)) / 60000;
        long actualValue = Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.UNLOCKS_COUNT)) / 60000;

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

    private void generateUsagePie() {
        List<SliceValue> values = new ArrayList<>();

        long goalValue = Long.parseLong(goalDataBase.get(App.DATE, GoalDataBaseHelper.GOAL_PHONE, GoalDataBaseHelper.GOAL_USAGE)) / 60000;
        long actualValue = Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)) / 60000;

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

    @Override
    public void onResume() {
        super.onResume();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //PIE CHARTS
                generateUsagePie();
                generateUnlockPie();

                ///USAGE
                usageComboData = new ComboLineColumnChartData(generateUsageColumnData(), generateUsageLineData());
                labelUsageAxis();
                usageComboChart.setComboLineColumnChartData(usageComboData);
                setUsageViewPortWidth(usageComboChart);


                ///UNLOCKS
                unlockComboData = new ComboLineColumnChartData(generateUnlockColumnData(), generateUnlockLineData());
                labelUnlockAxis();
                unlockComboChart.setComboLineColumnChartData(unlockComboData);
                setUnlockViewPortWidth(unlockComboChart);

                setStatusChecker();
            }
        });

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

    private ColumnChartData generateUsageColumnData() {
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        maxUsageValue = 0;
        for (int i = 0; i < week.length; i++) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStat(currentPeriod.get(0).get(i), DatabaseHelper.USAGE_TIME)) / 60000; //Math.random() * 50 + 5;

                usageStatusValues[i][0] = value;


                if (value == 0) {
                    values.add(new SubcolumnValue(value, Color.TRANSPARENT));
                    break;
                } else {
                    SubcolumnValue subcolumnValue = new SubcolumnValue(value, Color.DKGRAY);

                    int hours = (int) (value / (60) % 24);
                    int minutes = (int) (value % 60);


                    if (hours == 0) {
                        subcolumnValue.setLabel(String.format(Locale.ENGLISH, "%d%s", minutes, "m"));
                    } else {
                        subcolumnValue.setLabel(String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m"));
                    }

                    values.add(subcolumnValue);
                }

                if (maxUsageValue < value) {
                    maxUsageValue = value;
                }
            }

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }

        return new ColumnChartData(columns);
    }

    private LineChartData generateUsageLineData() {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < week.length; i++) {

            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_PHONE, GoalDataBaseHelper.GOAL_USAGE)) / 60000; //Math.random() * 50 + 5;
                PointValue pointValue;

                usageStatusValues[i][1] = value;
                //Log.i("VALUES", "SET:" + i +" | " + usageStatusValues[i][0] + "|" + usageStatusValues[i][1]);
                if (value == 0) {
                    pointValue = new PointValue(i, 0);
                    pointValue.setLabel("n/a");
                } else {
                    pointValue = new PointValue(i, value);

                    int hours = (int) (value / (60) % 24);
                    int minutes = (int) (value % 60);


                    if (hours == 0) {
                        pointValue.setLabel(String.format(Locale.ENGLISH, "%d%s", minutes, "m"));
                    } else {
                        pointValue.setLabel(String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m"));
                    }

                }

                if (maxUsageValue < value) {
                    maxUsageValue = value;
                }

                values.add(pointValue);

            }

            Line line = new Line(values).setHasLabelsOnlyForSelected(true);
            line.setColor(R.color.gold);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        // Log.i("VALUES", "" + usageStatus.get(0)[3][0]);
        return new LineChartData(lines);

    }

    private ColumnChartData generateUnlockColumnData() {
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        maxUnlockValue = 0;
        for (int i = 0; i < week.length; i++) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStat(currentPeriod.get(0).get(i), DatabaseHelper.UNLOCKS_COUNT));

                unlockStatusValues[i][0] = value;

                if (value == 0) {
                    values.add(new SubcolumnValue(value, Color.TRANSPARENT));
                    break;
                } else {
                    SubcolumnValue subcolumnValue = new SubcolumnValue(value, R.color.gold);

                    subcolumnValue.setLabel(String.valueOf(value));

                    values.add(subcolumnValue);
                }

                if (maxUnlockValue < value) {
                    maxUnlockValue = value;
                }
            }

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }

        return new ColumnChartData(columns);
    }

    private LineChartData generateUnlockLineData() {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < week.length; i++) {

            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_PHONE, GoalDataBaseHelper.GOAL_UNLOCKS)); //Math.random() * 50 + 5;
                PointValue pointValue;

                unlockStatusValues[i][1] = value;


                if (value == 0) {
                    pointValue = new PointValue(i, 0);
                    pointValue.setLabel("n/a");
                } else {
                    pointValue = new PointValue(i, value);

                    pointValue.setLabel(String.valueOf(value));

                }
                if (maxUnlockValue < value) {
                    maxUnlockValue = value;
                }

                values.add(pointValue);

            }

            Line line = new Line(values).setHasLabelsOnlyForSelected(true);
            line.setColor(R.color.gold);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        return new LineChartData(lines);

    }

}
