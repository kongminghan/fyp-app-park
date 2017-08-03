package com.example.minghan.park;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class CountDownActivity extends AppCompatActivity {

    private TextView tvSeconds, tvMins, tvAlert, tvRemind;
    private String carNum, carLocation, time, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        Bundle bundle = getIntent().getExtras();

        time = bundle.getString("carEntTime");
        date = bundle.getString("carEntDate");
        carNum = bundle.getString("carNum");
        carLocation = bundle.getString("carLocation");

        tvSeconds = (TextView)findViewById(R.id.txtTimeS);
        tvMins = (TextView)findViewById(R.id.txtTimeM);
        tvAlert = (TextView) findViewById(R.id.tvAlert);
        tvRemind = (TextView) findViewById(R.id.tvRemind);
        final FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabPay);
        fab.setVisibility(View.GONE);

        try{
//            long diff = Long.parseLong(intent.getStringExtra("time"));
            long a = bundle.getLong("DIFF_TIME");
            if(a>0){
                long left = (20*60*1000)-a;
                tvRemind.setVisibility(View.VISIBLE);
                new CountDownTimer(left, 1000) {
                    public void onTick(long millisUntilFinished) {
                        tvMins.setText(""+TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60);
                        tvSeconds.setText(""+TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                    }

                    public void onFinish() {
                        tvSeconds.setText("00");
                        tvAlert.setVisibility(View.VISIBLE);
                        tvRemind.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    }
                }.start();
            }else{
                tvAlert.setVisibility(View.VISIBLE);
                tvRemind.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
//            Toast.makeText(this, intent.getStringExtra("time"), Toast.LENGTH_SHORT).show();
//            new CountDownTimer(120000, 1000) {
//
//                public void onTick(long millisUntilFinished) {
//                    tvMins.setText(""+TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60);
//                    tvSeconds.setText(""+TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
//                }
//
//                public void onFinish() {
//                    tvSeconds.setText("00");
//                }
//            }.start();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CountDownActivity.this, DurationActivity.class);
                intent.putExtra("carNum", carNum);
                intent.putExtra("carEntTime", time);
                intent.putExtra("carEntDate", date);
                intent.putExtra("carLocation", carLocation);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();
            }
        });
    }
}
