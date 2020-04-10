package com.example.cs491_capstone.ui.usage.usage_graphs.weekly;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.UserUsageInfo;
import com.example.cs491_capstone.ui.detailed.DetailedAppActivity;
import com.example.cs491_capstone.ui.usage.UsageFragment;
import com.example.cs491_capstone.ui.usage.UsageListViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;

import static com.example.cs491_capstone.App.getHexForCategory;
import static com.example.cs491_capstone.App.localDatabase;
import static com.example.cs491_capstone.App.week;
import static com.example.cs491_capstone.ui.usage.UsageFragment.weeksSingleFormat;


public class WeeklyUsageGraph extends Fragment implements View.OnClickListener {
    /**
     * boolean to tell the graph if it should generate a stacked graph or not
     */
    private static boolean byCategory = false;
    private static List<UserUsageInfo> usedList;
    /**
     * The maximum number of days in the weeks list, we could get this manually, it is just 7*4
     */
    private final int MAX_NEXT = weeksSingleFormat.size();
    /**
     * The current index we are on inside the week list
     */
    private int Week = 0;
//    private int indexInWeek;
    /**
     * Layout under the graph used to hold keys
     */
    private GridLayout keyContainer;
    /**
     * graph show data
     */
    private ColumnChartView barChart;
    /**
     * view showing Today's date
     */
    private TextView todayDate;
    /**
     * prev button to change graph to prev date
     */
    private Button prevButton,
    /**
     * next button to change graph to next date
     */
    nextButton;
    /**
     * boolean representing the state of the graph, either the graph is stacked or regular
     */
    private Button changeGraph;
    /***
     * string holding the date of the current graph data
     */
    private String graphDate;
    /**
     * automatically goes to today's graph
     */
    private TextView showToday;
    /**
     * list adapter for list view
     */
    private UsageListViewAdapter listAdapter;
    /**
     * list view containing either apps used or categories used depending on value of boolean byCategory
     */
    private ListView listView;
    /**
     * the title of the list view shows either category or apps
     */
    private TextView listTitle;

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
        listView = view.findViewById(R.id.used_list);
        listTitle = view.findViewById(R.id.listlabel);
        ///


        //INDEX IN THE ARRAY LIST CONTAINING DATES OF THE LAST 4 WEEKS INCLUSIVE
        //USED TO EITHER MOVE BACK OR FORWARD TO SHOW DIFFERENT GRAPHS
//        indexInWeek = weeksSingleFormat.indexOf(App.DATE);
        //TO START THE GRAPH DATE IS TODAY'S DATE
        graphDate = "Week of " + App.currentPeriod.get(0).get(0);
        todayDate.setText("Week of " + graphDate);


        //CREATE A SINGLE ON CLICK LISTENER AND APPLY ALL CLICKABLE VIEWS TO IT
        //THIS WAY I DON'T HAVE TO CREATE SEPARATE ONES AND CLOG THE THIS METHOD
        changeGraph.setOnClickListener(this);
        showToday.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                createUsageChart(graphDate, byCategory);
            }
        });

    }

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

    @Override
    public void onResume() {
        super.onResume();
        //WEIRD ERRORS CAUSE BY RESUMING WITH THE CATEGORY GRAPH
        //TOO AVOID IT WE JUST SET THE GRAPH BACK TO NORMAL
        //BOOLEAN IS SET BACK TO FALSE
        todayDate.setText(graphDate);

        if (byCategory) {
            //BUTTON TEXT IS SET BACK TO DEFAULT
            changeGraph.setText(R.string.byCategory);
            listTitle.setText(R.string.listApps);

            keyContainer.removeAllViewsInLayout();
            //TODO MAKE LAYOUT HEIGHT ZERO
            byCategory = false;
            //AND GENERATE THE NORMAL GRAPH
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    createUsageChart(graphDate, false);
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void createUsageChart(String date, boolean byCategory) {

        keyContainer.removeAllViewsInLayout();

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
        int numColumns = week.length;

        //CHOOSE WHAT KIND OF GRAPH WE ARE CREATING BASED ON IF THE CATEGORY BUTTON WAS CLICKED
        if (byCategory) {
            //
            for (int i = 0; i < numColumns; i++) {
                values = new ArrayList<>();
                for (int j = 0; j < UsageFragment.category.length; j++) {
                    String category = UsageFragment.category[j];
                    long value = Long.parseLong(App.localDatabase.getSumTotalStatByCategory(App.currentPeriod.get(Week).get(i), DatabaseHelper.USAGE_TIME, category)) / 60000;

                    if (value == 0) {
                        //  values.add(new SubcolumnValue(value, Color.TRANSPARENT));
                    } else {
                        //THE SUB COLUMNS COLOR IS CHOSEN FROM A LIST OF COLORS SO IT WILL ALWAYS BE THE SAME COLOR
                        SubcolumnValue subcolumnValue = new SubcolumnValue(value, Color.parseColor(getHexForCategory(getContext(), category)));

                        subcolumnValue.setLabel("");
                        values.add(subcolumnValue);

                    }

                    if (maxValue < value) {
                        maxValue = value;
                    }
                }
                //THIS IS WHERE WE LABEL THE X-AXIS
                //WE SET THE CURRENT COLUMNS X-AXIS LABEL EQUAL TO THE CORRESPONDING TIME ON THE CLOCK
                xAxisValues.add(new AxisValue(i).setLabel(week[i]));
                //WE CREATE A COLUMN WE THE VALUES WE JUST RECEIVED FROM THE TABLE/DATABASE
                Column column = new Column(values)
                        .setHasLabelsOnlyForSelected(true);
                column.setHasLabels(false);
                //ADD THE CURRENT COLUMN TO THE LIST OF COLUMNS
                columns.add(column);
            }
            ArrayList<String> categories = localDatabase.categoryUsed(date, DatabaseHelper.USAGE_TIME);

            for (String category : categories) {
                long val = Long.parseLong(localDatabase.getSumTotalStatByCategory(date, DatabaseHelper.USAGE_TIME, category)) / 60000;
                int hours = (int) (val / (60) % 24);
                int minutes = (int) (val % 60);
                String formattedVal;
                if (hours == 0) {
                    formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
                } else {
                    formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
                }


                TextView key = new TextView(getContext());
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(15, 15, 15, 15);
                key.setLayoutParams(lp);
                key.setText(category + " " + formattedVal);
                key.setTextSize(15);
                key.setTextColor(Color.parseColor(getHexForCategory(getContext(), category)));
                keyContainer.addView(key);
            }

        } else {
            for (int i = 0; i < numColumns; ++i) {
                //WE CREATE A LIST OF VALUES, THIS WILL COME IN HANDY WHEN WE SPLIT BY CATEGORY AND HAVE A STACKED BAR GRAPH BUT FOR NOW IT WILL ONLY HOLD ONE VALUE
                values = new ArrayList<>();
                for (int j = 0; j < 1; ++j) {

                    //WE DO NOT NEED TO CATCH ANY EXCEPTIONS HERE BECAUSE THE getSumTotalStat() METHOD WILL RETURN 0 IF NULL AND ALL DATA IS CLEAN WHEN INSERTED INTO THE GRAPH
                    long value = Long.parseLong(App.localDatabase.getSumTotalStat(App.currentPeriod.get(Week).get(i), DatabaseHelper.USAGE_TIME)) / 60000;


                    //WE DIVIDE THE TOTAL TIME IN MILLI BY 60000 TO GET THE NUMBER OF MINUTES

                    if (value == 0) {
                        values.add(new SubcolumnValue(value, Color.TRANSPARENT));
                        break;
                    } else {
                        SubcolumnValue subcolumnValue = new SubcolumnValue(value, Color.LTGRAY);
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
                xAxisValues.add(new AxisValue(i).setLabel(week[i]));

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
                .setName("Days of the Week") //NAME OF X-AXIS
                .setHasTiltedLabels(true)  //MAKES THE LABELS TILTED SO WE CAN FIT MOORE LABELS ON THE X-AXIS
                .setTextColor(Color.WHITE)//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(4)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        Axis axisY = new Axis()
                .setName("Time Used (minutes)")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor(Color.WHITE)//MAKES TEXT COLOR BLACK
                ;


        //SET AXIS TO GRAPH
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        //SET COLUMNS TO GRAPH
        barChart.setColumnChartData(data);

        barChart.setOnValueTouchListener(new ValueTouchListener());

        if (maxValue < 10) {
            Viewport v = new Viewport(barChart.getMaximumViewport());
            v.top = 15;
            barChart.setMaximumViewport(v);
            barChart.setCurrentViewport(v);
            barChart.setViewportCalculationEnabled(false);
        } else {
            Viewport v = new Viewport(barChart.getMaximumViewport());
            v.top = ((maxValue + 4) / 5) * 5; //NEXT MULTIPLE OF 5;
            barChart.setMaximumViewport(v);
            barChart.setCurrentViewport(v);
            barChart.setViewportCalculationEnabled(false);
        }

        // Set selection mode to keep selected month column highlighted.
        barChart.setValueSelectionEnabled(true);
        // barChart.


        //ANIMATE GRAPH / NOT ACTUALLY NECESSARY
        barChart.animate();

    }

    private void swapGraph() {
        byCategory = !byCategory;
        if (byCategory) {
            changeGraph.setText(R.string.byApps);
            listTitle.setText((R.string.listCategory));

        } else {
            //REMOVE THE KEY
            keyContainer.removeAllViewsInLayout();
            changeGraph.setText(R.string.byCategory);
            listTitle.setText(R.string.listApps);
        }
        createUsageChart(graphDate, byCategory);
    }

    private void showTodayGraph() {
        //WHEN SHOW TODAY IS PRESSED EVERYTHING HAS TO BE RESET
        //DATE IS SET TOO TODAY
        Week = 0;
        graphDate = App.currentPeriod.get(0).get(0);
        //DATE TITLE IS SET TO TODAY
        todayDate.setText("Week of " + graphDate);
        //HIDE THE NEXT BUTTON, WE DO NOT SHOW FUTURE GRAPHS BECAUSE WE KNOW THEY ARE BLANK
        nextButton.setVisibility(View.GONE);
        //GRAPH IS SHOWING TODAY SO WE DO NOT SHOW THE SKIP TO TODAY BUTTON
        showToday.setVisibility(View.VISIBLE);
        //WE SHOW THE PREV BUTTON BECAUSE IT MAY HAVE BEEN SET TO GONE IF WE REACHED THE BEGINNING OF THE LIST
        prevButton.setVisibility(View.VISIBLE);

        //THIS IS THE INDEX OF TODAY

        //CREATE THE GRAPH
        createUsageChart(graphDate, byCategory);
    }

    private void nextGraph() {
        //IF THIS HAS BEEN CLICKED AT LEAST ONCE THEN IT MEANS WE ARE NO LONGER AT THE END AND CAN SHOW THE PREV BUTTON
        prevButton.setVisibility(View.VISIBLE);
        //GO FORWARD ONE DAY
        Week--;

        //MAX_NEXT IS THE END OOF THE LIST, PREVENTS OUT OF BOUNDS EXCEPTION
        if (Week >= 0) {
            //FIRST CHANGE THE GRAPH DATE TO THE DATE IN THE LIST
            graphDate = App.currentPeriod.get(Week).get(0);
            if (Week == 0) {
                nextButton.setVisibility(View.GONE);
            }
            //IF THIS DATE IS EQUAL TO TODAY'S DATE THEN WE HIDE THE BUTTONS
            if (!graphDate.equals(App.currentPeriod.get(0).get(0))) {
                nextButton.setVisibility(View.VISIBLE);
                showToday.setVisibility(View.VISIBLE);
            } else {
                //ELSE WE SHOW THE BUTTON TOO SKIP TO TODAY'S DATE
                showToday.setVisibility(View.GONE);
            }
            //SET THE DATE TEXT AND GENERATE THE GRAPH
            todayDate.setText("Week of " + graphDate);
            createUsageChart(graphDate, byCategory);
        } else {
            //IF WE ARE AT THE END OOF THE LIST THEN WE HIDE THE NEXT BUTTON
            nextButton.setVisibility(View.GONE);
        }


    }

    private void prevGraph() {
        //IF THIS HAS BEEN CLICKED AT LEAST ONCE THEN IT MEANS WE ARE NO LONGER AT THE BEGINNING AND CAN SHOW THE NEXT BUTTON
        nextButton.setVisibility(View.VISIBLE);
        //GO BACK ONE DAY
        Week++;

        //ONLY IF WE HAVE NOT PASSED THE BEGINNING OF THE LIST, THIS PREVENTS A NEGATIVE OUT OF BOUNDS EXCEPTION
        if (Week <= 3) {
            graphDate =App.currentPeriod.get(Week).get(0);
            if (Week == 3) {
                prevButton.setVisibility(View.GONE);
            }
            //AS LONG AS THE GRAPH DATE ISN'T SHOWING TODAY THEN WE SHOW THE SKIP TO TODAY BUTTON
            if (graphDate.equals(App.currentPeriod.get(0).get(0))) {
                showToday.setVisibility(View.GONE);
            } else {
                //ELSE WE HIDE THE BUTTON
                showToday.setVisibility(View.VISIBLE);
            }
            //SET THE DATE TEXT AND GENERATE THE GRAPH
            todayDate.setText("Week of " + graphDate);
            createUsageChart(graphDate, byCategory);
        } else {
            //IF WE HAVE EXCEEDED THE LIMIT THEN HIDE THE PREV BUTTON
            prevButton.setVisibility(View.GONE);
        }


    }


    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {



            usedList = localDatabase.appsUsed(App.currentPeriod.get(Week).get(columnIndex), DatabaseHelper.USAGE_TIME);


            listAdapter = new UsageListViewAdapter(getContext(), usedList);
            listAdapter.setByCategory(byCategory);
            listAdapter.setDay(App.currentPeriod.get(Week).get(columnIndex));
            listAdapter.setColumn(DatabaseHelper.USAGE_TIME);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UserUsageInfo item = (UserUsageInfo) parent.getItemAtPosition(position);

                    Intent intent = new Intent(getContext(), DetailedAppActivity.class);
                    intent.putExtra("APP", item);
                    startActivity(intent);
                }
            });

            App.setListViewHeightBasedOnChildren(listView);
        }

        @Override
        public void onValueDeselected() {
            listAdapter = new UsageListViewAdapter(getContext(), new ArrayList<UserUsageInfo>());
            listView.setAdapter(listAdapter);
            App.setListViewHeightBasedOnChildren(listView);
        }
    }
}