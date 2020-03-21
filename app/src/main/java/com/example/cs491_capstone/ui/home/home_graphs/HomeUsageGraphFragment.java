package com.example.cs491_capstone.ui.home.home_graphs;

import android.graphics.Color;
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
import com.example.cs491_capstone.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;

import static com.example.cs491_capstone.App.clock;

public class HomeUsageGraphFragment extends Fragment {
    private ColumnChartView barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_tab_graphs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //INITIALIZE CHARTS
        barChart = view.findViewById(R.id.home_charts);
    }

    @Override
    public void onResume() {
        super.onResume();
        createUsageChart();

    }

    private void createUsageChart() {

        //STYLING FOR GRAPHS
        barChart.setZoomEnabled(false);
        barChart.setInteractive(true);
        barChart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
        boolean lessThanTen = true;
        float maxValue = 0;
        //


        //THE LIST OF VALUES FOR THEE X-AXIS
        List<AxisValue> xAxisValues = new ArrayList<>();
        //A LIST OF COLUMNS TO APPEAR ON THE GRAPH
        List<Column> columns = new ArrayList<>();
        //EACH COLUMN CAN HAVE A SUB-COLUMN / I AM NOT SURE IF IT IS ALLOWED TO BE 0
        List<SubcolumnValue> values;

        //FOR NOW WE ONLY NEED ONE SUB COLUMN
        int numSubColumns = 1;
        //THE NUMBER OF COLUMNS IS THE CURRENT HOUR, THIS IS DONE FOR STYLING PURPOSES
        //THERE IS NO REASON TO SHOW THE COLUMNS AFTER THE CURRENT HOUR BECAUSE WE KNOW THEY WILL BE 0
        int numColumns = clock.length;

        //FOR EVERY COLUMN
        for (int i = 0; i < numColumns; ++i) {
            //WE CREATE A LIST OF VALUES, THIS WILL COME IN HANDY WHEN WE SPLIT BY CATEGORY AND HAVE A STACKED BAR GRAPH BUT FOR NOW IT WILL ONLY HOLD ONE VALUE
            values = new ArrayList<>();
            for (int j = 0; j < numSubColumns; ++j) {

                //WE DO NOT NEED TO CATCH ANY EXCEPTIONS HERE BECAUSE THE getSumTotalStat() METHOD WILL RETURN 0 IF NULL AND ALL DATA IS CLEAN WHEN INSERTED INTO THE GRAPH
                long value = Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, i + "", DatabaseHelper.USAGE_TIME)) / 60000;

                Log.i("GRAPH", "Hour:" + i + " ::Time:" + value);
                //WE DIVIDE THE TOTAL TIME IN MILLI BY 60000 TO GET THE NUMBER OF MINUTES

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
            //THIS IS WHERE WE LABEL THE X-AXIS
            //WE SET THE CURRENT COLUMNS X-AXIS LABEL EQUAL TO THE CORRESPONDING TIME ON THE CLOCK
            xAxisValues.add(new AxisValue(i).setLabel(clock[i]));

            //WE CREATE A COLUMN WE THE VALUES WE JUST RECEIVED FROM THE TABLE/DATABASE
            Column column = new Column(values)
                    .setHasLabelsOnlyForSelected(true);


            //ADD THE CURRENT COLUMN TO THE LIST OF COLUMNS
            columns.add(column);
        }//END OUTER FOR LOOP

        //PASS THE LIST OF COLUMNS TO THE GRAPH
        ColumnChartData data = new ColumnChartData(columns);


        //PASS THE LIST OF X-AXIS LABELS TO THE X-AXIS
        Axis axisX = new Axis(xAxisValues)
                .setName("Hour of Day") //NAME OF X-AXIS
                .setHasTiltedLabels(true)  //MAKES THE LABELS TILTED SO WE CAN FIT MOORE LABELS ON THE X-AXIS
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(4)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        Axis axisY = new Axis()
                .setName("Time Used (minutes)")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
                ;


        //SET AXIS TO GRAPH
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        //SET COLUMNS TO GRAPH
        barChart.setColumnChartData(data);

        if (maxValue < 10) {
            Viewport v = new Viewport(barChart.getMaximumViewport());
            v.top = 15;
            barChart.setMaximumViewport(v);
            barChart.setCurrentViewport(v);
            barChart.setViewportCalculationEnabled(false);
        } else {
            Viewport v = new Viewport(barChart.getMaximumViewport());
            v.top = maxValue + 20;
            barChart.setMaximumViewport(v);
            barChart.setCurrentViewport(v);
            barChart.setViewportCalculationEnabled(false);
        }

        // Set selection mode to keep selected month column highlighted.
        barChart.setValueSelectionEnabled(true);


        //ANIMATE GRAPH / NOT ACTUALLY NECESSARY
        barChart.animate();

    }
}
