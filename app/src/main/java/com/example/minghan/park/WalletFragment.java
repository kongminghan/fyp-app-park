package com.example.minghan.park;

import android.*;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minghan.park.Modal.WalletHistory;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class WalletFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    private int RC_SIGN_IN = 0;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private ConstraintLayout ly;
    private LinearLayout ly1;
    private OnFragmentInteractionListener mListener;
    private Button logout, addMoney;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView tvAmount;
    private ProgressBar progressBar;
    private RecyclerView rvTransaction;
    private ArrayList<WalletHistory> wallets;
    private LinearLayout layEmpty;

    public WalletFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WalletFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WalletFragment newInstance(String param1, String param2) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("834361452061-pou92n14as6o89gma1lbdoaffjj2uncf.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onPause() {
        super.onPause();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.stopAutoManage(getActivity());
            googleApiClient.disconnect();
        }
    }

    public void updateUI(boolean isConnected, FirebaseUser user) {
        if (isConnected == true) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(10);

            ly.setVisibility(View.GONE);
            ly1.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);

            final DatabaseReference database = FirebaseDatabase.getInstance()
                    .getReference("wallet")
                    .child(user.getUid());

            FingerprintManager fingerprintManager = (FingerprintManager) getActivity().getSystemService(FINGERPRINT_SERVICE);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!fingerprintManager.isHardwareDetected()){
                    SharedPreferences sp = getActivity().getSharedPreferences("parkingpasscode", MODE_PRIVATE);
                    String passcode = sp.getString("passcode", "");
                    if(passcode.equals(""))
                        startActivity(new Intent(getActivity(), PasscodeActivity.class));
                }
            }else{
                SharedPreferences sp = getActivity().getSharedPreferences("parkingpasscode", MODE_PRIVATE);
                String passcode = sp.getString("passcode", "");
                if(passcode.equals(""))
                    startActivity(new Intent(getActivity(), PasscodeActivity.class));
                startActivity(new Intent(getActivity(), PasscodeActivity.class));
            }

            database.child("balance").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        double amount = dataSnapshot.getValue(Double.class);
                        tvAmount.setText(amount + "0");
                    } catch (Exception e) {
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            final WalletAdapter walletAdapter = new WalletAdapter(wallets);
            rvTransaction.setAdapter(walletAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);
            rvTransaction.setLayoutManager(layoutManager);
            rvTransaction.setItemAnimator(new DefaultItemAnimator());

            database.child("history").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    WalletHistory walletHistory = dataSnapshot.getValue(WalletHistory.class);
                    wallets.add(walletHistory);

                    if(wallets.size() == 0){
                        layEmpty.setVisibility(View.VISIBLE);
                        rvTransaction.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }else{
                        layEmpty.setVisibility(View.GONE);
                        rvTransaction.setVisibility(View.VISIBLE);
                        walletAdapter.notifyDataSetChanged();
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            ly.setVisibility(View.VISIBLE);
            ly1.setVisibility(View.GONE);
            logout.setVisibility(View.GONE);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            hideProgressDialog();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(true, user);
                        } else {
                            hideProgressDialog();
                            Toast.makeText(getActivity(), "Failed to authenticate. Try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            Toast.makeText(getContext(), "Signed in le", Toast.LENGTH_SHORT).show();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(getContext(), "Failed to sign in leh", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_wallet, container, false);
        ly = (ConstraintLayout)view.findViewById(R.id.linearLayoutWallet);
        ly1 = (LinearLayout)view.findViewById(R.id.linearLayoutWallet2);
        addMoney = (Button)view.findViewById(R.id.btnAddMoney);
        logout = (Button)view.findViewById(R.id.logout);
        tvAmount = (TextView)view.findViewById(R.id.tvAmount);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        rvTransaction = (RecyclerView)view.findViewById(R.id.rvTransaction);
        layEmpty = (LinearLayout)view.findViewById(R.id.layEmpty);
        wallets = new ArrayList<>();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser != null, currentUser);

        SignInButton signInButton = (SignInButton)view.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                updateUI(false, null);
            }
        });

        addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.example.minghan.park.BottomSheetDialog bottomSheetDialog = new com.example.minghan.park.BottomSheetDialog();
                bottomSheetDialog.show(getActivity().getSupportFragmentManager(), "Dialog");
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
                firebaseAuthWithGoogle(googleSignInAccount);
            }
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
