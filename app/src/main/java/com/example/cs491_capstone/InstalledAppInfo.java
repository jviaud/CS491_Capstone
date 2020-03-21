package com.example.cs491_capstone;

import android.graphics.drawable.Drawable;


public class InstalledAppInfo implements Comparable<InstalledAppInfo> {
    private String packageName;
    private String simpleName;
    private Drawable icon;
    private boolean isTracked;

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



    ///

    ///SETTERS
    ///

}
