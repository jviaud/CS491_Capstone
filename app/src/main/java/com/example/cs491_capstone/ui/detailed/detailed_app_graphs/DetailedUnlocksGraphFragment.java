package com.example.cs491_capstone.ui.detailed.detailed_app_graphs;

import android.graphics.Color;
import android.os.Bundle;
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
import com.example.cs491_capstone.MainActivity;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.detailed.DetailedAppActivity;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;

import static com.example.cs491_capstone.App.clock;
import static com.example.cs491_capstone.App.unlockColColor;
import static com.example.cs491_capstone.App.week;

public class DetailedUnlocksGraphFragment extends Fragment implements View.OnClickListener {
    private ColumnChartView barChartTop;
    private ColumnChartView barChartBottom;

    private ColumnChartData barDataBottom;
    private int indexInPeriod = 0;
    /**
     * prev button to change graph to prev date
     */
    private Button prevButton,
    /**
     * next button to change graph to next date
     */
    nextButton;
    /**
     * automatically goes to today's graph
     */
    private TextView showToday;
    /***
     * string holding the date of the current graph data
     */
    private String graphDate;
    /**
     * view showing Today's date
     */
    private TextView todayDate;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detailed_activity_tab_graphs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        todayDate = view.findViewById(R.id.date);
        showToday = view.findViewById(R.id.show_today);
        prevButton = view.findViewById(R.id.backarrow);
        nextButton = view.findViewById(R.id.nextarrow);

        graphDate = App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(0), "MM/dd/yyyy") + " - " + App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(6), "MM/dd/yyyy");
        //todayDate.setText(graphDate);


        barChartTop = view.findViewById(R.id.barchart_top);
        //generateColumnDataTopAsUsage();

        showToday.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        barChartBottom = view.findViewById(R.id.barchart_bottom);
        generateInitialBarDataBottom();
    }

    @Override
    public void onResume() {
        super.onResume();
        todayDate.setText(graphDate);
        generateColumnDataTopAsUnlocks();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
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

    private void showTodayGraph() {
        //WHEN SHOW TODAY IS PRESSED EVERYTHING HAS TO BE RESET
        //DATE IS SET TOO TODAY
        indexInPeriod = 0;
        graphDate = App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(0), "MM/dd/yyyy") + " - " + App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(6), "MM/dd/yyyy");
        //DATE TITLE IS SET TO TODAY
        todayDate.setText(graphDate);
        //HIDE THE NEXT BUTTON, WE DO NOT SHOW FUTURE GRAPHS BECAUSE WE KNOW THEY ARE BLANK
        nextButton.setVisibility(View.GONE);
        //GRAPH IS SHOWING TODAY SO WE DO NOT SHOW THE SKIP TO TODAY BUTTON
        showToday.setVisibility(View.GONE);
        //WE SHOW THE PREV BUTTON BECAUSE IT MAY HAVE BEEN SET TO GONE IF WE REACHED THE BEGINNING OF THE LIST
        prevButton.setVisibility(View.VISIBLE);


        //CREATE THE GRAPH
        generateColumnDataTopAsUnlocks();
    }

    private void nextGraph() {
        //IF THIS HAS BEEN CLICKED AT LEAST ONCE THEN IT MEANS WE ARE NO LONGER AT THE END AND CAN SHOW THE PREV BUTTON
        prevButton.setVisibility(View.VISIBLE);
        //GO FORWARD ONE DAY
        indexInPeriod--;

        //MAX_NEXT IS THE END OF THE LIST, PREVENTS OUT OF BOUNDS EXCEPTION
        if (indexInPeriod >= 0) {
            //FIRST CHANGE THE GRAPH DATE TO THE DATE IN THE LIST
            graphDate = App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(0), "MM/dd/yyyy") + " - " + App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(6), "MM/dd/yyyy");
            //IF THIS DATE IS EQUAL TO TODAY'S DATE THEN WE HIDE THE BUTTONS
            if (indexInPeriod == 0) {
                nextButton.setVisibility(View.GONE);
                showToday.setVisibility(View.GONE);
            } else {
                //ELSE WE SHOW THE BUTTON TOO SKIP TO TODAY'S DATE
                showToday.setVisibility(View.VISIBLE);
            }
            //SET THE DATE TEXT AND GENERATE THE GRAPH
            todayDate.setText(graphDate);
            generateColumnDataTopAsUnlocks();
        } else {
            //IF WE ARE AT THE END OOF THE LIST THEN WE HIDE THE NEXT BUTTON
            nextButton.setVisibility(View.GONE);
        }


    }

    private void prevGraph() {
        //IF THIS HAS BEEN CLICKED AT LEAST ONCE THEN IT MEANS WE ARE NO LONGER AT THE BEGINNING AND CAN SHOW THE NEXT BUTTON
        nextButton.setVisibility(View.VISIBLE);
        //GO BACK ONE DAY
        indexInPeriod++;

        //ONLY IF WE HAVE NOT PASSED THE BEGINNING OF THE LIST, THIS PREVENTS A NEGATIVE OUT OF BOUNDS EXCEPTION
        if (indexInPeriod <= 3) {
            graphDate = App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(0), "MM/dd/yyyy") + " - " + App.dateFormatter(App.currentPeriod.get(indexInPeriod).get(6), "MM/dd/yyyy");
            if (indexInPeriod == 3) {
                prevButton.setVisibility(View.GONE);
            }
            //AS LONG AS THE GRAPH DATE ISN'T SHOWING TODAY THEN WE SHOW THE SKIP TO TODAY BUTTON
            if (indexInPeriod != 0) {
                showToday.setVisibility(View.VISIBLE);
            } else {
                //ELSE WE HIDE THE BUTTON
                showToday.setVisibility(View.GONE);
            }
            //SET THE DATE TEXT AND GENERATE THE GRAPH
            todayDate.setText(graphDate);
            generateColumnDataTopAsUnlocks();
        } else {
            //IF WE HAVE EXCEEDED THE LIMIT THEN HIDE THE PREV BUTTON
            prevButton.setVisibility(View.GONE);
        }


    }

    private void generateColumnDataTopAsUnlocks() {
        barChartTop.setZoomEnabled(false);
        barChartTop.setInteractive(true);
        barChartTop.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);


        int numSubcolumns = 1;
        //THIS TIME THE DAYS OF THE WEEK IS THE RANGE FOR THE BAR GRAPH MON-SUN
        //
        int numColumns = week.length; //WE COULD USE App.currentPeriod.size() HERE BUT days.length WILL BE THE SAME SIZE

        int maxValue = 0;

        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        List<Column> columns = new ArrayList<>(); //THE LIST OF COLUMNS
        List<SubcolumnValue> values; // THE LIST OF SUB-COLUMNS, EVERY COLUMN CAN HAVE A SUB-COLUMN
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();//CREATE NEW LIST OF VALUES
            for (int j = 0; j < numSubcolumns; j++) {
                //
                int val = Integer.parseInt(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(i), DatabaseHelper.UNLOCKS_COUNT, DetailedAppActivity.packageName));

                if (val == 0) {
                    values.add(new SubcolumnValue(val, Color.TRANSPARENT));
                    break;
                } else {
                    values.add(new SubcolumnValue(val,  Color.parseColor(unlockColColor)));
                }


                if (maxValue < val) {
                    maxValue = val;
                }
            }

            axisValues.add(new AxisValue(i).setLabel(week[i]));//SET THE X-AXIS VALUES WITH THE CORRESPONDING DAYS OF THE WEEK

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true)); //CREATE VALUES FOR THE COLUMNS
        }

        ColumnChartData barDataTop = new ColumnChartData(columns); //PASS THE COLUMNS TO THE BAR DATA

        //PASS THE LIST OF X-AXIS LABELS TO THE X-AXIS
        Axis axisX = new Axis(axisValues)
                .setName("Day of Week") //NAME OF X-AXIS
                .setHasTiltedLabels(true)  //MAKES THE LABELS TILTED SO WE CAN FIT MOORE LABELS ON THE X-AXIS
                .setTextColor( Color.parseColor(MainActivity.textColor))//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(4)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        Axis axisY = new Axis()
                .setName("Unlocks")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor( Color.parseColor(MainActivity.textColor))//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(3)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        barDataTop.setAxisXBottom(axisX);
        barDataTop.setAxisYLeft(axisY);

        barChartTop.setColumnChartData(barDataTop); //PASS THE BAR DATA TO THE COLUMNS

        // Set value touch listener that will trigger changes for chartTop.
        barChartTop.setOnValueTouchListener(new ValueTouchListener());

        // Set selection mode to keep selected month column highlighted.
        barChartTop.setValueSelectionEnabled(true);

        if (maxValue < 10) {
            Viewport v = new Viewport(barChartTop.getMaximumViewport());
            v.top = 10;
            barChartTop.setMaximumViewport(v);
            barChartTop.setCurrentViewport(v);
            barChartTop.setViewportCalculationEnabled(false);
        } else {
            Viewport v = new Viewport(barChartTop.getMaximumViewport());
            //NEXT MULTIPLE OF 10
            if (maxValue % 10 != 0) {
                maxValue = maxValue + (10 - maxValue % 10);
            }
            v.top = maxValue;
            barChartTop.setMaximumViewport(v);
            barChartTop.setCurrentViewport(v);
            barChartTop.setViewportCalculationEnabled(false);
        }
    }

    private void generateColumnDataBottomAsUnlocks(int columnIndex, int color) {
        // Cancel last animation if not finished.
        barChartBottom.cancelDataAnimation();

        int maxValue = 0;

        //CHANGE THE COLOR OF ALL THE COLUMNS
        for (Column bar : barDataBottom.getColumns()) {
            bar.getValues().get(0).setColor(color);
        }


        //FOR EVERY COLUMN IN THE GRAPH
        for (int i = 0; i < barDataBottom.getColumns().size(); i++) {
            //GET THE CURRENT COLUMN, THIS IS EFFECTIVELY REFERRING TO THE HOUR OF THE DAY
            Column column = barDataBottom.getColumns().get(i);
            for (int j = 0; j < column.getValues().size(); j++) {
                //SUB COLUMN HERE IS ALWAYS EQUAL TO 1 SINCE WE SET IT TO 1 EARLIER
                SubcolumnValue subcolumnValue = column.getValues().get(j);

                //columnIndex EFFECTIVELY REPRESENTS THE DAY OF THE WEEK SUN-SAT
                //THIS WILL ALSO CORRESPOND TO THE DAY OF THE WEEK INSIDE THE LIST OF DAYS OF THE CURRENT WEEK THAT WE GENERATED IN THE App CLASS
                //NOW WE CAN QUERY THE TABLE USING A SPECIFIC DAY AT A SPECIFIC HOUR USING THE VALUES OF i and columnIndex
                //AND WE DIVIDE BY 60000 TO GET THE NUMBER OF MINUTES
                int value = Integer.parseInt(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(columnIndex), String.valueOf(i), DatabaseHelper.UNLOCKS_COUNT, DetailedAppActivity.packageName));


                subcolumnValue.setTarget(value);

                if (value == 0) {
                    subcolumnValue.setColor(Color.TRANSPARENT);
                }

                if (maxValue < value) {
                    maxValue = value;
                }
            }


            //NOW WE CAN SET THIS TOO BE TRUE TO MAKE THE BARS DISPLAY A VALUE WHEN CLICKED
            column.setHasLabelsOnlyForSelected(true);
        }

        //THIS IS FOR STYLING,
        //IF THE LARGEST VALUE IS TEN THEN WE SET THE VIEWPORT HEIGHT OF THE GRAPH TO BE EQUAL TO 10: THIS KEEPS GRAPH Y-AXIS VALUES FROM TURNING INTO DECIMALS
        //OTHERWISE WE SET IS EQUAL TO THE MAXIMUM VALUE + 5: THIS ALLOWS A LITTLE EXTRA ROOM AT THE TOP OF THE GRAPH SO THE LARGEST VALUE INST HUGGING THE TOP MARGIN
        if (maxValue < 10) {
            Viewport v = new Viewport(barChartBottom.getMaximumViewport());
            v.top = 10;
            barChartBottom.setMaximumViewport(v);
            barChartBottom.setCurrentViewport(v);
            barChartBottom.setViewportCalculationEnabled(false);
        } else {
            Viewport v = new Viewport(barChartBottom.getMaximumViewport());
            v.top = ((maxValue + 4) / 5) * 5; //NEXT MULTIPLE OF 5;
            barChartBottom.setMaximumViewport(v);
            barChartBottom.setCurrentViewport(v);
            barChartBottom.setViewportCalculationEnabled(false);
        }


        barChartBottom.startDataAnimation(500);
    }

    private void generateInitialBarDataBottom() {
        barChartBottom.setZoomEnabled(false);
        barChartBottom.setInteractive(true);
        barChartBottom.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);

        int numSubcolumns = 1;
        //THIS TIME THE DAYS OF THE WEEK IS THE RANGE FOR THE BAR GRAPH MON-SUN
        int numColumns = clock.length;

        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        List<Column> columns = new ArrayList<>(); //THE LIST OF COLUMNS
        List<SubcolumnValue> values; // THE LIST OF SUB-COLUMNS, EVERY COLUMN CAN HAVE A SUB-COLUMN
        for (int i = 0; i < numColumns; i++) {
            values = new ArrayList<>();//CREATE NEW LIST OF VALUES
            for (int j = 0; j < numSubcolumns; j++) {
                //WE SET THE COLOR TO BE TRANSPARENT SO THE GRAPH APPEARS EMPTY. WE CAN'T SET VALUES TO ZERO BECAUSE THE ANIMATION EFFECT WONT WORK
                values.add(new SubcolumnValue((float) Math.random() * 50, Color.TRANSPARENT));
            }

            axisValues.add(new AxisValue(i).setLabel(clock[i]));//SET THE X-AXIS VALUES WITH THE CORRESPONDING DAYS OF THE WEEK
            columns.add(new Column(values)); //CREATE VALUES FOR THE COLUMNS
        }

        barDataBottom = new ColumnChartData(columns); //PASS THE COLUMNS TO THE BAR DATA

        //PASS THE LIST OF X-AXIS LABELS TO THE X-AXIS
        Axis axisX = new Axis(axisValues)
                .setName("Hour of Day") //NAME OF X-AXIS
                .setHasTiltedLabels(true)  //MAKES THE LABELS TILTED SO WE CAN FIT MOORE LABELS ON THE X-AXIS
                .setTextColor( Color.parseColor(MainActivity.textColor))//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(4)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        Axis axisY = new Axis()
                .setName("Unlocks")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor( Color.parseColor(MainActivity.textColor))//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(3)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        barDataBottom.setAxisXBottom(axisX);
        barDataBottom.setAxisYLeft(axisY);

        barChartBottom.setColumnChartData(barDataBottom); //PASS THE BAR DATA TO THE COLUMNS


        // Set selection mode to keep selected month column highlighted.
        barChartBottom.setValueSelectionEnabled(true);

        // For build-up animation you have to disable viewport recalculation.
        barChartBottom.setViewportCalculationEnabled(false);

    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {

            generateColumnDataBottomAsUnlocks(columnIndex, value.getColor());

        }


        @Override
        public void onValueDeselected() {

        }
    }

}
