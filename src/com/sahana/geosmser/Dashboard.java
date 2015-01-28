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
package com.sahana.geosmser;

import java.util.Observable;
import java.util.Observer;

import com.google.android.maps.GeoPoint;
import com.sahana.geosmser.R;
import com.sahana.geosmser.GeoSMSService.IGeoSMSService;
import com.sahana.geosmser.gps.MyLocation;
import com.sahana.geosmser.gps.MyLocation.ProvideType;
import com.sahana.geosmser.view.ReverseGeocoderView;
import com.sahana.geosmser.view.SMSDeliveryDialog;
import com.sahana.geosmser.view.SMSDeliveryDialog.HanMessageSentDialogPack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Dashboard extends FragmentActivity {

	private LinearLayout layoutIncidentReport;
	private LinearLayout layoutTaskDispatch;
	private LinearLayout layoutTeamCommunication;
	private LayoutInflater layoutInflaterFactory;
	
	private GeoSMSServiceController geosmsServiceController;	
	private GeoSMSPack curSelectedGeoSMSPackForSMSDelivery;
    
	private MyLocation mMySubjectLocationGPS;
    private MyLocation mMySubjectLocationAGPS;    
    private Location curLocationGPS;
    private Location curLocationAGPS;
    
  
    private boolean isFinishSMSDeliveryDialog;
    
    private static final int DIALOG_REVERSE_GEOCODER = 0;
	private static final int DIALOG_SMS_DELIVERY = 1;
	private static final int DIALOG_SMS_DELIVERY_MESSAGESENDING = 2;
	private static final int DIALOG_SMS_DELIVERY_MESSAGESENT = 3;
	private static final int DIALOG_SMS_DELIVERY_EXIT_SMS_CONFORM = 4;
	private static final int DIALOG_SMS_QUERY_MESSAGESENDING = 5;
   
	private AlertDialog mReverseGeocoder = null;
	private AlertDialog adSMSDVMessageSent;
    private ProgressDialog pdSMSDVMessageSending;
    private DialogEvtDisableKeyBackOnKeyListener evtDialogDisableKeyBack;
    private HanMessageSentDialogPack hanMessageSentDialogPack;
    private HanSMSDeliveryDialog mHanSMSDeliveryDialog;
    private ReverseGeocoderView mReverseGeocoderView;
    private SMSDeliveryDialog mSMSDeliveryView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		findViews();
		geosmsServiceController = new GeoSMSServiceController();
		setListensers();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        if(mSMSDeliveryView.isSMSReceiverRegistered()) {
            mSMSDeliveryView.unregisterSMSSendDeliveryReceiver();
        }
//        mSMSQueryView.smsWriter.close();
        super.onDestroy();
		
	}
	 
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		 geosmsServiceController.bindService();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
	    geosmsServiceController.unbindService();
	    super.onPause();
	}
	 
	 public void findViews(){
		
		layoutIncidentReport =  (LinearLayout)findViewById(R.id.layoutSahanaIncidentReport);
		layoutTaskDispatch = (LinearLayout)findViewById(R.id.layoutSahanaTaskDispatch);
		layoutTeamCommunication = (LinearLayout)findViewById(R.id.layoutSahanaTeamCommunication);
		layoutInflaterFactory = LayoutInflater.from(this);
		evtDialogDisableKeyBack = new DialogEvtDisableKeyBackOnKeyListener();
	 }
	 
	 private void setListensers() {
			// TODO Auto-generated method stub
		 layoutIncidentReport.setOnClickListener(new View.OnClickListener() {

			 public void onClick(View v) {
				    mMySubjectLocationGPS = new MyLocation(Dashboard.this);
		            mMySubjectLocationAGPS = new MyLocation(Dashboard.this);
		            
		            mMySubjectLocationGPS.enable(ProvideType.E_GPS);
		            mMySubjectLocationAGPS.enable(ProvideType.E_AGPS);
		            
		            curLocationGPS = mMySubjectLocationGPS.getCurrentLocation();
		            curLocationAGPS = mMySubjectLocationAGPS.getCurrentLocation();
		            
		            mMySubjectLocationGPS.addObserver(new GPSLocationObserver());
		            mMySubjectLocationAGPS.addObserver(new AGPSLocationObserver());
		            
		            GeoSMSPack gPack = null;
		            
		            if (curLocationGPS != null) {
		                GeoPoint gPoint = new GeoPoint((int)(curLocationGPS.getLatitude()*1E6),(int)(curLocationGPS.getLongitude()*1E6));
		                gPack = GeoSMSPackFactory.createIncidentPack(gPoint);
		                startDeliveryGeoSMS(gPack);
		            } else if (curLocationAGPS != null) {
		                GeoPoint gPoint = new GeoPoint((int)(curLocationAGPS.getLatitude()*1E6),(int)(curLocationAGPS.getLongitude()*1E6));
		                gPack = GeoSMSPackFactory.createIncidentPack(gPoint);
		                startDeliveryGeoSMS(gPack);
		            } else {
		                Toast.makeText(Dashboard.this, R.string.gps_state_no_provider, Toast.LENGTH_SHORT).show();
		            }
			}
		}); 

		 layoutTaskDispatch.setOnClickListener(new View.OnClickListener(){			 
			@Override
			public void onClick(View v) {
			// TODO Auto-generated method stub
			startActivity(new Intent(Dashboard.this,  TaskDispatch.class));
			}
		 });

		 layoutTeamCommunication.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(Dashboard.this, TeamCommunication.class));		
			}
		 });
		 
		
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
	        
		}

//==================================================================
	    
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
	                
	            case DIALOG_SMS_DELIVERY:
	            	/*
	            	 * mHanSMSDelivery and SMSDVEvtOnSourceBindingListene are initialized 
	            	 * when an SMSDeliveryDialog is launched or opened
	            	 */
	            	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	            	DialogFragment dialogFragment = new SMSDeliveryDialog(getApplicationContext(),new SMSDVEvtOnSourceBindingListene(),mHanSMSDeliveryDialog);
	            	dialogFragment.show(ft, "openDialog");
	            	break;
	            case DIALOG_SMS_DELIVERY_MESSAGESENDING:
	                
	            case DIALOG_SMS_DELIVERY_MESSAGESENT:
	                AlertDialog.Builder adBuilderMsgSent = new AlertDialog.Builder(this);
	                adBuilderMsgSent.setTitle(R.string.dialog_geosms_delivery_message_sent_title);
	                adBuilderMsgSent.setMessage(R.string.dialog_geosms_delivery_message_sent_message);
	                adBuilderMsgSent.setPositiveButton(R.string.button_sure_text, new DialogInterface.OnClickListener() {
	                    @Override 
	                    public void onClick(DialogInterface dialog, int which) {                        
	                        if(isFinishSMSDeliveryDialog) {                         
	                            //dismissDialog(DIALOG_SMS_DELIVERY);
	                            dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
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
	            case DIALOG_SMS_DELIVERY_EXIT_SMS_CONFORM:
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
	                        showDialog(DIALOG_SMS_DELIVERY);
	                    }
	                })
	                .create();
	            
	           case DIALOG_SMS_QUERY_MESSAGESENDING:
	                pdSMSDVMessageSending = new ProgressDialog(this);
	                pdSMSDVMessageSending.setTitle(R.string.dialog_geosms_delivery_message_sending_title);
	                pdSMSDVMessageSending.setMessage(getString(R.string.dialog_geosms_delivery_message_sending_message));
	                pdSMSDVMessageSending.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	                pdSMSDVMessageSending.setOnKeyListener(evtDialogDisableKeyBack);
	                return pdSMSDVMessageSending;
	        }
	        return super.onCreateDialog(id);
	    }
	    
	    
	    @Override 
	    protected void onPrepareDialog(int id, Dialog pDialog) {
	        switch (id) {
	            case DIALOG_SMS_DELIVERY_MESSAGESENDING:        
	            case DIALOG_SMS_DELIVERY_MESSAGESENT:
	                setSMSDLMessageSentDialogStatus();
	                //dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
	                break;
	        }
	        super.onPrepareDialog(id, pDialog);
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
	    private void setSMSDLMessageSentDialogStatus() {
	        setSMSDLMessageSentDialogStatus(hanMessageSentDialogPack);
	    }
	    
	    private void setSMSDLMessageSentDialogStatus(HanMessageSentDialogPack pack) {
	        if(pack != null) {
	            adSMSDVMessageSent.setMessage(pack.message);
	            isFinishSMSDeliveryDialog = pack.isFinish;
	        }
	    }
	    
	    private class DialogEvtDisableKeyBackOnKeyListener implements DialogInterface.OnKeyListener {
	        @Override
	        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
	            if(keyCode == KeyEvent.KEYCODE_BACK) return true;
	            return false;
	        }
	    }
	    
	    private class DialogEvtDisableSMSDeliveryDialogKeyBackOnKeyListener implements DialogInterface.OnKeyListener {
	        @Override
	        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {     
	            if(keyCode == KeyEvent.KEYCODE_BACK) {
	                dismissDialog(DIALOG_SMS_DELIVERY);
	                // TODO
	                if(!mSMSDeliveryView.getMessageFieldText().trim().equals("")
	                        || !mSMSDeliveryView.getPhoneFieldText().trim().equals("")) {
	                    showDialog(DIALOG_SMS_DELIVERY_EXIT_SMS_CONFORM);
	                }
	                else {
	                    mSMSDeliveryView.clearSavedState();
	                }
	                return true;
	            }
	            return false;
	        }
	    }
		private void setCurrentSelectedGeoSMSPackForSMSDelivery(GeoSMSPack pack) {
	        if(curSelectedGeoSMSPackForSMSDelivery == null) curSelectedGeoSMSPackForSMSDelivery = new GeoSMSPack();
	        curSelectedGeoSMSPackForSMSDelivery.assign(pack);
	    }
	    
		private void startDeliveryGeoSMS(GeoSMSPack pack) {
	        setCurrentSelectedGeoSMSPackForSMSDelivery(pack);
	        showDialog(DIALOG_SMS_DELIVERY);
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

	 
	 @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		 MenuItem[] menuItem = new MenuItem[]{
				 menu.add(0, 1, 0, R.string.menu_sahana_inbox)				 
		 };
		 
		 menuItem[0].setOnMenuItemClickListener(new MenuEvtGoInbox());
		return super.onCreateOptionsMenu(menu);
	}
	 
	 public class MenuEvtGoInbox implements OnMenuItemClickListener{

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			// TODO Auto-generated method stub
            Intent intent = new Intent(Dashboard.this, GeoSMSManagerAct.class);
            startActivity(intent);
            return true;
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
	        private static final int CODE_DISMISS_DIALOG_SMS_DELIVERY = 0;
			private int checkingTimes = 0;
	        private Handler messageSentCheckingHandler = new Handler();
	        
	        private Runnable mMessageSentCheckingTask = new Runnable() {
	            @Override
	            public void run() {
	                if(checkingTimes < 15) {
	                    if(hanMessageSentDialogPack.isFinish) {
	                        dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
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
	                        dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
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
	                case DIALOG_SMS_DELIVERY_MESSAGESENT:
	                    hanMessageSentDialogPack = (HanMessageSentDialogPack) msg.obj;
	                    //me.dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
	                    showDialog(DIALOG_SMS_DELIVERY_MESSAGESENT);
	                    break;
	                case DIALOG_SMS_DELIVERY_MESSAGESENDING:
	                    dismissDialog(DIALOG_SMS_DELIVERY);
	                    showDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
	                    hanMessageSentDialogPack.handler = messageSentCheckingHandler;
	                    messageSentCheckingHandler.removeCallbacks(mMessageSentCheckingTask);
	                    messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
	                    break;
	                case CODE_DISMISS_DIALOG_SMS_DELIVERY:
	                    dismissDialog(DIALOG_SMS_DELIVERY);
	                    break;
	                case DIALOG_SMS_DELIVERY_CANCEL:
	                    mMySubjectLocationGPS.disable();
	                    mMySubjectLocationAGPS.disable();
	                    break;
	            }
	        }
	    }
}
