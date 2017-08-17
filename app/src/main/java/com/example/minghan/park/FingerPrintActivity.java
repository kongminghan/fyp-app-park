package com.example.minghan.park;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.mobapphome.mahencryptorlib.MAHEncryptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerPrintActivity extends AppCompatActivity {
    private KeyStore keyStore;
    private static final String KEY_NAME = "parkingapppsm";
    private Cipher cipher;
    private TextView textView;
    public String amount, duration, carNum, extTime, extDate, entDate, entTime;
    private History history;
    private EditText etPin;
    private RelativeLayout relativeLay1, relativeLay2;
    private ProgressDialog progressDialog;
    private String SHAHash;
    private double balance;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        relativeLay1 = (RelativeLayout)findViewById(R.id.relativeLay1);
        relativeLay2 = (RelativeLayout)findViewById(R.id.relativeLay2);
        etPin = (EditText)findViewById(R.id.etPin);
        progressDialog = new ProgressDialog(FingerPrintActivity.this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                amount = intent.getStringExtra("amount");
                duration = intent.getStringExtra("duration");
                carNum = intent.getStringExtra("carNum");
                extTime = intent.getStringExtra("extTime");
                extDate = intent.getStringExtra("extDate");
                entDate = intent.getStringExtra("entDate");
                entTime = intent.getStringExtra("entTime");

                //Push history to firebase
                history = new History();
                history.CarNumber = carNum;
                history.Payment = Double.parseDouble(amount);
                history.ExtTime = extTime;
                history.ExtDate = extDate;
                history.EntDate = entDate;
                history.EntTime = entTime;
                history.Brand = "In-App Wallet";
                history.last4 = "";
                history.CarLocation = intent.getStringExtra("carLocation");
                history.Duration = duration;
            }
        }).start();

        setup();

        etPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        if(etPin.getText().toString().length()==6){
                            progressDialog.setMessage("Authenticating...");
                            progressDialog.show();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("wallet")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("passcode");
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String passcode = dataSnapshot.getValue(String.class);
                                    try {
                                        MAHEncryptor mahEncryptor = MAHEncryptor.newInstance(user.getUid());
                                        SHAHash = mahEncryptor.encode(etPin.getText().toString());
                                        if(passcode.equals(SHAHash)){
                                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference checkWallet = database.getReference("wallet").child(user.getUid()).child("balance");
                                            checkWallet.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    balance = dataSnapshot.getValue(Double.class);
                                                    if(history.Payment < balance){
                                                        HashMap<String, Object> totalAmount = new HashMap<>();
                                                        totalAmount.put("amount", history.Payment);
                                                        totalAmount.put("brand", history.Brand);
                                                        totalAmount.put("carNumber", history.CarNumber);
                                                        totalAmount.put("timestamp", ServerValue.TIMESTAMP);
                                                        database.getReference("stat").push().setValue(totalAmount);

                                                        final Query query = database.getReference("record").child(history.CarNumber).child("record").limitToLast(1);
                                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
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
                                                            public void onCancelled(DatabaseError databaseError) {}
                                                        });

                                                        DatabaseReference reference = database.getReference("history").child(user.getUid());
                                                        reference.push().setValue(history);

                                                        WalletHistory walletHistory = new WalletHistory();
                                                        walletHistory.amount = history.Payment;
                                                        walletHistory.dateTime = history.ExtDate + " " +history.ExtTime;
                                                        walletHistory.desc = "Parking fees";
                                                        DatabaseReference reference1 = database.getReference("wallet").child(user.getUid());
                                                        reference1.child("history").push().setValue(walletHistory);
                                                        reference1.child("balance").setValue(balance-walletHistory.amount);

                                                        progressDialog.dismiss();

                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(FingerPrintActivity.this, ReceiptsActivity.class);
                                                                intent.putExtra("amount", history.Payment+"");
                                                                intent.putExtra("duration", history.Duration);
                                                                intent.putExtra("last4", history.last4);
                                                                intent.putExtra("brand", history.Brand);
                                                                intent.putExtra("carNum", history.CarNumber);
                                                                intent.putExtra("carLocation", history.CarLocation);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }).start();
                                                    }else{
                                                        progressDialog.dismiss();
                                                        Toast.makeText(FingerPrintActivity.this, "You have insufficient balance. Please topup and try again.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {}
                                            });
                                        }else{
                                            progressDialog.dismiss();
                                            Toast.makeText(FingerPrintActivity.this, "You have entered wrong passcode. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (InvalidKeySpecException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeyException e) {
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    } catch (BadPaddingException e) {
                                        e.printStackTrace();
                                    } catch (IllegalBlockSizeException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchPaddingException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }else{
                            Toast.makeText(FingerPrintActivity.this, "PIN can't be left blank.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setup(){
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);

        textView = (TextView)findViewById(R.id.errorText);
        if(!fingerprintManager.isHardwareDetected()){
            SharedPreferences sp = getSharedPreferences("parkingpasscode", MODE_PRIVATE);
            String passcode = sp.getString("passcode", "");
            if(passcode.equals("")){
                startActivity(new Intent(FingerPrintActivity.this, PasscodeActivity.class));
                Toast.makeText(this, "You have to set your PIN first. ", Toast.LENGTH_SHORT).show();
            }
            else{
                relativeLay1.setVisibility(View.GONE);
                relativeLay2.setVisibility(View.VISIBLE);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }else{
            relativeLay1.setVisibility(View.VISIBLE);
            relativeLay2.setVisibility(View.GONE);
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                textView.setText("Fingerprint authentication permission not enabled");
            }else{
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    textView.setText("Register at least one fingerprint in Settings");
                }else{
                    // Checks whether lock screen security is enabled or not
                    if (!keyguardManager.isKeyguardSecure()) {
                        textView.setText("Lock screen security not enabled in Settings");
                    }else{
                        generateKey();
                        if (cipherInit()) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            FingerprintHandler helper = new FingerprintHandler(this, history);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }


        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }


        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
