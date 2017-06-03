package com.example.minghan.park;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener, HistoryFragment.OnFragmentInteractionListener, WalletFragment.OnFragmentInteractionListener{

    private static final String SELECTED_ITEM = "arg_selected_item";
    private BottomNavigationView bottomNavigation;
    private int mSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MenuItem menuItem;

        bottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        if(savedInstanceState != null){
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            menuItem = bottomNavigation.getMenu().findItem(mSelectedItem);
        } else{
            menuItem = bottomNavigation.getMenu().getItem(0);
        }
        selectFragment(menuItem);
        bottomNavigation.getMenu().getItem(0).setChecked(true);

        if(getIntent()!=null){
            boolean topup = getIntent().getBooleanExtra("topup", false);
            if(topup){
                menuItem = bottomNavigation.getMenu().getItem(2);
                selectFragment(menuItem);
                bottomNavigation.getMenu().getItem(2).setChecked(true);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                startService(intent);
            }
        }).start();

//        checkPermission();
//        startAnimation();
//        fragment = new MainFragment();
//        final FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.content, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = bottomNavigation.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            selectFragment(homeItem);
            bottomNavigation.getMenu().getItem(0).setChecked(true);
        } else {
            super.onBackPressed();
        }
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                frag = new MainFragment();
                break;
            case R.id.navigation_dashboard:
                frag = new HistoryFragment();
                break;
            case R.id.navigation_notifications:
                frag = new WalletFragment();
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        for (int i = 0; i< bottomNavigation.getMenu().size(); i++) {
            MenuItem menuItem = bottomNavigation.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == mSelectedItem);
        }

        if (frag != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, frag)
                    .commit();
        }
    }


//    private void checkPermission() {
//    // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(MainActivity.this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED &&
//        ContextCompat.checkSelfPermission(MainActivity.this,
//                android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    1);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    return;
//                } else {
//                    finish();
//                }
//                return;
//            }
//        }
//    }

    @Override
    public void onFragmentInteraction(Uri uri) { }

//    private void startAnimation(){
//        ImageView ivCloud = (ImageView)findViewById(R.id.ivCloud);
//        AnimatorSet cloudSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.cloud_move);
//        cloudSet.setTarget(ivCloud);
//        cloudSet.start();
//
//        ImageView ivCloud2 = (ImageView)findViewById(R.id.ivCloud2);
//        AnimatorSet cloudSet2 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.cloud2_move);
//        cloudSet2.setTarget(ivCloud2);
//        cloudSet2.start();
//
//        ImageView ivCar = (ImageView)findViewById(R.id.ivCar);
//        AnimatorSet carSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.car_move);
//        carSet.setTarget(ivCar);
//        carSet.start();
//    }

    private int getColorFromRes(@ColorRes int resId) {
        return ContextCompat.getColor(this, resId);
    }
}
