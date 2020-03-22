package com.example.cs491_capstone.ui.usage.usage_graphs.daily;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.usage.UsageFragment;

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
import static com.example.cs491_capstone.ui.usage.UsageFragment.categoryKey;
import static com.example.cs491_capstone.ui.usage.UsageFragment.weeksSingleFormat;

public class DailyUsageGraph extends Fragment {

    private static boolean[] keyTrack = new boolean[]{false, false, false, false, false, false, false, false};
    private static boolean byCategory = false;
    private final int MAX_NEXT = weeksSingleFormat.size();
    private ColumnChartView barChart;
    private TextView todayDate;
    private Button prevButton, nextButton;
    private Button changeGraph;
    private String graphDate;
    private int indexInWeek;
    private LinearLayout keyContainer;
    private TextView showToday;

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabbed_usage_graphs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //FIND AND INITIALIZE ALL VIEWS
        showToday = view.findViewById(R.id.show_today);
        barChart = view.findViewById(R.id.daily_chart);
        todayDate = view.findViewById(R.id.date);
        prevButton = view.findViewById(R.id.backarrow);
        nextButton = view.findViewById(R.id.nextarrow);
        changeGraph = view.findViewById(R.id.change_graph);
        keyContainer = view.findViewById(R.id.keycontainer);

        indexInWeek = weeksSingleFormat.indexOf(App.DATE);
        graphDate = weeksSingleFormat.get(indexInWeek);

        Click click = new Click();
        changeGraph.setOnClickListener(click);
        showToday.setOnClickListener(click);
        prevButton.setOnClickListener(click);
        nextButton.setOnClickListener(click);

    }


    @Override
    public void onResume() {
        super.onResume();
        byCategory = false;
        changeGraph.setText(R.string.byCategory);
        todayDate.setText(graphDate);
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


        //THE NUMBER OF COLUMNS IS THE CURRENT HOUR, THIS IS DONE FOR STYLING PURPOSES
        //THERE IS NO REASON TO SHOW THE COLUMNS AFTER THE CURRENT HOUR BECAUSE WE KNOW THEY WILL BE 0
        int numColumns = clock.length;

        //CHOOSE WHAT KIND OF GRAPH WE ARE CREATING BASED ON IF THE CATEGORY BUTTON WAS CLICKED
        if (byCategory) {
            //
            for (int i = 0; i < numColumns; i++) {
                values = new ArrayList<>();
                for (int j = 0; j < UsageFragment.category.length; j++) {
                    long value = Long.parseLong(App.localDatabase.getSumTotalStatByCategory(date, i + "", DatabaseHelper.USAGE_TIME, UsageFragment.category[j])) / 60000;

                    if (value == 0) {
                        values.add(new SubcolumnValue(value, Color.TRANSPARENT));
                    } else {
                        //THE SUB COLUMNS COLOR IS CHOSEN FROM A LIST OF COLORS SO IT WILL ALWAYS BE THE SAME COLOR
                        SubcolumnValue subcolumnValue = new SubcolumnValue(value, categoryKey[j]);

                        int hours = (int) (value / (60) % 24);
                        int minutes = (int) (value % 60);
                        String formattedVal;
                        if (hours == 0) {
                            formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
                            //  subcolumnValue.setLabel(String.format(Locale.ENGLISH, "%d%s", minutes, "m"));
                        } else {
                            formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
                            //   subcolumnValue.setLabel(String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m"));
                        }
                        subcolumnValue.setLabel("");
                        values.add(subcolumnValue);

                        //DYNAMICALLY ADD THE KEYS FOR ONLY THE COLUMN CATEGORIES THAT HAVE BEEN USED
                        if (!keyTrack[j]) {
                            TextView key = new TextView(getContext());
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                                    LinearLayout.LayoutParams.WRAP_CONTENT);// Height of TextView
                            lp.setMargins(15, 15, 15, 15);
                            key.setLayoutParams(lp);
                            key.setText(UsageFragment.category[j] + " " + formattedVal);
                            key.setTextSize(15);
                            key.setTextColor(categoryKey[j]);
                            keyContainer.addView(key);
                            keyTrack[j] = true;
                        }
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
                column.setHasLabels(false);

                //ADD THE CURRENT COLUMN TO THE LIST OF COLUMNS
                columns.add(column);
            }
        } else {
            for (int i = 0; i < numColumns; ++i) {
                //WE CREATE A LIST OF VALUES, THIS WILL COME IN HANDY WHEN WE SPLIT BY CATEGORY AND HAVE A STACKED BAR GRAPH BUT FOR NOW IT WILL ONLY HOLD ONE VALUE
                values = new ArrayList<>();
                for (int j = 0; j < 1; ++j) {

                    //WE DO NOT NEED TO CATCH ANY EXCEPTIONS HERE BECAUSE THE getSumTotalStat() METHOD WILL RETURN 0 IF NULL AND ALL DATA IS CLEAN WHEN INSERTED INTO THE GRAPH
                    long value = Long.parseLong(App.localDatabase.getSumTotalStat(date, i + "", DatabaseHelper.USAGE_TIME)) / 60000;


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
        }


        //FOR EVERY COLUMN


        //PASS THE LIST OF COLUMNS TO THE GRAPH
        ColumnChartData data = new ColumnChartData(columns);
        //MAKE THE BAR GRAPH STACKED
        if (byCategory) {
            data.setStacked(true);
            data.setValueLabelBackgroundColor(Color.TRANSPARENT);
            data.setValueLabelsTextColor(Color.TRANSPARENT);
        }


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
            v.top = ((maxValue + 4) / 5) * 5; //maxValue + 10;
            barChart.setMaximumViewport(v);
            barChart.setCurrentViewport(v);
            barChart.setViewportCalculationEnabled(false);
        }

        // Set selection mode to keep selected month column highlighted.
        // barChart.setValueSelectionEnabled(true);
        // barChart.


        //ANIMATE GRAPH / NOT ACTUALLY NECESSARY
        barChart.animate();

    }

    private void swapGraph() {
        byCategory = !byCategory;
        if (byCategory) {
            changeGraph.setText(R.string.byApps);
        } else {
            //REMOVE THE KEY
            keyContainer.removeAllViewsInLayout();
            keyTrack = new boolean[]{false, false, false, false, false, false, false, false};
            changeGraph.setText(R.string.byCategory);
        }
        createUsageChart(graphDate, byCategory);
    }

    private void showTodayGraph() {
        graphDate = App.DATE;
        todayDate.setText(graphDate);
        nextButton.setVisibility(View.GONE);
        showToday.setVisibility(View.GONE);

        //THIS IS THE INDEX OF TODAY
        indexInWeek = weeksSingleFormat.indexOf(App.DATE);

        createUsageChart(graphDate, byCategory);
    }

    private void nextGraph() {
        //IF THIS HAS BEEN CLICKED AT LEAST ONCE THEN IT MEANS WE ARE NO LONGER AT THE END AND CAN SHOW THE PREV BUTTON
        prevButton.setVisibility(View.VISIBLE);
        //GO FORWARD ONE DAY
        indexInWeek++;

        if (indexInWeek <= MAX_NEXT) {
            graphDate = weeksSingleFormat.get(indexInWeek);
            if (graphDate.equals(App.DATE)) {
                nextButton.setVisibility(View.GONE);
                showToday.setVisibility(View.GONE);
            }
            if (!graphDate.equals(App.DATE)) {
                showToday.setVisibility(View.VISIBLE);
            } else {
                showToday.setVisibility(View.GONE);
            }
            todayDate.setText(graphDate);
            createUsageChart(graphDate, byCategory);
        } else {
            nextButton.setVisibility(View.GONE);
        }


    }

    private void prevGraph() {
        //IF THIS HAS BEEN CLICKED AT LEAST ONCE THEN IT MEANS WE ARE NO LONGER AT THE BEGINNING AND CAN SHOW THE NEXT BUTTON
        nextButton.setVisibility(View.VISIBLE);
        //GO BACK ONE DAY
        indexInWeek--;

        if (indexInWeek >= 0) {
            graphDate = weeksSingleFormat.get(indexInWeek);
            if (indexInWeek == 0) {
                prevButton.setVisibility(View.GONE);
            }
            if (!graphDate.equals(App.DATE)) {
                showToday.setVisibility(View.VISIBLE);
            } else {
                showToday.setVisibility(View.GONE);
            }
            todayDate.setText(graphDate);
            createUsageChart(graphDate, byCategory);

        } else {
            prevButton.setVisibility(View.GONE);
        }


    }

    public class Click implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();

            switch (id) {
                case R.id.change_graph:
                    swapGraph();
                    break;
                case R.id.show_today:
                    showTodayGraph();
                    break;
                case R.id.nextarrow:
                    nextGraph();
                    break;
                case R.id.backarrow:
                    prevGraph();
                    break;

            }
        }
    }
}

