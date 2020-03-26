package com.example.cs491_capstone.services;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.DatabaseHelper;

import static com.example.cs491_capstone.App.INCLUDED_APPS_LIST;

public class NotificationListener extends NotificationListenerService {

    /**
     * BOOLEAN REPRESENTING IF NOTIFICATION MANAGER IS ABLE TO CONNECT
     * BY DEFAULT WE ASSUME IT IS UNABLE TO CONNECT AND THEREFORE PERMISSION IS NOT GRANTED
     * set to true in onListenerConnected() and set to false in onListenerDisconnected() methods
     */
    public static boolean connectedNL = false;
    /**
     * represents the package name oof the app sending the notification
     */
    static String notificationPackage,
    /**
     * represents the simple app name of the app sending the notification
     */
    simpleAppName;
    /**
     * CONTEXT IS NEEDED FOR PACKAGE MANAGER & PACKAGE MANAGER IS NEEDED FOR SIMPLE APP NAME
     */
    Context context;
    /**
     * used to get app info such as icon,package name and simple name
     */
    private PackageManager packageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        packageManager = context.getPackageManager();
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //THIS METHOD WILL TRACK ALL NOTIFICATIONS_COUNT THAT ARE RECEIVED BY THE SYSTEM AND SORT THEM INTO A DATABASE
        super.onNotificationPosted(sbn);

        //GRAB PACKAGE NAME FROM STATUS BAR NOTIFICATION (BUILD IN FUNCTION OF SBN)
        notificationPackage = sbn.getPackageName();


        //USE PACKAGE MANAGER TO GET STANDARD APP NAME
        simpleAppName = getSimpleAppName(notificationPackage);

        if (INCLUDED_APPS_LIST.contains(notificationPackage)) {
            try {
                App.localDatabase.insert(notificationPackage, 0, 1, 0);
                Log.i("TRACK", "NEW NOTIFICATION ENTRY SUCCESS " + notificationPackage + "  ");
            } catch (SQLiteConstraintException ce) {
                int count = 0;
                if (App.localDatabase.get(DatabaseHelper.NOTIFICATIONS_COUNT, notificationPackage).equals("")) {
                    App.localDatabase.set(notificationPackage, DatabaseHelper.NOTIFICATIONS_COUNT, count + 1);
                    Log.i("TRACK", "UPDATING NEW NOTIFICATION ENTRY " + notificationPackage + "  " + count);
                } else {
                    count = Integer.parseInt(App.localDatabase.get(DatabaseHelper.NOTIFICATIONS_COUNT, notificationPackage)) + 1;
                    App.localDatabase.set(notificationPackage, DatabaseHelper.NOTIFICATIONS_COUNT, count);
                    Log.i("TRACK", "UPDATING EXISTING NOTIFICATION ENTRY " + notificationPackage + "  " + count + "  : " + App.localDatabase.get(DatabaseHelper.NOTIFICATIONS_COUNT, notificationPackage));
                }
            }
        }
    }

    /**
     * @param notificationPackage the package name of the app sending the notification
     * @return the simple app name
     * @throws IllegalArgumentException will throw an Exception if a package name that can't be found is passed, which is extremely unlikely
     */
    private String getSimpleAppName(String notificationPackage) throws IllegalArgumentException {
        //THIS METHOD USES PACKAGE MANAGER TO CONVERT THE PACKAGE NAME TO A STANDARD USER FRIENDLY APP NAME
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(notificationPackage, 0);
            return packageManager.getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }


    //METHODS FOR CHECKING NOTIFICATION LISTENER STATE


    public void onListenerConnected() {
        super.onListenerConnected();
        //LISTENER IS ONLY ABLE TO CONNECT IF WE HAVE THE PERMISSION
        connectedNL = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getActiveNotifications();
        }
        Log.i("NotificationListener", "Connected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        //IF LISTENER IS EVER DISCONNECTED THEN WE ASSUME THE PERMISSION WAS REVOKED
        connectedNL = false;
        Log.i("NotificationListener", "Disconnected");
    }
}
