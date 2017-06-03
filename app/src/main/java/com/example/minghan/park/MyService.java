package com.example.minghan.park;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by MingHan on 5/23/2017.
 */

public class MyService extends Service{
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this,"Service Started...",Toast.LENGTH_LONG).show();
        FirebaseMessaging.getInstance();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"Service Stoped...",Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
