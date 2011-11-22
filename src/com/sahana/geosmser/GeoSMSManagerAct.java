package com.sahana.geosmser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import com.OpenGeoSMS.GeoSMS;
import com.OpenGeoSMS.format.GeoSMSFormat;
import com.sahana.geosmser.R;
import com.google.android.maps.GeoPoint;
import com.sahana.geosmser.database.QueryRepliedIDDBAdapter;
import com.sahana.geosmser.database.WhiteListDBAdapter;
import com.sahana.geosmser.gps.MyLocation;
import com.sahana.geosmser.gps.MyLocation.ProvideType;
import com.sahana.geosmser.view.GeoSMSListItemView;
import com.sahana.geosmser.view.MyLocationStatusView;
import com.sahana.geosmser.view.SMSQueryView;
import com.sahana.geosmser.widget.GeoSMSListItem;
import com.sahana.geosmser.widget.MapParcelableWrapper;
import com.sahana.geosmser.widget.SMSListAdapter;
import com.sahana.geosmser.widget.SMSListAdapter.SMSColumn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GeoSMSManagerAct extends Activity {
    private final static int LIST_SMS_QUERY_TOKEN = 1001;
    private final static int DELETE_SMS_TOKEN = 1002;
    public final static String VALUE_SMSINBOX_URISTRING = "content://sms/inbox";
    public final static String VALUE_SMS_URISTRING = "content://sms";
    public final static int ID_ALERTDIALOG_DELETEGEOSMS = 2001;
    public final static int ID_LAUNCH_MODE_STANDALONE = 1;
    public final static int ID_LAUNCH_MODE_SUBACTIVITY = 2;
    public final static String Key_SUBACTIVITY_RESULTPACK = "SubActivityResultPack";
    public final static String KEY_LAUNCH_MODE = "@LaunchMode";
    public static final String Key_SIS_DELETED_ID = "@DeletedID";
    public static final String Key_SIS_CACHED_LIST_ITEMS = "@GeoSMSCachedListItems";
    
    public static final String SHARED_PREFS_FILE = "com.geosmser";
    public static final String SHARED_PREFS_ID = "id";
    
    public static HashSet<Integer> mSMSRepliedIDTable;
    
    private String[] queryColProjection;
    private String querySelection;
    private String[] queryArgs;
    private Uri queryURI; 
    private int queryToken;
    public String mSMSSortOrder = "date DESC";
    
    private Object updateStateLock = new Object();
    private UpdateStateHandler updateStateHandler;
    private SMSListQueryHandler mQueryHandler;
    private SMSListAdapter mSMSListAdapter;
    //private GeoSMSParser mGeoSMSParser;
    private Activity me;
    
    private ListView lvSMSList;
    private TextView tvMessage;
    
    private int mCurrentGeoSMSID;
    private int mCurrentLaunchMode;
    
    private DeleteGeoSMSListener deleteGeoSMSListener;
    
    private SMSQueryView mSMSQueryView;
    
    private HanSMSQueryDialog mHanSMSQueryDialog;
    private LayoutInflater layoutInflaterFactory;
    private ProgressDialog pdSMSDVMessageSending;
    private DialogEvtDisableKeyBackOnKeyListener evtDialogDisableKeyBack;
    
    private GeoSMSPack curSelectedGeoSMSPackForSMSDelivery;
    
    //TODO For temporary, modify it future
    public static HashSet<Integer> SMSIDTable;
    private String curSelectedGeoSMSPackAddress;
    private int curSelectedGeoSMSListItemID = 0;
    
    private MyLocation mMySubjectLocationGPS;
    private MyLocation mMySubjectLocationAGPS;
    
    private Location curLocationGPS;
    private Location curLocationAGPS;
    
    public enum ViewState {
        showList, showMessage, showBusy, showItemLoading;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
        initEvent();
        setLaunchMode();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        startAsyncQuery();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSMSListAdapter.clear();
        mSMSQueryView.smsWriter.close();
        saveSharePreferences();
    }
    
    public void saveSharePreferences() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        try {
            editor.putString(SHARED_PREFS_ID, PreferenceSerializer.serialize(SMSIDTable));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Key_SIS_DELETED_ID, mCurrentGeoSMSID);
        outState.putParcelable(Key_SIS_CACHED_LIST_ITEMS, new MapParcelableWrapper(mSMSListAdapter.getCachedListItems()));
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mCurrentGeoSMSID = savedInstanceState.getInt(Key_SIS_DELETED_ID, -1);
        deleteGeoSMSListener.setID(mCurrentGeoSMSID);
        
        MapParcelableWrapper mapWrapper = savedInstanceState.getParcelable(Key_SIS_CACHED_LIST_ITEMS);
        mSMSListAdapter.setCachedListItems(mapWrapper.getMap());
        super.onRestoreInstanceState(savedInstanceState);
    }
    
    public static final int DIALOG_SMS_REPLY = 700;
    public static final int DIALOG_SMS_HAS_REPLIED = 701;
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case ID_ALERTDIALOG_DELETEGEOSMS:
                AlertDialog.Builder adBuilderDelGeoSMS = new AlertDialog.Builder(me);
                adBuilderDelGeoSMS.setTitle(R.string.dialog_geosms_inbox_delete_message_conform_title);
                adBuilderDelGeoSMS.setMessage(getString(R.string.dialog_geosms_inbox_delete_message_conform_message));
                adBuilderDelGeoSMS.setCancelable(true);
                adBuilderDelGeoSMS.setPositiveButton(R.string.button_yes_text, deleteGeoSMSListener);
                adBuilderDelGeoSMS.setNegativeButton(R.string.button_no_text, null);
                return adBuilderDelGeoSMS.create();
                
            case MainAct.DIALOG_SMS_QUERY_MESSAGESENDING:
                pdSMSDVMessageSending = new ProgressDialog(this);
                pdSMSDVMessageSending.setTitle(R.string.dialog_geosms_delivery_message_sending_title);
                pdSMSDVMessageSending.setMessage(getString(R.string.dialog_geosms_delivery_message_sending_message));
                pdSMSDVMessageSending.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pdSMSDVMessageSending.setOnKeyListener(evtDialogDisableKeyBack);
                return pdSMSDVMessageSending;
                
            case MainAct.DIALOG_SMS_QUERY:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_inbox_forward_title)
                .setView(mSMSQueryView)
                .setOnKeyListener(new DialogEvtDisableSMSQueryDialogKeyBackOnKeyListener())
                .create();
                
            case MainAct.DIALOG_SMS_QUERY_MESSAGESENT:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_delivery_message_sent_title)
                .setMessage(R.string.dialog_geosms_delivery_message_sent_message)
                .setPositiveButton(R.string.button_sure_text, new DialogInterface.OnClickListener() {
                    @Override 
                    public void onClick(DialogInterface dialog, int which) {
                        mSMSQueryView.clearInputField();
                        dismissDialog(MainAct.DIALOG_SMS_QUERY_MESSAGESENT);
                    }
                })
                .create();
                
            case MainAct.DIALOG_SMS_QUERY_EXIT_CONFORM:
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
                        showDialog(MainAct.DIALOG_SMS_QUERY);
                    }
                })
                .create();
                
            case DIALOG_SMS_REPLY:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_inbox_reply_title)
                .setCancelable(false)
                .setPositiveButton(R.string.button_yes_text, new SMSReplyOnClickListener())
                .setNegativeButton(R.string.button_no_text, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMySubjectLocationGPS.disable();
                        mMySubjectLocationAGPS.disable();
                    }
                    
                }).create();
                
            case DIALOG_SMS_HAS_REPLIED:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_geosms_inbox_reply_title)
                .setCancelable(false)
                .setMessage(R.string.dialog_geosms_inbox_replied_message)
                .setPositiveButton("Reply again", new SMSReplyOnClickListener())
                .setNegativeButton(R.string.button_no_text, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMySubjectLocationGPS.disable();
                        mMySubjectLocationAGPS.disable();
                    }
                }).create();
                
            
        }
        return super.onCreateDialog(id);
    }
    
    public class SMSReplyOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
                
            if (curLocationGPS != null) {
                    GeoPoint gPoint = new GeoPoint((int)(curLocationGPS.getLatitude()*1E6),(int)(curLocationGPS.getLongitude()*1E6));
                    GeoSMSPack gPack = GeoSMSPackFactory.createBasicPack(gPoint);
                    SmsManager.getDefault().sendTextMessage(curSelectedGeoSMSPackAddress, null, gPack.getHeaderString(), null, null);
                    mSMSListAdapter.notifyDataSetChanged();
                    SMSIDTable.add(curSelectedGeoSMSListItemID);
                } else if (curLocationAGPS != null) {
                    GeoPoint gPoint = new GeoPoint((int)(curLocationAGPS.getLatitude()*1E6),(int)(curLocationAGPS.getLongitude()*1E6));
                    GeoSMSPack gPack = GeoSMSPackFactory.createBasicPack(gPoint);
                    SmsManager.getDefault().sendTextMessage(curSelectedGeoSMSPackAddress, null, gPack.getHeaderString(), null, null);
                    mSMSListAdapter.notifyDataSetChanged();
                    SMSIDTable.add(curSelectedGeoSMSListItemID);
                } else {
                    Toast.makeText(me, R.string.toast_non_sending_geosms, Toast.LENGTH_SHORT).show();
                }
                
            mMySubjectLocationGPS.disable();
            mMySubjectLocationAGPS.disable();
        }
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog pDialog) {
        switch (id) {
            case ID_ALERTDIALOG_DELETEGEOSMS:
                deleteGeoSMSListener.setID(mCurrentGeoSMSID);
                break;
            case MainAct.DIALOG_SMS_QUERY_MESSAGESENDING:
                ProgressDialog dialog = (ProgressDialog)pDialog;
                dialog.setTitle(R.string.dialog_geosms_delivery_message_sending_title);
                dialog.setMessage(getString(R.string.dialog_geosms_delivery_message_sending_message));
                break;
            case MainAct.DIALOG_SMS_QUERY_MESSAGESENT:
                AlertDialog queryDialog = (AlertDialog) pDialog;
                queryDialog.setMessage(getString(R.string.message_sent_result_ok));
                break;
        }
        super.onPrepareDialog(id, pDialog);
    }

    private void init(Bundle savedInstanceState) {
        me = this;
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.geosms_manager);
        
        lvSMSList = (ListView) findViewById(R.id.geomsms_list);
        tvMessage = (TextView) findViewById(R.id.empty_message);
        mQueryHandler = new SMSListQueryHandler(me.getContentResolver());
        //mGeoSMSParser = GeoSMSParser.createDefaultParser();
        
        updateStateHandler = new UpdateStateHandler();
        deleteGeoSMSListener = new DeleteGeoSMSListener();
        
        initListAdapter();
        if (savedInstanceState != null) { }
        initListSMSQuery();
        
        layoutInflaterFactory = LayoutInflater.from(this);
        mSMSQueryView = (SMSQueryView) layoutInflaterFactory.inflate(R.layout.sms_query_view, null);
        mHanSMSQueryDialog = new HanSMSQueryDialog();
        mHanSMSQueryDialog.setView(mSMSQueryView);
        evtDialogDisableKeyBack = new DialogEvtDisableKeyBackOnKeyListener();
        
        mMySubjectLocationGPS = new MyLocation(me);
        mMySubjectLocationAGPS = new MyLocation(me);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @SuppressWarnings("unchecked")
    private void initEvent() {
        lvSMSList.setOnItemClickListener(mSMSListItemOnClickListener);
        lvSMSList.setOnCreateContextMenuListener(mGeoSMSListOnCreateContextMenuListener);
    
        mSMSQueryView.setMessageSentHandler(mHanSMSQueryDialog);
        mSMSQueryView.smsWriter.open();
        // TODO Optimize it
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        try {
            SMSIDTable = (HashSet<Integer>) PreferenceSerializer.deserialize(
                    prefs.getString(SHARED_PREFS_ID, PreferenceSerializer.serialize(new HashSet<Integer>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void initListAdapter() {
        mSMSListAdapter = new SMSListAdapter(me, null, mSMSListAdapter);
        mSMSListAdapter.setListViewStateHandler(updateStateHandler);
        lvSMSList.setAdapter(mSMSListAdapter);
    }
    
    private void initListSMSQuery() {
        // TODO Parsing data from Inbox
        queryURI = Uri.parse(VALUE_SMSINBOX_URISTRING);
        queryColProjection = SMSColumn.getValueStrings();
        
        String body = SMSColumn.Body.value();
        querySelection = body + " GLOB ? OR " + body + " GLOB ?";
        queryArgs = new String[] {getDefaultFilterString(), getVersion2FilterString()};
        
        //querySelection = SMSColumn.Body.value() + " LIKE ?";
        //queryArgs = new String[] {GeoSMSFormatBase.Prefix + "%"};
        queryToken = LIST_SMS_QUERY_TOKEN;
    }
    
    private class DialogEvtDisableSMSQueryDialogKeyBackOnKeyListener implements DialogInterface.OnKeyListener {     
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
                if(!mSMSQueryView.getPhoneFieldText().trim().equals("")) {
                    showDialog(MainAct.DIALOG_SMS_QUERY_EXIT_CONFORM);
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
                        dismissDialog(MainAct.DIALOG_SMS_QUERY_MESSAGESENDING);
                        showDialog(MainAct.DIALOG_SMS_QUERY_MESSAGESENT);
                    case -1:
                        
                    }
                }
                else {
                    dismissDialog(MainAct.DIALOG_SMS_QUERY_MESSAGESENDING);
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
                case MainAct.DIALOG_SMS_QUERY_MESSAGESENDING:
                        dismissDialog(MainAct.DIALOG_SMS_QUERY);
                        showDialog(MainAct.DIALOG_SMS_QUERY_MESSAGESENDING);
                        checkingTimes = 0;
                        messageSentCheckingHandler.removeCallbacks(mMessageSentCheckingTask);
                        messageSentCheckingHandler.postDelayed(mMessageSentCheckingTask, 1000);
                        break;
            }
        }
    }
    
    public String getDefaultFilterString() {
        //return "http://*&" + GeoSMS.CONST_GEOSMS_PARA_KEY + "*";
    	//Ye Revised
    	return "http://*[?]*&" + GeoSMS.CONST_GEOSMS_PARA_KEY + "*";
    }
    
    public String getVersion2FilterString() {
    	//original
        //return "GeoSMS/2;*;*;[ABEPQ];*";
    	//Ye revised
    	return "GeoSMS/2;*;*;S[;ITQ]*";
    }
    
    private void setLaunchMode() {
        Intent i = this.getIntent();
        mCurrentLaunchMode = i.getIntExtra(KEY_LAUNCH_MODE, ID_LAUNCH_MODE_STANDALONE);
    }
    
    private void setCurrentSelectedGeoSMSPackForSMSDelivery(GeoSMSPack pack) {
        if(curSelectedGeoSMSPackForSMSDelivery == null) curSelectedGeoSMSPackForSMSDelivery = new GeoSMSPack();
        curSelectedGeoSMSPackForSMSDelivery.assign(pack);
    }
    
    private GeoSMSPack getCurrentSelectedGeoSMSPackForSMSDelivery() {
        return curSelectedGeoSMSPackForSMSDelivery;
    }
    
    private void setIDTableSaved(int itemID) {
        curSelectedGeoSMSListItemID = itemID;
    }
    
    private void startAsyncQuery() {
        updateState(new UpdateStatePack(ViewState.showBusy, null));
        fireThread(new PreLoadListRunner());
        /*Thread queryThread = new Thread(new PreLoadListRunner());
        queryThread.setDaemon(true);
        queryThread.start();*/
        //preLoadListHandler.sendMessage(new Message());
    }

    private void updateState(UpdateStatePack pack) {
        synchronized(updateStateLock) {
            switch(pack.state) {
                case showList:
                    lvSMSList.setVisibility(View.VISIBLE);
                    tvMessage.setVisibility(View.GONE);
                    setTitle(R.string.menu_sahana_inbox);
                    setProgressBarIndeterminateVisibility(false);
                    break;
                case showMessage:
                    lvSMSList.setVisibility(View.GONE);
                    tvMessage.setVisibility(View.VISIBLE);
                    setTitle(R.string.menu_sahana_inbox);
                    setProgressBarIndeterminateVisibility(false);
                    break;
                case showBusy:
                    lvSMSList.setVisibility(View.GONE);
                    tvMessage.setVisibility(View.GONE);
                    setTitle(R.string.loading_inbox_activity_list);
                    setProgressBarIndeterminateVisibility(true);
                    break;
                case showItemLoading:
                    lvSMSList.setVisibility(View.VISIBLE);
                    tvMessage.setVisibility(View.GONE);
                    setTitle(R.string.loading_inbox_activity_list);
                    setProgressBarIndeterminateVisibility(true);
                    fireThread(new DelayListBindingRunner(500));
                    break;
                default:
                
            }
        
            if(pack.msg != null) setTitle(pack.msg);
        }
    }
    
    private final OnItemClickListener mSMSListItemOnClickListener = new OnItemClickListener() {
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
            Cursor cursor = getListAdapterCursor();
            mCurrentGeoSMSID = cursor.getInt(SMSColumn.ID.index());
            GeoSMSListItem gItem = mSMSListAdapter.getCachedListItem(mCurrentGeoSMSID);
            if(gItem != null) {
                showGeoSMSWithMap(gItem.mGeoSMSPack);
            }
        }
    };
    
    
    private final OnCreateContextMenuListener mGeoSMSListOnCreateContextMenuListener = new OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.context_menu_geosms_inbox_header_title);
            Cursor cursor = getListAdapterCursor();
            if ((cursor != null) && (cursor.getCount() > 0)) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                
                // TODO Optimization Pattern
                setIDTableSaved(cursor.getInt(SMSColumn.ID.index()));
                
                View targetView = info.targetView;
                if (info.position >= 0 && targetView instanceof GeoSMSListItemView) {
                    GeoSMSListItemView itemView = (GeoSMSListItemView) targetView;
                    GeoSMSListItem item = itemView.getListItem();
                    GeoSMSPack geoSMSPack = item.mGeoSMSPack;
                        switch (geoSMSPack.getGeoSMSFormat()) {
                            case BASIC:
                            //Ye modified
                            case TASK:
                            case INCIDENT:
                                // menu.add(0, 0, 0,
                                // R.string.context_menu_geosms_inbox_view)
                                // .setOnMenuItemClickListener(mViewGeoSMSOnMenuItemClickListener);
                                menu.add(0, 0, 0, R.string.context_menu_geosms_inbox_delete)
                                .setOnMenuItemClickListener(mDeleteGeoSMSOnMenuItemClickListener);
                                menu.add(0, 1, 0, R.string.context_menu_geosms_inbox_forward)
                                .setOnMenuItemClickListener( new ForwardGeoSMSOnMenuItemClickListener(item));
                                menu.add(0, 2, 0, R.string.context_menu_geosms_inbox_open_link)
                                .setOnMenuItemClickListener(new OpenLinkGeoSMSOnMenuItemClickListener(geoSMSPack.getHeaderString()));
                                break;

//                            case POI:
//                                menu.add(0, 0, 0, R.string.context_menu_geosms_inbox_delete)
//                                .setOnMenuItemClickListener(mDeleteGeoSMSOnMenuItemClickListener);
//                                menu.add(0, 1, 0, R.string.context_menu_geosms_inbox_forward)
//                                .setOnMenuItemClickListener(new ForwardGeoSMSOnMenuItemClickListener(item));
//                                menu.add(0, 2, 0, R.string.context_menu_geosms_inbox_open_link)
//                                .setOnMenuItemClickListener(new OpenLinkGeoSMSOnMenuItemClickListener(geoSMSPack.getHeaderString()));
//                                break;

                            case QUERY:
                                curSelectedGeoSMSPackAddress = item.mAddress;
                                menu.add(0, 0, 0, R.string.context_menu_geosms_inbox_delete)
                                .setOnMenuItemClickListener(mDeleteGeoSMSOnMenuItemClickListener);
                                menu.add(0, 1, 0, R.string.context_menu_geosms_inbox_reply)
                                .setOnMenuItemClickListener(mReplyGeoSMSOnMenuItemClickListener);
                                break;
                            default:
                                menu.add(0, 0, 0, R.string.context_menu_geosms_inbox_delete)
                                .setOnMenuItemClickListener(mDeleteGeoSMSOnMenuItemClickListener);
                        }
                } 
            }
        }
    };
    
    private final OnMenuItemClickListener mReplyGeoSMSOnMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            mMySubjectLocationGPS.enable(ProvideType.E_GPS);
            mMySubjectLocationAGPS.enable(ProvideType.E_AGPS);
            
            mMySubjectLocationGPS.addObserver(new GPSLocationObserver());
            mMySubjectLocationAGPS.addObserver(new AGPSLocationObserver());
            
            curLocationGPS = mMySubjectLocationGPS.getCurrentLocation();
            curLocationAGPS = mMySubjectLocationAGPS.getCurrentLocation();
            
            if (!SMSIDTable.contains(curSelectedGeoSMSListItemID)) me.showDialog(DIALOG_SMS_REPLY);
            else me.showDialog(DIALOG_SMS_HAS_REPLIED);
            return true;
        }
    };
    
    private final OnMenuItemClickListener mDeleteGeoSMSOnMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            mCurrentGeoSMSID = getListAdapterCursor().getInt(SMSColumn.ID.index());
            me.showDialog(ID_ALERTDIALOG_DELETEGEOSMS);
            return true;
        }
    };
    
    private class ForwardGeoSMSOnMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        
        private GeoSMSListItem gItem = new GeoSMSListItem();
        
        public ForwardGeoSMSOnMenuItemClickListener(GeoSMSListItem item) {
            this.gItem = item;
        }
        
        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            setCurrentSelectedGeoSMSPackForSMSDelivery(gItem.mGeoSMSPack);
            mSMSQueryView.setCurrGeoSMSPack(gItem.mGeoSMSPack);
            me.showDialog(MainAct.DIALOG_SMS_QUERY);
            return true;
        }
        
    }
    
    private class OpenLinkGeoSMSOnMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        
        private Uri mUri = null;
        
        public OpenLinkGeoSMSOnMenuItemClickListener(String str) {
            mUri = Uri.parse(str);
        }
        
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
            // TODO put string on file..
            startActivity(Intent.createChooser(intent, "what browser u choose?"));
            return true;
        }

    }
    
    private void showGeoSMSWithMap(GeoSMSPack pack) {
        //GeoSMSPack pack = mGeoSMSParser.Parse(msgBody);
        if(pack != null) {
            switch(pack.getGeoSMSFormat()) {
                case QUERY:
                case UNKNOWN:
                    return;
            }
            
            switch(mCurrentLaunchMode) {
                case ID_LAUNCH_MODE_STANDALONE:
                    Intent i = new Intent(me, com.sahana.geosmser.MainAct.class);
                    i.putExtra(GeoSMSService.Key_Extra_SMSPack, pack);
                    //i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    break;
                case ID_LAUNCH_MODE_SUBACTIVITY:
                    Intent resultIntent = new Intent();
                    Bundle resultBundle = new Bundle();
                    resultBundle.putParcelable(Key_SUBACTIVITY_RESULTPACK, pack);
                    resultIntent.putExtras(resultBundle);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    break;
            }   
            
        }
    }
    
    private Cursor getListAdapterCursor() {
        return mSMSListAdapter.getCursor();
    }
    
    private void fireThread(Runnable runner) {
        Thread thread = new Thread(runner);
        thread.setDaemon(true);
        thread.start();
    }
    
    public final static class UpdateStatePack {
        ViewState state;
        String msg;
        
        public UpdateStatePack(ViewState pState, String pMsg) {
            state = pState;
            msg = pMsg;
        }
    }
    
    private class DeleteGeoSMSListener implements OnClickListener {
        private int mID;
        private Uri mDeleteUri;
        
        public DeleteGeoSMSListener() {
            this(-1);
        }
        
        public DeleteGeoSMSListener(int id) {
            setID(id);
        }
        
        public void setID(int id) {
            mID = id;
            if(id != -1) mDeleteUri = ContentUris.withAppendedId(Uri.parse(VALUE_SMS_URISTRING), mID);
            else mDeleteUri = null;
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mQueryHandler.startDelete(DELETE_SMS_TOKEN, null, mDeleteUri, null, null);
        }
    }
    
    public class UpdateStateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj != null) {
                UpdateStatePack pack = (UpdateStatePack) msg.obj;
                updateState(pack);
            }
            super.handleMessage(msg);
        }
    }
    
    private class PreLoadListRunner implements Runnable {
        @Override
        public void run() {
            mQueryHandler.cancelOperation(LIST_SMS_QUERY_TOKEN);
            mQueryHandler.startQuery(queryToken, null, queryURI, queryColProjection, querySelection, queryArgs, mSMSSortOrder);
        }
    }
    
    private class DelayListBindingRunner implements Runnable {
        long delayTime = 1000;
        
        public DelayListBindingRunner(long time) {
            delayTime = time;
        }
        
        @Override
        public void run() {
            try {
                Thread.sleep(delayTime);
            } catch (InterruptedException e) { }
            Message msg = new Message();
            msg.obj = new UpdateStatePack(ViewState.showList, null);
            updateStateHandler.sendMessage(msg);
        }
    }
    
    private final class SMSListQueryHandler extends AsyncQueryHandler {

        public SMSListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if(cursor != null && cursor.getCount() > 0) {
                switch (token) {
                    case LIST_SMS_QUERY_TOKEN:
                        
                        mSMSListAdapter.changeCursor(cursor);
                        fireThread(new DelayListBindingRunner(500));
                        break;
                    default:
                        break;
                }
            }
            else {
                updateState(new UpdateStatePack(ViewState.showMessage, null));
            }
        }
        
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
             switch (token) {
                case DELETE_SMS_TOKEN:
                    mSMSListAdapter.removeCachedListItem(mCurrentGeoSMSID);
                    mCurrentGeoSMSID = -1;
                    startAsyncQuery();
                    onContentChanged();
                    break;
             }
        }
        
        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            super.onUpdateComplete(token, cookie, result);
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
