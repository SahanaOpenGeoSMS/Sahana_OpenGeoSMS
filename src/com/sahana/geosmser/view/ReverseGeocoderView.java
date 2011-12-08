/*******************************************************************************
 * Copyright 2011 Cai Fang, Ye
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sahana.geosmser.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.android.maps.GeoPoint;
import com.sahana.geosmser.GeoSMSPack;
import com.sahana.geosmser.WhereToMeet;
import com.sahana.geosmser.widget.NetworkStateDetector;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReverseGeocoderView extends RelativeLayout{
    
    private Context mContext;
    private EditText mEditText;
    private TextView mTextView;
    private Button mButton;
    
    
    public ReverseGeocoderView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.mContext = context;
        initialUIComponent();
        initialEvent();
        
    }
    
    public ReverseGeocoderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public ReverseGeocoderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
    
    public void initialUIComponent() {
        mTextView = new TextView(mContext);
        
        mEditText = new EditText(mContext);
        
        mButton = new Button(mContext);
        
        mTextView.setId(3);
        mEditText.setId(1);
        mButton.setId(2);
        
        mTextView.setText("請輸入查詢地點");
        mEditText.setHint("EX: 台北市圓山捷運站");
        mButton.setText("輸入");
        
        RelativeLayout.LayoutParams mTextLayoutParams 
        = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams mEditLayoutParams
        = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams mButtonLayoutParams
        = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        mTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mEditLayoutParams.addRule(BELOW, mTextView.getId());
        mButtonLayoutParams.addRule(RIGHT_OF, mEditText.getId());
        mButtonLayoutParams.addRule(ALIGN_BASELINE, mEditText.getId());
        
        mButton.setOnClickListener(getGeocoder);
        
        this.addView(mTextView, mTextLayoutParams);
        this.addView(mEditText, mEditLayoutParams);
        this.addView(mButton, mButtonLayoutParams);
    }
    
    public void initialEvent() {
    }
    
    private static final String KEY_VALUE = "EXCIA";
    
    public Button.OnClickListener getGeocoder = new Button.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            // Regular Expression used, future
            getGeoByDefaultAddress("New York");
            GeoSMSPack mPack = com.sahana.geosmser
           .GeoSMSPackFactory
           .createBasicPack(getGeoByAddress(mEditText.getText().toString()));
            Intent mIntent = new Intent();
            mIntent.setClass(mContext, WhereToMeet.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable(KEY_VALUE, mPack);
            mIntent.putExtras(mBundle);
            mContext.startActivity(mIntent);
        }
    };

    public GeoPoint getGeoByAddress(String searchAddress) {
        GeoPoint mGeoPoint = null;
        StringBuilder responseBuilder = new StringBuilder();
       
        try {
            URL url = new URL("http://ajax.googleapis.com/ajax/services/search/local?v=1.0&q="
            + URLEncoder.encode(searchAddress, "UTF-8"));
            // + "&key=ABQIAAAAi0Qn28OD_1M-BbLsxPRwYBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxSdXKLoXxi1NXTY1EpEtAahu1gPgg");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();
            } catch (MalformedURLException me) {
                // me.printStackTrace();
                Toast.makeText(mContext, me.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException ue) {
                // ue.printStackTrace();
                Toast.makeText(mContext, ue.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IOException ie) {
                // ie.printStackTrace();
                Toast.makeText(mContext, ie.getMessage(), Toast.LENGTH_SHORT).show();
            }
        try {
            // JSONObject json = new JSONObject(responseBuilder.toString());
            JSONObject json = (JSONObject) new JSONTokener(responseBuilder.toString()).nextValue();
            JSONObject responseData = json.getJSONObject("responseData");
            JSONObject viewport = responseData.getJSONObject("viewport");
            JSONObject center = viewport.getJSONObject("center");
            Log.d(WhereToMeet.TAG, "" + viewport.toString());
            mGeoPoint = new GeoPoint((int)center.getDouble("lat"), (int)center.getDouble("lng"));
        } catch (JSONException e) {
            Log.d(WhereToMeet.TAG, "" + e.getMessage());
        }
        
        return mGeoPoint;
        
    }
    
    public GeoPoint getGeoByDefaultAddress(String searchAddress) {
        GeoPoint mGeoPoint = null;
        try {
            if (!searchAddress.equals("")) {
                Geocoder mGeocoder = new Geocoder(mContext, Locale.getDefault());
                List<Address> mAddressList = mGeocoder.getFromLocationName(searchAddress, 1);
                if (!mAddressList.isEmpty()) {
                    Address mAddress = mAddressList.get(0);
                    double mLatitude = mAddress.getLatitude()*1E6;
                    double mLongitude = mAddress.getLongitude()*1E6;
                    mGeoPoint = new GeoPoint((int)mLatitude, (int)mLongitude);
                } else {
                    Log.d(WhereToMeet.TAG, "Address Not Found!");
                }
            } 
        } catch (Exception e) {
            Log.d(com.sahana.geosmser.WhereToMeet.TAG, e.getMessage());
        }
        return mGeoPoint;
    }
    
    @Override
    protected void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
    }
}
