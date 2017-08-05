package com.example.minghan.park;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minghan.park.Modal.History;
import com.example.minghan.park.Modal.Rate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import com.skyfishjy.library.RippleBackground;


import org.apache.commons.net.time.TimeTCPClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DurationActivity extends AppCompatActivity implements BottomSheetInterface {

    public static final String PAYPAL_CLIENT_ID = "AQN_w-_y3d0LCR81EHoKIKW8EHSvscIKAua-9yLbtPfWwdYeuX1uUp8V3aRVb8a1dY8gb45rNR_0gMNm";
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static final int REQUEST_CODE_PAYMENT = 1;
    String paymentAmount;
    String extTime, extDate;
    String time, date;
    private BottomSheetPayment bottomSheetPayment;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(PAYPAL_CLIENT_ID);

    long MillisecondTime, StartTime = 0L;
    Handler handler;
    int Seconds, Minutes, Hour;
    TextView txtTimeS, txtTimeM, txtTimeH, tvAmount, tvLocation;
    String carNum, carLocation;
    Rate rate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration);

        Intent intent = getIntent();
        time = intent.getStringExtra("carEntTime");
        date = intent.getStringExtra("carEntDate");
        carNum = intent.getStringExtra("carNum");
        carLocation = intent.getStringExtra("carLocation");

        txtTimeS = (TextView) findViewById(R.id.txtTimeS);
        txtTimeM = (TextView) findViewById(R.id.txtTimeM);
        txtTimeH = (TextView) findViewById(R.id.txtTimeH);
        tvAmount = (TextView) findViewById(R.id.tvAmount);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvLocation.setText("You've parked you car at "+carLocation);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("user").child(carLocation).child("rate");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rate = new Rate();
                rate = dataSnapshot.getValue(Rate.class);

                try {
                    SimpleDateFormat se = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    Date date1 = new Date();
                    date1 = se.parse(date + " " + time);
                    cal.setTime(date1);
                    StartTime = cal.getTimeInMillis();

                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MillisecondTime = System.currentTimeMillis() - StartTime;
                            Seconds = (int) (MillisecondTime / 1000);
                            Hour = Seconds / 3600;
                            Minutes = Seconds % 3600;
                            if(Minutes <= 60){
                                Minutes = 0;
                            }
                            if(Minutes > 60){
                                Minutes = Minutes / 60;
                            }
                            Seconds = Seconds % 60;

                            txtTimeH.setText(String.valueOf(Hour));
                            txtTimeM.setText(String.valueOf(Minutes));
                            txtTimeS.setText(String.format("%02d", Seconds));

                            handler.postDelayed(this, 0);

                            if(Hour <=1){
                                paymentAmount = String.valueOf((rate.firstHour * 1));
                            }else{
                                paymentAmount = String.valueOf((rate.nextHour * (Hour)) + rate.firstHour);
                            }
                            tvAmount.setText("RM"+paymentAmount+"0 only");
                        }
                    }, 0);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(DurationActivity.this, PayPalService.class);
                intent1.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                startService(intent1);
            }
        }).start();

        Button btnPay = (Button) findViewById(R.id.btnPay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetPayment = new BottomSheetPayment();
                bottomSheetPayment.show(getSupportFragmentManager(), "Dialog");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        extDate = sdf.format(new Date());
                        extTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

                        HashMap<String, Object> totalAmount = new HashMap<>();
                        totalAmount.put("amount", Double.parseDouble(paymentAmount));
                        totalAmount.put("brand", "PayPal");
                        totalAmount.put("timestamp", ServerValue.TIMESTAMP);
                        totalAmount.put("carNumber", carNum);
                        database.getReference("stat").child(carLocation).push().setValue(totalAmount);

                        Query query = database.getReference("record").child(carNum).child("record").limitToLast(1);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    // TODO: handle the post
                                    DatabaseReference ref = postSnapshot.getRef();
                                    ref.child("Status").setValue("Paid");
                                    ref.child("Payment").setValue(Double.parseDouble(paymentAmount));
                                    ref.child("ExtDate").setValue(extDate);
                                    ref.child("ExtTime").setValue(extTime);
                                    ref.child("Brand").setValue("PayPal");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        DatabaseReference reference = database.getReference("car").child(carNum).child("Status");
                        reference.setValue("Paid");

                        //Push history to firebase or sqlite
                        History history = new History();
                        history.CarNumber = carNum;
                        history.Payment = Double.parseDouble(paymentAmount);
                        history.ExtTime = extTime;
                        history.ExtDate = extDate;
                        history.EntDate = date;
                        history.EntTime = time;
                        history.Brand = "PayPal";
                        history.last4 = "";
                        history.CarLocation = carLocation;
                        history.Duration = txtTimeH.getText().toString() + "h " + txtTimeM.getText().toString() + "m";
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                            DatabaseReference reference2 = database1.getReference("history").child(user.getUid());
                            reference2.push().setValue(history);
                        } else {
                            CarDB carDB = new CarDB(getApplicationContext());
                            carDB.insertHistory(history);
                        }

                        //Starting a new activity for the payment details and also putting the payment details with intent
                        startActivity(new Intent(this, ReceiptsActivity.class)
                                .putExtra("brand", "PayPal")
                                .putExtra("last4", " ")
                                .putExtra("duration", txtTimeH.getText().toString() + "h " + txtTimeM.getText().toString() + "m")
                                .putExtra("carNum", carNum)
                                .putExtra("carLocation", carLocation)
                                .putExtra("amount", paymentAmount));
                        finish();

                    } catch (Exception e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    public void onLayoutCreated(View view) {
        LinearLayout layCC = (LinearLayout)view.findViewById(R.id.layCC);
        LinearLayout layPayPal = (LinearLayout)view.findViewById(R.id.layPayPal);
        final LinearLayout layWallet = (LinearLayout)view.findViewById(R.id.layWallet);
        Button btnPayMethod = (Button)view.findViewById(R.id.btnPayMethod);

        if(Double.parseDouble(paymentAmount) == 0){
            layWallet.setVisibility(View.GONE);
            layCC.setVisibility(View.GONE);
            layPayPal.setVisibility(View.GONE);

            btnPayMethod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    extDate = sdf.format(new Date());
                    extTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

                    HashMap<String, Object> totalAmount = new HashMap<>();
                    totalAmount.put("amount", Double.parseDouble(paymentAmount));
                    totalAmount.put("brand", "PayPal");
                    totalAmount.put("timestamp", ServerValue.TIMESTAMP);
                    totalAmount.put("carNumber", carNum);
                    database.getReference("stat").push().setValue(totalAmount);

                    Query query = database.getReference("record").child(carNum).child("record").limitToLast(1);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                // TODO: handle the post
                                DatabaseReference ref = postSnapshot.getRef();
                                ref.child("Status").setValue("Paid");
                                ref.child("Payment").setValue(Double.parseDouble(paymentAmount));
                                ref.child("ExtDate").setValue(extDate);
                                ref.child("ExtTime").setValue(extTime);
                                ref.child("Brand").setValue("PayPal");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    DatabaseReference reference = database.getReference("car").child(carNum).child("Status");
                    reference.setValue("Paid");

                    //Push history to firebase or sqlite
                    History history = new History();
                    history.CarNumber = carNum;
                    history.Payment = Double.parseDouble(paymentAmount);
                    history.ExtTime = extTime;
                    history.ExtDate = extDate;
                    history.EntDate = date;
                    history.EntTime = time;
                    history.Brand = "FOC";
                    history.last4 = "";
                    history.CarLocation = carLocation;
                    history.Duration = txtTimeH.getText().toString() + "h " + txtTimeM.getText().toString() + "m";
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                        DatabaseReference reference2 = database1.getReference("history").child(user.getUid());
                        reference2.push().setValue(history);
                    } else {
                        CarDB carDB = new CarDB(getApplicationContext());
                        carDB.insertHistory(history);
                    }

                    startActivity(new Intent(DurationActivity.this, ReceiptsActivity.class)
                            .putExtra("brand", "Free Of Charge")
                            .putExtra("last4", " ")
                            .putExtra("duration", txtTimeH.getText().toString() + "h " + txtTimeM.getText().toString() + "m")
                            .putExtra("carNum", carNum)
                            .putExtra("amount", paymentAmount));
                    finish();
                }
            });

        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(FirebaseAuth.getInstance().getCurrentUser() != null){
                        layWallet.setVisibility(View.VISIBLE);
                    }
                }
            }).start();

            layCC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPaymentActivity(PaymentActivity.class);
                }
            });

            layPayPal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    extDate = sdf.format(new Date());
                    extTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

                    PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), "USD", "Parking Fee",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), com.paypal.android.sdk.payments.PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
                    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                    bottomSheetPayment.dismiss();
                }
            });

            layWallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPaymentActivity(FingerPrintActivity.class);
                }
            });
        }
    }

    private void goToPaymentActivity(final Class activityClass){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        extDate = sdf.format(new Date());
        extTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent ccIntent = new Intent(getApplicationContext(), activityClass);
                ccIntent.putExtra("amount", paymentAmount);
                ccIntent.putExtra("duration", txtTimeH.getText().toString()+ "h "+txtTimeM.getText().toString()+"m");
                ccIntent.putExtra("carNum", carNum);
                ccIntent.putExtra("extTime", extTime);
                ccIntent.putExtra("extDate", extDate);
                ccIntent.putExtra("entDate", date);
                ccIntent.putExtra("entTime", time);
                ccIntent.putExtra("carLocation", carLocation);
                ccIntent.putExtra("activity", "DurationActivity");
                startActivity(ccIntent);
                bottomSheetPayment.dismiss();
            }
        }).start();
    }
}
