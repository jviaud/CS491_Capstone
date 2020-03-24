package com.example.cs491_capstone;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class App extends Application {

    /**
     * notification channel for services, required in API 29+
     */
    public static final String CHANNEL_1_ID_SERVICE = "ServiceChannel";
    /**
     * notification channel for alerts, required in API 29+
     */
    public static final String CHANNEL_2_ID_ALERTS = "Alerts";
    /**
     * The days of the week
     */
    public final static String[] week = new String[]{"Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};
    /**
     * List of values on a clock from 12AM to 11PM used to label the X-Axis on the graphs
     */
    public final static String[] clock = new String[]{"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM",
            "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM",
            "8PM", "9PM", "10PM", "11PM"};
    /**
     * string containing the package name of this app
     */
    public static String PACKAGE_NAME;
    /**
     * The SQL Lite database
     */
    public static DatabaseHelper localDatabase;
    /**
     * The SQL Lite database for Awards
     */
    public static GoalDataBaseHelper awardDataBase;
    /**
     * The current date in yyyy-MM-dd format. This is used for the firebaseDatabase and must be "-" separated because "/" is a special character is firebase
     */
    public static String DATE = getCurrentDate();
    /**
     * The numerical value of the current hour in military time 0-24. This is used for the firebaseDatabase
     */
    public static String HOUR = getCurrentHourInterval();
    /**
     * this boolean is used to mark the start of a new week. A new week is defined as the first Sunday
     */
    public static boolean isNewWeek = false;
    /**
     * The array list containing the last 4 weeks. The most current week at the 0 index and the furthest week being at the last index
     * The sub list inside this list is ordered from start of week to end. So, 0 is the start of the week, Sun, and 7 is the end of the week, Sat.
     */
    public static ArrayList<ArrayList<String>> currentPeriod = getCurrentPeriod();
    public static List<String> INCLUDED_APPS_LIST;
    public static List<InstalledAppInfo> ALL_APPS_LIST;
    public static Set<String> SPECIAL_APPS;

    public static void getInstalledApps(Context context) {

        PackageManager packageManager = context.getPackageManager();
        // CREATE LIST OF PACKAGE INFO TO GRAB A LIST OF ALL INSTALLED APPS
        //packageManager.getInstalledPackages IS A METHOD THAT IS PART OF THE PACKAGE MANAGER API
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            //FOR EVERY PACKAGE INFO IN THIS LIST
            //GRAB A SPECIFIC PACKAGE INFO AND ASSIGN IT TO P, THIS IS NOT REQUIRED BUT MAKES CODE LOOK CLEANER BELOW
            PackageInfo p = packs.get(i);
            String pack = p.packageName;
            String appName = p.applicationInfo.loadLabel(packageManager).toString();
            Drawable icon = p.applicationInfo.loadIcon(packageManager);

            if (isSystemApp(pack, context)) {
                INCLUDED_APPS_LIST.add(pack);
                ALL_APPS_LIST.add(new InstalledAppInfo(pack, appName, icon).setTracked(true));
            } else {
                ALL_APPS_LIST.add(new InstalledAppInfo(pack, appName, icon).setTracked(false));
            }


        }
        // Collections.addAll(INCLUDED_APPS_LIST, SPECIAL_APPS);
        Collections.sort(ALL_APPS_LIST, Collections.<InstalledAppInfo>reverseOrder());
        Log.i("TRACKED", "INCLUDED" + INCLUDED_APPS_LIST);
        for (InstalledAppInfo info : ALL_APPS_LIST) {
            Log.i("TRACKED", "EXCLUDED" + info.getPackageName() + "|" + info.isTracked());
        }

        //RETURN THE LIST OF OBJECTS
    }


    public static boolean isTrackedApp(String packageName) {

        return (INCLUDED_APPS_LIST.contains(packageName));
    }


    /**
     * @param packageName the package name of the app too check
     * @return true if the app is a System app as identified by ApplicationInfo but will return false if the app is in the list of exclusions
     */
    public static boolean isSystemApp(String packageName, Context context) {
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //IF THE APPLICATION INFO DOES NOT IDENTIFY THE PACKAGE AS A UPDATED_SYSTEM_APP & A FLAG_SYSTEM APP & IT IS NOT IN THE EXCLUSION LIST THEN RETURN TRUE; ELSE RETURN FALSE;
        return !(!SPECIAL_APPS.contains(packageName) && ((ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) & ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0));

    }

    /**
     * Gets all days belonging to the current week in the yyyy-MM-dd format
     * This will be used to query the database for days in a week
     *
     * @return List of days in the current week
     */
    private static ArrayList<ArrayList<String>> getCurrentPeriod() {

        ArrayList<ArrayList<String>> period = new ArrayList<>();

        //MUST USE FORMATTER SO LOCAL DATE CAN UNDERSTAND WHAT FORMAT THE STRING IS IN
        // DATE HAS TO BE IN THIS FORMAT TO TAKE ADVANTAGE OF THE DATE FUNCTION IN SQL LITE
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        //SET THE LOCAL DATE EQUAL TO THE STRING REPRESENTATION OF TODAY
        LocalDate now = LocalDate.parse(DATE, formatter);
        LocalDate thisSunday = null;

        //GET THE NAME OF THE CURRENT DAY OF THE WEEK
        String dayOfWeek = now.dayOfWeek().getAsShortText();
        //GET THE NAME OF THE FOLLOWING DAY
        //String dayOfWeekNext = dayOfWeek;

        Log.i("DATE_LIST", "NOW:" + dayOfWeek);


        ArrayList<String> currentWeek = new ArrayList<>();

        //WE NEED TO TAKE CARE OF THE CASE WHEN TODAY IS SUNDAY.
        //WE WILL NEVER GO INTO THE WHILE LOOP AND THE SUNDAY WILL BE MISSING FROM THE LOOP
        //WE ALSO TRIGGER THE LAST WEEK REMOVAL FROM THE DATABASE
        if (dayOfWeek.equals("Sun")) {
            thisSunday = now;
            // currentWeek.add(0, formatter.print(now));
            isNewWeek = true;
        }

        //WHILE THE DAY IS NOT SUNDAY
        while (!dayOfWeek.equals("Sun")) {
            //GO BACK ONE DAY
            now = now.minusDays(1);

            //ADD THE DATE IN THE CORRECT FORMAT TO THE LIST, WE WILL NOT ADD THE CURRENT DATE HERE INSTEAD WE WILL DOO IT FURTHER DOWN
            currentWeek.add(0, formatter.print(now));

            //SET THE DAY OF THE WEEK EQUAL TO THE PREVIOUS DAY
            dayOfWeek = now.dayOfWeek().getAsShortText();

            if (dayOfWeek.equals("Sun")) {
                thisSunday = now;
            }
            Log.i("DATE_LIST", "prev:" + dayOfWeek);
        }


        //RESET THE CURRENT DATE BACK TO TODAY
        now = LocalDate.parse(DATE, formatter);
        dayOfWeek = now.dayOfWeek().getAsShortText();
        currentWeek.add(formatter.print(now));


        //BECAUSE WE ARE LOOKING AT THE FOLLOWING DAY THIS TAKES CARE OF THE SCENARIO WHERE TODAY IS MONDAY AND WE DON'T MAKE IT INTO THE WHILE LOOP
        while (!dayOfWeek.equals("Sat")) {
            Log.i("DATE_LIST", "now:" + now);
            now = now.plusDays(1);

            //NOW WE ADD THE CURRENT DATE HERE. THIS WAY IT WON'T BE DUPLICATED & WE CAN ENSURE THAT
            currentWeek.add(formatter.print(now));


            dayOfWeek = now.dayOfWeek().getAsShortText();
            Log.i("DATE_LIST", "next:" + dayOfWeek);
        }
        //ONCE WE ARE DONE GETTING THIS WEE WE ADD IT TOO THE CURRENT MONTH
        period.add(currentWeek);

        ///NOW WE CAN GET THE PREVIOUS WEEKS
        //WE ONLY WANT THE PREVIOUS 3 WEEKS WHICH MAKES 4 WEEKS INCLUDING THE CURRENT WEEK
        for (int i = 0; i < 3; i++) {
            //EACH WEEK HAS 7 DAYS
            ArrayList<String> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                thisSunday = thisSunday.minusDays(1);
                week.add(0, formatter.print(thisSunday));
            }
            period.add(week);
        }


        Log.i("DATE_LIST", "" + period);
        //THIS LIST WILL BE MORE USEFUL IN THE REVERSE ORDER SINCE THE GRAPHS WILL START ON SUNDAY
        //Collections.reverse(week);
        return period;
    }

    /**
     * @param time millisecond representation of a time
     * @return String representation of the time in HH:MM:SS format
     */
    public static String timeFormatter(long time) {
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    /**
     * @return A string representing the hour of the day in military time 12AM in 0, 1PM is 13
     */
    public static String getCurrentHourInterval() {
        DateTimeZone timeZone = DateTimeZone.forID("America/Montreal");
        DateTime now = DateTime.now(timeZone);

        return String.valueOf(now.getHourOfDay());
    }

    /**
     * @return a string representing the current date in MM/dd/yyyy format
     */
    public static String getCurrentDate() {
        DateTimeZone timeZone = DateTimeZone.forID("America/Montreal");
        DateTime now = DateTime.now(timeZone);

        // DateTimeFormatter dtf = DateTimeFormat.forPattern("MM-dd-yyyy");
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

        return dtf.print(now);
    }

    /**
     * @param packageName : the package name that you want to receive the category name for.
     * @param context     : package manager required a context, we do not assign a context in here because this method is static and to prevent memory leaks
     * @return a string representing the category, return undefined if package is not found or if category is not set
     */
    public static String getAppCategoryName(String packageName, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                //WE USE PACKAGE MANAGER TO GET APPLICATION INFO
                //FROM APPLICATION INFO WE CAN GET A GLOBAL int REPRESENTING THE APP'S CATEGORY
                int categoryVal;

                categoryVal = context.getPackageManager().getApplicationInfo(packageName, 0).category;

                //THE INT WILL BE -1 IF THE APP'S CATEGORY HAS NOT BEEN DEFINED IN ITS MANIFEST
                if (categoryVal == -1) {
                    return "Other";
                } else {
                    //ONCE WE HAVE THE CORRECT CATEGORY AWE MUST CONVERT IT TO A STRING BECAUSE IT IS RETURNED AS A CHARACTER ARRAY
                    //WE MUST ALSO REMOVE THE UNDERSCORE
                    return ApplicationInfo.getCategoryTitle(context, categoryVal).toString().replace("_", " ");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "Other";
    }

    /**
     * Modifies a list view's height so that it will be the summation of its children.
     * Because I can not use a secondary scrolling view inside a nested scrollview I have to give the list view a hard value
     * but because the list view's contents may vary I will not know the proper height until after it has been created
     *
     * @param listView the list view who's height will be modified
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
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

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getBoolean("firstrun", true)) {
            SPECIAL_APPS = new HashSet<>();
            String[] apps = {"com.google.android.calender", "com.android.camera2", "com.android.chrome",
                    "com.google.android.deskclock", "com.google.contacts", "com.google.android.apps.docs", "com.android.documentsui",
                    "com.google.android.gm", "com.google.android.videos", "com.google.android.music", "com.google.android.vending", "com.google.android.apps.maps",
                    "com.google.android.apps.messaging", "com.android.phone", "com.google.android.apps.photos", "com.android.storagemanager",
                    "com.google.android.apps.wallpaper", "com.google.android.youtube"};
            Collections.addAll(SPECIAL_APPS, apps);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("exclusion_list", SPECIAL_APPS);
            editor.apply();
        }
        SPECIAL_APPS = prefs.getStringSet("exclusion_list", SPECIAL_APPS);

        //TODO IF THE APP IS TERMINATED WE NEED TO BE ABLE TOO SAVE THE LIST OF APPS THAT SHOULD BE TRACKED, FOR NOW WE CAN IGNORE THIS FOR PRESENTATION PURPOSES

        INCLUDED_APPS_LIST = new ArrayList<>();
        ALL_APPS_LIST = new ArrayList<>();
        getInstalledApps(getApplicationContext());


        //CREATE INSTANCE OF THE SQL LITE DATABASE
        localDatabase = new DatabaseHelper(this);


        /*
        1. SINCE currentPeriod IS SET BEFORE WE REACH THIS METHOD WE DON'T NEED TOO WORRY ABOUT IT BEING NULL
        2. WE ALSO CAN ASSURE THAT THE BOOLEAN isNewWeek IS SET TO THE CORRECT VALUE BECAUSE IT IS FALSE BY DEFAULT AND ONLY CHANGED WHEN currentPeriod IS BEING INITIALIZED
        3. WE CAN REFERENCE THE INDEXES IN currentPeriod DIRECTLY BECAUSE WE ALWAYS KNOW THE SIZE OF THE LIST(4) AND THE SUB LIST(7) (THE INDEXES WILL BE ONE LESS SINCE THEY START AT 0
        SO, WE GET THE LAST INDEX OF THE LIST AND THE LAST INDEX OF THE SUB LIST AND REMOVE ANY DATE FROM THE DATABASE LESS THAN IT
         */
        if (isNewWeek) {
            localDatabase.emptyWeek(currentPeriod.get(3).get(6));
        }


        //THIS IS A SELF CHECK FOR PACKAGE NAME
        //WHEN CHECKING FOR USAGE_TIME PERMISSION, A PACKAGE NAME IS NEEDED
        //MAKES SENSE TO CHECK FOR SELF BECAUSE I CAN ALWAYS GUARANTEE THIS APP EXIST WHEN CHECK OCCURS
        //THIS IS DONE DYNAMICALLY IN CASE PACKAGE NAME CHANGES
        PACKAGE_NAME = getApplicationContext().getPackageName();

        ///

        ///CREATE NOTIFICATION CHANNELS
        createNotificationChannel();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    }

    /**
     * This method creates necessary notification channels for general alerts and background services
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_1_ID_SERVICE,
                    "App Tracker",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("This Tracks App Activity");


            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_2_ID_ALERTS,
                    "General Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            alertChannel.setDescription("This Shows Application Alerts");


            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                manager.createNotificationChannel(alertChannel);
            }

        }
    }
}

