package com.example.cs491_capstone.services;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.BlockerActivity;
import com.example.cs491_capstone.DatabaseHelper;
import com.example.cs491_capstone.MainActivity;
import com.example.cs491_capstone.R;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.example.cs491_capstone.App.INCLUDED_APPS_LIST;
import static com.example.cs491_capstone.App.isTrackedApp;
import static com.example.cs491_capstone.ui.settings.SettingsFragment.dataDisabled;
import static com.example.cs491_capstone.ui.settings.SettingsFragment.parentalControls;
import static com.example.cs491_capstone.ui.settings.SettingsFragment.wifiDisabled;

public class BackgroundMonitor extends Service {
    /**
     * Since the check occurs every 1 minute we will add 60000ms to the total time the app has been used
     */
    private static final long totalTime = 60000L;
    /**
     * The package name for the current prevApp being used
     */
    public static String appInUse = null;
    /**
     * The first prevApp to be used when the phone has been unlocked
     */
    public static String firstUsed;
    /**
     * boolean representing whether or not the phone is locked, value is change in a broadcast receiver
     */
    public static boolean locked;
    /**
     * tracks the time left to use your phone
     */
    public static long timeLeft = -1;
    /**
     * The broadcast receiver for the unlock event
     */
    private UnlockListener unlock;//UNLOCK LISTENER RECEIVER
    /**
     * the exact moment the phone was unlocked
     */
    private long UNLOCK_TIME = 0;
    /**
     * variable used to get two scheduled task running at different intervals without interfering with each other
     */
    private long minuteTimer = 60000L;

    @Override
    public void onCreate() {
        super.onCreate();

        //REGISTER UNLOCK LISTENER
        //ACTION_USER_PRESENT IS WHEN DEVICE HAS BEEN UNLOCKED (TURNING ON THE PHONE DOES NOT TRIGGER THIS EVENT)
        //IF THE USER DOES NOT HAVE A LOCK SCREEN, WHICH IS POSSIBLE ON ANDROID, THEN THIS WILL NOT BE TRIGGERED
        IntentFilter unlockFilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        unlock = new UnlockListener();
        registerReceiver(unlock, unlockFilter);
        ////


        //CREATE A TASK THAT OCCURS EVERY 1 SECOND AND RUNS IN A POOL OF 5 DESIGNATED BACKGROUND THREADS
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(4);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {

                /*
                I CAN'T GET TWO SCHEDULED TASK WORKING AT THE SAME TIME SO INSTEAD I WILL USE ONE
                AND HAVE THE SECOND METHOD ONLY TRIGGER ONCE EVERY MINUTE
                */

                if (parentalControls && !(timeLeft < 0)) {
                    timeLeft -= 1000;
                }

                minuteTimer -= 1000;
                if (minuteTimer == 0) {
                    minuteTimer = 60000L;
                    processTracker();

                    //CHECK IF PARENTAL CONTROLS IS TURNED ON
                    if (parentalControls) {

                        if (timeLeft == 0) {
                            checkLimit();
                        }

                        if (minuteTimer == 0) {

                        }

                        if (wifiDisabled) {

                        }
                        if (dataDisabled) {

                        }
                    }
                }


                checkLock();
            }//END RUN
        }, 0, 1000, TimeUnit.MILLISECONDS);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(unlock);
    }

    /**
     * check to see if the phone is locked oor unlocked
     * if locked -> record the time it was locked and continue to record the time as it remains locked -> We need to capture the last moment it was locked.
     * if unlocked -> record the exact moment it was unlocked.
     * Then we get the time between the locked and unlocked events and see what apps were active during this time
     */
    public void checkLock() {
        Log.i("PROCESS", "START tracker: " + Thread.currentThread().getName());
        KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);

        //THIS STATEMENT IS TRUE IF THE PHONE IS LOCKED, THIS WILL NEVER BE TRUE IF THE PHONE IS NEVER LOCKED.
        //LOCK IN THIS CASE REFERS TO A FINGERPRINT,PASSCODE,PATTERN OR SWIPE
        //IT IS HOWEVER POSSIBLE ON ANDROID TO NOT HAVE A LOCK SCREEN
        if (myKM.isKeyguardLocked()) {
            //SAVE THE INSTANCE THE PHONE WAS LOCKED TO BE USED LATER
            /*
              the exact time the phone is locked the the timestamp as it continues to be locked
             */
            long LOCK_TIME = System.currentTimeMillis();
            if (!locked) {
                locked = true;
            }

            Log.i("LOCK", "LOCKED:" + LOCK_TIME);

            //THIS IF STATEMENT STOPS THIS SECTION OF THE CODE FROM RUNNING
            //SINCE THIS METHOD IS CONSTANTLY BEING CALLED EVERY 1000 MILLISECOND
            if (UNLOCK_TIME != 0) {
                Log.i("LOCK", "MOMENT");

                //INITIALIZE USAGE_TIME STATE MANAGER AND AGGREGATE DATA INTO A MAP
                //THE TIME FRAME FOR THIS USAGE_TIME STATE IS THE MOMENT THE USER UNLOCKS_COUNT THE PHONE TO WHEN THE PHONE IS LOCKED AGAIN
                UsageStatsManager usm = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
                Map<String, UsageStats> usage = usm.queryAndAggregateUsageStats(UNLOCK_TIME, LOCK_TIME);

                //PLACE DATA INTO A SORTED MAP
                //SORTED MAP IS USED BECAUSE I CAN EASILY GET THE FIRST KEY WITHOUT ITERATING OVER THE ENTIRE LIST
                //A LONG REPRESENTING THE CURRENT TIME OF INSERTION IS USED THOUGH THIS IS NOT ENTIRELY NECESSARY SO IT MAY BE REMOVED LATER
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (String stat : usage.keySet()) {
                    //THIS VALUE IS USED TWICE SO I INITIALIZE IT HERE TO SAVE TIME/SPACE
                    long lastTimeVisible;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        lastTimeVisible = usage.get(stat).getLastTimeVisible();
                    } else {
                        lastTimeVisible = usage.get(stat).getLastTimeUsed();
                    }

                    //BECAUSE USAGE_TIME STAT GETS ALL DATA FROM A GIVEN TIME FRAME I NEED TO REMOVE DATA THAT IS NOT NEEDED
                    //1) I ONLY NEED DATA BELONGING TO APPS THAT AREN'T SYSTEM APPS
                    //THIS HAS TWO ISSUES
                    //1A) THE RECENT TASK SCREEN IS NOT FILTERED
                    //2B) SOME SYSTEM APPS THAT ARE NEEDED ARE FILTERED
                    //2) I ONLY NEED DATA FROM APPS THAT WERE VISIBLE DURING THE SPECIFIED TIME FRAME


                    if (isTrackedApp(stat) & (lastTimeVisible >= UNLOCK_TIME & lastTimeVisible <= LOCK_TIME)) {
                        //IF DATA MEETS REQUIREMENTS ADD IT TO THE SORTED MAP
                        mySortedMap.put(System.currentTimeMillis(), usage.get(stat));
                    }
                }

//                for (Long stat : mySortedMap.keySet()) {
//                    Log.i("LOCK", "MAP:" + mySortedMap.get(stat).getPackageName() + "|" + mySortedMap.get(stat).getLastTimeVisible());
//                }


                //THIS IS DONE JUST IN CASE TO PREVENT NULL POINTER EXCEPTIONS
                //STOPS ME FROM QUERYING AN EMPTY MAP
                if (!mySortedMap.isEmpty()) {
                    //THIS STRING REPRESENTS THE FIRST APP THAT IS USED ONCE I UNLOCKED THE PHONE
                    //I WILL DO SOMETHING WITH THIS DATA LATER
                    firstUsed = mySortedMap.get(mySortedMap.firstKey()).getPackageName();

                }
                Log.i("LOCK", "APP:" + firstUsed);

                try {
                    Log.i("LOCK", "NEW LOCK LOGGED");
                    App.localDatabase.insert(firstUsed, 1, 0, 0);
                } catch (SQLiteConstraintException ce) {
                    int count = 0;
                    if (App.localDatabase.get(DatabaseHelper.UNLOCKS_COUNT, firstUsed).equals("")) {
                        Log.i("LOCK", "EMPTY LOCK LOGGED");
                        App.localDatabase.set(firstUsed, DatabaseHelper.UNLOCKS_COUNT, count++);
                    } else {
                        Log.i("LOCK", "EXISTING LOCK LOGGED");
                        count = Integer.parseInt(App.localDatabase.get(DatabaseHelper.UNLOCKS_COUNT, firstUsed));
                        App.localDatabase.set(firstUsed, DatabaseHelper.UNLOCKS_COUNT, count + 1);
                    }

                }

                //STORE IN DATABASE RIGHT HERE
//                if (!(App.localDatabase.get(DatabaseHelper.UNLOCKS_COUNT, firstUsed).equals(""))) {
//                    int count = Integer.parseInt(App.localDatabase.get(DatabaseHelper.UNLOCKS_COUNT, firstUsed));
//                    App.localDatabase.set(firstUsed, DatabaseHelper.UNLOCKS_COUNT, count + 1);
//                } else {
//                    //THE JOB SCHEDULER SHOULD CREATE THE ENTRY SO THAT IT IS NOT NULL
//                    //BUT ON THE OFF CHANCE THAT THIS METHOD IS ACCESSED A MILLISECOND BEFORE THE JOB SCHEDULER WE WILL ESSENTIALLY MANUALLY MAKE THE NEW ENTRY
//                    App.localDatabase.insert(firstUsed, 0, 1, 0);
//                }

                // mySortedMap.clear();

                //RESET UNLOCK_TIME BACK TO ZERO SO THIS ENTIRE METHOD ISN'T TRIGGERED EVERY TIME & WAStE RESOURCES
                UNLOCK_TIME = 0;
            }
        }

    }


    /**
     *
     */
    public void checkLimit() {
         /*
        THIS ONLY WORKS ON APIs LESS THAN 29, ON 29 AND UP GOOGLE BLOCKS ACTIVITIES FROM STARTING OVER OTHER ACTIVITIES
        IT ALSO BLOCKS APPS FROM DRAWING OVER OTHER APPS
         */
        //getApplicationContext() IS USED IN PLACE OF "this" for CONTEXT TOO PREVENT MEMORY LEAK
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
            //WE FIRST CHECK TO SEE IF THE APP BEING USED ISN'T OUR OWN APP, OR A SYSTEM APP

            if (timeLeft <= 0) {
                if ((!appInUse.equals(App.PACKAGE_NAME)) & isTrackedApp(appInUse)) {
                    //WE THEN CHECK TO SEE IF THE TIMER HAS RUN OUT, IF IT HAS THEN WE START THE TRANSPARENT BLOCKER ACTIVITY

                    Intent dialogIntent = new Intent(getApplicationContext(), BlockerActivity.class);
                    dialogIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(dialogIntent);

                    //TODO AD FUNCTIONALITY TO CHECK TIMER FOR SPECIFIC APPS, WILL PROBABLY USE HASH MAP OF APP AND THEIR RESPECTIVE TIME
                }
            }


        } else {
            if (timeLeft <= 0) {
                //Toast.makeText(getApplicationContext(), "TIME=0", Toast.LENGTH_SHORT).show();
                if ((!appInUse.equals(App.PACKAGE_NAME)) & isTrackedApp(appInUse)) {

                    //NOTIFICATION ISN'T ALWAYS SENT WHEN WE SEND IT FROM THE BACKGROUND SERVICE
                    //SO WE CALL THE UI THREAD AND RUN IT THERE
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(getApplicationContext(), "LIMIT REACHED", Toast.LENGTH_SHORT).show();
                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

                            Notification notify = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_2_ID_ALERTS)
                                    .setSmallIcon(R.drawable.bn_icon_usage)
                                    .setContentTitle("Usage Limit")
                                    .setContentText("Your phone usage for today has been reached")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                                    .build();

                            notificationManagerCompat.notify(2, notify);
                        } // This is your code
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }
    }

    /**
     * Tracks apps that are being used
     */
    public void processTracker() {
        //ALL OF THE OBJECTS CREATED IN THIS METHOD WILL BE ELIGIBLE FOR JAVA GARBAGE COLLECTION AND SHOULD'NT CAUSE OUT OF MEMORY ERRORS


        Log.i("PROCESS", "START tracker: " + Thread.currentThread().getName());


        //INITIALIZE AND REGISTER USAGE_TIME STATS MANAGER
        UsageStatsManager usm = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) {
            throw new IllegalStateException("UsageStatsManager must be initialized");
        }

        //System.currentTimeMillis() IS USED TWICE SO I ASSIGNED IT TO A VARIABLE TO SAVE TIME SO I DON'T HAVE TO CALL CONSTRUCTOR TWICE
        long time = System.currentTimeMillis();


        //CREATE A LIST OF USAGE_TIME STATS BETWEEN NOW AND 10 SECONDS AGO
        //
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 10000 * 10, time);
        //CREATE A SORTED MAP & PUT INTO IT ALL USAGE_TIME DATA, SORTED IN ASC ORDER, MOST RECENT USAGE_TIME STAT IS AT THE END OF MAP
        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
        /*
        THERE IS AN ISSUE WITH THE TIMING OF THE HOME LAUNCHER'S LIFE CYCLE. SOMETIMES IT GETS TRIGGERED BEFORE AN APP FULLY CLOSES
        SO IF I GO FROM ONE APP TO HOME, THE APP THAT I CAME FROM WILL SHOW UP AS STILL BEING IN USE BECAUSE THE HOME LAUNCHER WAS CREATED BEFORE THE APP WAS FULLY CLOSED
         */
        for (UsageStats usage : stats) {
            mySortedMap.put(usage.getLastTimeUsed(), usage);

        }


        //IF THE LIST IS NOT EMPTY THEN WE PROCESS WHAT IS INSIDE OF IT
        if (!mySortedMap.isEmpty()) {
            //THE LAST KEY IS THEORETICALLY THE MOST RECENT APP TO BE USED SINCE THEY ARE SORTED BY LAST TIME USED
            UsageStats stat = mySortedMap.get(mySortedMap.lastKey());
            appInUse = stat.getPackageName();


            if (INCLUDED_APPS_LIST.contains(appInUse)) {

                try {
                    App.localDatabase.insert(appInUse, 0, 0, totalTime);
                    Log.i("TRACK", "NEW USAGE ENTRY " + appInUse + "  " + totalTime);
                } catch (SQLiteConstraintException ce) {
                    long timeSpent = totalTime + Long.parseLong(App.localDatabase.get(DatabaseHelper.USAGE_TIME, appInUse));
                    Log.i("TRACK", "UPDATE EXISTING USAGE ENTRY " + appInUse + " " + timeSpent + " : " + App.localDatabase.get(DatabaseHelper.USAGE_TIME, appInUse));
                    App.localDatabase.set(appInUse, DatabaseHelper.USAGE_TIME, timeSpent);
                }

            }
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_1_ID_SERVICE)
                .setContentTitle("Usage Tracker")
                .setContentText("App is running in background to track usage data")
                .setSmallIcon(R.drawable.ic_notification_service)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        return START_REDELIVER_INTENT;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Boradcast receiver that listens when the phone has been locked and sets the boolean locked too be true
     */
    public class UnlockListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //SAVE THE INSTANCE THE PHONE WAS UNLOCKED TO BE SUED LATER
            locked = false;
            UNLOCK_TIME = System.currentTimeMillis();
            Log.i("LOCK", "UNLOCKED" + UNLOCK_TIME);

        }

    }

}
