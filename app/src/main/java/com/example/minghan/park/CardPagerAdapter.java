package com.example.minghan.park;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by MingHan on 3/26/2017.
 */

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;
    private TextView tvCCNum, tvCCExpiry, tvCCName, tvCCV;
    private boolean mValidateCard = true;
    private ImageView ivLogo;
    ViewPagerHeightWrapper vp;
//    private Button btnCheckout;

    public CardPagerAdapter(TextView num, TextView name, TextView expiry, ImageView ivLogo, ViewPagerHeightWrapper vp) {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
        tvCCNum = num;
        tvCCExpiry = expiry;
        tvCCName = name;
        this.ivLogo = ivLogo;
//        this.btnCheckout = btnCheckout;
        this.vp = vp;
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    @Override
    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.credit_card_pager_item, container, false);

//        View mainView = LayoutInflater.from(container.getContext())
//                .inflate(R.layout.activity_confirmation, container, false);
//
//        tvCCExpiry = (TextView) mainView.findViewById(R.id.tvCCExpiry);
//        tvCCName = (TextView) mainView.findViewById(R.id.tvCCName);
//        tvCCNum = (TextView) mainView.findViewById(R.id.tvCCNum);

        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(CardItem item, View view) {
        final EditText et = (EditText) view.findViewById(R.id.etCC);
        final TextInputLayout til = (TextInputLayout)view.findViewById(R.id.tilCC);
        final TextView title = (TextView)view.findViewById(R.id.tvCC);
        title.setText(item.getTitle());

        if(!item.getTitle().equals("CVV")){
            et.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    switch (keyCode){
                        case KeyEvent.KEYCODE_ENTER:
                            vp.setCurrentItem(vp.getCurrentItem() + 1);
                    }
                    return false;
                }
            });
        }

        if(item.getTitle().equals("Card Number")){
            et.addTextChangedListener(new FourDigitCardFormatWatcher());
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().startsWith("4")){
                        ivLogo.setImageResource(R.drawable.visa);
                        til.setError(null);
                    }
                    else if(s.toString().startsWith("5")){
                        ivLogo.setImageResource(R.drawable.ms);
                        til.setError(null);
                    }
                    else if(s.toString().startsWith("6") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }
                    else if(s.toString().startsWith("7") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }
                    else if(s.toString().startsWith("8") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }
                    else if(s.toString().startsWith("9") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }
                    else if(s.toString().startsWith("3") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }
                    else if(s.toString().startsWith("2") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }
                    else if(s.toString().startsWith("1") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }
                    else if(s.toString().startsWith("0") ){
                        til.setError("Not supported card");
                        tvCCNum.setText("xxxx xxxx xxxx xxxx");
                    }

                    if(!s.toString().equals("")){
                        tvCCNum.setText(s.toString());
                    }
                }
            });
            et.setInputType(InputType.TYPE_CLASS_PHONE);
            InputFilter[] input = new InputFilter[1];
            input[0] = new InputFilter.LengthFilter(19);
            et.setFilters(input);
        } else if (item.getTitle().equals("Name on Card")) {
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if(!s.toString().equals("")) {
                        tvCCName.setText(s.toString());
                        til.setError(null);
                    }else{
                        til.setError("Name should not be empty");
                    }
                }
            });
        }
        else if (item.getTitle().equals("Expiry (month year) eg. 0218")){
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(4);
            et.setFilters(filterArray);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);

            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {

                    String text = s.toString().replace("/", "");

                    String month, year="";
                    if(text.length() >= 2) {
                        month = text.substring(0, 2);

                        if(text.length() > 2) {
                            year = text.substring(2);
                        }

                        if(mValidateCard) {
                            int mm = Integer.parseInt(month);

                            if (mm <= 0 || mm >= 13) {
                                til.setError("Invalid month");
                                return;
                            }

                            if (text.length() >= 4) {

                                int yy = Integer.parseInt(year);

                                final Calendar calendar = Calendar.getInstance();
                                int currentYear = calendar.get(Calendar.YEAR);
                                int currentMonth = calendar.get(Calendar.MONTH) + 1;

                                int millenium = (currentYear / 1000) * 1000;


                                if (yy + millenium < currentYear) {
                                    til.setError("Card expired");
                                    return;
                                } else if (yy + millenium == currentYear && mm < currentMonth) {
                                    til.setError("Card expired");
                                    return;
                                }
                            }
                        }

                    }
                    else {
                        month = text;
                    }

                    int previousLength = et.getText().length();
                    int cursorPosition = et.getSelectionEnd();

                    text = handleExpiration(month+year);

                    int modifiedLength = text.length();

                    if(modifiedLength <= previousLength && cursorPosition < modifiedLength) {
                        et.setSelection(cursorPosition);
                    }

//                    if(text.length() == 5)
//                        et.setText(text);

                    if(!s.toString().equals("")) {
                        tvCCExpiry.setText(text);
                        til.setError(null);
                    }
                }
            });
        }
        else if(item.getTitle().equals("CVV")){
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(3);
            et.setFilters(filterArray);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    public String handleExpiration(@NonNull String dateYear) {

        String expiryString = dateYear.replace("/", "");
        String text;
        if(expiryString.length() >= 2) {
            String mm = expiryString.substring(0, 2);
            String yy;
            text = mm;

            try {
                if (Integer.parseInt(mm) > 12) {
                    mm = "12"; // Cannot be more than 12.
                }
            }
            catch (Exception e) {
                mm = "01";
            }

            if(expiryString.length() >=4) {
                yy = expiryString.substring(2,4);

                try{
                    Integer.parseInt(yy);
                }catch (Exception e) {

                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    yy = String.valueOf(year).substring(2);
                }

                text = mm + "/" + yy;

            }
            else if(expiryString.length() > 2){
                yy = expiryString.substring(2);
                text = mm + "/" + yy;
            }
        }
        else {
            text = expiryString;
        }

        return text;
    }
}
