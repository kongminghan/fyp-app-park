package com.example.minghan.park;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.minghan.park.Modal.History;
import com.example.minghan.park.Modal.Record;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by MingHan on 5/12/2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<History> histories;

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView tvCar, tvAmount, tvDuration, tvDate;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvAmount = (TextView)itemView.findViewById(R.id.txtAmt);
            tvCar = (TextView)itemView.findViewById(R.id.txtCar);
            tvDate = (TextView)itemView.findViewById(R.id.txtDate);
            tvDuration = (TextView)itemView.findViewById(R.id.txtDuration);
        }
    }

    public HistoryAdapter(Context context, ArrayList<History> histories){
        this.context = context;
        this.histories = histories;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tvCar.setText("Car Plate No: "+histories.get(position).CarNumber);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        try {
            Date date = sdf.parse(histories.get(position).EntDate+" "+histories.get(position).EntTime);
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd, MMM hh:mma");
            String dateTime = sdf2.format(date);
            holder.tvDate.setText("Parked on "+dateTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDuration.setText(histories.get(position).Duration);
        holder.tvAmount.setText("RM"+histories.get(position).Payment+"0");
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }
}
