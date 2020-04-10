package com.example.cs491_capstone;

import android.graphics.drawable.Drawable;


public class InstalledAppInfo implements Comparable<InstalledAppInfo> {
    private String packageName;
    private String simpleName;
    private Drawable icon;
    private boolean isTracked;
    private long time_limit_milliseconds;

    ///CONSTRUCTORS
    public InstalledAppInfo(String packageName, String simpleName, Drawable icon) {
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.icon = icon;
    }
    ///

    ///GETTERS
    public String getSimpleName() {
        return simpleName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isTracked() {
        return isTracked;
    }

    public InstalledAppInfo setTracked(boolean tracked) {
        isTracked = tracked;
        return this;
    }

    @Override
    public int compareTo(InstalledAppInfo o) {
        return o.getSimpleName().compareTo(getSimpleName());
    }
    public void setTimeLimitMilliseconds(long ms)
    {
        this.time_limit_milliseconds = ms;
    }

    public String getTimeLimitString()
    {
        String hours = "";
        String minutes = "";
        String time = "";
        long minutes_remainder = time_limit_milliseconds%3600000;
        hours = Integer.toString((int)(time_limit_milliseconds/3600000));
        minutes = Integer.toString((int)(minutes_remainder/60000));
        time = hours + "hr " + minutes + "m";
        return time;
    }

}
