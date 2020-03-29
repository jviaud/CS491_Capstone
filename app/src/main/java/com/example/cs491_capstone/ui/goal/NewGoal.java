package com.example.cs491_capstone.ui.goal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.GoalDataBaseHelper;
import com.example.cs491_capstone.InstalledAppInfo;
import com.example.cs491_capstone.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.example.cs491_capstone.App.ALL_APPS_LIST;

public class NewGoal extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private static String date;
    private static String goalType;
    Chip specifyDate, specifyApp, usage, unlocks;
    ChipGroup type, categoryList, stats;
    CardView appInfo, usageCard, unlocksCard;
    EditText hour, minutes, unlock_amount;
    TextView dateTitle, appName, errorMessage;
    ImageView appIcon;
    Button done;
    private InstalledAppsListAdapter listAdapter;
    private List<InstalledAppInfo> trackedApps;
    private boolean[] formCompletion = {false, false, false};

    public static boolean areAllTrue(boolean[] array) {
        for (boolean b : array) if (!b) return false;
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_goal);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        trackedApps = new ArrayList<>();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < ALL_APPS_LIST.size(); i++) {
                    String packageName = ALL_APPS_LIST.get(i).getPackageName();

                    if (ALL_APPS_LIST.get(i).isTracked()) {
                        String appName = ALL_APPS_LIST.get(i).getSimpleName();
                        Drawable icon = ALL_APPS_LIST.get(i).getIcon();
                        trackedApps.add(new InstalledAppInfo(packageName, appName, icon));
                    }
                }
                Collections.sort(trackedApps);
            }
        });

        specifyDate = findViewById(R.id.chip_date);
        dateTitle = findViewById(R.id.date);
        specifyApp = findViewById(R.id.chip_app);
        type = findViewById(R.id.chip_group_filter);
        categoryList = findViewById(R.id.chip_group_filter2);
        stats = findViewById(R.id.chip_group_stat);
        appInfo = findViewById(R.id.app_card);
        usage = findViewById(R.id.goal_usage);
        unlocks = findViewById(R.id.goal_unlocks);
        appName = findViewById(R.id.app_name);
        appIcon = findViewById(R.id.app_icon);
        usageCard = findViewById(R.id.usage_stat_container);
        unlocksCard = findViewById(R.id.unlocks_stat_container);
        hour = findViewById(R.id.hour);
        minutes = findViewById(R.id.minutes);
        unlock_amount = findViewById(R.id.unlock_amount);
        done = findViewById(R.id.done_button);
        errorMessage = findViewById(R.id.error_message);


        //MIN AND MAX VALUES FOR EDIT TEXT
        hour.setFilters(new InputFilter[]{new InputFilterMinMax(0, 23)});
        minutes.setFilters(new InputFilter[]{new InputFilterMinMax(0, 59)});

        //REGISTER CLICK LISTENERS
        specifyDate.setOnClickListener(this);
        specifyApp.setOnClickListener(this);
        usage.setOnClickListener(this);
        unlocks.setOnClickListener(this);
        done.setOnClickListener(this);

        hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {
                    if (Integer.parseInt(s.toString()) < 0) {
                        if (Integer.parseInt(minutes.getText().toString()) > 0) {
                            Log.i("GOALS", "" + Arrays.toString(formCompletion));
                            formCompletion[2] = true;
                            if (areAllTrue(formCompletion)) {
                                done.setEnabled(true);
                            } else {
                                done.setEnabled(false);
                            }
                        } else {
                            formCompletion[2] = false;
                        }
                    } else {
                        formCompletion[2] = true;
                        if (areAllTrue(formCompletion)) {
                            done.setEnabled(true);
                        } else {
                            done.setEnabled(false);
                        }
                    }
                } else {
                    formCompletion[2] = false;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        minutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    if (Integer.parseInt(s.toString()) < 0) {
                        if (Integer.parseInt(hour.getText().toString()) > 0) {
                            Log.i("GOALS", "" + Arrays.toString(formCompletion));
                            formCompletion[2] = true;
                            if (areAllTrue(formCompletion)) {
                                done.setEnabled(true);
                            } else {
                                done.setEnabled(false);
                            }
                        } else {
                            formCompletion[2] = false;
                        }
                    } else {
                        formCompletion[2] = true;
                        if (areAllTrue(formCompletion)) {
                            done.setEnabled(true);
                        } else {
                            done.setEnabled(false);
                        }
                    }
                } else {
                    formCompletion[2] = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        unlock_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    if (Integer.parseInt(s.toString()) < 0) {
                        Log.i("GOALS", "" + Arrays.toString(formCompletion));
                        formCompletion[2] = true;
                        if (areAllTrue(formCompletion)) {
                            done.setEnabled(true);
                        } else {
                            done.setEnabled(false);
                        }
                    } else {
                        formCompletion[2] = true;
                        if (areAllTrue(formCompletion)) {
                            done.setEnabled(true);
                        } else {
                            done.setEnabled(false);
                        }
                    }
                } else {
                    formCompletion[2] = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


        //REGISTER CHANGE LISTENER
        type.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.chip_byPhone:
                        specifyApp.setVisibility(View.GONE);
                        appInfo.setVisibility(View.GONE);
                        categoryList.setVisibility(View.GONE);
                        formCompletion[1] = true;
                        goalType = GoalDataBaseHelper.GOAL_PHONE;
                        if (areAllTrue(formCompletion)) {
                            done.setEnabled(true);
                        } else {
                            done.setEnabled(false);
                        }
                        break;
                    case R.id.chip_byCategory:
                        specifyApp.setVisibility(View.GONE);
                        appInfo.setVisibility(View.GONE);
                        categoryList.setVisibility(View.VISIBLE);
                        goalType = GoalDataBaseHelper.GOAL_CATEGORY;
                        break;
                    case R.id.chip_byApp:
                        categoryList.setVisibility(View.GONE);
                        specifyApp.setVisibility(View.VISIBLE);
                        appInfo.setVisibility(View.VISIBLE);
                        goalType = GoalDataBaseHelper.GOAL_APP;
                        break;
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String day;
        if (dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        } else {
            day = String.valueOf(dayOfMonth);
        }
        date = year + "/" + (month + 1) + "/" + day;
        dateTitle.setText(date);


        formCompletion[0] = true;
        Log.i("GOALS", "" + Arrays.toString(formCompletion));
        if (areAllTrue(formCompletion)) {
            done.setEnabled(true);
        } else {
            done.setEnabled(false);
        }
    }

    public void setSpecifyDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);


        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    public void setSpecifyApp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_apps_list_layout, null);


        listAdapter = new InstalledAppsListAdapter(this, trackedApps);
        final ListView listView = view.findViewById(R.id.dialog_list);
        listView.setAdapter(listAdapter);

        final Dialog dialog = builder.setView(view).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }).create();

        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InstalledAppInfo info = trackedApps.get(position);
                appName.setText(info.getSimpleName());
                appIcon.setImageDrawable(info.getIcon());
                dialog.dismiss();
                formCompletion[1] = true;
                Log.i("GOALS", "" + Arrays.toString(formCompletion));
                if (areAllTrue(formCompletion)) {
                    done.setEnabled(true);
                } else {
                    done.setEnabled(false);
                }
            }
        });
    }

    public void showUsage() {

        switch (usageCard.getVisibility()) {
            case View.GONE:
                usageCard.setVisibility(View.VISIBLE);
                break;
            case View.VISIBLE:
                usageCard.setVisibility(View.GONE);
                if (unlocksCard.getVisibility() == View.GONE)
                    formCompletion[2] = false;
                Log.i("GOALS", "" + Arrays.toString(formCompletion));
                break;
        }

    }


    public void showUnlocks() {
        switch (unlocksCard.getVisibility()) {
            case View.GONE:
                unlocksCard.setVisibility(View.VISIBLE);
                break;
            case View.VISIBLE:
                unlocksCard.setVisibility(View.GONE);
                if (usageCard.getVisibility() == View.GONE)
                    formCompletion[2] = false;
                Log.i("GOALS", "" + Arrays.toString(formCompletion));
                break;
        }
    }

    public void insertData() {
        long usage;
        int unlocks;
        if (usageCard.getVisibility() != View.GONE) {
            usage = (Integer.parseInt(hour.getText().toString()) * 360000) + (Integer.parseInt(minutes.getText().toString()) * 60000);
        } else {
            usage = 0;
        }
        if (unlocksCard.getVisibility() != View.GONE) {
            unlocks = (Integer.parseInt(unlock_amount.getText().toString()));
        } else {
            unlocks = 0;
        }

        //TODO ADD DUPLICATE CHECKER
        App.goalDataBase.insert(date, goalType, usage, unlocks);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.chip_date:
                setSpecifyDate();
                break;
            case R.id.chip_app:
                setSpecifyApp();
                break;
            case R.id.goal_usage:
                showUsage();
                break;
            case R.id.goal_unlocks:
                showUnlocks();
                break;
            case R.id.done_button:
                insertData();
                break;
        }
    }

    static class InstalledAppsViewHolder {
        TextView name;
        ImageView icon;
    }

    private static class InstalledAppsListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<InstalledAppInfo> installedAppInfoList;

        //CONSTRUCTOR
        InstalledAppsListAdapter(Context context, List<InstalledAppInfo> installedAppInfoList) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            InstalledAppsViewHolder listHolder;

            if (convertView == null) {
                //CREATE VIEWHOLDER AND INFLATE LAYOUT
                listHolder = new InstalledAppsViewHolder();
                convertView = inflater.inflate(R.layout.new_goal_app_card, parent, false);

                //ASSIGN VIEW HOLDER CLASS VARIABLE TO LAYOUT
                listHolder.icon = convertView.findViewById(R.id.icon);
                listHolder.name = convertView.findViewById(R.id.name);

                convertView.setTag(listHolder);

            } else {
                listHolder = (InstalledAppsViewHolder) convertView.getTag();
            }

            listHolder.icon.setImageDrawable(installedAppInfoList.get(position).getIcon());
            listHolder.name.setText(installedAppInfoList.get(position).getSimpleName());

            return convertView;
        }

    }

    public static class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
}
