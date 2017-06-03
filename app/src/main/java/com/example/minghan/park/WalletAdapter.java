package com.example.minghan.park;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.minghan.park.Modal.WalletHistory;

import java.util.ArrayList;

/**
 * Created by MingHan on 5/14/2017.
 */

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.MyViewHolder>{

    private ArrayList<WalletHistory> wallets;

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView tvAmount, tvDateTime, tvDesc;
        public ImageView ivIcon;

        public MyViewHolder(View view){
            super(view);
            tvAmount = (TextView)view.findViewById(R.id.tvAmount);
            tvDateTime = (TextView)view.findViewById(R.id.tvDateTime);
            tvDesc = (TextView)view.findViewById(R.id.tvDesc);
            ivIcon = (ImageView)view.findViewById(R.id.ivIcon);
        }

    }

    public WalletAdapter(ArrayList<WalletHistory> wallets){
        this.wallets = wallets;
    }

    @Override
    public WalletAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_item, parent, false);
        return new WalletAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WalletAdapter.MyViewHolder holder, int position) {
        holder.tvDateTime.setText(wallets.get(position).dateTime);
        holder.tvDesc.setText(wallets.get(position).desc);
        if(wallets.get(position).desc.equals("Wallet TopUp")){
            holder.ivIcon.setImageResource(R.drawable.ic_account_balance_wallet_black_24dp);
            holder.tvAmount.setText("+ RM"+wallets.get(position).amount+"0");
        }else{
            holder.ivIcon.setImageResource(R.drawable.ic_local_parking_black_24dp);
            holder.tvAmount.setText("- RM"+wallets.get(position).amount+"0");
        }
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }
}
