package com.example.cs491_capstone;

import android.graphics.drawable.Drawable;

public class InstalledAppInfo {
    private String packageName;
    private String simpleName;
    private Drawable icon;

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

    public String getPackageName() { return packageName;}
    ///

    ///SETTERS
    ///

}
