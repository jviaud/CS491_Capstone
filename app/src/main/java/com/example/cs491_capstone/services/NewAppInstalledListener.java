package com.example.cs491_capstone.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NewAppInstalledListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message = "";
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            generateNewAppList();
        }
    }

    private void generateNewAppList() {
        //TODO GENERATE NEW LIST OF APPS WHEN A NEW APP IS INSTALLED, THIS WAY WE DO NOT HAVE TO GENERATE IT IN ONRESUME
    }


}
