package com.example.cs491_capstone.services;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;

public class SyncFireDBWorker extends Worker {
    public SyncFireDBWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Cursor cursor = App.localDatabase.getAllData();
        final int dateIndex = cursor.getColumnIndex(DatabaseHelper.DATE);
        final int hourIndex = cursor.getColumnIndex(DatabaseHelper.HOUR_OF_DAY);
        final int packageNameIndex = cursor.getColumnIndex(DatabaseHelper.PACKAGE_NAME);
        final int unlocksIndex = cursor.getColumnIndex(DatabaseHelper.UNLOCKS_COUNT);
        final int notificationsIndex = cursor.getColumnIndex(DatabaseHelper.NOTIFICATIONS_COUNT);
        final int usageIndex = cursor.getColumnIndex(DatabaseHelper.USAGE_TIME);

        try {

            while (cursor.moveToNext()) {
                final String date = cursor.getString(dateIndex);
                final String hour = cursor.getString(hourIndex);
                String _packageName = cursor.getString(packageNameIndex);
                final String packageName = _packageName.replace(".", "-");
                final float unlocks = cursor.getInt(unlocksIndex);
                final float notifications = cursor.getInt(notificationsIndex);
                final float usage = cursor.getFloat(usageIndex);

                Log.i("DB", "" + date + "/" + hour + "/" + packageName + "/" + unlocks + "/" + notifications + "/" + usage);
                App.usageDatabase.child(date).child(hour).child(packageName).child(DatabaseHelper.UNLOCKS_COUNT).setValue(unlocks);
                App.usageDatabase.child(date).child(hour).child(packageName).child(DatabaseHelper.NOTIFICATIONS_COUNT).setValue(notifications);
                App.usageDatabase.child(date).child(hour).child(packageName).child(DatabaseHelper.USAGE_TIME).setValue(usage);

            }
        } finally {
            cursor.close();
        }


        return Result.success();
    }
}
