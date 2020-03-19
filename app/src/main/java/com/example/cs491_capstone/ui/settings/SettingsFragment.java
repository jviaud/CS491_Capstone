package com.example.cs491_capstone.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.R;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.example.cs491_capstone.App.localDatabase;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int RESULT_CODE_FILE = 10123;
    private static int dbSize;
    NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        dbSize = localDatabase.getRowCount();
        notificationManagerCompat = NotificationManagerCompat.from(getContext());

        Preference exportCSV = findPreference("export_csv");
        exportCSV.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                putCSV();

                return true;
            }
        });


        Preference importCSV = findPreference("import_csv");
        importCSV.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getCSV();

                return true;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_FILE && resultCode == RESULT_OK) {

            new ImportCSV().execute(data);
        }
    }

    private void getCSV() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            new MaterialFilePicker()
                    .withSupportFragment(this)
                    .withRequestCode(RESULT_CODE_FILE)
                    .withFilter(Pattern.compile(".*\\.csv$")) // Filtering files and directories by file name using regexp
                    //.withFilterDirectories(false) // Set directories filterable (false by default)
                    .withHiddenFiles(false) // Show hidden files and folders
                    .start();


        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permission Needed")
                        .setMessage("CSV must be loaded from storage")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);

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
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            }
        }

    }

    private void putCSV() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext(), App.CHANNEL_2_ID_ALERTS)
                    .setSmallIcon(R.drawable.bn_icon_home)
                    .setContentTitle("Download")
                    .setContentText("Download in Progress")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setProgress(dbSize, 0, false);


            notificationManagerCompat.notify(3, notification.build());

            new ExportCSV().execute(notification);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permission Needed")
                        .setMessage("CSV must be loaded to storage")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);

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
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            }
        }

    }


    private class ExportCSV extends AsyncTask<NotificationCompat.Builder, Integer, String> {


        @Override
        protected String doInBackground(NotificationCompat.Builder... notifications) {
            SystemClock.sleep(1000);

            NotificationCompat.Builder notification = notifications[0];
            File exportDir = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, "USAGE-" + App.DATE + ".csv");

            Log.i("VALUES", "" + file);
            try {
                file.createNewFile();
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

    private class ImportCSV extends AsyncTask<Intent, Integer, String> {

        @Override
        protected String doInBackground(Intent... intents) {
            Intent data = intents[0];
            String message = null;
            IllegalArgumentException exception = new IllegalArgumentException("Headers are not in correct order");
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Log.i("VALUES", "PATH:" + filePath);
            try {
                List<String> rowData;
                CSVReader reader = new CSVReader(new FileReader(new File(filePath)));
                String[] nextLine;
                int header = 0;
                while ((nextLine = reader.readNext()) != null) {
                    rowData = new ArrayList<>();
                    for (String s : nextLine) {
                        if (header == 0) {
                            if ((!nextLine[0].equals("ENTRY_ID"))
                                    && (!nextLine[1].equals("DATE"))
                                    && (!nextLine[2].equals("HOUR_OF_DAY"))
                                    && (!nextLine[3].equals("PACKAGE_NAME"))
                                    && (!nextLine[4].equals("UNLOCKS_COUNT"))
                                    && (!nextLine[5].equals("NOTIFICATIONS_COUNT"))
                                    && (!nextLine[6].equals("USAGE_TIME"))) {
                                throw exception;
                            }

                        } else {
                            rowData.add(s);
                        }
                    }
                    if (!rowData.isEmpty()) {
                        Log.i("VALUES", "" + rowData);
                        localDatabase.insert(rowData.get(0), rowData.get(1), rowData.get(2), rowData.get(3), rowData.get(4), rowData.get(5), rowData.get(6));
                    }

                    header++;
                }
                message = "Successfully inserted " + header + " lines";
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
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
        }
    }


}
