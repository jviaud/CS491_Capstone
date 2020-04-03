package com.example.cs491_capstone.ui.goal;

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
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

import static com.example.cs491_capstone.App.currentPeriod;
import static com.example.cs491_capstone.App.week;

public class PhoneGoalFragment extends Fragment {
    private ComboLineColumnChartView usageBarChart;
    private ComboLineColumnChartData usageChartData;
    private PieChartView usagePie;
    private PieChartData usagePieData;
    private PieChartView unlockPie;
    private PieChartData unlockPieData;


    private ComboLineColumnChartView unlockBarChart;
    private ComboLineColumnChartData unlockChartData;

    private CardView usageCard, unlockCard;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goals_graph_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usageBarChart = view.findViewById(R.id.graph);
        unlockBarChart = view.findViewById(R.id.unlock_graph);
        usagePie = view.findViewById(R.id.usage_pie);
        unlockPie = view.findViewById(R.id.unlock_pie);
        usageCard = view.findViewById(R.id.usage_card);
        unlockCard = view.findViewById(R.id.unlock_card);

        unlockPie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usageCard.setVisibility(View.VISIBLE);
                usageCard.setVisibility(View.GONE);
            }
        });

        usagePie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usageCard.setVisibility(View.GONE);
                usageCard.setVisibility(View.VISIBLE);

            }
        });


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                ///USAGE
                usageChartData = new ComboLineColumnChartData(generateColumnDataAsUsage(), generateLineDataAsUsage());

                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);

                axisX.setName("Day of Week");
                axisY.setName("Usage vs. Goal Value (minutes)");

                List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
                for (int i = 0; i < week.length; i++) {
                    axisValues.add(new AxisValue(i).setLabel(week[i]));
                }
                axisX.setValues(axisValues);

                usageChartData.setAxisXBottom(axisX);
                usageChartData.setAxisYLeft(axisY);


                ///UNLOCKS
                unlockChartData = new ComboLineColumnChartData(generateColumnDataAsUnlock(), generateLineDataAsUnlock());

                Axis unlockAxisY = new Axis().setHasLines(true);
                unlockAxisY.setName("Unlock vs. Goal Value");

                unlockChartData.setAxisXBottom(axisX);
                unlockChartData.setAxisYLeft(unlockAxisY);
            }
        });

    }

    private void generateUnlockPie() {
        List<SliceValue> values = new ArrayList<>();


        SliceValue goal = new SliceValue(25f, Color.CYAN);
        SliceValue actual = new SliceValue(75f, Color.LTGRAY);

        values.add(goal);
        values.add(actual);


        unlockPieData = new PieChartData(values);
        unlockPieData.setHasLabels(false);
        unlockPieData.setHasCenterCircle(true);

        unlockPieData.setCenterText1("25%");
//        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), font);
        unlockPieData.setCenterText1Typeface(Typeface.DEFAULT_BOLD);
        unlockPieData.setCenterText1Color(Color.CYAN);
        unlockPieData.setCenterText1FontSize(25);


        unlockPie.setChartRotationEnabled(false);
        unlockPie.setPieChartData(unlockPieData);

    }

    private void generateUsagePie() {
        List<SliceValue> values = new ArrayList<>();

        SliceValue goal = new SliceValue(35f, Color.DKGRAY);
        SliceValue actual = new SliceValue(65f, Color.LTGRAY);

        values.add(goal);
        values.add(actual);


        usagePieData = new PieChartData(values);
        usagePieData.setHasLabels(false);
        usagePieData.setHasCenterCircle(true);

        usagePieData.setCenterText1("35%");
        usagePieData.setCenterText1Typeface(Typeface.DEFAULT_BOLD);
        usagePieData.setCenterText1Color(Color.DKGRAY);
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


                //USAGE COLUMN
                usageBarChart.setValueSelectionEnabled(true);
                usageBarChart.setZoomEnabled(false);
                usageBarChart.setInteractive(true);
                usageBarChart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);

                usageBarChart.setComboLineColumnChartData(usageChartData);


                //UNLOCKS COLUMN
                unlockBarChart.setValueSelectionEnabled(true);
                unlockBarChart.setZoomEnabled(false);
                unlockBarChart.setInteractive(true);
                unlockBarChart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);

                unlockBarChart.setComboLineColumnChartData(unlockChartData);
            }
        });
    }

    private ColumnChartData generateColumnDataAsUsage() {
        int numColumns = week.length;
        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStat(currentPeriod.get(0).get(i), DatabaseHelper.USAGE_TIME)) / 60000; //Math.random() * 50 + 5;

                Log.i("VALUES", " BAR VALUES:" + value);

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
            }

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }

        return new ColumnChartData(columns);
    }

    private LineChartData generateLineDataAsUsage() {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < week.length; i++) {

            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_PHONE, GoalDataBaseHelper.GOAL_USAGE)) / 60000; //Math.random() * 50 + 5;
                PointValue pointValue = null;

                Log.i("VALUES", "LINE VALUE:" + value);


                if (value == 0) {
                    pointValue = new PointValue(i, -1);
                    break;
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

                values.add(pointValue);

            }

            Line line = new Line(values).setHasLabelsOnlyForSelected(true);
            line.setColor(Color.RED);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        return new LineChartData(lines);

    }

    private ColumnChartData generateColumnDataAsUnlock() {
        int numColumns = week.length;
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStat(currentPeriod.get(0).get(i), DatabaseHelper.UNLOCKS_COUNT));


                if (value == 0) {
                    values.add(new SubcolumnValue(value, Color.TRANSPARENT));
                    break;
                } else {
                    SubcolumnValue subcolumnValue = new SubcolumnValue(value, Color.CYAN);

                    subcolumnValue.setLabel(String.valueOf(value));

                    values.add(subcolumnValue);
                }
            }

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }

        return new ColumnChartData(columns);
    }

    private LineChartData generateLineDataAsUnlock() {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < week.length; i++) {

            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_PHONE, GoalDataBaseHelper.GOAL_UNLOCKS)) / 60000; //Math.random() * 50 + 5;
                PointValue pointValue = null;

                Log.i("VALUES", "LINE VALUE:" + value);


                if (value == 0) {
                    pointValue = new PointValue(i, -1);
                    break;
                } else {
                    pointValue = new PointValue(i, value);

                    pointValue.setLabel(String.valueOf(value));

                }

                values.add(pointValue);

            }

            Line line = new Line(values).setHasLabelsOnlyForSelected(true);
            line.setColor(Color.CYAN);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        return new LineChartData(lines);

    }

}
