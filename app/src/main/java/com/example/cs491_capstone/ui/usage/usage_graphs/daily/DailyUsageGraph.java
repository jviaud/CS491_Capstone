package com.example.cs491_capstone.ui.usage.usage_graphs.daily;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;

import java.util.ArrayList;
import java.util.HashMap;
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
import static com.example.cs491_capstone.App.currentPeriod;

public class DailyUsageGraph extends Fragment {
    private static boolean byCategory = false;
    private static HashMap<String, Integer> categoryMap;
    private final String MAX_PREV = currentPeriod.get(3).get(0);
    private final String MAX_NEXT = currentPeriod.get(0).get(6);
    private ColumnChartView barChart;
    private TextView todayDate;
    private Button prevButton, nextButton;
    private Button changeGraph;
    private String graphDate;
    private int indexInPeriod = 0;
    private int indexInWeek;

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabbed_usage_graphs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.daily_chart);
        final TextView showToday = view.findViewById(R.id.show_today);
        todayDate = view.findViewById(R.id.date);
        prevButton = view.findViewById(R.id.backarrow);
        nextButton = view.findViewById(R.id.nextarrow);
        changeGraph = view.findViewById(R.id.change_graph);


        indexInWeek = currentPeriod.get(indexInPeriod).indexOf(App.DATE);
        graphDate = currentPeriod.get(indexInPeriod).get(indexInWeek);
        Log.i("GRAPHS", "DATE=" + graphDate + "|" + currentPeriod.get(0).get(6));
        if (graphDate.equals(MAX_NEXT)) {
            nextButton.setVisibility(View.GONE);
        }

        changeGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byCategory = !byCategory;
                if (byCategory) {
                    changeGraph.setText(R.string.byApps);
                } else {
                    changeGraph.setText(R.string.byCategory);
                }
            }
        });


        showToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graphDate = App.DATE;
                todayDate.setText(graphDate);
                if (graphDate.equals(MAX_NEXT)) {
                    nextButton.setVisibility(View.GONE);
                }
                showToday.setVisibility(View.GONE);
                createUsageChart(graphDate, byCategory);

            }
        });


        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("GRAPHS", "PREV1.INDEX IN WEEK " + indexInWeek + "| INDEX IN PERIOD " + indexInPeriod);
                nextButton.setVisibility(View.VISIBLE);
                indexInWeek--;
                if (indexInWeek >= 0) {
                    graphDate = currentPeriod.get(indexInPeriod).get(indexInWeek);
                    todayDate.setText(graphDate);
                    createUsageChart(graphDate, byCategory);
                } else {
                    indexInPeriod++;
                    indexInWeek = 6;
                    if (indexInPeriod < 4) {
                        graphDate = currentPeriod.get(indexInPeriod).get(indexInWeek);
                        todayDate.setText(graphDate);
                        createUsageChart(graphDate, byCategory);
                    }
                }
                if (graphDate.equals(MAX_PREV)) {
                    prevButton.setVisibility(View.GONE);
                }
                if (!graphDate.equals(App.DATE)) {
                    showToday.setVisibility(View.VISIBLE);
                } else {
                    showToday.setVisibility(View.GONE);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("GRAPHS", "NEXT1.INDEX IN WEEK " + indexInWeek + "| INDEX IN PERIOD " + indexInPeriod);
                prevButton.setVisibility(View.VISIBLE);
                indexInWeek++;
                if (indexInWeek <= 6) {
                    graphDate = currentPeriod.get(indexInPeriod).get(indexInWeek);
                    todayDate.setText(graphDate);
                    createUsageChart(graphDate, byCategory);
                } else {
                    indexInPeriod--;
                    indexInWeek = 0;
                    if (indexInPeriod > 0) {
                        graphDate = currentPeriod.get(indexInPeriod).get(indexInWeek);
                        todayDate.setText(graphDate);
                        createUsageChart(graphDate, byCategory);
                    }

                }
                if (graphDate.equals(MAX_NEXT)) {
                    nextButton.setVisibility(View.GONE);
                }
                if (!graphDate.equals(App.DATE)) {
                    showToday.setVisibility(View.VISIBLE);
                } else {
                    showToday.setVisibility(View.GONE);
                }
            }
        });

        todayDate.setText(graphDate);
    }


    @Override
    public void onResume() {
        super.onResume();
        createUsageChart(graphDate, byCategory);
    }

    private void createUsageChart(String date, boolean byCategory) {

        //STYLING FOR GRAPHS
        barChart.setZoomEnabled(false);
        barChart.setInteractive(true);
        barChart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
        float maxValue = 0;
        //


        //THE LIST OF VALUES FOR THEE X-AXIS
        List<AxisValue> xAxisValues = new ArrayList<>();
        //A LIST OF COLUMNS TO APPEAR ON THE GRAPH
        List<Column> columns = new ArrayList<>();
        //EACH COLUMN CAN HAVE A SUB-COLUMN / I AM NOT SURE IF IT IS ALLOWED TO BE 0
        List<SubcolumnValue> values;

        //FOR NOW WE ONLY NEED ONE SUB COLUMN
        int numSubColumns;
        if (byCategory) {
            numSubColumns = 8;
        } else {
            numSubColumns = 1;
        }


        //THE NUMBER OF COLUMNS IS THE CURRENT HOUR, THIS IS DONE FOR STYLING PURPOSES
        //THERE IS NO REASON TO SHOW THE COLUMNS AFTER THE CURRENT HOUR BECAUSE WE KNOW THEY WILL BE 0
        int numColumns = clock.length;

        //FOR EVERY COLUMN
        for (int i = 0; i < numColumns; ++i) {
            //WE CREATE A LIST OF VALUES, THIS WILL COME IN HANDY WHEN WE SPLIT BY CATEGORY AND HAVE A STACKED BAR GRAPH BUT FOR NOW IT WILL ONLY HOLD ONE VALUE
            values = new ArrayList<>();
            for (int j = 0; j < numSubColumns; ++j) {
                long value = 0;
                if (byCategory) {
                    value = Long.parseLong(App.localDatabase.getSumTotalStat(date, i + "", DatabaseHelper.USAGE_TIME)) / 60000;

                } else {
                    //WE DO NOT NEED TO CATCH ANY EXCEPTIONS HERE BECAUSE THE getSumTotalStat() METHOD WILL RETURN 0 IF NULL AND ALL DATA IS CLEAN WHEN INSERTED INTO THE GRAPH
                    value = Long.parseLong(App.localDatabase.getSumTotalStat(date, i + "", DatabaseHelper.USAGE_TIME)) / 60000;
                }


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
