package com.example.cs491_capstone;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UserUsageInfo implements Comparable<UserUsageInfo>, Parcelable {

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
    private Long usage;


    /**
     * @param packageName The String representing the app's package name e.g "com.example.cs491_capstone".
     * @param simpleName  The String representing the apps simple name as it appears in the Google PlayStore
     * @param icon        The Drawable Icon as it appears in the Google PlayStore
     * @param usage       g
     */
    public UserUsageInfo(String packageName, String simpleName, Drawable icon, Long usage) {
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.icon = icon;
        this.usage = usage;
    }

    ///

    ///GETTERS

    protected UserUsageInfo(Parcel in) {
        packageName = in.readString();
        simpleName = in.readString();
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
     * @return The usage for the current day up until now
     */
    public long getUsage() {
        return usage;
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


    @Override
    public int compareTo(UserUsageInfo o) {
        long t1 = o.getUsage();
        long t2 = getUsage();

        return Long.compare(t1, t2);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(simpleName);
    }
}
