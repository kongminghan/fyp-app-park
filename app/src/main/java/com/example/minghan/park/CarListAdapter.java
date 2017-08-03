package com.example.minghan.park;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minghan.park.Modal.Car;
import com.example.minghan.park.Modal.Record;
import com.google.android.gms.tasks.Task;
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

/**
 * Created by MingHan on 5/23/2017.
 */

public class CarListAdapter extends ArrayAdapter<String> {

    Context context;
    ArrayList<String> cars;
    LinearLayout layShow, layLV;

    private static class ViewHolder {
        TextView tvCarNum;
        LinearLayout ivDelete;
        CardView cv;
    }

    public CarListAdapter(@NonNull Context context, ArrayList<String> cars, LinearLayout layShow, LinearLayout layLV) {
        super(context, 0, cars);
        this.context = context;
        this.cars = cars;
        this.layLV = layLV;
        this.layShow = layShow;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final String car = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_car, parent, false);
            viewHolder.tvCarNum = (TextView) convertView.findViewById(R.id.tvPrevCarPlate);
            viewHolder.ivDelete = (LinearLayout) convertView.findViewById(R.id.layDelete);
            viewHolder.cv = (CardView) convertView.findViewById(R.id.cvListItem);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvCarNum.setText(car);
        viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Delete");
                alert.setMessage("Are you sure you want to delete?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CarDB carDB = new CarDB(context);
                        carDB.deleteSearch(viewHolder.tvCarNum.getText().toString());
                        dialog.dismiss();
                        Task<Void> databaseReference = FirebaseDatabase.getInstance().getReference("car").child(car).child("CMToken").removeValue();
                        cars.remove(position);
                        notifyDataSetChanged();
                        if (cars.size() == 0) {
                            layShow.setVisibility(View.VISIBLE);
                            layLV.setVisibility(View.GONE);
                        }
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });

        viewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPlate(car);
            }
        });
        return convertView;
    }

    public void checkPlate(final String plate) {
        if (plate.trim().length() > 2) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            final Car[] receivedCar = {new Car()};

            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("car");
            try {
                Query query = databaseReference.child(plate);
                final boolean[] found = {false};
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        receivedCar[0] = dataSnapshot.getValue(Car.class);
                        boolean paid = false;
                        if (receivedCar[0] != null) {
                            found[0] = true;

                            // Get messaging token and store in FireBase
                            String fbToken = FirebaseInstanceId.getInstance().getToken();
                            databaseReference.child(receivedCar[0].getCarNumber()).child("CMToken").setValue(fbToken);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    CarDB carDB = new CarDB(context);
                                    if (carDB.getSearchById(plate).moveToNext())
                                        carDB.updateSearch(plate);
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

                            if (receivedCar[0].getStatus() != null) {
                                if(receivedCar[0].getStatus().equals("Paid")){
                                    paid = true;
                                    Query query1 = FirebaseDatabase.getInstance().getReference("record").child(plate).child("record").limitToLast(1);
                                    query1.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Record record = new Record();
                                            for(DataSnapshot recordSnapshot : dataSnapshot.getChildren())
                                                record = recordSnapshot.getValue(Record.class);
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                            try{
                                                Date date = simpleDateFormat.parse(record.ExtDate+" "+record.ExtTime);
                                                Date now = new Date();
                                                if(now.getTime() - date.getTime() >= 20*60*1000){
                                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("car")
                                                            .child(plate);
                                                    reference.child("LastEnterTime").setValue(record.ExtTime);
                                                    reference.child("LastEnterDate").setValue(record.ExtDate);
                                                    reference.child("Status").setValue("");
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Intent intent = new Intent(context, CountDownActivity.class);
                                                            intent.putExtra("carNum", receivedCar[0].getCarNumber());
                                                            intent.putExtra("carEntTime", receivedCar[0].getLastEnterTime());
                                                            intent.putExtra("carEntDate", receivedCar[0].getLastEnterDate());
                                                            intent.putExtra("carLocation", receivedCar[0].getCarLocation());
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                            context.startActivity(intent);
                                                        }
                                                    }).start();

                                                    //Toast.makeText(context, "You've exceeded the allowable time after payment. Please make your payment. Thank You.", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    //Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(context, CountDownActivity.class);
                                                    long diff = now.getTime() - date.getTime();
                                                    intent.putExtra("DIFF_TIME", diff);
                                                    intent.putExtra("carNum", receivedCar[0].getCarNumber());
                                                    intent.putExtra("carEntTime", receivedCar[0].getLastEnterTime());
                                                    intent.putExtra("carEntDate", receivedCar[0].getLastEnterDate());
                                                    intent.putExtra("carLocation", receivedCar[0].getCarLocation());
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    context.startActivity(intent);
//                                                    ((Activity)context).finish();
                                                }
                                            }catch (ParseException e){
                                                e.printStackTrace();
                                            }
                                            progressDialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {}
                                    });

                                } else if(receivedCar[0].getStatus().equals("Left")){
                                    paid = true;
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Great news! Your parking fee has been paid.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (found[0] == true && !paid) {
                                progressDialog.dismiss();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(context, DurationActivity.class);
                                        intent.putExtra("carNum", receivedCar[0].getCarNumber());
                                        intent.putExtra("carEntTime", receivedCar[0].getLastEnterTime());
                                        intent.putExtra("carEntDate", receivedCar[0].getLastEnterDate());
                                        intent.putExtra("carLocation", receivedCar[0].getCarLocation());
                                        context.startActivity(intent);
                                    }
                                }).start();
                            } else if (found[0] == false) {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Car plate number not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Car plate number not found. Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } catch (Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Car plate number not found. Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Invalid Car plate number", Toast.LENGTH_SHORT).show();
        }
    }
}
