package com.example.cs491_capstone.ui.goal;

public class Goal {
    private String date;
    private String type;
    private long usage;
    private int unlocks;

    public Goal(String date, String type, long usage) {
        this.date = date;
        this.type = type;
        this.usage = usage;
    }

    public Goal(String date, String type, int unlocks) {
        this.date = date;
        this.type = type;
        this.unlocks = unlocks;
    }

    public Goal(String date, String type, long usage, int unlocks) {
        this.date = date;
        this.type = type;
        this.usage = usage;
        this.unlocks = unlocks;
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
