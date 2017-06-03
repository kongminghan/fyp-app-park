package com.example.minghan.park;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.minghan.park.Modal.History;
import com.example.minghan.park.Modal.Record;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private ArrayList<History>histories;
    private String carNum, carAmount, carDate, carTime = "";
    private OnFragmentInteractionListener mListener;
    private SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    private int RC_SIGN_IN = 0;
    private ProgressDialog mProgressDialog;
    private LinearLayout layEmpty;


    public HistoryFragment() {}

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        histories = new ArrayList<>();

        final RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.rv);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
        layEmpty = (LinearLayout)view.findViewById(R.id.layEmpty);

        final HistoryAdapter historyAdapter = new HistoryAdapter(getActivity(), histories);
        recyclerView.setAdapter(historyAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if(user == null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CarDB carDB = new CarDB(getActivity());
                    Cursor cursor = carDB.getAllRecord();
                    if(cursor.moveToFirst()){
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(10);
                        History history = new History();
                        do{
                            history.CarNumber = cursor.getString(cursor.getColumnIndex("carNum"));
                            history.Payment = Double.parseDouble(cursor.getString(cursor.getColumnIndex("carAmount")));
                            history.EntDate = cursor.getString(cursor.getColumnIndex("carEntDate"));
                            history.EntTime = cursor.getString(cursor.getColumnIndex("carEntTime"));
                            history.ExtDate = cursor.getString(cursor.getColumnIndex("carExtDate"));
                            history.ExtTime = cursor.getString(cursor.getColumnIndex("carExtTime"));
                            history.Duration = cursor.getString(cursor.getColumnIndex("carDuration"));
                            histories.add(history);
                        }while(cursor.moveToNext());
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(histories.size()==0){
                                recyclerView.setVisibility(View.GONE);
                                layEmpty.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }else{
                                layEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                historyAdapter.notifyDataSetChanged();
                                progressBar.setProgress(100);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }).start();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(10);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("history").child(user.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    histories.clear();
                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                        for(DataSnapshot historySnapshot : dataSnapshot.getChildren()){
                            History history = historySnapshot.getValue(History.class);
                            histories.add(history);

                            if(histories.size()==0){
                                recyclerView.setVisibility(View.GONE);
                                layEmpty.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }else{
                                layEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                historyAdapter.notifyDataSetChanged();
                                progressBar.setProgress(100);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }else{
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
//            reference.addChildEventListener(new ChildEventListener() {
//                @Override
//                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
//                        History history = dataSnapshot.getValue(History.class);
//                        histories.add(history);
//                        historyAdapter.notifyDataSetChanged();
//                        progressBar.setProgress(100);
//                        progressBar.setVisibility(View.GONE);
//                    }
//                }
//
//                @Override
//                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                }
//
//                @Override
//                public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                }
//
//                @Override
//                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    progressBar.setProgress(100);
//                    progressBar.setVisibility(View.GONE);
//                }
//            });
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context context) {
            mDivider = ContextCompat.getDrawable(getActivity(), R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

}
