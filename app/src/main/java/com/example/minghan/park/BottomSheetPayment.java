package com.example.minghan.park;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by MingHan on 5/14/2017.
 */

public class BottomSheetPayment extends BottomSheetDialogFragment implements BottomSheetInterface {
    public View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.dialog_payment_method, container, false);
        this.mView = view;
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            BottomSheetInterface callback = (BottomSheetInterface) getActivity();
            callback.onLayoutCreated(mView);
        } catch (ClassCastException e) {
            Log.e("ERROR", " must implement ViewInterface");
        }
    }

    @Override
    public void onLayoutCreated(View view) {

    }
}
