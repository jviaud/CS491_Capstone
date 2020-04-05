package com.example.cs491_capstone.ui.goal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class AppGoalFragment extends Fragment {
    RecyclerView appsRecycler;
    ImageAdapter adapter;
    List<Goal> goals;
    float usageMaxValue, unlockMaxValue;
    private ComboLineColumnChartView usageBarChart;
    private ComboLineColumnChartData usageChartData;
    private PieChartView usagePie;
    private PieChartView unlockPie;
    private ComboLineColumnChartView unlockBarChart;
    private ComboLineColumnChartData unlockChartData;
    private String packageName;
    private CardView usageCard, unlockCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goals_graph_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goals = App.goalDataBase.getGoal(App.DATE, GoalDataBaseHelper.GOAL_APP);
        appsRecycler = view.findViewById(R.id.appsWithGoals);
        usageBarChart = view.findViewById(R.id.graph);
        unlockBarChart = view.findViewById(R.id.unlock_graph);
        usagePie = view.findViewById(R.id.usage_pie);
        unlockPie = view.findViewById(R.id.unlock_pie);
        usageCard = view.findViewById(R.id.usage_card);
        unlockCard = view.findViewById(R.id.unlock_card);

        unlockPie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockCard.setVisibility(View.VISIBLE);
                usageCard.setVisibility(View.GONE);
            }
        });

        usagePie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockCard.setVisibility(View.GONE);
                usageCard.setVisibility(View.VISIBLE);

            }
        });

        if (goals.isEmpty()) {
            appsRecycler.setVisibility(View.GONE);
        } else {
            appsRecycler.setVisibility(View.VISIBLE);
        }


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        appsRecycler.setLayoutManager(layoutManager);
        appsRecycler.setHasFixedSize(true);

        adapter = new ImageAdapter(getContext(), goals);
        appsRecycler.setAdapter(adapter);


        packageName = goals.get(0).getPackageName();

        //PIE CHARTS
        generateUsagePie();
        generateUnlockPie();

        usageChartData = new ComboLineColumnChartData(generateColumnDataAsUsage(), generateLineDataAsUsage());

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);

        axisX.setName("Day of Week");
        axisY.setName("Usage vs. Goal Value (minutes)");
        axisY.setTextColor(Color.BLACK);
        axisX.setTextColor(Color.BLACK);


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
        unlockAxisY.setTextColor(Color.BLACK);

        unlockChartData.setAxisXBottom(axisX);
        unlockChartData.setAxisYLeft(unlockAxisY);


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


        adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ImageView v) {
                packageName = goals.get(position).getPackageName();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        generateUnlockPie();
                        generateUsagePie();


                    }
                });

                changeUsageData();

            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        goals.clear();
        goals.addAll(App.goalDataBase.getGoal(App.DATE, GoalDataBaseHelper.GOAL_APP));
        adapter.notifyDataSetChanged();

    }

    private LineChartData generateLineDataAsUsage() {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < week.length; i++) {

            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_USAGE)) / 60000; //Math.random() * 50 + 5;
                PointValue pointValue = null;

                Log.i("VALUES", "LINE VALUE:" + value);


                if (value == 0) {
                    // pointValue = new PointValue(i, -1);
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

    private LineChartData generateLineDataAsUnlock() {

        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < week.length; i++) {

            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_UNLOCKS)); //Math.random() * 50 + 5;
                PointValue pointValue = null;

                Log.i("VALUES", "LINE VALUE:" + value);

                if (value == 0) {
//                    pointValue = new PointValue(i, -1);
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

    private ColumnChartData generateColumnDataAsUsage() {
        int numColumns = week.length;
        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        usageMaxValue = 0;
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(currentPeriod.get(0).get(i), DatabaseHelper.USAGE_TIME, packageName)) / 60000; //Math.random() * 50 + 5;

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
                if (usageMaxValue < value) {
                    usageMaxValue = value;
                }
            }

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }


        return new ColumnChartData(columns);

    }

    private ColumnChartData generateColumnDataAsUnlock() {
        int numColumns = week.length;
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();
            for (int j = 0; j < 1; j++) {
                long value = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(currentPeriod.get(0).get(i), DatabaseHelper.UNLOCKS_COUNT, packageName));


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

    private void changeUsageData() {
        usageBarChart.cancelDataAnimation();


        for (int i = 0; i < usageChartData.getLineChartData().getLines().size(); i++) {
            Line line = usageChartData.getLineChartData().getLines().get(i);
            for (PointValue value : line.getValues()) {

                long target = Long.parseLong(App.goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_USAGE)) / 60000;
                value.setTarget(value.getX(), target);

                int hours = (int) (target / (60) % 24);
                int minutes = (int) (target % 60);


                if (hours == 0) {
                    value.setLabel(String.format(Locale.ENGLISH, "%d%s", minutes, "m"));
                } else {
                    value.setLabel(String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m"));
                }

            }
        }


        for (int i = 0; i < usageChartData.getColumnChartData().getColumns().size(); i++) {
            Column column = usageChartData.getColumnChartData().getColumns().get(i);
            for (SubcolumnValue value : column.getValues()) {

                long target = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(currentPeriod.get(0).get(i), DatabaseHelper.USAGE_TIME, packageName)) / 60000;
                value.setTarget(target);

                int hours = (int) (target / (60) % 24);
                int minutes = (int) (target % 60);


                if (hours == 0) {
                    value.setLabel(String.format(Locale.ENGLISH, "%d%s", minutes, "m"));
                } else {
                    value.setLabel(String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m"));
                }

                if (target == 0) {
                    value.setColor(Color.WHITE);
                } else {
                    value.setColor(Color.DKGRAY);
                }

            }
        }


        usageBarChart.startDataAnimation();

    }

    private void changeUnlockData() {
        unlockBarChart.cancelDataAnimation();
        // usageMaxValue

        for (int i = 0; i < usageChartData.getLineChartData().getLines().size(); i++) {
            Line line = usageChartData.getLineChartData().getLines().get(i);
            for (PointValue value : line.getValues()) {
                long target = Long.parseLong(App.goalDataBase.get(currentPeriod.get(0).get(i), GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_UNLOCKS));
                value.setTarget(value.getX(), target);
            }
        }


        for (int i = 0; i < unlockChartData.getColumnChartData().getColumns().size(); i++) {
            Column column = unlockChartData.getColumnChartData().getColumns().get(i);
            for (SubcolumnValue value : column.getValues()) {

                long target = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(currentPeriod.get(0).get(i), DatabaseHelper.UNLOCKS_COUNT, packageName));
                value.setTarget(target);


                if (target == 0) {
                    value.setColor(Color.WHITE);
                } else {
                    value.setColor(Color.DKGRAY);
                }
            }
        }


        unlockBarChart.startDataAnimation();

    }

    private void generateUnlockPie() {
        List<SliceValue> values = new ArrayList<>();


        long goalValue = Long.parseLong(App.goalDataBase.get(App.DATE, GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_UNLOCKS));
        long actualValue = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.UNLOCKS_COUNT, packageName));

        float full = 100;
        float remaining = 0;
        String formatRemaining;

        if (actualValue > goalValue & goalValue > 0) {
            formatRemaining = "100%";

        } else if (goalValue == 0) {
            formatRemaining = "~%";
        } else {
            Log.i("GOAL", "goal:" + goalValue + "| actual:" + actualValue + "|%:" + ((float) actualValue / goalValue * 100));
            remaining = ((float) actualValue / goalValue * 100);
            full = 100 - remaining;
            formatRemaining = String.format(Locale.ENGLISH, "%.1f%%", remaining);
        }


        SliceValue goal = new SliceValue(remaining, Color.CYAN);
        SliceValue actual = new SliceValue(full, Color.LTGRAY);

        values.add(goal);
        values.add(actual);


        PieChartData unlockPieData = new PieChartData(values);
        unlockPieData.setHasLabels(false);
        unlockPieData.setHasCenterCircle(true);

        unlockPieData.setCenterText1(formatRemaining);
        unlockPieData.setCenterText1Typeface(Typeface.DEFAULT_BOLD);
        unlockPieData.setCenterText1Color(Color.CYAN);
        unlockPieData.setCenterText1FontSize(25);


        unlockPie.setChartRotationEnabled(false);
        unlockPie.setPieChartData(unlockPieData);

    }

    private void generateUsagePie() {
        List<SliceValue> values = new ArrayList<>();

        long goalValue = Long.parseLong(App.goalDataBase.get(App.DATE, GoalDataBaseHelper.GOAL_APP, packageName, GoalDataBaseHelper.GOAL_USAGE)) / 60000;
        long actualValue = Long.parseLong(App.localDatabase.getSumTotalStatByPackage(App.DATE, DatabaseHelper.USAGE_TIME, packageName)) / 60000;

        float full = 100;
        float remaining = 0;
        String formatRemaining;

        if (actualValue > goalValue & goalValue > 0) {
            formatRemaining = "100%";

        } else if (goalValue == 0) {
            formatRemaining = "~%";
        } else {
            remaining = ((float) actualValue / goalValue * 100);
            full = 100 - remaining;
            formatRemaining = String.format(Locale.ENGLISH, "%.1f%%", remaining);
        }


        SliceValue goal = new SliceValue(remaining, Color.RED);
        SliceValue actual = new SliceValue(full, Color.LTGRAY);

        values.add(goal);
        values.add(actual);


        PieChartData usagePieData = new PieChartData(values);
        usagePieData.setHasLabels(false);
        usagePieData.setHasCenterCircle(true);

        usagePieData.setCenterText1(formatRemaining);
        usagePieData.setCenterText1Typeface(Typeface.DEFAULT_BOLD);
        usagePieData.setCenterText1Color(Color.RED);
        usagePieData.setCenterText1FontSize(25);

        usagePie.setChartRotationEnabled(false);
        usagePie.setPieChartData(usagePieData);
    }

    public static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private ImageAdapter.OnItemClickListener mListener;//CUSTOM ON CLICK LISTENER
        private Context context;
        private List<Goal> subscriptions;
        private PackageManager packageManager;


        ImageAdapter(Context context, List<Goal> subscriptions) {
            this.context = context;
            this.subscriptions = subscriptions;
            packageManager = context.getPackageManager();


        }

        @NonNull
        @Override
        public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.app_recycler_icons, parent, false);

            return new ImageViewHolder(v, mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {
            Goal goal = subscriptions.get(position);


            try {
                ApplicationInfo ai = packageManager.getApplicationInfo(goal.getPackageName(), 0);
                Drawable icon = ai.loadIcon(packageManager);
                holder.icon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


        }

        @Override
        public int getItemCount() {
            return subscriptions.size();
        }

        void setOnItemClickListener(ImageAdapter.OnItemClickListener listener) {
            mListener = listener;
        }

        //CUSTOM ONCLICK LISTENER INTERFACE
        public interface OnItemClickListener {
            void onItemClick(int position, ImageView v);
        }
        ////END CUSTOM ONCLICK LISTENER INTERFACE

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;

            ImageViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);

                //INITIALIZE ON CLICK LISTENER IN VIEW HOLDER
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                listener.onItemClick(position, icon);

                            }
                        }
                    }
                });
            }
        }
    }

}


