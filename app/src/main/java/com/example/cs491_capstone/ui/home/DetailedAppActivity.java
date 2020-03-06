package com.example.cs491_capstone.ui.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.UserUsageInfo;

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

public class DetailedAppActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final static String[] days = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    /**
     * List of values on a clock from 12AM to 11PM used to label the X-Axis on the graphs
     */
    private final static String[] clock = new String[]{"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM",
            "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM",
            "8PM", "9PM", "10PM", "11PM"};

    private ColumnChartView barChartTop;
    private ColumnChartView barChartBottom;

    private ColumnChartData barDataTop;
    private ColumnChartData barDataBottom;


    private ImageView appIcon;
    private TextView appName, appCategory, usage_val, notification_val, unlocks_val;

    private UserUsageInfo appInfo;

    private String packageName;

    private String selectedGraph = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_app);


        //INITIALISE ALL OF THE VIEWS
        appIcon = findViewById(R.id.app_icon);
        appName = findViewById(R.id.app_name);
        appCategory = findViewById(R.id.app_category);

        usage_val = findViewById(R.id.usage_val);
        notification_val = findViewById(R.id.notification_value);
        unlocks_val = findViewById(R.id.unlock_val);

        //INITIALISE THE APP OBJECT
        Intent intent = getIntent();
        appInfo = intent.getParcelableExtra("APP");

        //SET APP INFO TEXT VIEWS
        appName.setText(appInfo.getSimpleName());


        packageName = appInfo.getPackageName();
        //SET ICON & CATEGORY
        try {
            Drawable mIcon = getPackageManager().getApplicationIcon(packageName);

            appCategory.setText(App.getAppCategoryName(packageName, this));

            appIcon.setImageDrawable(mIcon);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        barChartTop = findViewById(R.id.barchart_top);
        //generateColumnDataTopAsUsage();

        barChartBottom = findViewById(R.id.barchart_bottom);
        generateInitialBarDataBottom();


        setBanner();


        Spinner graphChoiceSpinner = findViewById(R.id.graph_choice);
        ArrayAdapter<CharSequence> spinner_options_adapter = ArrayAdapter.createFromResource(this, R.array.graphs, android.R.layout.simple_spinner_item);
        spinner_options_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphChoiceSpinner.setAdapter(spinner_options_adapter);
        graphChoiceSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        switch (selectedGraph) {
            case "USAGE":
                generateColumnDataTopAsUsage();
                break;
            case "NOTIFICATIONS":
                generateColumnDataTopAsNotification();
                break;
            case "UNLOCKS":
                generateColumnDataTopAsUnlocks();
                break;
            default:
                break;
        }
    }

    private void setBanner() {

        long usageTime = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.USAGE_TIME, packageName)) / 60000;
        String usage = "";
        if (usageTime < 60) {
            usage = usageTime + " min(s)";
        } else {
            usageTime = usageTime / 60;
            usage = usageTime + " hr(s)";
        }
        usage_val.setText(usage);

        notification_val.setText(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.NOTIFICATIONS_COUNT, packageName));
        unlocks_val.setText(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.UNLOCKS_COUNT, packageName));
    }

    private void generateColumnDataTopAsUsage() {

        barChartTop.setZoomEnabled(false);
        barChartTop.setInteractive(true);
        barChartTop.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);


        int numSubcolumns = 1;
        //THIS TIME THE DAYS OF THE WEEK IS THE RANGE FOR THE BAR GRAPH MON-SUN
        //
        int numColumns = days.length; //WE COULD USE App.currentPeriod.size() HERE BUT days.length WILL BE THE SAME SIZE

        float maxValue = 0;

        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        List<Column> columns = new ArrayList<>(); //THE LIST OF COLUMNS
        List<SubcolumnValue> values; // THE LIST OF SUB-COLUMNS, EVERY COLUMN CAN HAVE A SUB-COLUMN
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();//CREATE NEW LIST OF VALUES
            for (int j = 0; j < numSubcolumns; j++) {
                //
                Log.i("STATS", "" + App.currentPeriod.get(0) + "  " + App.currentPeriod.get(0).size());
                long val = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(i), DatabaseHelper.USAGE_TIME, packageName)) / 60000;

                if (val == 0) {
                    values.add(new SubcolumnValue(val, Color.TRANSPARENT));
                    break;
                } else {
                    values.add(new SubcolumnValue(val, Color.DKGRAY));
                }


                if (maxValue < val) {
                    maxValue = val;
                }
            }

            axisValues.add(new AxisValue(i).setLabel(days[i]));//SET THE X-AXIS VALUES WITH THE CORRESPONDING DAYS OF THE WEEK

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true)); //CREATE VALUES FOR THE COLUMNS
        }

        barDataTop = new ColumnChartData(columns); //PASS THE COLUMNS TO THE BAR DATA

        //PASS THE LIST OF X-AXIS LABELS TO THE X-AXIS
        Axis axisX = new Axis(axisValues)
                .setName("Day of Week") //NAME OF X-AXIS
                .setHasTiltedLabels(true)  //MAKES THE LABELS TILTED SO WE CAN FIT MOORE LABELS ON THE X-AXIS
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(4)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        Axis axisY = new Axis()
                .setName("Time Used (minutes)")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
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
            if (maxValue >= 60) {
                axisY.setName("Time Used (hours)");//NAME OF Y-AXIS
                for (Column column : columns) {
                    for (SubcolumnValue subcolumnValue : column.getValues()) {
                        subcolumnValue.setValue(subcolumnValue.getValue() / 60);
                    }
                }

            }
            Viewport v = new Viewport(barChartTop.getMaximumViewport());
            v.top = maxValue + 5;
            barChartTop.setMaximumViewport(v);
            barChartTop.setCurrentViewport(v);
            barChartTop.setViewportCalculationEnabled(false);
        }


        // barChartTop.animate();
    }

    private void generateColumnDataTopAsUnlocks() {
        barChartTop.setZoomEnabled(false);
        barChartTop.setInteractive(true);
        barChartTop.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);


        int numSubcolumns = 1;
        //THIS TIME THE DAYS OF THE WEEK IS THE RANGE FOR THE BAR GRAPH MON-SUN
        //
        int numColumns = days.length; //WE COULD USE App.currentPeriod.size() HERE BUT days.length WILL BE THE SAME SIZE

        int maxValue = 0;

        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        List<Column> columns = new ArrayList<>(); //THE LIST OF COLUMNS
        List<SubcolumnValue> values; // THE LIST OF SUB-COLUMNS, EVERY COLUMN CAN HAVE A SUB-COLUMN
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();//CREATE NEW LIST OF VALUES
            for (int j = 0; j < numSubcolumns; j++) {
                //
                int val = Integer.parseInt(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(i), DatabaseHelper.UNLOCKS_COUNT, packageName));

                if (val == 0) {
                    values.add(new SubcolumnValue(val, Color.TRANSPARENT));
                    break;
                } else {
                    values.add(new SubcolumnValue(val, Color.DKGRAY));
                }


                if (maxValue < val) {
                    maxValue = val;
                }
            }

            axisValues.add(new AxisValue(i).setLabel(days[i]));//SET THE X-AXIS VALUES WITH THE CORRESPONDING DAYS OF THE WEEK

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true)); //CREATE VALUES FOR THE COLUMNS
        }

        barDataTop = new ColumnChartData(columns); //PASS THE COLUMNS TO THE BAR DATA

        //CREATE AND STYLE THE TWO AXIS
        barDataTop.setAxisXBottom(new Axis(axisValues).setName("Day of Week"));
        barDataTop.setAxisYLeft(new Axis().setHasLines(true).setName("Unlocks")
                .setMaxLabelChars(3));

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
            v.top = maxValue + 5;
            barChartTop.setMaximumViewport(v);
            barChartTop.setCurrentViewport(v);
            barChartTop.setViewportCalculationEnabled(false);
        }
    }

    private void generateColumnDataTopAsNotification() {
        barChartTop.setZoomEnabled(false);
        barChartTop.setInteractive(true);
        barChartTop.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);


        int numSubcolumns = 1;
        //THIS TIME THE DAYS OF THE WEEK IS THE RANGE FOR THE BAR GRAPH MON-SUN
        //
        int numColumns = days.length; //WE COULD USE App.currentPeriod.size() HERE BUT days.length WILL BE THE SAME SIZE

        int maxValue = 0;

        List<AxisValue> axisValues = new ArrayList<>(); //THE LIST OF X-AXIS VALUES
        List<Column> columns = new ArrayList<>(); //THE LIST OF COLUMNS
        List<SubcolumnValue> values; // THE LIST OF SUB-COLUMNS, EVERY COLUMN CAN HAVE A SUB-COLUMN
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();//CREATE NEW LIST OF VALUES
            for (int j = 0; j < numSubcolumns; j++) {
                //
                int val = Integer.parseInt(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(i), DatabaseHelper.NOTIFICATIONS_COUNT, packageName));

                if (val == 0) {
                    values.add(new SubcolumnValue(val, Color.TRANSPARENT));
                    break;
                } else {
                    values.add(new SubcolumnValue(val, Color.DKGRAY));
                }


                if (maxValue < val) {
                    maxValue = val;
                }
            }

            axisValues.add(new AxisValue(i).setLabel(days[i]));//SET THE X-AXIS VALUES WITH THE CORRESPONDING DAYS OF THE WEEK

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true)); //CREATE VALUES FOR THE COLUMNS
        }

        barDataTop = new ColumnChartData(columns); //PASS THE COLUMNS TO THE BAR DATA

        //CREATE AND STYLE THE TWO AXIS
        barDataTop.setAxisXBottom(new Axis(axisValues).setName("Day of Week"));
        barDataTop.setAxisYLeft(new Axis().setHasLines(true).setName("Notifications Received")
                .setMaxLabelChars(3));

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
            v.top = maxValue + 5;
            barChartTop.setMaximumViewport(v);
            barChartTop.setCurrentViewport(v);
            barChartTop.setViewportCalculationEnabled(false);
        }
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
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(4)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        Axis axisY = new Axis()
                .setName("Time Used (minutes)")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
                .setMaxLabelChars(3)//MAXIMUM NUMBER OF CHARACTER PER LABEL, THIS IS JUST FOR STYLING AND SPACING
                ;


        barDataBottom.setAxisXBottom(axisX);
        barDataBottom.setAxisYLeft(axisY);

        barChartBottom.setColumnChartData(barDataBottom); //PASS THE BAR DATA TO THE COLUMNS


        // For build-up animation you have to disable viewport recalculation.
        barChartBottom.setViewportCalculationEnabled(false);

    }

    private void generateColumnDataBottomAsUsage(int columnIndex, int color) {
        // Cancel last animation if not finished.
        barChartBottom.cancelDataAnimation();

        float maxValue = 0;

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
                long value = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(columnIndex), String.valueOf(i), DatabaseHelper.USAGE_TIME, packageName)) / 60000;


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
            if (maxValue >= 60) {
                barDataBottom.getAxisYLeft().setName("Time Used (hours)");//NAME OF Y-AXIS
                for (Column column : barDataBottom.getColumns()) {
                    for (SubcolumnValue subcolumnValue : column.getValues()) {
                        subcolumnValue.setValue(subcolumnValue.getValue() / 60);
                    }
                }

            }
            Viewport v = new Viewport(barChartBottom.getMaximumViewport());
            v.top = maxValue + 5;
            barChartBottom.setMaximumViewport(v);
            barChartBottom.setCurrentViewport(v);
            barChartBottom.setViewportCalculationEnabled(false);
        }


        barChartBottom.startDataAnimation(500);
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
                int value = Integer.parseInt(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(columnIndex), String.valueOf(i), DatabaseHelper.UNLOCKS_COUNT, packageName));


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
            v.top = maxValue + 5;
            barChartBottom.setMaximumViewport(v);
            barChartBottom.setCurrentViewport(v);
            barChartBottom.setViewportCalculationEnabled(false);
        }


        barChartBottom.startDataAnimation(500);
    }

    private void generateColumnDataBottomAsNotifications(int columnIndex, int color) {
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
                int value = Integer.parseInt(App.localDatabase.getSumTotalStatByPackage(App.currentPeriod.get(0).get(columnIndex), String.valueOf(i), DatabaseHelper.NOTIFICATIONS_COUNT, packageName));


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
            v.top = maxValue + 5;
            barChartBottom.setMaximumViewport(v);
            barChartBottom.setCurrentViewport(v);
            barChartBottom.setViewportCalculationEnabled(false);
        }


        barChartBottom.startDataAnimation(500);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedGraph = parent.getItemAtPosition(position).toString();


        switch (selectedGraph) {
            case "USAGE":
                //TODO ADD METHOD TO CREATE AND POPULATE GRAPHS WITH DATA, HERE
                //graphContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                generateColumnDataTopAsUsage();
                break;
            case "NOTIFICATIONS":
                //TODO
                //graphContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                generateColumnDataTopAsNotification();
                break;
            case "UNLOCKS":
                //TODO
                //graphContainer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                generateColumnDataTopAsUnlocks();
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            Toast.makeText(DetailedAppActivity.this, "HGI", Toast.LENGTH_SHORT).show();

            Log.i("SELECTED", "graph:" + selectedGraph + "   " + columnIndex);
            switch (selectedGraph) {
                case "USAGE":
                    Toast.makeText(DetailedAppActivity.this, "USAGE", Toast.LENGTH_SHORT).show();
                    generateColumnDataBottomAsUsage(columnIndex, value.getColor());
                    break;
                case "NOTIFICATIONS":
                    Toast.makeText(DetailedAppActivity.this, "NOTIF", Toast.LENGTH_SHORT).show();
                    generateColumnDataBottomAsNotifications(columnIndex, value.getColor());
                    break;
                case "UNLOCKS":
                    Toast.makeText(DetailedAppActivity.this, "UNLOC", Toast.LENGTH_SHORT).show();
                    generateColumnDataBottomAsUnlocks(columnIndex, value.getColor());
                    break;

            }


        }

        @Override
        public void onValueDeselected() {

        }
    }


}
