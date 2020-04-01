package com.example.cs491_capstone.ui.goal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.R;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;

import static com.example.cs491_capstone.App.week;

public class PhoneGoalFragment extends Fragment {
    private ComboLineColumnChartView chart;
    private ComboLineColumnChartData data;
    private int maxUnlocks;
    private long maxUsage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goals_graph_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chart = view.findViewById(R.id.graph);

        data = new ComboLineColumnChartData(generateColumnData(), generateLineData());

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);

        axisX.setName("Axis X");
        axisY.setName("Axis Y");

        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        for (int i = 0; i < week.length; i++) {
            axisValues.add(new AxisValue(i).setLabel(week[i]));
        }
        axisX.setValues(axisValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);



        chart.setComboLineColumnChartData(data);
    }

    private ColumnChartData generateColumnData() {
        int numSubcolumns = 1;
        int numColumns = week.length;
        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; ++j) {
                SubcolumnValue subcolumnValue = new SubcolumnValue((float) Math.random() * 50 + 5, ChartUtils.COLOR_GREEN);



                values.add(subcolumnValue);
            }

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }

        ColumnChartData columnChartData = new ColumnChartData(columns);
        return columnChartData;
    }

    private LineChartData generateLineData() {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < 1; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < week.length; ++j) {
                PointValue pointValue = new PointValue(j, (float) Math.random() * 50 + 5);


                values.add(pointValue);
            }

            Line line = new Line(values).setHasLabelsOnlyForSelected(true);
            line.setColor(ChartUtils.COLORS[i]);
            line.setHasLines(false);
            line.setHasPoints(true);
            lines.add(line);
        }

        LineChartData lineChartData = new LineChartData(lines);

        return lineChartData;

    }
}
