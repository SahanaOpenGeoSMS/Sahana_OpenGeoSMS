/*
 * This file is part of Open GeoSMSer. 
 * 
 * Copyright (c) 2011 Korth Luo <allen.cause@gmail.com> - All rights
 * reserved.
 */
package com.sahana.geosmser;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.telephony.SmsManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sahana.geosmser.R;
import com.google.android.maps.GeoPoint;
import com.sahana.geosmser.GeoSMSService.IGeoSMSService;
import com.sahana.geosmser.gps.MyLocation;
import com.sahana.geosmser.gps.MyLocation.ProvideType;
import com.sahana.geosmser.view.*;
import com.sahana.geosmser.view.SMSDeliveryDialog.HanMessageSentDialogPack;

public class TeamCommunication extends FragmentActivity{
    
    private static final int DIALOG_REVERSE_GEOCODER = 100;
 
    private Bundle mBundle = null;
    
    private ReverseGeocoderView mReverseGeocoderView;
 
    private ImageView mWhereAreWePic, mWhereAmIPic, mWhereAreYouPic;
    private TextView mTextWhereAreWeTitle, mTextWhereAmITitle, mTextWhereAreYouTitle;
    private LinearLayout mLayoutWhereAreWe, mLayoutWhereAmI, mLayoutWhereAreYou;
    
    private SMSDeliveryDialog mSMSDeliveryView;
    private SMSQueryView mSMSQueryView;
    
    private HanMessageSentDialogPack hanMessageSentDialogPack;
    
    private HanSMSQueryDialog mHanSMSQueryDialog;
    private HanSMSDeliveryDialog mHanSMSDeliveryDialog;
    
    private LayoutInflater layoutInflaterFactory;
    
    private AlertDialog mReverseGeocoder = null;
    private AlertDialog adSMSDVMessageSent;
    private ProgressDialog pdSMSDVMessageSending;
    
    private DialogEvtDisableKeyBackOnKeyListener evtDialogDisableKeyBack;
    private GeoSMSServiceController geosmsServiceController;
    
    private GeoSMSPack curSelectedGeoSMSPackForSMSDelivery;
    
    private boolean isFinishSMSDeliveryDialog;
    
    private MyLocation mMySubjectLocationGPS;
    private MyLocation mMySubjectLocationAGPS;
    
    private Location curLocationGPS;
    private Location curLocationAGPS;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menus);
        findViews();
        initialEvent();
    }
    
    @Override
    protected void onDestroy() {
        if(mSMSDeliveryView.isSMSReceiverRegistered()) {
            mSMSDeliveryView.unregisterSMSSendDeliveryReceiver();
        }
        mSMSQueryView.smsWriter.close();
        super.onDestroy();
    }

    
    public void onResume() {
        //Ye mark
    	//geosmsServiceController.bindService();
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // mReverseGeocoderView.setNetworkDetectorTerminated();
        Log.d(WhereToMeet.TAG, "Pause...");
        if (mReverseGeocoder != null && mReverseGeocoder.isShowing()) {
            mReverseGeocoder.dismiss();
        }
      //Ye mark
    	//geosmsServiceController.unbindService();
        super.onPause();
    }
    
    public void findViews() {
        
        mLayoutWhereAreWe = (LinearLayout) findViewById(R.id.layoutWhereAreWe);
        mLayoutWhereAmI = (LinearLayout) findViewById(R.id.layoutWhereAmI);
        mLayoutWhereAreYou = (LinearLayout) findViewById(R.id.layoutWhereAreYou);
        
        mLayoutWhereAreWe.setOnClickListener(new MeetHereListener());
        //mLayoutWhereAreWe.setOnLongClickListener(reverseGeoCoder);
        mLayoutWhereAmI.setOnClickListener(new MyLocationSenderListener());
        mLayoutWhereAreYou.setOnClickListener(new SMSQueryListener());
        
        mWhereAreWePic = (ImageView) findViewById(R.id.imgWhereAreWe);
        mWhereAmIPic = (ImageView) findViewById(R.id.imgWhereAmI);
        mWhereAreYouPic = (ImageView) findViewById(R.id.imgWhereAreYou);
        
        mTextWhereAreWeTitle = (TextView) findViewById(R.id.textWhereAreWe);
        mTextWhereAmITitle = (TextView) findViewById(R.id.textWhereAmI);
        mTextWhereAreYouTitle = (TextView) findViewById(R.id.textWhereAreYou);
        
        mTextWhereAreWeTitle.setTextColor(Color.WHITE);
        mTextWhereAreWeTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTextWhereAreWeTitle.setTypeface(Typeface.SERIF);
        
        mTextWhereAmITitle.setTextColor(Color.WHITE);
        mTextWhereAmITitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTextWhereAmITitle.setTypeface(Typeface.SERIF);
        
        mTextWhereAreYouTitle.setTextColor(Color.WHITE);
        mTextWhereAreYouTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTextWhereAreYouTitle.setTypeface(Typeface.SERIF);
        
        layoutInflaterFactory = LayoutInflater.from(this);
        mSMSQueryView = (SMSQueryView) layoutInflaterFactory.inflate(R.layout.sms_query_view, null);
        
        mHanSMSQueryDialog = new HanSMSQueryDialog();
        mHanSMSQueryDialog.setView(mSMSQueryView);
        
        evtDialogDisableKeyBack = new DialogEvtDisableKeyBackOnKeyListener();
    }
    
    public void initialEvent() { 
    	//Ye mark
    	//geosmsServiceController = new GeoSMSServiceController();
        
        mReverseGeocoderView = new ReverseGeocoderView(this);
        
        mSMSDeliveryView = new SMSDeliveryDialog(getApplicationContext());
        evtDialogDisableKeyBack = new DialogEvtDisableKeyBackOnKeyListener();
        hanMessageSentDialogPack = new HanMessageSentDialogPack();
        mHanSMSDeliveryDialog = new HanSMSDeliveryDialog();
        mSMSDeliveryView.registerSMSSendDeliveryReceiver();
        
        /* not needed now as sent through SMSDeliveryDialog Constructor
        mSMSDeliveryView.setOnSourceBindingListener(new SMSDVEvtOnSourceBindingListene());
        mSMSDeliveryView.setMessageSentHandler(mHanSMSDeliveryDialog);
        */
        
        mSMSQueryView.setMessageSentHandler(mHanSMSQueryDialog);
        mSMSQueryView.smsWriter.open();
    }
    
    private class SMSDVEvtOnSourceBindingListene implements SMSDeliveryDialog.ISMSDeliveryRenderer.OnSourceBindingListener {
        @Override
        public void onSourceBind(GeoSMSPack pack) {
            GeoSMSPack p = getCurrentSelectedGeoSMSPackForSMSDelivery();
            pack.assign(p);
        }
    }
    
    private GeoSMSPack getCurrentSelectedGeoSMSPackForSMSDelivery() {
        return curSelectedGeoSMSPackForSMSDelivery;
    }
    
    private void setCurrentSelectedGeoSMSPackForSMSDelivery(GeoSMSPack pack) {
        if(curSelectedGeoSMSPackForSMSDelivery == null) curSelectedGeoSMSPackForSMSDelivery = new GeoSMSPack();
        curSelectedGeoSMSPackForSMSDelivery.assign(pack);
    }
    
    private class DialogEvtDisableSMSDeliveryDialogKeyBackOnKeyListener implements DialogInterface.OnKeyListener {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {     
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                dismissDialog(WhereToMeet.DIALOG_SMS_DELIVERY);
                // TODO
                if(!mSMSDeliveryView.getMessageFieldText().trim().equals("")
                        || !mSMSDeliveryView.getPhoneFieldText().trim().equals("")) {
                    showDialog(WhereToMeet.DIALOG_SMS_DELIVERY_EXIT_SMS_CONFORM);
                }
                else {
                    mSMSDeliveryView.clearSavedState();
                }
                return true;
            }
            return false;
        }
    }
    
    private void setSMSDLMessageSentDialogStatus() {
        setSMSDLMessageSentDialogStatus(hanMessageSentDialogPack);
    }
    
    private void setSMSDLMessageSentDialogStatus(HanMessageSentDialogPack pack) {
        if(pack != null) {
            adSMSDVMessageSent.setMessage(pack.message);
            isFinishSMSDeliveryDialog = pack.isFinish;
        }
    }
    
    protected Dialog onCreateDialog(int id) {
        
        switch(id) {
            case DIALOG_REVERSE_GEOCODER:
                AlertDialog.Builder mReverseGeocoderBuilder = new AlertDialog.Builder(this)
                .setTitle("WARN!")
                .setView(mReverseGeocoderView)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
                
                mReverseGeocoder = mReverseGeocoderBuilder.create();
                
                return mReverseGeocoder;
                
            case WhereToMeet.DIALOG_SMS_DELIVERY:
            	/*
            	 * mHanSMSDelivery and SMSDVEvtOnSourceBindingListene are initialized 
            	 * when an SMSDeliveryDialog is launched or opened
            	 */
            	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            	DialogFragment dialogFragment = new SMSDeliveryDialog(getApplicationContext(),new SMSDVEvtOnSourceBindingListene(),mHanSMSDeliveryDialog);
            	dialogFragment.show(ft, "openDialog");
            	break;
            case WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING:
                
            case WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENT:
                AlertDialog.Builder adBuilderMsgSent = new AlertDialog.Builder(this);
                adBuilderMsgSent.setTitle(R.string.dialog_geosms_delivery_message_sent_title);
                adBuilderMsgSent.setMessage(R.string.dialog_geosms_delivery_message_sent_message);
                adBuilderMsgSent.setPositiveButton(R.string.button_sure_text, new DialogInterface.OnClickListener() {
                    @Override 
                    public void onClick(DialogInterface dialog, int which) {                        
                        if(isFinishSMSDeliveryDialog) {                         
                            //dismissDialog(DIALOG_SMS_DELIVERY);
                            dismissDialog(WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING);
                            mSMSDeliveryView.clearSavedState();
                            isFinishSMSDeliveryDialog = false;
                        }               
                    }
                });
                adSMSDVMessageSent = adBuilderMsgSent.create();
                adSMSDVMessageSent.setOnKeyListener(evtDialogDisableKeyBack);
                setSMSDLMessageSentDialogStatus();
                //dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
                return adSMSDVMessageSent;
            case WhereToMeet.DIALOG_SMS_DELIVERY_EXIT_SMS_CONFORM:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_delivery_exit_conform_title)
                .setMessage(R.string.dialog_geosms_delivery_exit_conform_message)
                .setPositiveButton(R.string.button_yes_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismissDialog(DIALOG_SMS_DELIVERY);
                        mSMSDeliveryView.clearInputField();
                        mSMSDeliveryView.clearSavedState();
                    }
                })
                .setNegativeButton(R.string.button_no_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDialog(WhereToMeet.DIALOG_SMS_DELIVERY);
                    }
                })
                .create();
            
           case WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENDING:
                pdSMSDVMessageSending = new ProgressDialog(this);
                pdSMSDVMessageSending.setTitle(R.string.dialog_geosms_delivery_message_sending_title);
                pdSMSDVMessageSending.setMessage(getString(R.string.dialog_geosms_delivery_message_sending_message));
                pdSMSDVMessageSending.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pdSMSDVMessageSending.setOnKeyListener(evtDialogDisableKeyBack);
                return pdSMSDVMessageSending;
                
            case WhereToMeet.DIALOG_SMS_QUERY:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_query_title)
                .setView(mSMSQueryView)
                .setOnKeyListener(new DialogEvtDisableSMSQueryDialogKeyBackOnKeyListener())
                .create();
                
            case WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENT:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_delivery_message_sent_title)
                .setMessage(R.string.dialog_geosms_delivery_message_sent_message)
                .setPositiveButton(R.string.button_sure_text, new DialogInterface.OnClickListener() {
                    @Override 
                    public void onClick(DialogInterface dialog, int which) {
                        mSMSQueryView.clearInputField();
                        dismissDialog(WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENT);
                    }
                })
                .create();
            case WhereToMeet.DIALOG_SMS_QUERY_EXIT_CONFORM:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_delivery_exit_conform_title)
                .setMessage(R.string.dialog_geosms_delivery_exit_conform_message)
                .setPositiveButton(R.string.button_yes_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSMSQueryView.clearInputField();
                    }
                })
                .setNegativeButton(R.string.button_no_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDialog(WhereToMeet.DIALOG_SMS_QUERY);
                    }
                })
                .create();
                
        }
        return super.onCreateDialog(id);
    }
    
    
    @Override 
    protected void onPrepareDialog(int id, Dialog pDialog) {
        switch (id) {
            case WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING:        
            case WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENT:
                setSMSDLMessageSentDialogStatus();
                //dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
                break;
            case WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENDING:
                ProgressDialog dialog = (ProgressDialog)pDialog;
                dialog.setTitle(R.string.dialog_geosms_delivery_message_sending_title);
                dialog.setMessage(getString(R.string.dialog_geosms_delivery_message_sending_message));
                break;
            case WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENT:
                AlertDialog queryDialog = (AlertDialog) pDialog;
                queryDialog.setMessage(getString(R.string.message_sent_result_ok));
                break;
        }
        super.onPrepareDialog(id, pDialog);
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem[] menuItem = new MenuItem[]{
                menu.add(0, 1, 0, R.string.menu_edit_whitelist),
                menu.add(0, 2, 0, R.string.menu_sahana_inbox)};
        menuItem[0].setOnMenuItemClickListener(new MenuEvtEditWhitelist());
        menuItem[0].setVisible(false);
        menuItem[1].setOnMenuItemClickListener(new MenuEvtGoInbox());
        return super.onCreateOptionsMenu(menu);
    }
    
    private class MenuEvtGoInbox implements OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent(TeamCommunication.this, GeoSMSManagerAct.class);
            startActivity(intent);
            return true;
        }
        
    }

    private class MenuEvtEditWhitelist implements OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            startActivity(new Intent(TeamCommunication.this, GeoSMSContactEditor.class));
            return true;
        }
        
    }

    //TODO Refine by Factory Pattern, future!
    public class MeetHereListener implements LinearLayout.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(TeamCommunication.this, WhereToMeet.class);
            startActivity(intent);
        }
        
    };
    
    public class ReverseGeoCoderListener implements LinearLayout.OnLongClickListener {

        @Override
        public boolean onLongClick(View view) {
            // mNetworkDetector.start();
            showDialog(DIALOG_REVERSE_GEOCODER);
            return false;
        }
        
    };
    
    public class SMSQueryListener implements LinearLayout.OnClickListener {

        @Override
        public void onClick(View view) {
            showDialog(WhereToMeet.DIALOG_SMS_QUERY);
        }
    };
    
    private void startDeliveryGeoSMS(GeoSMSPack pack) {
        setCurrentSelectedGeoSMSPackForSMSDelivery(pack);
        showDialog(WhereToMeet.DIALOG_SMS_DELIVERY);
    }
    
    public class MyLocationSenderListener implements LinearLayout.OnClickListener {

        @Override
        public void onClick(View view) {
            mMySubjectLocationGPS = new MyLocation(TeamCommunication.this);
            mMySubjectLocationAGPS = new MyLocation(TeamCommunication.this);
            
            mMySubjectLocationGPS.enable(ProvideType.E_GPS);
            mMySubjectLocationAGPS.enable(ProvideType.E_AGPS);
            
            curLocationGPS = mMySubjectLocationGPS.getCurrentLocation();
            curLocationAGPS = mMySubjectLocationAGPS.getCurrentLocation();
            
            mMySubjectLocationGPS.addObserver(new GPSLocationObserver());
            mMySubjectLocationAGPS.addObserver(new AGPSLocationObserver());
            
            GeoSMSPack gPack = null;
            
            if (curLocationGPS != null) {
                GeoPoint gPoint = new GeoPoint((int)(curLocationGPS.getLatitude()*1E6),(int)(curLocationGPS.getLongitude()*1E6));
                gPack = GeoSMSPackFactory.createBasicPack(gPoint);
                startDeliveryGeoSMS(gPack);
            } else if (curLocationAGPS != null) {
                GeoPoint gPoint = new GeoPoint((int)(curLocationAGPS.getLatitude()*1E6),(int)(curLocationAGPS.getLongitude()*1E6));
                gPack = GeoSMSPackFactory.createBasicPack(gPoint);
                startDeliveryGeoSMS(gPack);
            } else {
                Toast.makeText(TeamCommunication.this, R.string.gps_state_no_provider, Toast.LENGTH_SHORT).show();
            }
            
        }

    };
    
    private class DialogEvtDisableSMSQueryDialogKeyBackOnKeyListener implements DialogInterface.OnKeyListener {     
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
                if(!mSMSQueryView.getPhoneFieldText().trim().equals("")) {
                    showDialog(WhereToMeet.DIALOG_SMS_QUERY_EXIT_CONFORM);
                }
                return true;
            }
            return false;
        }
    }
    
    private class DialogEvtDisableKeyBackOnKeyListener implements DialogInterface.OnKeyListener {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_BACK) return true;
            return false;
        }
    }
    
    private class HanSMSQueryDialog extends Handler {
        private SMSQueryView smsQueryView;
        private int checkingTimes = 0;
        private Handler messageSentCheckingHandler = new Handler();
        
        private Runnable mMessageSentCheckingTask = new Runnable() {
            @Override
            public void run() {
                if(checkingTimes < 15) {
                    switch(smsQueryView.sendingCode) {
                    case 0:
                        checkingTimes += 1;
                        messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
                    case 1:
                        dismissDialog(WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENDING);
                        showDialog(WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENT);
                    case -1:
                        
                    }
                }
                else {
                    dismissDialog(WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENDING);
                    Toast.makeText(getApplicationContext(), R.string.message_sent_handler_checking_timeout, Toast.LENGTH_LONG).show();
                }
            }
        };
        
        public void setView(SMSQueryView view) {
            smsQueryView = view;
        }
        
        public SMSQueryView getView() {
            return smsQueryView;
        }
        
        @Override public void handleMessage(Message msg) {
            switch(msg.what) {
                case WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENDING:
                        dismissDialog(WhereToMeet.DIALOG_SMS_QUERY);
                        showDialog(WhereToMeet.DIALOG_SMS_QUERY_MESSAGESENDING);
                        checkingTimes = 0;
                        messageSentCheckingHandler.removeCallbacks(mMessageSentCheckingTask);
                        messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
                        break;
            }
        }
    }
    
    private class GeoSMSServiceController {
        private Context baseContext;
        private Intent serviceIntent;
        private IGeoSMSService.Stub binder;
        
        public ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (IGeoSMSService.Stub) service;
                if(!binder.isServiceStart()) {
                    startService();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }           
        };
        
        public GeoSMSServiceController() {
            baseContext = getApplicationContext();
            serviceIntent = new Intent(baseContext, GeoSMSService.class);
        }
        
        public void bindService() {
            baseContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        
        public void unbindService() {
            baseContext.unbindService(serviceConnection);
        }
        
        public void startService() {
            baseContext.startService(serviceIntent);
        }
        
        public void stopService() {
            baseContext.stopService(serviceIntent);
        }
    }
    
    //TODO Random integer... please revise it
    public static final int DIALOG_SMS_DELIVERY_CANCEL = 12345;
    
    private class HanSMSDeliveryDialog extends Handler {
        private int checkingTimes = 0;
        private Handler messageSentCheckingHandler = new Handler();
        
        private Runnable mMessageSentCheckingTask = new Runnable() {
            @Override
            public void run() {
                if(checkingTimes < 15) {
                    if(hanMessageSentDialogPack.isFinish) {
                        dismissDialog(WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING);
                        mMySubjectLocationGPS.disable();
                        mMySubjectLocationAGPS.disable();
                    }
                    else {
                        checkingTimes += 1;
                        messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
                    }
                }
                else {
                    if(!hanMessageSentDialogPack.isFinish) {
                        dismissDialog(WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING);
                        Toast.makeText(getApplicationContext(), R.string.message_sent_handler_checking_timeout, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        
        public void postMessageSent() {
            if(hanMessageSentDialogPack.isFinish) {
                //me.dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
                messageSentCheckingHandler.removeCallbacks(mMessageSentCheckingTask);
            }
        }
        
        @Override public void handleMessage(Message msg) {
            switch(msg.what) {
                case WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENT:
                    hanMessageSentDialogPack = (HanMessageSentDialogPack) msg.obj;
                    //me.dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
                    showDialog(WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENT);
                    break;
                case WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING:
                    dismissDialog(WhereToMeet.DIALOG_SMS_DELIVERY);
                    showDialog(WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING);
                    hanMessageSentDialogPack.handler = messageSentCheckingHandler;
                    messageSentCheckingHandler.removeCallbacks(mMessageSentCheckingTask);
                    messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
                    break;
                case WhereToMeet.CODE_DISMISS_DIALOG_SMS_DELIVERY:
                    dismissDialog(WhereToMeet.DIALOG_SMS_DELIVERY);
                    break;
                case DIALOG_SMS_DELIVERY_CANCEL:
                    mMySubjectLocationGPS.disable();
                    mMySubjectLocationAGPS.disable();
                    break;
            }
        }
    }
    
    private class GPSLocationObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            curLocationGPS = (Location) data;
        }
    }
    
    private class AGPSLocationObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            curLocationAGPS = (Location) data;
        }
    }
    
}
