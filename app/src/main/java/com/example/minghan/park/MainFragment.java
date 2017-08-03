package com.example.minghan.park;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minghan.park.Modal.Car;
import com.example.minghan.park.Modal.Record;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View view;
    private OnFragmentInteractionListener mListener;
    private EditText etCarPlateMain;
    private ProgressDialog progressDialog;
    private Car receivedCar;
    private LinearLayout layShowLV;
    private LinearLayout layShowET;
    private ListView lvCar;
    private CarDB carDB;
    private CarListAdapter carListAdapter;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main_fragment, container, false);
        etCarPlateMain = (EditText) view.findViewById(R.id.etCarPlateMain);
        lvCar = (ListView) view.findViewById(R.id.lvCar);
        layShowET = (LinearLayout) view.findViewById(R.id.layShowET);
        layShowLV = (LinearLayout) view.findViewById(R.id.layShowLV);
        Button btnAddNewPlate = (Button) view.findViewById(R.id.btnAddNewPlate);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> searches = new ArrayList<String>();
                carDB = new CarDB(getActivity());
                Cursor cursor = carDB.getAllSearches();
                if (cursor.moveToFirst()) {
                    do {
                        searches.add(cursor.getString(cursor.getColumnIndex("carNum")));
                    } while (cursor.moveToNext());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layShowET.setVisibility(View.GONE);
                            layShowLV.setVisibility(View.VISIBLE);
                            carListAdapter = new CarListAdapter(getActivity(), searches, layShowET, layShowLV);
                            lvCar.setAdapter(carListAdapter);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layShowET.setVisibility(View.VISIBLE);
                            layShowLV.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();

        Button button = (Button) view.findViewById(R.id.btnEnterFrag);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCarPlate();
            }
        });

        etCarPlateMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog(false);
            }
        });

        btnAddNewPlate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog(true);
            }
        });

        startAnimation();
        return view;
    }

    private void setupDialog(boolean isViewBefore) {
        final Dialog dialogEt = new Dialog(getActivity());
        dialogEt.setContentView(R.layout.modal_edit_text);
        Button dialogBtnEnter = (Button) dialogEt.findViewById(R.id.btnEnter);
        final EditText etCarPlate = (EditText) dialogEt.findViewById(R.id.etCarPlate);
        etCarPlate.setText(etCarPlateMain.getText());
        dialogEt.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        if (isViewBefore) {
            dialogBtnEnter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etCarPlateMain.setText(etCarPlate.getText().toString());
                    dialogEt.dismiss();
                    checkCarPlate();
                }
            });
        } else {
            dialogBtnEnter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etCarPlateMain.setText(etCarPlate.getText().toString());
                    dialogEt.dismiss();
                }
            });
        }

        dialogEt.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                etCarPlateMain.setText(etCarPlate.getText().toString());
                dialogEt.dismiss();
            }
        });
        dialogEt.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> searches = new ArrayList<String>();
                carDB = new CarDB(getActivity());
                Cursor cursor = carDB.getAllSearches();
                if (cursor.moveToFirst()) {
                    do {
                        searches.add(cursor.getString(cursor.getColumnIndex("carNum")));
                    } while (cursor.moveToNext());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layShowET.setVisibility(View.GONE);
                            layShowLV.setVisibility(View.VISIBLE);
                            carListAdapter = new CarListAdapter(getActivity(), searches, layShowET, layShowLV);
                            lvCar.setAdapter(carListAdapter);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layShowET.setVisibility(View.VISIBLE);
                            layShowLV.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    private void checkCarPlate() {
        if (etCarPlateMain.getText().toString().trim().length() > 2) {
            final String carPlate = etCarPlateMain.getText().toString();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            receivedCar = new Car();
            final Record[] record = {new Record()};

            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("car");

            try {
                Query query = databaseReference.child(carPlate);
                final boolean[] found = {false};
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        receivedCar = dataSnapshot.getValue(Car.class);
                        boolean paid = false;
                        if (receivedCar != null) {
                            found[0] = true;

                            // Get messaging token and store in FireBase
                            String fbToken = FirebaseInstanceId.getInstance().getToken();
                            databaseReference.child(receivedCar.getCarNumber()).child("CMToken").setValue(fbToken);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
//                                    try {
                                    if (carDB.getSearchById(carPlate).moveToNext())
                                        carDB.updateSearch(carPlate);
                                    else
                                        carDB.insertSearch(receivedCar.getCarNumber());
//                                    getActivity().runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            layShowLV.setVisibility(View.VISIBLE);
//                                            layShowET.setVisibility(View.GONE);
//                                        }
//                                    });
//                                    } catch (Exception e) {
//                                        carDB.insertSearch(receivedCar.getCarNumber());
//                                    }
                                }
                            }).start();

                            if(receivedCar.getStatus()!= null){
                                if (receivedCar.getStatus().equals("Paid")) {
                                    paid = true;

                                    Query query1 = FirebaseDatabase.getInstance().getReference("record").child(carPlate).child("record").limitToLast(1);
                                    query1.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Record record = new Record();
                                            for (DataSnapshot recordSnapshot : dataSnapshot.getChildren())
                                                record = recordSnapshot.getValue(Record.class);
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                            try {
                                                Date date = simpleDateFormat.parse(record.ExtDate + " " + record.ExtTime);
                                                Date now = new Date();
                                                if (now.getTime() - date.getTime() >= 20 * 60 * 1000) {
                                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("car")
                                                            .child(carPlate);
                                                    reference.child("LastEnterTime").setValue(record.ExtTime);
                                                    reference.child("LastEnterDate").setValue(record.ExtDate);
                                                    reference.child("Status").setValue("");
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Intent intent = new Intent(getActivity(), CountDownActivity.class);
                                                            intent.putExtra("carNum", receivedCar.getCarNumber());
                                                            intent.putExtra("carEntTime", receivedCar.getLastEnterTime());
                                                            intent.putExtra("carEntDate", receivedCar.getLastEnterDate());
                                                            intent.putExtra("carLocation", receivedCar.getCarLocation());
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                            getActivity().startActivity(intent);
//                                                            getActivity().finish();
                                                        }
                                                    }).start();

                                                    //Toast.makeText(context, "You've exceeded the allowable time after payment. Please make your payment. Thank You.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    //Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getActivity(), CountDownActivity.class);
                                                    long diff = now.getTime() - date.getTime();
                                                    intent.putExtra("DIFF_TIME", diff);
                                                    intent.putExtra("carNum", receivedCar.getCarNumber());
                                                    intent.putExtra("carEntTime", receivedCar.getLastEnterTime());
                                                    intent.putExtra("carEntDate", receivedCar.getLastEnterDate());
                                                    intent.putExtra("carLocation", receivedCar.getCarLocation());
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    getActivity().startActivity(intent);
//                                                    getActivity().finish();
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            progressDialog.dismiss();
//                                Toast.makeText(getActivity(), "Great news! Your parking fee has been paid.", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                } else if(receivedCar.getStatus().equals("Left")){
                                    paid = true;
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Great news! Your parking fee has been paid.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (found[0] == true && !paid) {
                                progressDialog.dismiss();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(getActivity(), DurationActivity.class);
                                        intent.putExtra("carNum", receivedCar.getCarNumber());
                                        intent.putExtra("carEntTime", receivedCar.getLastEnterTime());
                                        intent.putExtra("carEntDate", receivedCar.getLastEnterDate());
                                        intent.putExtra("carLocation", receivedCar.getCarLocation());
                                        startActivity(intent);
                                    }
                                }).start();
                            } else if (found[0] == false) {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), "Car plate number not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Car plate number not found. Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } catch (Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Car plate number not found. Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Invalid Car plate number", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAnimation() {
        ImageView ivCloud = (ImageView) view.findViewById(R.id.ivCloud);
        AnimatorSet cloudSet = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.cloud_move);
        cloudSet.setTarget(ivCloud);
        cloudSet.start();

        ImageView ivCloud2 = (ImageView) view.findViewById(R.id.ivCloud2);
        AnimatorSet cloudSet2 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.cloud2_move);
        cloudSet2.setTarget(ivCloud2);
        cloudSet2.start();

        ImageView ivCar = (ImageView) view.findViewById(R.id.ivCar);
        AnimatorSet carSet = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.car_move);
        carSet.setTarget(ivCar);
        carSet.start();
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
}
