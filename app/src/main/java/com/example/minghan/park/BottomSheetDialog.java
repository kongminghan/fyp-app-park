package com.example.minghan.park;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MingHan on 5/14/2017.
 */

public class BottomSheetDialog extends BottomSheetDialogFragment{

    public static final String PAYPAL_CLIENT_ID = "AQN_w-_y3d0LCR81EHoKIKW8EHSvscIKAua-9yLbtPfWwdYeuX1uUp8V3aRVb8a1dY8gb45rNR_0gMNm";
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static final int REQUEST_CODE_PAYMENT = 1;
    public double amount;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(PAYPAL_CLIENT_ID);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        Button close = (Button)view.findViewById(R.id.btnClose);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog.this.dismiss();
            }
        });

        final RadioButton radioPaypal = (RadioButton)view.findViewById(R.id.radioPaypal);
        final RadioButton radioCC = (RadioButton)view.findViewById(R.id.radioCC);

        final Spinner spinnerAmount = (Spinner)view.findViewById(R.id.spinnerAmount);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAmount.setAdapter(adapter);

        Button btnPay = (Button)view.findViewById(R.id.btnPay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radioPaypal.isChecked()){
                    String selectedAmount = spinnerAmount.getSelectedItem().toString();
                    if(selectedAmount.equals("RM10.00")){
                        amount = 10;
                    }else if(selectedAmount.equals("RM20.00")){
                        amount = 20;
                    }else if(selectedAmount.equals("RM40.00")){
                        amount = 40;
                    }else if(selectedAmount.equals("RM60.00")){
                        amount = 60;
                    }else if(selectedAmount.equals("RM80.00")){
                        amount = 80;
                    }else if(selectedAmount.equals("RM100.00")){
                        amount = 100;
                    }
                    final PayPalPayment payment = new PayPalPayment(new BigDecimal(amount), "USD", "Wallet TopUp",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), com.paypal.android.sdk.payments.PaymentActivity.class);
                            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                            intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
                            startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                        }
                    }).start();
                }else if(radioCC.isChecked()){
                    String selectedAmount = spinnerAmount.getSelectedItem().toString();
                    if(selectedAmount.equals("RM10.00")){
                        amount = 10;
                    }else if(selectedAmount.equals("RM20.00")){
                        amount = 20;
                    }else if(selectedAmount.equals("RM40.00")){
                        amount = 40;
                    }else if(selectedAmount.equals("RM60.00")){
                        amount = 60;
                    }else if(selectedAmount.equals("RM80.00")){
                        amount = 80;
                    }else if(selectedAmount.equals("RM100.00")){
                        amount = 100;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getContext(), PaymentActivity.class)
                                    .putExtra("activity", "MainActivity").putExtra("amount", amount+""));
                        }
                    }).start();

                }else{
                    Toast.makeText(getActivity(), "Please select a payment method", Toast.LENGTH_SHORT).show();
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(getActivity(), PayPalService.class);
                intent1.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                getActivity().startService(intent1);
            }
        }).start();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("wallet")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        //Push history to FireBase
                        WalletHistory walletHistory = new WalletHistory();
                        walletHistory.dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                        walletHistory.amount = amount;
                        walletHistory.desc = "Wallet TopUp";
                        reference.child("history").push().setValue(walletHistory);

                        TextView tvAmount = (TextView) getActivity().findViewById(R.id.tvAmount);
                        float amt = Float.parseFloat(tvAmount.getText().toString());
                        amt += amount;
                        tvAmount.setText("RM"+amt+".00");
                        reference.child("balance").setValue(amt);
                    } catch (Exception e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
                BottomSheetDialog.this.dismiss();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }
}
