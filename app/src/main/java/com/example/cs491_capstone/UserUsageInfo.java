package com.example.cs491_capstone;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

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
    private String category;


    UserUsageInfo(String packageName, String simpleName, Drawable icon, Long usage) {
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.icon = icon;
        this.usage = usage;
    }

    UserUsageInfo(String packageName, String simpleName, String category) {
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.category = category;
    }

    ///

    ///GETTERS

    protected UserUsageInfo(Parcel in) {
        packageName = in.readString();
        simpleName = in.readString();
        category = in.readString();
    }

    public String getCategory() {
        return category;
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
     * @return the formatted string in HH:MM:SS format
     */
    public String formatTime() {
        String formattedVal;
        long value = usage / 60000;
        int hours = (int) (value / (60) % 24);
        int minutes = (int) (value % 60);


        if (hours == 0) {
            formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
        } else {
            formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
        }
        return formattedVal;
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
        dest.writeString(category);
    }
}
