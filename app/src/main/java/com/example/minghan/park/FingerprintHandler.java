package com.example.minghan.park;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minghan.park.Modal.History;
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

import org.json.JSONException;

import java.util.HashMap;

/**
 * Created by MingHan on 5/14/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private Context context;
    private History history;
    private double balance;

    // Constructor
    public FingerprintHandler(Context mContext, History history) {
        context = mContext;
        this.history = history;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        this.update("Fingerprint Authentication error\n" + errString, false);
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        this.update("Fingerprint Authentication help\n" + helpString, false);
    }


    @Override
    public void onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false);
    }


    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("Fingerprint Authentication succeeded.", true);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference checkWallet = database.getReference("wallet").child(user.getUid()).child("balance");
        checkWallet.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                balance = dataSnapshot.getValue(Double.class);
                if (history.Payment < balance) {
                    HashMap<String, Object> totalAmount = new HashMap<>();
                    totalAmount.put("amount", history.Payment);
                    totalAmount.put("brand", history.Brand);
                    totalAmount.put("timestamp", ServerValue.TIMESTAMP);
                    totalAmount.put("carNumber", history.CarNumber);
                    database.getReference("stat").child(history.CarLocation).push().setValue(totalAmount);

                    final Query query = database.getReference("record").child(history.CarNumber).child("record").limitToLast(1);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                // TODO: handle the post
                                DatabaseReference ref = postSnapshot.getRef();
                                ref.child("Status").setValue("Paid");
                                ref.child("Payment").setValue(history.Payment);
                                ref.child("ExtTime").setValue(history.ExtTime);
                                ref.child("ExtDate").setValue(history.ExtDate);
                                ref.child("Brand").setValue(history.Brand);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    DatabaseReference reference = database.getReference("history").child(user.getUid());
                    reference.push().setValue(history);

                    final WalletHistory walletHistory = new WalletHistory();
                    walletHistory.amount = history.Payment;
                    walletHistory.dateTime = history.ExtDate + " " + history.ExtTime;
                    walletHistory.desc = "Parking fees";
                    DatabaseReference reference1 = database.getReference("wallet").child(user.getUid());
                    reference1.child("history").push().setValue(walletHistory);
                    reference1.child("balance").setValue(balance - walletHistory.amount);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(context, ReceiptsActivity.class);
                            intent.putExtra("amount", walletHistory.amount + "");
                            intent.putExtra("duration", history.Duration);
                            intent.putExtra("last4", history.last4);
                            intent.putExtra("brand", history.Brand);
                            intent.putExtra("carNum", history.CarNumber);
                            intent.putExtra("carLocation",history.CarLocation);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        }
                    }).start();
                } else {
                    Toast.makeText(context, "You have insufficient balance. Please topup and try again.", Toast.LENGTH_SHORT).show();
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void update(String e, Boolean success) {
        TextView textView = (TextView) ((Activity) context).findViewById(R.id.errorText);
        textView.setText(e);
        if (success) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }
}
