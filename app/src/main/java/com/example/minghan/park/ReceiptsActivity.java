package com.example.minghan.park;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.minghan.park.Modal.Rate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;

public class ReceiptsActivity extends AppCompatActivity {

    private Query query1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RatingBar ratingBar = (RatingBar)findViewById(R.id.ratingBar);
        TextView tvDuration = (TextView)findViewById(R.id.tvRDuration);
        TextView tvCC = (TextView)findViewById(R.id.tvRCC);
        TextView tvTotal = (TextView)findViewById(R.id.tvRTotal);
        ImageView ivcc = (ImageView)findViewById(R.id.ivCC);
        TextView tvCarNum = (TextView)findViewById(R.id.tvCarNum);
        final TextView tvRRate = (TextView)findViewById(R.id.tvRRate);
        final TextView tvRRate2 = (TextView)findViewById(R.id.tvRRate2);

        final Intent intent = getIntent();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("user").child(intent.getStringExtra("carLocation")).child("rate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DecimalFormat df = new DecimalFormat("#.00");
                Rate rate = dataSnapshot.getValue(Rate.class);
                tvRRate.setText("First hour: RM"+df.format(rate.getFirstHour())+"");
                tvRRate2.setText("Subsequent hour: RM"+df.format(rate.getNextHour())+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        tvDuration.setText(intent.getStringExtra("duration"));
        tvCC.setText(intent.getStringExtra("brand") + " ends with " +intent.getStringExtra("last4"));
        tvTotal.setText("RM"+intent.getStringExtra("amount")+"0");
        tvCarNum.setText(intent.getStringExtra("carNum") + " - " + intent.getStringExtra("carLocation"));

        if(intent.getStringExtra("brand").equals("Visa")){
            ivcc.setImageResource(R.drawable.visa1);
        }else if(intent.getStringExtra("brand").equals("MasterCard")){
            ivcc.setImageResource(R.drawable.ms);
        }else if(intent.getStringExtra("brand").equals("PayPal")){
            ivcc.setImageResource(R.drawable.paypal);
            tvCC.setText("Pay using PayPal service");
        }else{
            ivcc.setImageResource(R.drawable.wallet);
            tvCC.setText("Pay using In-App Wallet");
        }

        final Query query = database.getReference("record").child(intent.getStringExtra("carNum")).child("record").limitToLast(1);

        DatabaseReference reference = database.getReference("car").child(intent.getStringExtra("carNum")).child("Status");
        reference.setValue("Paid");

        if(FirebaseAuth.getInstance().getCurrentUser()!= null){
                query1 = database.getReference("history")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .limitToLast(1);
        }

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, final float rating, boolean fromUser) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            DatabaseReference ref = postSnapshot.getRef();
                            ref.child("Rating").setValue(rating);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

                if(FirebaseAuth.getInstance().getCurrentUser()!= null) {
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                DatabaseReference ref = postSnapshot.getRef();
                                ref.child("Rating").setValue(rating);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(ReceiptsActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}
