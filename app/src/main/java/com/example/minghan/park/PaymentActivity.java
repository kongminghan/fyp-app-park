package com.example.minghan.park;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.minghan.park.Modal.History;
import com.example.minghan.park.Modal.Record;
import com.example.minghan.park.Modal.WalletHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator;

public class PaymentActivity extends AppCompatActivity {

    private ViewPagerHeightWrapper mViewPager;

    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;

    private boolean mShowingFragments = false;
    ImageView ivLogo;
    TextView tvCCNum, tvCCExpiry, tvCCName, tvCCV;
    Button btnCheckout;
    String amount, duration, carNum, extTime, extDate, entDate, entTime;
    private boolean topup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        if(!intent.getStringExtra("activity").equals("MainActivity")){
            amount = intent.getStringExtra("amount");
            duration = intent.getStringExtra("duration");
            carNum = intent.getStringExtra("carNum");
            extTime = intent.getStringExtra("extTime");
            extDate = intent.getStringExtra("extDate");
            entDate = intent.getStringExtra("entDate");
            entTime = intent.getStringExtra("entTime");
        }else{
            topup = true;
            amount = intent.getStringExtra("amount");
            Toast.makeText(this, amount, Toast.LENGTH_SHORT).show();
        }

        mViewPager = (ViewPagerHeightWrapper)findViewById(R.id.viewPager);
        tvCCExpiry = (TextView)findViewById(R.id.tvCCExpiry);
        tvCCName = (TextView)findViewById(R.id.tvCCName);
        tvCCNum = (TextView)findViewById(R.id.tvCCNum);
        ivLogo = (ImageView)findViewById(R.id.ivLogo);
        final CircleIndicator indicator = (CircleIndicator)findViewById(R.id.indicator);

        mCardAdapter = new CardPagerAdapter(tvCCNum, tvCCName, tvCCExpiry, ivLogo, mViewPager);
        mCardAdapter.addCardItem(new CardItem("Card Number"));
        mCardAdapter.addCardItem(new CardItem("Name on Card"));
        mCardAdapter.addCardItem(new CardItem("Expiry (month year) eg. 0218"));
        mCardAdapter.addCardItem(new CardItem("CVV"));

//        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
//        mViewPager.setClipToPadding(false);
//        mViewPager.setPadding(10, 20, 10, 0);
        mViewPager.setPageMargin(-80);
        mViewPager.setAdapter(mCardAdapter);

//        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);
        indicator.setViewPager(mViewPager);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ocr.ttf");
        tvCCExpiry.setTypeface(typeface);
        tvCCName.setTypeface(typeface);
        tvCCNum.setTypeface(typeface);

        btnCheckout = (Button)findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View vp = mViewPager.getChildAt(0);
                EditText etCC = (EditText)vp.findViewById(R.id.etCC);
                String cc = etCC.getText().toString().replace(" ", "");

                vp = mViewPager.getChildAt(1);
                etCC = (EditText)vp.findViewById(R.id.etCC);
                String name = etCC.getText().toString();
//                vp = mViewPager.getChildAt(2);
//                etCC = (EditText)vp.findViewById(R.id.etCC);
//                String expiry = etCC.getText().toString();
                vp = mViewPager.getChildAt(3);
                etCC = (EditText)vp.findViewById(R.id.etCC);
                String cvv = etCC.getText().toString();

//                if(cc.length() < 16 || name.length() < 2 || expiry == )
//                Toast.makeText(PaymentActivity.this, String.valueOf(tvCCNum.getText().toString().length()), Toast.LENGTH_SHORT).show();

                if(cc.length() <16 || name.length() < 3 || tvCCExpiry.length() < 5
                        || cvv.length() < 3){
                    Toast.makeText(PaymentActivity.this, "Invalid. Please correct your card details.", Toast.LENGTH_SHORT).show();
                }
                else{
                    final Dialog dialog = new Dialog(PaymentActivity.this, R.style.AppTheme);
                    dialog.setContentView(R.layout.payment_loading);
                    ImageView iv = (ImageView)dialog.findViewById(R.id.ivHand);
                    AnimatorSet hand = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.hand);
                    hand.setTarget(iv);
                    hand.start();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

//                Card card = new Card("4242424242424242", 12, 2018, "123");
                    Card card = new Card("4242424242424242", 12, 2018, "123");
                    //Remember to validate the card object before you use it to save time.
                    if (card.validateCard()) {
                        Stripe stripe = null;
                        try {
                            stripe = new Stripe(PaymentActivity.this, "pk_test_nqVGuzwudJ4zg5bgWT4NyUp5");
                            dialog.show();
                        } catch (AuthenticationException e) {
//                        dialog.dismiss();
                            e.printStackTrace();
                        }
                        stripe.createToken(
                                card,
                                new TokenCallback() {
                                    public void onSuccess(final Token token) {

                                        final StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://boiling-river-96141.herokuapp.com/park.php",
                                                new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        dialog.dismiss();
                                                        try {
                                                            final JSONObject json = new JSONObject(response);
                                                            Toast.makeText(PaymentActivity.this,json.getString("status"),Toast.LENGTH_LONG).show();

                                                            if(!topup){
                                                                FirebaseDatabase database = FirebaseDatabase.getInstance();

                                                                HashMap<String, Object> totalAmount = new HashMap<>();
                                                                totalAmount.put("amount", Double.parseDouble(amount));
                                                                totalAmount.put("brand", json.getString("brand"));
                                                                totalAmount.put("timestamp", ServerValue.TIMESTAMP);
                                                                totalAmount.put("carNumber", carNum);
                                                                database.getReference("stat").push().setValue(totalAmount);

                                                                final Query query = database.getReference("record").child(carNum).child("record").limitToLast(1);
                                                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                                                            // TODO: handle the post
                                                                            DatabaseReference ref = postSnapshot.getRef();
                                                                            ref.child("Status").setValue("Paid");
                                                                            ref.child("Payment").setValue(Double.parseDouble(amount));
                                                                            ref.child("ExtTime").setValue(extTime);
                                                                            ref.child("ExtDate").setValue(extDate);
                                                                            try {
                                                                                ref.child("Brand").setValue(json.getString("brand"));
                                                                                ref.child("Duration").setValue(duration);
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {}
                                                                });

                                                                //Push history to firebase
                                                                History history = new History();
                                                                history.CarNumber = carNum;
                                                                history.Payment = Double.parseDouble(amount);
                                                                history.ExtTime = extTime;
                                                                history.ExtDate = extDate;
                                                                history.EntDate = entDate;
                                                                history.EntTime = entTime;
                                                                history.Brand = json.getString("brand");
                                                                history.last4 = json.getString("last4");
                                                                history.Duration = duration;
                                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                if(user!=null){
                                                                    FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                                                    DatabaseReference reference = database1.getReference("history").child(user.getUid());
                                                                    reference.push().setValue(history);
                                                                }else{
                                                                    CarDB carDB = new CarDB(getApplicationContext());
                                                                    carDB.insertHistory(history);
                                                                }

                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            Intent intent = new Intent(PaymentActivity.this, ReceiptsActivity.class);
                                                                            intent.putExtra("amount", amount);
                                                                            intent.putExtra("duration", duration);
                                                                            intent.putExtra("last4", json.getString("last4"));
                                                                            intent.putExtra("brand", json.getString("brand"));
                                                                            intent.putExtra("carNum", carNum);
                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }).start();
                                                            }else{
                                                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                final DatabaseReference reference = database.getReference("wallet")
                                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                                                //Push history to FireBase
                                                                WalletHistory walletHistory = new WalletHistory();
                                                                walletHistory.dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                                                                walletHistory.amount = Double.parseDouble(amount);
                                                                walletHistory.desc = "Wallet TopUp";
                                                                reference.child("history").push().setValue(walletHistory);

                                                                reference.child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        try{
                                                                            double amt = dataSnapshot.getValue(Double.class);
                                                                            amt += Double.parseDouble(amount);
                                                                            reference.child("balance").setValue(amt);
                                                                        }catch (Exception e){
                                                                            reference.child("balance").setValue(Double.parseDouble(amount));
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });

                                                                startActivity(new Intent(PaymentActivity.this, MainActivity.class).putExtra("topup", true));
                                                                finish();
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                },
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        dialog.dismiss();
                                                        error.printStackTrace();
                                                        Toast.makeText(PaymentActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
//                                                        Intent intent = new Intent(PaymentActivity.this, ReceiptsActivity.class);
//                                                        intent.putExtra("amount", amount);
//                                                        intent.putExtra("duration", duration);
//                                                        startActivity(intent);
                                                    }
                                                }){
                                            @Override
                                            protected Map<String,String> getParams(){
                                                Map<String,String> params = new HashMap<String, String>();
                                                params.put("stripeToken",token.getId().toString());
                                                params.put("amount", String.valueOf(Math.round(Float.parseFloat(amount))*100));
                                                return params;
                                            }
//                                            @Override
//                                            public Map<String, String> getHeaders(){
//                                                HashMap<String, String> params = new HashMap<String, String>();
//                                                String creds = String.format("%s:%s","USERNAME","PASSWORD");
//                                                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
//                                                params.put("Authorization", auth);
//                                                return params;
//                                            }
                                        };
                                        RequestQueue requestQueue = Volley.newRequestQueue(PaymentActivity.this);
                                        stringRequest.setRetryPolicy(
                                                new DefaultRetryPolicy(8000,
                                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                                                ));
                                        requestQueue.add(stringRequest);
                                    }
                                    public void onError(Exception error) {
                                        dialog.dismiss();
                                        // Show localized error message
                                        Toast.makeText(PaymentActivity.this,
                                                error.getLocalizedMessage().toString(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                        );
                    }else{
                        dialog.dismiss();
                        Toast.makeText(PaymentActivity.this, "Invalid. Please correct your card details.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
