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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.sahana.geosmser.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.sahana.geosmser.GeoSMSService.IGeoSMSService;
import com.sahana.geosmser.overlay.DoubleCircleMark;
import com.sahana.geosmser.overlay.MapEventOverlay;
import com.sahana.geosmser.overlay.SingleLocationOverlay;
import com.sahana.geosmser.overlay.IDel.IGeoSMSPackBinder;
import com.sahana.geosmser.overlay.IDel.IMenuEvtOverlayLocation;
import com.sahana.geosmser.view.GeoSMSInformationPanel;
import com.sahana.geosmser.view.SMSDeliveryDialog;
import com.sahana.geosmser.view.SMSQueryView;
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
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class WhereToMeet extends MapActivity {
    
    public static final String TAG = "Debug Open GeoSMSer";
    
	public static final String FILE_PREFERENCE_MAINACT = "MAINACT_PREFERENCE";
	public static final String Key_Parcelable_SIS_GeoSMSLocationOrientation = "@SISGeoSMSLocationOrientation";
	public static final String Key_Parcelable_SIS_LastFingerLocation = "@SISLastFingerLocation";
	public static final String KEY_PARCELABLE_SIS_CURRENT_SMS_DELIVERY_GEOSMSPACK = "@SISCurrnetSMSDeliveryGeoSMSPack";
	public static final String KEY_PARCELABLE_SIS_IS_FINISH_SMS_DELIVERY_DIALOG = "@SISIsFinishSMSDeliveryDialog";
	public static final String KEY_PARCELABLE_SIS_MESSAGE_SENT_DIALOG_PACK = "@SISMessageSentDialogPack";
	public static final int DIALOG_SMS_QUERY = 40;
	public static final int DIALOG_SMS_QUERY_MESSAGESENDING = 41;
	public static final int DIALOG_SMS_QUERY_MESSAGESENT = 42;
	public static final int DIALOG_SMS_QUERY_EXIT_CONFORM = 43;
	public static final int DIALOG_SMS_DELIVERY = 50;
	public static final int DIALOG_SMS_DELIVERY_MESSAGESENDING = 51;
	public static final int DIALOG_SMS_DELIVERY_MESSAGESENT = 52;
	public static final int DIALOG_SMS_DELIVERY_EXIT_SMS_CONFORM = 53;
	private static final int DIALOG_WHERE_TO_MEET_ABOUT = 54;
	public static final int DIALOG_SMS_MYLOCATION_SEND = 60;
	
	public static final int CODE_DISMISS_DIALOG_SMS_DELIVERY = 70;
	public static final int CODE_SUBACTIVITY_SELECT_GEOSMSLOCATION = 10;
	
	private MapActivity me;
	private MapView mainMap;
	private RelativeLayout mainLayout;
	private MenuItem[] menuItems;
	private SharedPreferences mainActPreference;
	
	private MenuEvtMarkFingerLocation fingerLocationController;
	private MenuEvtMarkGeoSMSLocation geosmsLocationController;
	private MenuEvtMarkMyLocation myLocationController;
	
	private GeoSMSServiceController geosmsServiceController;
	
	private LayoutInflater layoutInflaterFactory;
	private SMSDeliveryDialog mSMSDeliveryView;
	private GeoSMSPack curSelectedGeoSMSPackForSMSDelivery;
	
	private SMSQueryView mSMSQueryView;
	private HanSMSQueryDialog mHanSMSQueryDialog;
	private HanSMSDeliveryDialog mHanSMSDeliveryDialog;
	
	private boolean isFinishSMSDeliveryDialog;
	private AlertDialog adSMSDVMessageSent;
	private ProgressDialog pdSMSDVMessageSending;
	private HanMessageSentDialogPack hanMessageSentDialogPack;
	private DialogEvtDisableKeyBackOnKeyListener evtDialogDisableKeyBack;
	
	private boolean isGetPackFromSMSInboxResult = false;


	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.where_to_meet);
        init();
        initEvents();
    }
    
    @Override
    protected void onResume() {
    	 super.onResume();
         autoEnableMyLocation();
         autoEnableGeoSMSLocation();
         myLocationController.enable();
         geosmsServiceController.bindService();
    }
    
    @Override
    protected void onPause() {
    	myLocationController.disable();
    	geosmsServiceController.unbindService();
    	super.onPause();
    }

    @Override
    protected void onDestroy() {
    	if(mSMSDeliveryView.isSMSReceiverRegistered()) {
			mSMSDeliveryView.unregisterSMSSendDeliveryReceiver();
		}
    	mSMSQueryView.smsWriter.close();
    	super.onDestroy();
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   // SubMenu mAdvanceSubMenu = menu.addSubMenu(0, 7, 6, R.string.menu_advance);
	    menuItems = new MenuItem[]{menu.add(0, 1, 1, R.string.menu_sahana_inbox),
								   menu.add(0, 2, 2, R.string.menu_my_location),
								   menu.add(0, 3, 3, R.string.menu_geosms_location),
								   //menu.add(0, 4, 4, R.string.menu_configure),
								   //mAdvanceSubMenu.add(0, 1, 1, R.string.menu_configure),
								   menu.add(0, 4, 5, R.string.menu_where_to_meet_about),
								   menu.add(0, 5, 4, R.string.menu_sendmylocation),
								   menu.add(0, 6, 5, R.string.menu_exit),
								  
								   //mAdvanceSubMenu.add(0, 1, 1, R.string.menu_edit_whitelist)
								   };
	    
		menuItems[0].setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			// start GeoSMS-Manager Activity by the menu
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				Intent i = new Intent(me, GeoSMSManagerAct.class);
				i.putExtra(GeoSMSManagerAct.KEY_LAUNCH_MODE, GeoSMSManagerAct.ID_LAUNCH_MODE_SUBACTIVITY);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				//startActivity(i);
				
				startActivityForResult(i, CODE_SUBACTIVITY_SELECT_GEOSMSLOCATION);
				return true;
			}
		});
		
		menuItems[1].setOnMenuItemClickListener(myLocationController);
		menuItems[2].setOnMenuItemClickListener(geosmsLocationController);
//		menuItems[3].setOnMenuItemClickListener(new MenuEvtOnQueryListener());
		menuItems[3].setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_WHERE_TO_MEET_ABOUT);
				return true;
			}
		});
		menuItems[4].setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				myLocationController.sendMyLocation();
				return true;
			}
		});

		menuItems[5].setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				me.finish();
				return true;
			}
		});
		
		
//		.new.new MenuEvtExitApplicatioin());
//		
//		// exit this application (kill the application process) by the menu
//		private class MenuEvtExitApplicatioin implements OnMenuItemClickListener {
//			@Override
//			public boolean onMenuItemClick(MenuItem item) {
//				me.finish();
//				return true;
//			}
//		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		//original
		//setGeoSMSLocationMenuItemVisible(geosmsLocationController.isGeoSMSLocationEnable());
		//Ye add 
		setGeoSMSLocationMenuItemVisible(false);
		return super.onMenuOpened(featureId, menu);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case CODE_SUBACTIVITY_SELECT_GEOSMSLOCATION:
				if(resultCode == RESULT_OK) {
					GeoSMSPack pack = data.getParcelableExtra(GeoSMSManagerAct.Key_SUBACTIVITY_RESULTPACK);
					if(pack != null) {
						isGetPackFromSMSInboxResult = true;
						geosmsLocationController.setLastSource(pack);
						geosmsLocationController.startGeoSMSLocation(pack, true);
						data.removeExtra(GeoSMSManagerAct.Key_SUBACTIVITY_RESULTPACK);
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		int informationViewInflateCode = geosmsLocationController.informationRenderer.isInflated() ? 1 : 0;
		GeoSMSLocationOrientationPack geoSMSLocationOrientationPack = new GeoSMSLocationOrientationPack(geosmsLocationController.getSource(), informationViewInflateCode);
		outState.putParcelable(Key_Parcelable_SIS_GeoSMSLocationOrientation, geoSMSLocationOrientationPack);
		
		outState.putParcelable(Key_Parcelable_SIS_LastFingerLocation, fingerLocationController.getSource()); // save FingerLocation on the map
		outState.putParcelable(KEY_PARCELABLE_SIS_CURRENT_SMS_DELIVERY_GEOSMSPACK, getCurrentSelectedGeoSMSPackForSMSDelivery()); // save GeoSMSPack of the location tapped by user
		outState.putBoolean(KEY_PARCELABLE_SIS_IS_FINISH_SMS_DELIVERY_DIALOG, isFinishSMSDeliveryDialog);
		outState.putParcelable(KEY_PARCELABLE_SIS_MESSAGE_SENT_DIALOG_PACK, hanMessageSentDialogPack); // save MESSAGESENT dialog info
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		GeoSMSLocationOrientationPack geoSMSLocationOrientationPack = savedInstanceState.getParcelable(Key_Parcelable_SIS_GeoSMSLocationOrientation);
		geosmsLocationController.setLastSource(geoSMSLocationOrientationPack.mGeoSMSPack);
		geosmsLocationController.isInformationViewInflated = (geoSMSLocationOrientationPack.mInflateCode == 1)?true:false;
		
		GeoSMSPack gp = savedInstanceState.getParcelable(Key_Parcelable_SIS_LastFingerLocation);
		fingerLocationController.setSource(gp);
		
		gp = savedInstanceState.getParcelable(KEY_PARCELABLE_SIS_CURRENT_SMS_DELIVERY_GEOSMSPACK);
		if(gp != null) setCurrentSelectedGeoSMSPackForSMSDelivery(gp);
		
		isFinishSMSDeliveryDialog = savedInstanceState.getBoolean(KEY_PARCELABLE_SIS_IS_FINISH_SMS_DELIVERY_DIALOG);
		hanMessageSentDialogPack = savedInstanceState.getParcelable(KEY_PARCELABLE_SIS_MESSAGE_SENT_DIALOG_PACK);
		mHanSMSDeliveryDialog.postMessageSent();
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_SMS_DELIVERY:
				 SMSDelivery sDelivery = (SMSDelivery) new FragmentActivity();
				 sDelivery.showDialog();
				 break;
			case DIALOG_SMS_DELIVERY_MESSAGESENDING:
			case DIALOG_SMS_QUERY_MESSAGESENDING:
				pdSMSDVMessageSending = new ProgressDialog(me);
				pdSMSDVMessageSending.setTitle(R.string.dialog_geosms_delivery_message_sending_title);
				pdSMSDVMessageSending.setMessage(getString(R.string.dialog_geosms_delivery_message_sending_message));
				pdSMSDVMessageSending.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				pdSMSDVMessageSending.setOnKeyListener(evtDialogDisableKeyBack);
				return pdSMSDVMessageSending;
			case DIALOG_SMS_DELIVERY_MESSAGESENT:
				AlertDialog.Builder adBuilderMsgSent = new AlertDialog.Builder(me);
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
				return new AlertDialog.Builder(me)
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
						me.showDialog(DIALOG_SMS_DELIVERY);
					}
				})
				.create();
			case DIALOG_SMS_QUERY:
				return new AlertDialog.Builder(me)
				.setTitle(R.string.dialog_geosms_query_title)
				.setView(mSMSQueryView)
				.setOnKeyListener(new DialogEvtDisableSMSQueryDialogKeyBackOnKeyListener())
				.create();
			case DIALOG_SMS_QUERY_MESSAGESENT:
				return new AlertDialog.Builder(me)
				.setTitle(R.string.dialog_geosms_delivery_message_sent_title)
				.setMessage(R.string.dialog_geosms_delivery_message_sent_message)
				.setPositiveButton(R.string.button_sure_text, new DialogInterface.OnClickListener() {
					@Override 
					public void onClick(DialogInterface dialog, int which) {
						mSMSQueryView.clearInputField();
						dismissDialog(DIALOG_SMS_QUERY_MESSAGESENT);
					}
				})
				.create();
			case DIALOG_SMS_QUERY_EXIT_CONFORM:
				return new AlertDialog.Builder(me)
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
						me.showDialog(DIALOG_SMS_QUERY);
					}
				})
				.create();
			case DIALOG_WHERE_TO_MEET_ABOUT:
				return new AlertDialog.Builder(me)
				.setTitle(R.string.dialog_where_to_meet_about_title)
				.setMessage(R.string.dialog_where_to_meet_about_message)
				.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})			
				.create();
				

		}
		return super.onCreateDialog(id);
	}
	
	@Override 
	protected void onPrepareDialog(int id, Dialog pDialog) {
		switch (id) {
			case DIALOG_SMS_DELIVERY_MESSAGESENDING:
			case DIALOG_SMS_QUERY_MESSAGESENDING:
				ProgressDialog dialog = (ProgressDialog)pDialog;
				dialog.setTitle(R.string.dialog_geosms_delivery_message_sending_title);
				dialog.setMessage(getString(R.string.dialog_geosms_delivery_message_sending_message));
				break;
			case DIALOG_SMS_DELIVERY_MESSAGESENT:
				setSMSDLMessageSentDialogStatus();
				//dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
				break;
			case DIALOG_SMS_QUERY_MESSAGESENT:
				AlertDialog queryDialog = (AlertDialog) pDialog;
				queryDialog.setMessage(me.getString(R.string.message_sent_result_ok));
				break;
		}
		super.onPrepareDialog(id, pDialog);
	}
	
	private void init() {
		me = this;
		mainMap = (MapView) findViewById(R.id.mainMap);
		mainMap.setBuiltInZoomControls(true);
		mainLayout = (RelativeLayout)findViewById(R.id.mainlayout);
		
		mainActPreference = me.getSharedPreferences(FILE_PREFERENCE_MAINACT, Activity.MODE_PRIVATE);
		
		MapEventOverlay overlayEvent = new MapEventOverlay(mainMap);
		overlayEvent.setOnTapListener(new MapEvtOnTapListener());
		overlayEvent.setOnTapDownListener(new MapEvtOnTapDownListener());
		
		fingerLocationController = new MenuEvtMarkFingerLocation();
		geosmsLocationController = new MenuEvtMarkGeoSMSLocation();
		myLocationController = new MenuEvtMarkMyLocation();
		
		geosmsServiceController = new GeoSMSServiceController();
		
		layoutInflaterFactory = LayoutInflater.from(this);
		mSMSDeliveryView = new SMSDeliveryDialog(getApplicationContext());
		
		evtDialogDisableKeyBack = new DialogEvtDisableKeyBackOnKeyListener();
		hanMessageSentDialogPack = new HanMessageSentDialogPack();
		mHanSMSDeliveryDialog = new HanSMSDeliveryDialog();
		
		mSMSQueryView = (SMSQueryView) layoutInflaterFactory.inflate(R.layout.sms_query_view, null);
		mHanSMSQueryDialog = new HanSMSQueryDialog();
		mHanSMSQueryDialog.setView(mSMSQueryView);
	
	}
	
	private void initEvents() {
		myLocationController.bindMapView(mainMap);
		geosmsLocationController.setOverlay(new GeoSMSLocationOverlay());
		geosmsLocationController.bindMapView(mainMap);
		fingerLocationController.setOverlay(new FingerLocationOverlay());
		fingerLocationController.bindMapView(mainMap);
		
		mSMSDeliveryView.registerSMSSendDeliveryReceiver();
		/* not needed now as sent through SMSDeliveryDialog Constructor
        mSMSDeliveryView.setOnSourceBindingListener(new SMSDVEvtOnSourceBindingListene());
        mSMSDeliveryView.setMessageSentHandler(mHanSMSDeliveryDialog);
        */
        		
		mSMSQueryView.setMessageSentHandler(mHanSMSQueryDialog);
		mSMSQueryView.smsWriter.open();
	}
		
	private void autoEnableMyLocation() {
		if(myLocationController.isMyLocationKeepEnabling() && !myLocationController.isStartMyLocation()) {
			myLocationController.startMyLocation();
		}
	}
	
	private void autoEnableGeoSMSLocation() {
	    if(!isGetPackFromSMSInboxResult) {
			Intent i = this.getIntent();
			GeoSMSPack pack = i.getParcelableExtra(GeoSMSService.Key_Extra_SMSPack);
			if(pack != null) {
				geosmsLocationController.startGeoSMSLocation(pack, true);
				i.removeExtra(GeoSMSService.Key_Extra_SMSPack);
			}
			else if(geosmsLocationController.isCachedSource()) {
				geosmsLocationController.recoverGeoSMSLocation();
			}
		}
		isGetPackFromSMSInboxResult = false;
	}
	
	private void setGeoSMSLocationMenuItemVisible(boolean visible) {
		if(menuItems != null) {
			menuItems[2].setVisible(visible);
		}
	}
	
	//Ye add mothod
	private void setGeoSMSLocationMenuItemUnVisible(boolean visible) {
		if(menuItems != null) {
			menuItems[2].setVisible(false);
		}
	}
	
	private GeoSMSPack getCurrentSelectedGeoSMSPackForSMSDelivery() {
		return curSelectedGeoSMSPackForSMSDelivery;
	}
	
	private void setCurrentSelectedGeoSMSPackForSMSDelivery(GeoSMSPack pack) {
		if(curSelectedGeoSMSPackForSMSDelivery == null) curSelectedGeoSMSPackForSMSDelivery = new GeoSMSPack();
		curSelectedGeoSMSPackForSMSDelivery.assign(pack);
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
	
	private void startDeliveryGeoSMS(GeoSMSPack pack) {
		setCurrentSelectedGeoSMSPackForSMSDelivery(pack);
		me.showDialog(DIALOG_SMS_DELIVERY);
	}
	
	
	private class MapEvtOnTapListener implements MapEventOverlay.ITapEventListener {
		
		public MapEvtOnTapListener() {
			DoubleCircleMark mark = new DoubleCircleMark();
			mark.setDefaultPaints();	
		}
		
		@Override
		public boolean onTap(GeoPoint point, MapView mapView) {
			fingerLocationController.setLocation(point);
			geosmsLocationController.inflateInformationView(false);
			return true;
		}
	}
	
	
	private class MapEvtOnTapDownListener implements MapEventOverlay.ITouchTapEventListener {
		@Override
		public boolean onEvent(MotionEvent e) {
			return false;
		}
	}
	
	
	private class FingerLocationOverlay extends SingleLocationOverlay {
		private GeoSMSPack boundPack = new GeoSMSPack();
		
		@Override
		public boolean onTap(GeoPoint gp, MapView mapView) {
			if(areaCircle != null) {
				Point point = new Point(); 
				mapView.getProjection().toPixels(gp, point);
				if(areaCircle.contains(point.x, point.y)) {
					sourceBinder.onBind(boundPack);
					startDeliveryGeoSMS(boundPack);
					//Toast.makeText(me, boundPack.toString(), Toast.LENGTH_LONG).show();
					return true;
				}
			}
			return false;
		}
	}
	
	
	private class MenuEvtMarkFingerLocation implements IMenuEvtOverlayLocation<FingerLocationOverlay> {
		private FingerLocationOverlay fingerLocationOverlay;
		private GeoSMSPack sourcePack;
		private MapView mapStub;
		
		@Override
		public void bindMapView(MapView mapView) {
			mapStub = mapView;
			mapStub.getOverlays().add(fingerLocationOverlay);
		}

		@Override
		public GeoPoint getLocation() {
			return fingerLocationOverlay.getLocation();
		}

		@Override
		public FingerLocationOverlay getOverlay() {
			return fingerLocationOverlay;
		}

		@Override
		public GeoSMSPack getSource() {
			return sourcePack;
		}

		@Override
		public void moveToLocation() {
			MapController mc = mapStub.getController();
			GeoPoint point = getLocation();
			mc.animateTo(point);
		}

		@Override
		public void setLocation(GeoPoint location) {
			try {
				if(sourcePack == null) {
					sourcePack = GeoSMSPackFactory.createBasicPack(location);
				}
				else {
					sourcePack.setPositionFromGeoPoint(location);
				}
				fingerLocationOverlay.setPosition(location);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void setOverlay(FingerLocationOverlay overlay) {
			fingerLocationOverlay = overlay;
			fingerLocationOverlay.setSourceBinder(sourceBinder);
		}

		@Override
		public void setSource(GeoSMSPack pack) {
			sourcePack = pack;
			if(sourcePack != null) // source assignment only driven on tapping a finger location overlay 
				fingerLocationOverlay.setPosition(sourcePack.toGeoPoint());
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			moveToLocation();
			return true;
		}
		
		public IGeoSMSPackBinder sourceBinder = new IGeoSMSPackBinder() {
			@Override
			public void onBind(final GeoSMSPack pack) {
				pack.assign(sourcePack);
			}
		};
	}
	
	
	
	private class GeoSMSLocationOverlay extends SingleLocationOverlay {
		private GeoSMSPack boundPack = new GeoSMSPack();
		
		@Override
		public void initMark() {
			mark.setDefaultPaints();
			mark.areaCirclePaint.setARGB(80, 213, 137, 135); 
			mark.centerCirclePaint.setARGB(255, 192, 80, 77);
			mark.areaBorderPaint.setARGB(255, 192, 80, 77); 
		}

		@Override
		public boolean onTap(GeoPoint gp, MapView mapView) {
			
			if(areaCircle != null) {
				Point point = new Point(); 
				mapView.getProjection().toPixels(gp, point);
				if(areaCircle.contains(point.x, point.y)) {
					sourceBinder.onBind(boundPack);
					startDeliveryGeoSMS(boundPack);
					return true;
				}
			}
			return false;
		}
	}
	
	
	private class MenuEvtMarkGeoSMSLocation implements IMenuEvtOverlayLocation<GeoSMSLocationOverlay> {
		private GeoSMSLocationOverlay geosmsLocationOverlay;
		private MapView mapStub;
		private GeoSMSPack gPack, lastGPack;
		private boolean isEnabled = false;
		private GeoSMSInformationPanel informationRenderer = new GeoSMSInformationPanel(me);
		private View informationView;
		
		public boolean isInformationViewInflated = true;
		
		public void setLastSource(GeoSMSPack pack) {
			lastGPack = pack;
		}
		
		public boolean isCachedSource() {
			return (lastGPack != null);
		}
		
		public boolean isGeoSMSLocationEnable() {
			return isEnabled;
		}
		
		@Override
		public void bindMapView(MapView mapView) {
			mapStub = mapView;
		}

		@Override
		public GeoPoint getLocation() {
			return geosmsLocationOverlay.getLocation();
		}

		@Override
		public GeoSMSLocationOverlay getOverlay() {
			return geosmsLocationOverlay;
		}

		@Override
		public com.sahana.geosmser.GeoSMSPack getSource() {
			return gPack;
		}

		@Override
		public void moveToLocation() {
			if(mapStub != null) {
				MapController mc = mapStub.getController();
				GeoPoint gloc = getLocation();
				if(gloc != null) {
					mc.animateTo(gloc);
				}
			}
		}

		@Override
		public void setLocation(GeoPoint location) {
			geosmsLocationOverlay.setPosition(location);
		}

		@Override
		public void setOverlay(GeoSMSLocationOverlay overlay) {
			geosmsLocationOverlay = overlay;
			geosmsLocationOverlay.setSourceBinder(sourceBinder);
		}

		@Override
		public void setSource(com.sahana.geosmser.GeoSMSPack pack) {
			if(pack != null) {
				gPack = pack;
				if(geosmsLocationOverlay != null) {
					geosmsLocationOverlay.setPosition(gPack.toGeoPoint());
					mapStub.getOverlays().add(geosmsLocationOverlay);
					isEnabled = true;
				}
			}
			else gPack = null;
		}

		@Override
		public boolean onMenuItemClick(MenuItem arg0) {
			moveToLocation();
			inflateInformationView(true);
			return true;
		}
		
		public void startGeoSMSLocation(GeoSMSPack pack, boolean movable) {
			setSource(pack);
			if(movable) {
				geosmsLocationController.moveToLocation();
			}
			renderInformationView(true);
		}
		
		public void inflateInformationView(boolean isFlated) {
			if(informationView != null) {
				informationRenderer.inflate(isFlated);
			}
		}
		
		public void renderInformationView(boolean isInflated) {
			View view = informationRenderer.render(gPack);
			if(informationView != null) {
				mainLayout.removeView(informationView);	
			}
			if(view != null) {
				informationView = view;
				informationRenderer.inflate(isInflated);
				mainLayout.addView(informationView);
			}
		}
		
		public void recoverGeoSMSLocation() {
			setSource(lastGPack);
			if(informationView != null) {
				GeoSMSInformationPanel panel = (GeoSMSInformationPanel) informationView;
				isInformationViewInflated = panel.isInflated();
			}
			renderInformationView(isInformationViewInflated);
		}
		
		public IGeoSMSPackBinder sourceBinder = new IGeoSMSPackBinder() {
			@Override
			public void onBind(final GeoSMSPack pack) {
				pack.assign(gPack);
			}
		};
	}
	
	
//	private class MenuEvtSendMyLocation implements OnMenuItemClickListener {
//		@Override 
//		public boolean onMenuItemClick(MenuItem item) {
//			myLocationController.sendMyLocation();
//			return true;
//		}
//	}

	
	private class MenuEvtMarkMyLocation implements OnMenuItemClickListener {
		public final String KEY_KEEPENABLING = "KEY_KEEPENABLING";
		public final boolean VALUE_KEEPENABLING_DEFAULT = true;
		
		public MyLocationOverlayImpl myLocationOverlay;
		private MapView mapStub;
		private volatile boolean isEnabled;
		private volatile boolean isMovable;
		private boolean isOpenSMSDeliveryDialog;
		private Context baseContext;
		private Handler hanFirstLocationFix;
		
		private MenuEvtMarkMyLocation() {
			init();
			initEvent();
		}
		
		private void init() {
			baseContext = getApplicationContext();
			isEnabled = false;
			isMovable = false;
			isOpenSMSDeliveryDialog = false;
		}
		
		private void initEvent() {
			hanFirstLocationFix = new HanFirstMyLocationFound(); 
		}
		
		private void bindMapView(MapView pMapView) {
			mapStub = pMapView;
			myLocationOverlay = new MyLocationOverlayImpl(baseContext, pMapView);
		}
		
		@Override 
		public boolean onMenuItemClick(MenuItem item) {
			isMovable = true;
			isOpenSMSDeliveryDialog = false;
			return findMyLocation();
		}
		
		public boolean findMyLocation() {
			if(myLocationOverlay != null) {
				geosmsLocationController.inflateInformationView(false);
				if(!isEnabled) {
					startMyLocation();
					Toast.makeText(baseContext, R.string.mylocation_finding, Toast.LENGTH_SHORT).show();
				}
				else {
					GeoPoint point = myLocationOverlay.getMyLocation();
					if(point != null) {
						mapStub.getController().animateTo(point);
						if(isOpenSMSDeliveryDialog) {
							GeoSMSPack pack = GeoSMSPackFactory.createBasicPack(point);
							startDeliveryGeoSMS(pack);
						}
					}
					else {
						Toast.makeText(baseContext, R.string.mylocation_finding, Toast.LENGTH_SHORT).show();
						startMyLocation();
					}
				}
				return true;
			}
			return false;
		}
		
		public boolean isStartMyLocation() {
			return isEnabled;
		}
		
		
		public void startMyLocation() {
			if(!isEnabled) {
				isEnabled = true;
				enable();
				mapStub.getOverlays().add(myLocationOverlay);
				
				LocationFix EvtOnFoundMyLocation = new LocationFix();
		        myLocationOverlay.runOnFirstFix(EvtOnFoundMyLocation);
		        
		        writeMyLocationKeepEnabling(true);
			}
		}
		
		public void stopMyLocation() {
			if(isEnabled) {
				disable();
				isEnabled = false;
				mapStub.getOverlays().remove(myLocationOverlay);
				writeMyLocationKeepEnabling(false);
				
			}
		}
		
		public void sendMyLocation() {
			isMovable = true;
			isOpenSMSDeliveryDialog = true;
			findMyLocation();
		}
		
		public boolean writeMyLocationKeepEnabling(boolean enable) {
			SharedPreferences.Editor edt = mainActPreference.edit();
			
			edt.putBoolean(KEY_KEEPENABLING, enable);
			return edt.commit();
		}
		
		public boolean isMyLocationKeepEnabling() {
			return mainActPreference.getBoolean(KEY_KEEPENABLING, VALUE_KEEPENABLING_DEFAULT);
		}
		
		public Overlay getOverlay() {
			return myLocationOverlay;
		}
		
		public void enable() {
			if(myLocationOverlay != null && isEnabled && !myLocationOverlay.isMyLocationEnabled()) {
				myLocationOverlay.enableMyLocation();
			}
		}
		
		public void disable() {
			if(myLocationOverlay != null && isEnabled && myLocationOverlay.isMyLocationEnabled()) {
				myLocationOverlay.disableMyLocation();
			}
		}
		
		private class HanFirstMyLocationFound extends Handler {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
					case 1:
						
						break;
					case 2:
						
						if(isOpenSMSDeliveryDialog) {
							GeoSMSPack pack = GeoSMSPackFactory.createBasicPack((GeoPoint) msg.obj);
							startDeliveryGeoSMS(pack);
						}
						break;					
				}
				super.handleMessage(msg);
			}
		}
		
		private class LocationFix implements Runnable {
			@Override
			public void run() {
				GeoPoint point = myLocationOverlay.getMyLocation();
				Message msg = new Message();
				if(point != null) {
					msg.what = 1;
					if(isMovable) {
						mapStub.getController().animateTo(point);
						msg.what = 2;
					}
					msg.obj = point;	
				}
				else {
					msg.what = 0;
				}
				hanFirstLocationFix.sendMessage(msg);
			}
		}
		
		private class MyLocationOverlayImpl extends MyLocationOverlay {
			private RectF myLocationRectF;
			private DoubleCircleMark mark;
			private Point drawPoint; 		
			
			public MyLocationOverlayImpl(Context context, MapView mapView) {
				super(context, mapView);
				init();
			}
			
			private void init() {
				myLocationRectF = null;
				mark = new DoubleCircleMark();
				initMark();
				drawPoint = new Point();
			}
			
			public void initMark() {	
				mark.setDefaultPaints();				
				mark.areaCirclePaint.setARGB(80, 99, 184, 255);
				mark.centerCirclePaint.setARGB(255, 24, 116, 205);
				mark.areaBorderPaint.setARGB(255, 24, 116, 205);
			}
			
			@Override
			public boolean onTap(GeoPoint gp, MapView map) {
				if(myLocationRectF != null) {
					Point point = new Point(); 
					mapStub.getProjection().toPixels(gp, point);
					if(myLocationRectF.contains(point.x, point.y)) {
						try {
							GeoSMSPack pack = GeoSMSPackFactory.createBasicPack(getMyLocation());
							startDeliveryGeoSMS(pack);
							return super.onTap(gp, map);
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
				}
				return false; 
			}
			
			@Override
			public synchronized boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
				//GeoPoint Position = myLocationOverlay.getMyLocation();
				GeoPoint Position = this.getMyLocation();
				
				if(Position != null) {
					mapView.getProjection().toPixels(Position, drawPoint);
					mark.drawDoubleCircleMark(canvas, drawPoint);
					myLocationRectF = mark.areaCircleRectF;
					return true;
				}
				return super.draw(canvas, mapView, shadow, when);
			}
		}
	}
	
//	// start GeoSMS-Manager Activity by the menu
//	private class MenuEvtOpenGeoSMSManager implements OnMenuItemClickListener {
//		@Override
//		public boolean onMenuItemClick(MenuItem item) {
//			Intent i = new Intent(me, GeoSMSManagerAct.class);
//			i.putExtra(GeoSMSManagerAct.KEY_LAUNCH_MODE, GeoSMSManagerAct.ID_LAUNCH_MODE_SUBACTIVITY);
//			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
//			//startActivity(i);
//			
//			startActivityForResult(i, CODE_SUBACTIVITY_SELECT_GEOSMSLOCATION);
//			return true;
//		}
//	}
	
	

	

	/** Register GeoSMSService first */
	private class MenuEvtOnQueryListener implements OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem arg0) {
			me.showDialog(DIALOG_SMS_QUERY);
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
	
	// disable back key events driven by a dialog
	private class DialogEvtDisableKeyBackOnKeyListener implements DialogInterface.OnKeyListener {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK) return true;
			return false;
		}
	}
	
	
	private class SMSDVEvtOnSourceBindingListene implements SMSDeliveryDialog.ISMSDeliveryRenderer.OnSourceBindingListener {
		@Override
		public void onSourceBind(GeoSMSPack pack) {
			GeoSMSPack p = getCurrentSelectedGeoSMSPackForSMSDelivery();
			pack.assign(p);
		}
	}
	
	
	private class DialogEvtDisableSMSDeliveryDialogKeyBackOnKeyListener implements DialogInterface.OnKeyListener {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {		
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				me.dismissDialog(DIALOG_SMS_DELIVERY);
				if(!mSMSDeliveryView.getMessageFieldText().trim().equals("")
				        || !mSMSDeliveryView.getPhoneFieldText().trim().equals("")
				      //  || !mSMSDeliveryView.getReverseCodeFieldText().trim().equals("")
				) {
					me.showDialog(DIALOG_SMS_DELIVERY_EXIT_SMS_CONFORM);
				}
				else {
					mSMSDeliveryView.clearSavedState();
				}
				return true;
			}
			return false;
		}
	}
	
	
	private class HanSMSDeliveryDialog extends Handler {
		private int checkingTimes = 0;
		private Handler messageSentCheckingHandler = new Handler();
		
		private Runnable mMessageSentCheckingTask = new Runnable() {
			@Override
			public void run() {
				if(checkingTimes < 15) {
					if(hanMessageSentDialogPack.isFinish) {
						me.dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
					}
					else {
						checkingTimes += 1;
						messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
					}
				}
				else {
					if(!hanMessageSentDialogPack.isFinish) {
						me.dismissDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
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
					me.showDialog(DIALOG_SMS_DELIVERY_MESSAGESENT);
					break;
				case DIALOG_SMS_DELIVERY_MESSAGESENDING:
					me.dismissDialog(DIALOG_SMS_DELIVERY);
					me.showDialog(DIALOG_SMS_DELIVERY_MESSAGESENDING);
					
					hanMessageSentDialogPack.handler = messageSentCheckingHandler;
					messageSentCheckingHandler.removeCallbacks(mMessageSentCheckingTask);
					messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
					break;
				case CODE_DISMISS_DIALOG_SMS_DELIVERY:
					me.dismissDialog(DIALOG_SMS_DELIVERY);
					break;
			}
		}
	}
	
	
	private class DialogEvtDisableSMSQueryDialogKeyBackOnKeyListener implements DialogInterface.OnKeyListener {		
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				dialog.dismiss();
				if(!mSMSQueryView.getPhoneFieldText().trim().equals("")) {
					me.showDialog(DIALOG_SMS_QUERY_EXIT_CONFORM);
				}
				return true;
			}
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
						me.dismissDialog(DIALOG_SMS_QUERY_MESSAGESENDING);
						me.showDialog(DIALOG_SMS_QUERY_MESSAGESENT);
					case -1:
						
					}
				}
				else {
					me.dismissDialog(DIALOG_SMS_QUERY_MESSAGESENDING);
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
				case DIALOG_SMS_QUERY_MESSAGESENDING:
					me.dismissDialog(DIALOG_SMS_QUERY);
					me.showDialog(DIALOG_SMS_QUERY_MESSAGESENDING);
					checkingTimes = 0;
					messageSentCheckingHandler.removeCallbacks(mMessageSentCheckingTask);
					messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
			}
		}
	}
	
	private static class GeoSMSLocationOrientationPack implements Parcelable {
		public GeoSMSPack mGeoSMSPack;
		public int mInflateCode;
		
		public static final Parcelable.Creator<GeoSMSLocationOrientationPack> CREATOR = new Parcelable.Creator<GeoSMSLocationOrientationPack>() {
			public GeoSMSLocationOrientationPack createFromParcel(Parcel in) {
				return new GeoSMSLocationOrientationPack(in);
	        }

	        public GeoSMSLocationOrientationPack[] newArray(int size) {
	            return new GeoSMSLocationOrientationPack[size];
	        }
		};
		
		public GeoSMSLocationOrientationPack(GeoSMSPack pack, int code) { 
			mGeoSMSPack = pack;
			mInflateCode = code;
		}
		
		public GeoSMSLocationOrientationPack(Parcel in) {
			readFromParcel(in);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(mGeoSMSPack, flags);
			dest.writeInt(mInflateCode);
		}
		
		public void readFromParcel(Parcel in) {
			mGeoSMSPack = in.readParcelable(GeoSMSPack.class.getClassLoader());
			mInflateCode = in.readInt();
		}

		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	/*
	 * Cannot instantiate fragment inside MapActivty so created external
	 * class extending Fragment and open the SMSDeliveryDialog from there 
	 */
	
	private class SMSDelivery extends FragmentActivity{
		
			public void showDialog(){
				/*
				 * mHanSMSDelivery and SMSDVEvtOnSourceBindingListene are initialized 
            	 * when an SMSDeliveryDialog is launched or opened
            	 */
            	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            	DialogFragment dialogFragment = new SMSDeliveryDialog(getApplicationContext(),new SMSDVEvtOnSourceBindingListene(),mHanSMSDeliveryDialog);
            	dialogFragment.show(ft, "openDialog");
			}
	}

}
