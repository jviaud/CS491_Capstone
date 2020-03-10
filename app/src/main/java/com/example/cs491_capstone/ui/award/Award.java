package com.example.cs491_capstone.ui.award;

import android.os.Parcel;
import android.os.Parcelable;

public class Award implements Parcelable {
    public static final Creator<Award> CREATOR = new Creator<Award>() {
        @Override
        public Award createFromParcel(Parcel in) {
            return new Award(in);
        }

        @Override
        public Award[] newArray(int size) {
            return new Award[size];
        }
    };
    String name, description;
    int icon;
    boolean lock;

    public Award(String name, String description, int icon, boolean lock) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.lock = lock;
    }

    protected Award(Parcel in) {
        name = in.readString();
        description = in.readString();
        icon = in.readInt();
        lock = in.readByte() != 0;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(icon);
        dest.writeByte((byte) (lock ? 1 : 0));
    }
}
