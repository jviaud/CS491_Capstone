package com.example.cs491_capstone.ui.award;

public class Award {
    String name, description;
    int icon;
    boolean lock;


    public Award() {
    }

    public Award(String name, String description, int icon,boolean lock) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.lock = lock;
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
}
