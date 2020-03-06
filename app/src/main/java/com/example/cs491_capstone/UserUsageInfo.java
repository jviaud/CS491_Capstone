package com.example.cs491_capstone;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserUsageInfo implements Comparable<UserUsageInfo>, Parcelable {

    /**
     * this is the package name used the refer to the app through package manager
     */
    private String packageName,
    /**
     * This is the simple app name that appears on the home screen
     */
    simpleName;
    /**
     * The apps icon as it appears in Google PlayStore, acquired through using the PackageManager
     */
    private Drawable icon;
    /**
     * The total number of times for today the app has been opened first upon unlock
     */
    private int unlocks,
    /**
     * The total number of notifications this app has sent
     */
    notifications;

    private Map<String, Map<Integer, Long>> usage;
    private HashMap<Integer, Long> todaysUsage;


    ///CONSTRUCTOR
    //EMPTY CONSTRUCTOR FOR FIREBASE

    public UserUsageInfo() {

    }

    /**
     * @param packageName The String representing the app's package name e.g "com.example.cs491_capstone".
     * @param simpleName  The String representing the apps simple name as it appears in the Google PlayStore
     * @param icon        The Drawable Icon as it appears in the Google PlayStore
     */
    public UserUsageInfo(String packageName, String simpleName, Drawable icon, Map<String, Map<Integer, Long>> usage) {
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.icon = icon;
        this.usage = usage;


        //UNLOCKS_COUNT AND NOTIFICATIONS_COUNT ARE SET TO 0 AT START
        unlocks = 0;
        notifications = 0;
    }

    /**
     * @param packageName The String representing the app's package name e.g "com.example.cs491_capstone".
     * @param appName     The String representing the apps simple name as it appears in the Google PlayStore
     * @param icon        The Drawable Icon as it appears in the Google PlayStore
     * @param todayUsage  The Map containing the usage info for today by hour of day
     */
    public UserUsageInfo(String packageName, String appName, Drawable icon, HashMap<Integer, Long> todayUsage) {
        this.packageName = packageName;
        this.simpleName = appName;
        this.icon = icon;
        this.todaysUsage = todayUsage;

        //UNLOCKS_COUNT AND NOTIFICATIONS_COUNT ARE SET TO 0 AT START
        unlocks = 0;
        notifications = 0;
    }

    ///

    ///GETTERS

    protected UserUsageInfo(Parcel in) {
        packageName = in.readString();
        simpleName = in.readString();
        unlocks = in.readInt();
        notifications = in.readInt();
    }


    /**
     * @return String for package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return String for simple app name
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * @return drawable for app's icon
     */
    public Drawable getIcon() {
        return icon;
    }

    /**
     * @return number of times this app was the first to be opened when unlocked
     */
    public int getUnlocks() {
        return unlocks;
    }

    /**
     * @return number of notifications this app has sent
     */
    public int getNotifications() {
        return notifications;
    }

    public Map<Integer, Long> getTodayUsage() {
        return todaysUsage;
    }

    /**
     * @return The usage for the current day up until now
     */
    public long getDailyUsage() {
        long totalUsage = 0L;
        for (Integer hour : todaysUsage.keySet()) {
            totalUsage += todaysUsage.get(hour);
        }
        return totalUsage;
    }

    /**
     * @param interval the specefic hour interval we are interested in
     * @return the total usage for the specific hour interval
     */
    public long getIntervalUsage(int interval) {
        if (todaysUsage.isEmpty()) {
            return 0;
        }
        if (todaysUsage.get(interval) == null) {
            return 0;
        } else return todaysUsage.get(interval);
    }

    /**
     * @param time the time as a long
     * @return the formatted string in HH:MM:SS format
     */
    public String formatTime(long time) {
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );
    }

    public Map<Integer, Long> getDayUsage(String day) {
        return usage.get(day);
    }

    @Override
    public int compareTo(UserUsageInfo o) {
        long t1 = o.getDailyUsage();
        long t2 = getDailyUsage();

        return Long.compare(t1, t2);
    }


    public static final Creator<UserUsageInfo> CREATOR = new Creator<UserUsageInfo>() {
        @Override
        public UserUsageInfo createFromParcel(Parcel in) {
            return new UserUsageInfo(in);
        }

        @Override
        public UserUsageInfo[] newArray(int size) {
            return new UserUsageInfo[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(simpleName);
        dest.writeInt(unlocks);
        dest.writeInt(notifications);
    }
}
