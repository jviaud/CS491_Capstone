package com.example.cs491_capstone.ui.settings;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.InstalledAppInfo;
import com.example.cs491_capstone.R;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.example.cs491_capstone.App.ALL_APPS_LIST;
import static com.example.cs491_capstone.App.INCLUDED_APPS_LIST;
import static com.example.cs491_capstone.App.localDatabase;
import static com.example.cs491_capstone.services.BackgroundMonitor.timeLeft;

public class SettingsFragment extends PreferenceFragmentCompat {
    /**
     * An int used to mark the result code for the file picker
     */
    private static final int RESULT_CODE_FILE = 10123;
    /**
     * the result code for READ_EXTERNAL permission
     */
    private static final int REQUEST_CODE_READ = 123;
    /**
     * the result code for WRITE_EXTERNAL permission
     */
    private static final int REQUEST_CODE_WRITE = 321;
    /**
     *
     */
    public static boolean wifiDisabled = false;
    /**
     *
     */
    public static boolean dataDisabled = false;
    /**
     *
     */
    public static boolean parentalControls = false;
    public static boolean trackedAppsChanged = false;
    /**
     * Represents the size of the database, used when downloading/uploading the database to show progress
     */
    private static int dbSize;
    private InstalledAppsListAdapter listAdapter;
    private List<InstalledAppInfo> COPY_OF_LIST;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        //Initialise the size of the database
        dbSize = localDatabase.getRowCount();

        if (App.HOUR.equals("0")) {
            //TODO CLEAR ALL APP AND PHONE LIMITS AT START OPF DAY
        }


        //WE CREATE A COPY OF THE LIST IN CASE THE USER DECIDES TO CANCEL MIDWAY WE WANT TO BE ABLE TO REVERT THE CHANGES
        COPY_OF_LIST = new ArrayList<>();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (InstalledAppInfo info : ALL_APPS_LIST) {
                    COPY_OF_LIST.add(new InstalledAppInfo(info.getPackageName(), info.getSimpleName(), info.getIcon()).setTracked(info.isTracked()));
                }
            }
        });
//


        /*
        It would be better too implement the Onclick listener so it isn't so cluttered here but I can't get the click events to trigger the listeners that way
        So I unfortunately have to put all the click listeners here
         */

        Preference appList = findPreference(("exclusion_list"));
        appList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_apps_list_layout, null);

                listAdapter = new InstalledAppsListAdapter(getContext(), COPY_OF_LIST);
                final ListView listView = view.findViewById(R.id.dialog_list);
                listView.setAdapter(listAdapter);

                builder.setView(view)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //IF USER CLICKS OKAY THEN WE SAVE THE CHANGES TO THE LIST
                                ALL_APPS_LIST.clear();
                                INCLUDED_APPS_LIST.clear();
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (InstalledAppInfo info : COPY_OF_LIST) {
                                            ALL_APPS_LIST.add(new InstalledAppInfo(info.getPackageName(), info.getSimpleName(), info.getIcon()).setTracked(info.isTracked()));
                                            INCLUDED_APPS_LIST.add(info.getPackageName());
                                        }
                                        trackedAppsChanged = true;
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //IF THEY CANCEL WE SET THE COPY BACK TO THE ORIGINAL
                                COPY_OF_LIST.clear();
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (InstalledAppInfo info : ALL_APPS_LIST) {
                                            COPY_OF_LIST.add(new InstalledAppInfo(info.getPackageName(), info.getSimpleName(), info.getIcon()).setTracked(info.isTracked()));
                                        }
                                    }
                                });
                                dialog.dismiss();
                            }
                        }).create().show();


                return true;
            }
        });


        //CLICK LISTENER FOR PARENTAL CONTROLS  SWITCH
        SwitchPreference parentalSwitch = findPreference("parental_controls");
        parentalSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                parentalControls = (boolean) newValue;
                return true;
            }
        });

        //CLICK LISTENER FOR PHONE LIMIT SWITCH
        SwitchPreference phoneLimitSwitch = findPreference("parental_controls");
        phoneLimitSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!(boolean) newValue) {
                    timeLeft = -1;
                }
                return true;
            }
        });

        //CLICK LISTENER FOR PHONE LIMIT
        Preference phoneLimit = findPreference("phone_limit");
        phoneLimit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_limit_layout, null);
                final NumberPicker hour = view.findViewById(R.id.hour);
                final NumberPicker minutes = view.findViewById(R.id.minutes);


                hour.setMinValue(0);
                hour.setMaxValue(23);
                hour.setWrapSelectorWheel(false);
                //
                minutes.setMinValue(0);
                minutes.setMaxValue(59);
                minutes.setWrapSelectorWheel(false);


                builder.setView(view)
                        // Add action buttons
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // timeLeft = picker.getValue() * 60000;
                                timeLeft = (hour.getValue() * 3600000) + (minutes.getValue() * 60000);


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).create().show();
                return true;
            }
        });

        //CLICK LISTENER FOR WIFI SWITCH
        SwitchPreference wifiSwitch = findPreference("toggle_wifi");
        wifiSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                wifiDisabled = (boolean) newValue;
                return true;
            }
        });

        //CLICK LISTENER FOR DATA SWITCH
        SwitchPreference dataSwitch = findPreference("toggle_data");
        dataSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                dataDisabled = (boolean) newValue;
                return true;
            }
        });

        //CLICK LISTENER FOR EXPORTING TOO CSV OPTION
        Preference exportCSV = findPreference("export_csv");
        exportCSV.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                putCSV();
                return true;
            }
        });
        //CLICK LISTENER FOR IMPORTING FROM CSV OPTION
        Preference importCSV = findPreference("import_csv");
        importCSV.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getCSV();
                return true;
            }
        });
        ///

    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new MaterialFilePicker()
                        .withSupportFragment(this)
                        .withRequestCode(RESULT_CODE_FILE)
                        .withFilter(Pattern.compile(".*\\.csv$")) // Filtering files and directories by file name using regexp
                        //.withFilterDirectories(false) // Set directories filterable (false by default)
                        .withHiddenFiles(false) // Show hidden files and folders
                        .start();
            }
        }

        if (requestCode == REQUEST_CODE_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //MATCH RESULT CODE WITH CORRESPONDING RESULT ACTIVITY(FILE PICKER)
        if (requestCode == RESULT_CODE_FILE && resultCode == RESULT_OK) {
            //START ASYNC ACTIVITY TO IMPORT CSV
            //ASYNC TASK IS PASSED ACTIVITY BECAUSE WE NEED TO ACCESS THE CONTEXT
            //BUT CONTEXT CAN NOT BE STATIC OR MEMORY LEAK WILL OCCUR
            ImportCSV aSyncTask = new ImportCSV(getActivity());
            //WE ALSO PASS IT DATA BECAUSE THIS IS THE FILE WE HAVE CHOSEN FROM FILE PICKER
            aSyncTask.execute(data);
        }
    }

    private void getCSV() {

        //SELF CHECK READ_EXTERNAL_STORAGE PERMISSION
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            //STORE REFERENCE TO THIS FRAGMENT
            final Fragment fragment = this;
            //DISPLAY DIALOG WARNING USER ABOUT FORMATTING
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("CSV FORMAT")
                    .setMessage("The .csv file must contain the following headers in the same order\n" +
                            "ENTRY_ID\nDATE\nHOUR_OF_DAY\nPACKAGE_NAME\nUNLOCKS_COUNT\nNOTIFICATIONS_COUNT\nUSAGE_TIME\nCATEGORY")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //IF PERMISSION IS GRANTED THEN START THE MATERIAL FILE PICKER ACTIVITY
                            new MaterialFilePicker()
                                    .withSupportFragment(fragment)
                                    .withRequestCode(RESULT_CODE_FILE)
                                    .withFilter(Pattern.compile(".*\\.csv$")) // Filtering files and directories by file name using regexp
                                    //.withFilterDirectories(false) // Set directories filterable (false by default)
                                    .withHiddenFiles(false) // Show hidden files and folders
                                    .start();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();


        } else {
            //IF THE USER CLICKED CANCEL THE FIRST TIME THE PERMISSION WAS REQUESTED
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permission Needed")
                        .setMessage("CSV must be loaded from storage")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

            } else {
                //START THE PERMISSION REQUEST DIALOG
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ);
            }
        }

    }

    private void putCSV() {
        //SELF CHECK WRITE PERMISSION
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //IF PERMISSION IS GRANTED THEN CREATE A NOTIFICATION TO SHOW PROGRESS OF DOWNLOAD
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext(), App.CHANNEL_2_ID_ALERTS)
                    .setSmallIcon(R.drawable.ic_file_download)
                    .setContentTitle("Download")
                    .setContentText("Download in Progress")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setProgress(dbSize, 0, false);

            //BUILD NOTIFICATION
            notificationManagerCompat.notify(3, notification.build());

            //START ASYNC TASK TO EXPORT CSV
            ExportCSV aSyncTask = new ExportCSV(getActivity());
            aSyncTask.execute(notification);

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //IF USER DECLINED THE PERMISSION THE FIRST TIME BUT TRIES TO ACCESS THIS FEATURE AGAIN, THEN WE SHOW THEM WHY WE NEED THIS PERMISSION
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permission Needed")
                        .setMessage("CSV must be loaded to storage")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

            } else {
                //SHOW THE NORMAL PERMISSION DIALOG
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE);
            }
        }

    }


    private static class ImportCSV extends AsyncTask<Intent, Integer, String> {
        /**
         * Weak reference to activity because we need context but don't want to cause memory leak
         */
        private final WeakReference<Activity> weakActivity;

        /**
         * The constructor for the Async task
         *
         * @param myActivity the reference to the activity
         */
        ImportCSV(Activity myActivity) {
            this.weakActivity = new WeakReference<>(myActivity);
        }


        @Override
        protected String doInBackground(Intent... intents) {
            //THIS IS THE DATA WE SENT WHEN STARTING THE TASK, WE RECEIVED IT FROM THE ACTIVITY RESULT
            Intent data = intents[0];
            //STORED THE RESULT OF THE UPLOAD
            String message = null;
            //THIS SEEMS LIKE THE MOST APPROPRIATE ARGUMENT TO THROW ASIDE FROM A RUNTIME OR IO EXCEPTION
            IllegalArgumentException exception = new IllegalArgumentException("Headers are not in correct order");
            //FILE PICKER IS PART OF THE FILE CHOOSER LIBRARY WE USED TOO GET THE FILE, WE USE ITS GLOBAL VARIABLE TOO GET THE FILE PATH AND STORE IT
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            //WE GET THE ACTIVITY
            Activity activity = weakActivity.get();
            //CREATE THE NOTIFICATION
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(activity.getApplicationContext());
            NotificationCompat.Builder notification = new NotificationCompat.Builder(activity.getApplicationContext(), App.CHANNEL_2_ID_ALERTS)
                    .setSmallIcon(R.drawable.ic_file_download)
                    .setContentTitle("Upload")
                    .setContentText("Reading File")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setProgress(dbSize, 0, false);

            //BUILD NOTIFICATION
            notificationManagerCompat.notify(3, notification.build());

            try {
                //ALL OF THE DATA IN ONE ROW
                List<String> rowData;
                //THIS IS PART OF THE OpenCSV LIBRARY, WE PASS THE READER THE PATH TO THE FILE
                CSVReader reader = new CSVReader(new FileReader(new File(filePath)));
                //REPRESENTS 1 ROW, EACH INDEX BEING A COLUMN
                String[] nextLine;
                //WE NEED TO GET THE HEADERS SEPARATELY
                int header = 0;
                List<List<String>> csvData = new ArrayList<>();

                while ((nextLine = reader.readNext()) != null) {
                    //CREATE NEW LIST FOR ROWS
                    rowData = new ArrayList<>();

                    for (String s : nextLine) {
                        //IF WE ARE READING THE FIRST LINE
                        //THIS MEANS THAT THE IMPORTED CSV MUST HAVE HEADERS AND THEY MUST BE IN THE FOLLOWING ORDER
                        if (header == 0) {
                            if ((!nextLine[0].equals("ENTRY_ID"))
                                    && (!nextLine[1].equals("DATE"))
                                    && (!nextLine[2].equals("HOUR_OF_DAY"))
                                    && (!nextLine[3].equals("PACKAGE_NAME"))
                                    && (!nextLine[4].equals("UNLOCKS_COUNT"))
                                    && (!nextLine[5].equals("NOTIFICATIONS_COUNT"))
                                    && (!nextLine[6].equals("USAGE_TIME"))
                                    && (!nextLine[6].equals("CATEGORY"))) {
                                //WE PURPOSELY THROW AN EXCEPTION IF THE HEADERS ARE NOT IN THIS ORDER
                                throw exception;
                            }

                        } else {
                            //ROW HEADERS DON'T NEED TO BE ADDED TO THE DATABASE SO WE ONLY ADD ROWS AFTER ROW 0
                            rowData.add(s);
                            csvData.add(rowData);

                        }
                    }
                    //UPDATE PROGRESS BASED ON POSITION IN THE LOOP
                    notification.setProgress(dbSize, header++, false);
                    notificationManagerCompat.notify(3, notification.build());
                }//END WHILE LOOP

                //NOW THAT WE ARE OUTSIDE THE LOOP WE CAN CHANGE THE NOTIFICATION TO SHW WE ARE DONE READING AND ARE NOW UPLOADING
                notification
                        .setContentText("Uploading to Database")
                        .setProgress(dbSize, 0, false);
                notificationManagerCompat.notify(3, notification.build());

                //WE DO THE SAME THING THAT WE DID WITH THE WHILE LOOP, WITH THE FOR LOOP, THIS TIME WE ARE ACTUALLY READING DATA IN
                int index = 0;
                for (List<String> row : csvData) {
                    localDatabase.insert(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5), row.get(6));

                    notification.setProgress(dbSize, index++, false);
                    notificationManagerCompat.notify(3, notification.build());

                }
                //HANDLE SUCCESS MESSAGES, IF IT MAKES IT OUT OF BOTH LOOPS WITHOUT THROWING AN ERROR THEN IT IS A SUCCESS
                message = "Successfully inserted " + header + " lines";

                //HANDLE ERROR MESSAGES
                //TODO ADD FUNCTION THAT UNDOES CHANGES TO DATABASE AFTER AN ERROR IS CAUGHT
            } catch (IOException io) {
                message = "Upload Failed";
                Log.i("ERROR", "ERROR:" + io.getMessage());
            } catch (SQLException sql) {
                message = "Invalid or Existing Data";
                Log.i("ERROR", "ERROR:" + sql.getMessage());
            } catch (IllegalArgumentException ia) {
                message = "Invalid Column Headers";
                Log.i("ERROR", "ERROR:" + ia.getMessage());
            }
            //RETURN MESSAGE TO BE PROCESSED LATER
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Activity activity = weakActivity.get();
            //Toast.makeText(activity.getApplicationContext(), s, Toast.LENGTH_LONG).show();

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(activity.getApplicationContext());

            NotificationCompat.Builder notification = new NotificationCompat.Builder(activity.getApplicationContext(), App.CHANNEL_2_ID_ALERTS)
                    .setSmallIcon(R.drawable.ic_file_download)
                    .setContentTitle("Upload")
                    .setContentText("Error Uploading:\n" + s)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setProgress(0, 0, false);

            //BUILD NOTIFICATION
            notificationManagerCompat.notify(3, notification.build());

        }
    }

    private static class ExportCSV extends AsyncTask<NotificationCompat.Builder, Integer, String> {

        /**
         * store weak reference too activity to prevent memory leak
         */
        private final WeakReference<Activity> weakActivity;

        /**
         * constructor for the Async task
         *
         * @param myActivity reference to activity that calls this task
         */
        ExportCSV(Activity myActivity) {
            this.weakActivity = new WeakReference<>(myActivity);
        }

        @Override
        protected String doInBackground(NotificationCompat.Builder... notifications) {
            //SystemClock.sleep(1000);
            Activity activity = weakActivity.get();
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(activity.getApplicationContext());
            NotificationCompat.Builder notification = notifications[0];


            File exportDir = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, "USAGE-" + App.DATE + ".csv");

            Log.i("VALUES", "" + file);
            try {
                //file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor curCSV = localDatabase.getAllData();
                csvWrite.writeNext(curCSV.getColumnNames());
                Log.i("VALUES", "ColumnNames" + Arrays.toString(curCSV.getColumnNames()));
                int line = 0;
                while (curCSV.moveToNext()) {


                    String[] arrStr = {curCSV.getString(0),
                            curCSV.getString(1),
                            curCSV.getString(2),
                            curCSV.getString(3),
                            curCSV.getString(4),
                            curCSV.getString(5),
                            curCSV.getString(6),
                            String.format(Locale.ENGLISH, "%.0f", curCSV.getFloat(6))};
                    csvWrite.writeNext(arrStr);
                    line++;

                    notification.setProgress(dbSize, line, false)
                            .setOngoing(false);
                    notificationManagerCompat.notify(3, notification.build());

                    Log.i("VALUES", "" + Arrays.toString(arrStr));
                }

                notification.setContentText("Download Finished")
                        .setProgress(0, 0, false);
                notificationManagerCompat.notify(3, notification.build());


                csvWrite.close();
                curCSV.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file.getPath();
        }
    }


    static class InstalledAppsViewHolder {
        CheckBox box;
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
                convertView = inflater.inflate(R.layout.dialog_list_layout, parent, false);

                //ASSIGN VIEW HOLDER CLASS VARIABLE TO LAYOUT
                listHolder.icon = convertView.findViewById(R.id.icon);
                listHolder.box = convertView.findViewById(R.id.checkBox);

                convertView.setTag(listHolder);

            } else {
                listHolder = (InstalledAppsViewHolder) convertView.getTag();
            }

            listHolder.icon.setImageDrawable(installedAppInfoList.get(position).getIcon());
            listHolder.box.setText(installedAppInfoList.get(position).getSimpleName());


            //CHECK BOXES IN LIST VIEW WILL NOT RETAIN THEIR POSITION AND THUS WILL BECOME UNCHECKED OR CHECKED INCORRECTLY WHEN SCROLLED
            //SO WE MUST DO THIS TO SAVE THE STATE OF THE CHECKBOX
            final boolean state = installedAppInfoList.get(position).isTracked();
            listHolder.box.setChecked(state);
            listHolder.box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ESSENTIALLY IT IS EQUAL TOO THE OPPOSITE OF ITSELF WHEN CLICKED, IF TRUE THEN FALSE, IF FALSE THEN TRUE
                    installedAppInfoList.get(position).setTracked(!state);
                }
            });
            /*Set tag to all checkBox**/
            listHolder.box.setTag(position);
            return convertView;
        }

    }
}
