package com.example.cs491_capstone.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.cs491_capstone.R;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.example.cs491_capstone.App.localDatabase;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int RESULT_CODE_FILE = 10123;
    Preference importCSV;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        importCSV = findPreference("import_csv");

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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("VALUES", "CODE:" + requestCode);

        if (requestCode == RESULT_CODE_FILE && resultCode == RESULT_OK) {

            IllegalArgumentException exception = new IllegalArgumentException("Headers are not in correct order");
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Log.i("VALUES", "PATH2:" + filePath);


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

                Toast.makeText(getContext(), "Successfully inserted " + header + " lines", Toast.LENGTH_SHORT).show();

            } catch (IOException io) {
                Toast.makeText(getContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                Log.i("ERROR", "ERROR:" + io.getMessage());
            } catch (SQLException sql) {
                Toast.makeText(getContext(), "Invalid or Existing Data", Toast.LENGTH_SHORT).show();
                Log.i("ERROR", "ERROR:" + sql.getMessage());
            } catch (IllegalArgumentException ia) {
                Toast.makeText(getContext(), "Invalid Column Headers", Toast.LENGTH_SHORT).show();
                Log.i("ERROR", "ERROR:" + ia.getMessage());
            }

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

}
