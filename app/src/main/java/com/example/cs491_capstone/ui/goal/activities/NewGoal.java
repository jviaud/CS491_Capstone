package com.example.cs491_capstone.ui.goal.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.preference.PreferenceManager;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.GoalDataBaseHelper;
import com.example.cs491_capstone.InstalledAppInfo;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.goal.InputFilterMinMax;
import com.example.cs491_capstone.ui.goal.adapter.InstalledAppsListAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

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
    ChipGroup type, stats;
    CardView appInfo, usageCard, unlocksCard;
    EditText hour, minutes, unlock_amount;
    TextView dateTitle, appName, errorMessage, errorUsage, errorUnlocks;
    ImageView appIcon;
    Button done;
    String selectedApp;
    private List<InstalledAppInfo> trackedApps;
    private boolean[] formCompletion = {false, false, false};

    public static boolean areAllTrue(boolean[] array) {
        for (boolean b : array) if (!b) return false;
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = pref.getBoolean("dark_mode", true);
        if (darkMode) {
            getTheme().applyStyle(R.style.GoalTheme_Dark, true);
        } else {
            getTheme().applyStyle(R.style.GoalTheme_Light, true);
        }
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
        errorUsage = findViewById(R.id.error_usage);
        errorUnlocks = findViewById(R.id.error_unlocks);


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

                            formCompletion[2] = true;
                            if (areAllTrue(formCompletion)) {
                                done.setEnabled(true);
                            } else {
                                done.setEnabled(false);
                            }
                        } else {
                            formCompletion[2] = false;
                            done.setEnabled(false);
                            String error = "Either Hour or Minutes has to be greater than 0";
                            errorUsage.setText(error);
                            errorUsage.setVisibility(View.VISIBLE);
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
                            formCompletion[2] = true;
                            if (areAllTrue(formCompletion)) {
                                done.setEnabled(true);
                            } else {
                                done.setEnabled(false);
                            }
                        } else {
                            formCompletion[2] = false;
                            done.setEnabled(false);
                            String error = "Either Hour or Minutes has to be greater than 0";
                            errorUsage.setText(error);
                            errorUsage.setVisibility(View.VISIBLE);
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
                    if (Integer.parseInt(s.toString()) > 0) {
                        errorUnlocks.setVisibility(View.GONE);
                        formCompletion[2] = true;
                        if (areAllTrue(formCompletion)) {
                            done.setEnabled(true);
                        } else {
                            done.setEnabled(false);
                        }
                    } else {
                        formCompletion[2] = false;
                        errorUnlocks.setVisibility(View.VISIBLE);
                        String error = "Must be greater than 0";
                        errorUnlocks.setText(error);
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
                        formCompletion[1] = true;
                        goalType = GoalDataBaseHelper.GOAL_PHONE;
                        if (areAllTrue(formCompletion)) {
                            done.setEnabled(true);
                        } else {
                            done.setEnabled(false);
                        }
                        break;
                    case R.id.chip_byApp:
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
        String orgDate = year + "-" + (month + 1) + "-" + dayOfMonth;

        DateTime dt = DateTime.parse(orgDate, DateTimeFormat.forPattern("yyyy-M-d"));

        date = dt.toString("yyyy-MM-dd");


        dateTitle.setText(dt.toString("EEEE, MMMM dd, yyyy"));


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
        View view = inflater.inflate(R.layout.dialog_limit, null);


        InstalledAppsListAdapter listAdapter = new InstalledAppsListAdapter(this, trackedApps);
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
                selectedApp = info.getPackageName();
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
                errorUsage.setVisibility(View.GONE);
                if (unlocksCard.getVisibility() == View.GONE) {
                    formCompletion[2] = false;
                }
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
                errorUnlocks.setVisibility(View.GONE);
                if (usageCard.getVisibility() == View.GONE) {
                    formCompletion[2] = false;
                }
                break;
        }
    }

    public void insertData() {
        long usage = 0;
        int unlocks = 0;
        if (usageCard.getVisibility() != View.GONE) {
            if (hour.getText().length() > 0) {
                usage = (Integer.parseInt(hour.getText().toString()) * 3600000);
            }
            if (minutes.getText().length() > 0) {
                usage += (Integer.parseInt(minutes.getText().toString()) * 60000);
            }
        }
        if (unlocksCard.getVisibility() != View.GONE) {
            if (unlock_amount.getText().length() > 0) {
                unlocks = (Integer.parseInt(unlock_amount.getText().toString()));
            }

        }

        //DUPLICATE CHECKER
        if (goalType.equals(GoalDataBaseHelper.GOAL_APP)) {
            if (App.goalDataBase.canInsertApp(date, goalType, selectedApp)) {
                App.goalDataBase.insert(date, goalType, usage, unlocks, selectedApp);
                finish();
            } else {
                String error = "An App Goal has already been set for " + selectedApp + " on " + date + ". Please try editing the goal instead.";
                errorMessage.setText(error);
                errorMessage.setVisibility(View.VISIBLE);
            }

        } else {
            if (App.goalDataBase.canInsert(date, goalType)) {
                App.goalDataBase.insert(date, goalType, usage, unlocks);
                finish();

            } else {
                String error = "A Phone Goal has already been set for " + date + ". Please try editing the goal instead.";
                errorMessage.setText(error);
                errorMessage.setVisibility(View.VISIBLE);
            }

        }
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


}
