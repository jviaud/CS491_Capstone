package com.example.cs491_capstone.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppChangeListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_PACKAGE_ADDED:
            case Intent.ACTION_PACKAGE_REMOVED:
                generateNewAppList();
                break;
        }

    }


    private void generateNewAppList() {
        //TODO GENERATE NEW LIST OF APPS WHEN A NEW APP IS INSTALLED, THIS WAY WE DO NOT HAVE TO GENERATE IT IN ONRESUME
        Log.i("BROADCASTING", "RECEIVED");
    }


}
