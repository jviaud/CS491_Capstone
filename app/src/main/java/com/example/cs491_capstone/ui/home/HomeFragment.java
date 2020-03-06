package com.example.cs491_capstone.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.UserUsageInfo;
import com.example.cs491_capstone.services.BackgroundMonitor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

import static com.example.cs491_capstone.MainActivity.usageInfo;


public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    /**
     * List of values on a clock from 12AM to 11PM used to label the X-Axis on the graphs
     */
    private final static String[] clock = new String[]{"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM",
            "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM",
            "8PM", "9PM", "10PM", "11PM"};
    /**
     * This is the maximum number of apps we want to show total usage time for, because we will allow the user to set the maximum number themselves we have a variable for it here
     */
    private static int maximumOnDisplay = 5;
    private ListView mostUsedListView;
    private MostUsedAppsListAdapter listAdapter;
    private ArrayList<UserUsageInfo> topItems = new ArrayList<>();
    //TIMER
    private Long timeLeft = 0L;
    private TextView hour_timer;
    private TextView minutes_timer;
    private TextView seconds_timer;
    private ColumnChartView barChart;

    private String selectedGraph = "";

    private TextView totalUsage, percentDelta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mostUsedListView = view.findViewById(R.id.most_used_list);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //HEADER
        totalUsage = view.findViewById(R.id.total_usage);
        percentDelta = view.findViewById(R.id.percent_delta);
        setPercentDelta();

        String time = App.timeFormatter(Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)));
        totalUsage.setText(time);


        ///VIEWS
        TextView date = view.findViewById(R.id.date_actual);
        DateTimeZone timeZone = DateTimeZone.forID("America/Montreal");
        DateTime now = DateTime.now(timeZone);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy");
        date.setText(dtf.print(now));

        //INITIALIZE CHARTS
        barChart = view.findViewById(R.id.home_charts);


        ///TIMER
        hour_timer = view.findViewById(R.id.timer_hours);
        minutes_timer = view.findViewById(R.id.timer_minutes);
        seconds_timer = view.findViewById(R.id.timer_seconds);


        //SORT THE LIST OF APP USAGE INFO, THIS IS SORTED BY TOTAL TIME USED FOR THE DAY

        /*
        EVEN THOUGH WE WANT TO DISPLAY UP TO X AMOUNT OF APPS ON THE HOME PAGE, THERE IS NO REASON TO DISPLAY USAGE FOR APPS WITH 0 USAGE TIME
        SO WE LOOK AT THE TOP X AMOUNT OF APPS AND IF THEY DON'T HAVE A TIME GREATER THAN 0 WE DON'T INCLUDE IT IN THE DISPLAY
         */

        for (int i = 0; i <= maximumOnDisplay; i++) {
            if (usageInfo.get(i).getDailyUsage() > 0) {
                topItems.add(usageInfo.get(i));
            } else
                break;
        }
        //NOW WE FORM A SUBLIST OF THE TOP X APPS AND PASS IT TO THE ADAPTER
        listAdapter = new MostUsedAppsListAdapter(getContext(), topItems);
        mostUsedListView.setAdapter(listAdapter);
        setListViewHeightBasedOnChildren(mostUsedListView);


        //GRAPHS
        //graphContainer = view.findViewById(R.id.graph_container);
        //GRAPHS
        Spinner graphChoiceSpinner = view.findViewById(R.id.graph_choice);
        ArrayAdapter<CharSequence> spinner_options_adapter = ArrayAdapter.createFromResource(getContext(), R.array.graphs, R.layout.spinner_item_alt);
        spinner_options_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        graphChoiceSpinner.setAdapter(spinner_options_adapter);
        graphChoiceSpinner.setOnItemSelectedListener(this);


        updateTimer();

        mostUsedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserUsageInfo item = (UserUsageInfo) parent.getItemAtPosition(position);
                Toast.makeText(getContext(), "CLICKED:  " + item.getPackageName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getContext(), DetailedAppActivity.class);
                intent.putExtra("APP", item);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //WE MUST REFRESH THE ADAPTER CONTAINING THE LIST VIEW
        //AND RECREATE THE LIST
        topItems.clear();
        for (int i = 0; i <= maximumOnDisplay; i++) {
            if (usageInfo.get(i).getDailyUsage() > 0) {
                topItems.add(usageInfo.get(i));
            } else
                break;
        }
        listAdapter.notifyDataSetChanged();

        switch (selectedGraph) {
            case "USAGE":
                createUsageChart();
                break;
            case "NOTIFICATIONS":
                createNotificationChart();
                break;
            case "UNLOCKS":
                createUnlocksChart();
                break;
            default:
                break;
        }

        String time = App.timeFormatter(Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)));
        totalUsage.setText(time);

        setPercentDelta();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void setPercentDelta() {
        int todayIndex = App.currentPeriod.indexOf(App.DATE);

        try {
            String yesterdayDate = App.currentPeriod.get(0).get(todayIndex - 1);

            double todayUsage = Long.parseLong(App.localDatabase.getSumTotalStat(App.DATE, DatabaseHelper.USAGE_TIME)) / 60000;
            double yesterdayUsage = Long.parseLong(App.localDatabase.getSumTotalStat(yesterdayDate, DatabaseHelper.USAGE_TIME)) / 60000;

            String text;

            if (todayUsage == 0 || yesterdayUsage == 0) {
                text = "+" + 0 + "%";
                percentDelta.setText(text);
            } else {

                double delta = ((todayUsage - yesterdayUsage) / yesterdayUsage) * 100;
                Log.i("DELTA", "" + todayUsage + ":" + yesterdayUsage + ":" + ":" + delta);

                if (delta > 0) {
                    text = String.format(Locale.ENGLISH, "%s%.2f%s", "+", delta, "%");
                    // "+" + delta + "%";
                    percentDelta.setText(text);

                } else {
                    text = String.format(Locale.ENGLISH, "%.2f%s", delta, "%");
                    percentDelta.setText(text);
                }

            }

        } catch (IndexOutOfBoundsException OoBE) {
            String text = "+" + 0 + "%";
            percentDelta.setText(text);
        }

    }


    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup)
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);

    }


    private void updateTimer() {
        //Toast.makeText(getContext(), "NO MORE TIME", Toast.LENGTH_SHORT).show();
        ///TIMER
        CountDownTimer timer = new CountDownTimer(App.START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                timeLeft = BackgroundMonitor.timeLeft;

                int hours = (int) (timeLeft / (1000 * 60 * 60) % 24);
                int minutes = (int) (timeLeft / (1000 * 60) % 60);
                int seconds = (int) (timeLeft / 1000) % 60;

                if (hours < 10) {
                    hour_timer.setText(String.format(Locale.ENGLISH, "0%d", hours));
                } else {
                    hour_timer.setText(String.format(Locale.ENGLISH, "%d", hours));
                }
                if (minutes < 10) {
                    minutes_timer.setText(String.format(Locale.ENGLISH, "0%d", minutes));
                } else {
                    minutes_timer.setText(String.format(Locale.ENGLISH, "%d", minutes));
                }
                if (seconds < 10) {
                    seconds_timer.setText(String.format(Locale.ENGLISH, "0%d", seconds));
                } else {
                    seconds_timer.setText(String.format(Locale.ENGLISH, "%d", seconds));
                }

            }

            @Override
            public void onFinish() {
            }
        }.start();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedGraph = parent.getItemAtPosition(position).toString();


        switch (selectedGraph) {
            case "USAGE":
                //TODO ADD METHOD TO CREATE AND POPULATE GRAPHS WITH DATA, HERE
                //graphContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                createUsageChart();
                break;
            case "NOTIFICATIONS":
                //TODO
                //graphContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                createNotificationChart();
                break;
            case "UNLOCKS":
                //TODO
                //graphContainer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                createUnlocksChart();
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
        int numColumns = Integer.parseInt(App.HOUR);

        //FOR EVERY COLUMN
        for (int i = 0; i <= numColumns; ++i) {
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
                    values.add(new SubcolumnValue(value, Color.DKGRAY));
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
            Log.i("MAXVAL","<10");
            Viewport v = new Viewport(barChart.getMaximumViewport());
            v.top = 11;
            barChart.setMaximumViewport(v);
            barChart.setCurrentViewport(v);
            barChart.setViewportCalculationEnabled(false);
        } else {
            if (maxValue >= 60) {
                axisY.setName("Time Used (hours)");//NAME OF Y-AXIS
                for (Column column : columns) {
                    for (SubcolumnValue subcolumnValue : column.getValues()) {
                        subcolumnValue.setValue(subcolumnValue.getValue() / 60);
                    }
                }

            }

            Viewport v = new Viewport(barChart.getMaximumViewport());
            v.top = maxValue + 5;
            barChart.setMaximumViewport(v);
            barChart.setCurrentViewport(v);
            barChart.setViewportCalculationEnabled(false);
        }

        //ANIMATE GRAPH / NOT ACTUALLY NECESSARY
        barChart.animate();

    }

    private void createNotificationChart() {

        //STYLING FOR GRAPHS
        barChart.setZoomEnabled(false);
        barChart.setInteractive(true);
        barChart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
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
        int numColumns = Integer.parseInt(App.HOUR);

        //FOR EVERY COLUMN
        for (int i = 0; i <= numColumns; ++i) {
            //WE CREATE A LIST OF VALUES, THIS WILL COME IN HANDY WHEN WE SPLIT BY CATEGORY AND HAVE A STACKED BAR GRAPH BUT FOR NOW IT WILL ONLY HOLD ONE VALUE
            values = new ArrayList<>();
            for (int j = 0; j < numSubColumns; ++j) {

                //WE DO NOT NEED TO CATCH ANY EXCEPTIONS HERE BECAUSE THE getSumTotalStat() METHOD WILL RETURN 0 IF NULL AND ALL DATA IS CLEAN WHEN INSERTED INTO THE GRAPH
                String val = App.localDatabase.getSumTotalStat(App.DATE, i + "", DatabaseHelper.NOTIFICATIONS_COUNT);
                Log.i("GRAPH", "Hour:" + i + " ::VALUE:" + val);
                //WE DIVIDE THE TOTAL TIME IN MILLI BY 60000 TO GET THE NUMBER OF MINUTES
                values.add(new SubcolumnValue(Long.parseLong(val), ChartUtils.COLOR_RED));
            }
            //THIS IS WHERE WE LABEL THE X-AXIS
            //WE SET THE CURRENT COLUMNS X-AXIS LABEL EQUAL TO THE CORRESPONDING TIME ON THE CLOCK
            xAxisValues.add(new AxisValue(i).setLabel(clock[i]));

            //WE CREATE A COLUMN WE THE VALUES WE JUST RECEIVED FROM THE TABLE/DATABASE
            Column column = new Column(values);

            //THIS IS JUST STYLING
            column.setHasLabels(true);
            column.setHasLabelsOnlyForSelected(true);

            //ADD THE CURRENT COLUMN TO THE LIST OF COLUMNS
            columns.add(column);
        }

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
                .setName("Notifications Received")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
                ;

        //SET AXIS TO GRAPH
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        //SET COLUMNS TO GRAPH
        barChart.setColumnChartData(data);

        //ANIMATE GRAPH / NOT ACTUALLY NECESSARY
        barChart.animate();
    }

    private void createUnlocksChart() {

        //STYLING FOR GRAPHS
        barChart.setZoomEnabled(false);
        barChart.setInteractive(true);
        barChart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
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
        int numColumns = Integer.parseInt(App.HOUR);

        //FOR EVERY COLUMN
        for (int i = 0; i <= numColumns; ++i) {
            //WE CREATE A LIST OF VALUES, THIS WILL COME IN HANDY WHEN WE SPLIT BY CATEGORY AND HAVE A STACKED BAR GRAPH BUT FOR NOW IT WILL ONLY HOLD ONE VALUE
            values = new ArrayList<>();
            for (int j = 0; j < numSubColumns; ++j) {

                //WE DO NOT NEED TO CATCH ANY EXCEPTIONS HERE BECAUSE THE getSumTotalStat() METHOD WILL RETURN 0 IF NULL AND ALL DATA IS CLEAN WHEN INSERTED INTO THE GRAPH
                String val = App.localDatabase.getSumTotalStat(App.DATE, i + "", DatabaseHelper.UNLOCKS_COUNT);
                Log.i("GRAPH", "Hour:" + i + " ::VALUE:" + val);
                //WE DIVIDE THE TOTAL TIME IN MILLI BY 60000 TO GET THE NUMBER OF MINUTES
                values.add(new SubcolumnValue(Long.parseLong(val), ChartUtils.COLOR_RED));
            }
            //THIS IS WHERE WE LABEL THE X-AXIS
            //WE SET THE CURRENT COLUMNS X-AXIS LABEL EQUAL TO THE CORRESPONDING TIME ON THE CLOCK
            xAxisValues.add(new AxisValue(i).setLabel(clock[i]));

            //WE CREATE A COLUMN WE THE VALUES WE JUST RECEIVED FROM THE TABLE/DATABASE
            Column column = new Column(values);

            //THIS IS JUST STYLING
            column.setHasLabels(true);
            column.setHasLabelsOnlyForSelected(true);

            //ADD THE CURRENT COLUMN TO THE LIST OF COLUMNS
            columns.add(column);
        }

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
                .setName("Unlocks")//NAME OF Y-AXIS
                .setHasLines(true)//HORIZONTAL LINES
                .setTextColor(R.color.black)//MAKES TEXT COLOR BLACK
                ;

        //SET AXIS TO GRAPH
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        //SET COLUMNS TO GRAPH
        barChart.setColumnChartData(data);

        //ANIMATE GRAPH / NOT ACTUALLY NECESSARY
        barChart.animate();
    }

    static class MostUsedAppsViewHolder {
        TextView name;
        ImageView icon;
        TextView time;
    }

    private static class MostUsedAppsListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<UserUsageInfo> installedAppInfoList;

        //CONSTRUCTOR
        MostUsedAppsListAdapter(Context context, List<UserUsageInfo> installedAppInfoList) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.installedAppInfoList = installedAppInfoList;
        }


        @Override
        public int getCount() {
            return installedAppInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return installedAppInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MostUsedAppsViewHolder listHolder;

            if (convertView == null) {
                //CREATE VIEWHOLDER AND INFLATE LAYOUT
                listHolder = new MostUsedAppsViewHolder();
                convertView = inflater.inflate(R.layout.home_most_used_list_layout, parent, false);

                //ASSIGN VIEW HOLDER CLASS VARIABLE TO LAYOUT
                listHolder.icon = convertView.findViewById(R.id.icon);
                listHolder.name = convertView.findViewById(R.id.name);
                listHolder.time = convertView.findViewById(R.id.time);

                convertView.setTag(listHolder);

            } else {
                listHolder = (MostUsedAppsViewHolder) convertView.getTag();
            }

            listHolder.icon.setImageDrawable(installedAppInfoList.get(position).getIcon());
            listHolder.name.setText(installedAppInfoList.get(position).getSimpleName());
            ///

            //String packageName = installedAppInfoList.get(position).getPackageName();

            String time = installedAppInfoList.get(position).formatTime(installedAppInfoList.get(position).getDailyUsage());
            //long time = installedAppInfoList.get(position).getDailyUsage();
            Log.i("TRACK", "" + time);
            listHolder.time.setText(String.valueOf(time));


            return convertView;
        }


    }


}
