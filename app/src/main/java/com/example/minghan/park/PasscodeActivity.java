package com.example.minghan.park;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobapphome.mahencryptorlib.MAHEncryptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PasscodeActivity extends AppCompatActivity {

    private TextView tvPin;
    private EditText etPin;
    private String SHAHash;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        tvPin = (TextView)findViewById(R.id.tvPin);
        etPin = (EditText)findViewById(R.id.etPin);

        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("One moment...");

        etPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        if(etPin.getText().toString().length() == 6){
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            try {
                                MAHEncryptor mahEncryptor = MAHEncryptor.newInstance(user.getUid());
                                SHAHash = mahEncryptor.encode(etPin.getText().toString());
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("wallet")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("passcode");
                                reference.setValue(SHAHash);

                                SharedPreferences sp = getSharedPreferences("parkingpasscode", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("passcode", SHAHash);
                                editor.commit();

                                Toast.makeText(PasscodeActivity.this, "PIN is added", Toast.LENGTH_SHORT).show();
                                finish();
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
                        }else if(etPin.getText().toString().length() < 6){
                            Toast.makeText(PasscodeActivity.this, "PIN must be consisted of 6 numbers", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(PasscodeActivity.this, "It can't be blank", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return;
    }
}
