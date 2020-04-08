package com.example.cs491_capstone.ui.goal;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.cs491_capstone.App;

public class Goal implements Parcelable, Comparable<Goal> {
    public static final Creator<Goal> CREATOR = new Creator<Goal>() {
        @Override
        public Goal createFromParcel(Parcel in) {
            return new Goal(in);
        }

        @Override
        public Goal[] newArray(int size) {
            return new Goal[size];
        }
    };
    private String id;
    private String date;
    private String type;
    private long usage;
    private int unlocks;
    private String packageName;
    private boolean expanded;

    public Goal(String id, String date, String type, long usage, int unlocks, String packageName) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.usage = usage;
        this.unlocks = unlocks;
        this.packageName = packageName;
        expanded = false;
    }

    protected Goal(Parcel in) {
        id = in.readString();
        date = in.readString();
        type = in.readString();
        usage = in.readLong();
        unlocks = in.readInt();
        packageName = in.readString();
        expanded = in.readByte() != 0;
    }

    public boolean isPassed() {
        return App.goalDataBase.getStatus(id);
    }

    public void setPassed(boolean passed) {
        App.goalDataBase.setStatus(id, passed ? "0" : "1");
    }

    public boolean isUsagePassed() {
        return App.goalDataBase.getUsageStatus(id);
    }

    public void setUsagePassed(boolean passed) {
        App.goalDataBase.setUsageStatus(id, passed ? "0" : "1");
    }

    public boolean isUnlockPassed() {
        return App.goalDataBase.getUnlockStatus(id);
    }

    public void setUnlockPassed(boolean passed) {
        App.goalDataBase.setUnlockStatus(id, passed ? "0" : "1");
    }


    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
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

    @Override
    public int compareTo(Goal o) {
        Integer a = Integer.parseInt(o.getId());
        Integer b = Integer.parseInt(getId());
        return a.compareTo(b);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(date);
        dest.writeString(type);
        dest.writeLong(usage);
        dest.writeInt(unlocks);
        dest.writeString(packageName);
        dest.writeByte((byte) (expanded ? 1 : 0));
    }
}
