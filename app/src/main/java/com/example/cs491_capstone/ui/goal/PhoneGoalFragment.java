package com.example.cs491_capstone.ui.goal;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;

import static com.example.cs491_capstone.App.currentPeriod;
import static com.example.cs491_capstone.App.week;

public class PhoneGoalFragment extends Fragment {
    private ComboLineColumnChartView chart;
    private ComboLineColumnChartData data;
    private int maxUnlocks;
    private long maxUsage, maxValue = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goals_graph_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chart = view.findViewById(R.id.graph);
    }

    @Override
    public void onResume() {
        super.onResume();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                data = new ComboLineColumnChartData(generateColumnData(), generateLineData());

                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);

                axisX.setName("Day of Week");
                axisY.setName("Actual vs. Goal Value (minutes)");

                List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
                for (int i = 0; i < week.length; i++) {
                    axisValues.add(new AxisValue(i).setLabel(week[i]));
                }
                axisX.setValues(axisValues);

                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);

                chart.setValueSelectionEnabled(true);
                chart.setZoomEnabled(false);
                chart.setInteractive(true);
                chart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);


                chart.setComboLineColumnChartData(data);
            }
        });
    }

    private ColumnChartData generateColumnData() {
        int numColumns = week.length;
        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStat(currentPeriod.get(0).get(i), DatabaseHelper.USAGE_TIME)) / 60000; //Math.random() * 50 + 5;
                //SubcolumnValue subcolumnValue = new SubcolumnValue(value, ChartUtils.COLOR_GREEN);

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

                if (maxValue < value) {
                    maxValue = value;
                }
            }

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }

        return new ColumnChartData(columns);
    }

    private LineChartData generateLineData() {

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
            line.setColor(Color.CYAN);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        return new LineChartData(lines);

    }
}
