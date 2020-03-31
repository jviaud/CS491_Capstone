package com.example.cs491_capstone.ui.goal;

public class Goal {
    private String id;
    private String date;
    private String type;
    private long usage;
    private int unlocks;
    private String packageName;


    public Goal(String id, String date, String type, long usage, int unlocks, String packageName) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.usage = usage;
        this.unlocks = unlocks;
        this.packageName = packageName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getUsage() {
        return usage;
    }

    public void setUsage(long usage) {
        this.usage = usage;
    }

    public int getUnlocks() {
        return unlocks;
    }

    public void setUnlocks(int unlocks) {
        this.unlocks = unlocks;
    }
}
