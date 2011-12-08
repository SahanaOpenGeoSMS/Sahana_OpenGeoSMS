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

import com.OpenGeoSMS.GeoLocation;
import com.OpenGeoSMS.GeoSMS;
import com.OpenGeoSMS.exception.GeoSMSException;
import com.OpenGeoSMS.format.GeoSMSFormat;
import com.OpenGeoSMS.parser.GeoSMSParser;
import com.OpenGeoSMS.parser.GeoSMSParser.HeaderParameter;
import com.sahana.geosmser.R;
import com.google.android.maps.GeoPoint;
import com.sahana.geosmser.database.WhiteListDBAdapter;
import com.sahana.geosmser.gps.MyLocation;
import com.sahana.geosmser.parser.GeoSMSParserV2;
import com.sahana.geosmser.parser.GeoSMSParserV3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.PopupWindow;
import android.widget.Toast;

public class GeoSMSService extends Service {
    public static final String TELEPHONY_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private static final String TAG = "GeoSMSService";

    public static final String Key_Extra_SMSPack = "SMSPack";

    public NotificationManager nm;

    public SMSReceiver smsReceiver;

    private GeoSMSParser Parser; // version 3 parser

    private GeoSMSParserV2 ParserV2; // version 2 parser
    
    private GeoSMSParserV3 ParserV3;
    
    private boolean isStarted, isServiceNotificationInvolved;
    
    private final IBinder mBinder = new IGeoSMSService.Stub() {
        @Override
        public GeoSMSService getService() {
            return GeoSMSService.this;
        }

        @Override
        public boolean isServiceStart() {
            return isStarted;
        }

        @Override
        public void setServiceNotification(boolean notify) {
            isServiceNotificationInvolved = notify;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Bind GeoSMSService");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Create GeoSMSService");
        isStarted = false;
        isServiceNotificationInvolved = false;
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "Start GeoSMSService");
        isStarted = true;
        RegisterSMSReceived();
        showServiceNotification();
        Parser = new GeoSMSParser();
        ParserV2 = new GeoSMSParserV2();
        ParserV3 = new GeoSMSParserV3();
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy GeoSMSService");
        RemoveRegisterSMSReceived();
        if (nm != null)
            nm.cancelAll();
        super.onDestroy();
    }

    public boolean RegisterSMSReceived() {
        IntentFilter infer = new IntentFilter(
                GeoSMSService.TELEPHONY_SMS_RECEIVED);
        smsReceiver = new SMSReceiver();
        registerReceiver(smsReceiver, infer);
        return true;
    }

    public boolean RemoveRegisterSMSReceived() {
        if (smsReceiver != null) {
            this.unregisterReceiver(smsReceiver);
            return true;
        }
        return false;
    }

    private void showServiceNotification() {
        if (isServiceNotificationInvolved) {
            nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification n = new Notification(R.drawable.sahana_icon,
                    "Sahana GeoSMS Service", System.currentTimeMillis());
            int id = 10021;

            Intent i = new Intent(this,
                    com.sahana.geosmser.GeoSMSServiceControlAct.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            n.setLatestEventInfo(this, "Sahana GeoSMS Service", "", contentIntent);
            
            nm.cancelAll();
            nm.notify(id, n);
        }
    }

    private void showSMSNotification(String msg, GeoSMSPack pack) {
        try {
            nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // TODO Put String on file
            Notification n = new Notification(R.drawable.sahana_icon,
                    "New Sahana GeoSMS Notification.", System.currentTimeMillis());
            
            int id = 10022;
            switch (pack.getGeoSMSFormat()) {
                case QUERY:
                    PendingIntent contentIntent2 = PendingIntent.getActivity(this, 0, null,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    n.setLatestEventInfo(this, "Sahana GeoSMS", "Receive a Query Sahana GeoSMS.", contentIntent2);
                    nm.notify(id, n);
                    break;
                default:
                    Intent i = new Intent(this, com.sahana.geosmser.WhereToMeet.class);
                    i.putExtra(Key_Extra_SMSPack, pack);
                    // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    n.setLatestEventInfo(this, "Sahana GeoSMS", "Receive a Sahana GeoSMS, select to show message.", contentIntent);
                    // nm.cancelAll();
                    nm.notify(id, n);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SMSReceiver extends BroadcastReceiver {

        MyLocation myLocation;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Receive SMS");
            
            if (intent.getAction().equals(TELEPHONY_SMS_RECEIVED)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdusObj = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdusObj.length];
                    String messageString = "";
                    for (int i = 0; i<pdusObj.length; i++) {
                        messages[i] = SmsMessage.createFromPdu ((byte[]) pdusObj[i]);
                        messageString += messages[i].getMessageBody();
                    }
                    
                  GeoSMSPack pack = null;
                try {
                    pack = new GeoSMSPack(ParserV3.parse(messageString));
                } catch (GeoSMSException e) {
                    e.printStackTrace();
                }
                
                if (pack == null && ParserV2.checkStructure(messageString)) {
                    try {
                        pack = new GeoSMSPack(ParserV2.parse(messageString));
                    } catch (GeoSMSException e) {
                        e.printStackTrace();
                    }
                }
                
                if (pack != null) {
                    Log.e(TAG, "pack not null");
                    switch (pack.getGeoSMSFormat()) {
                    case UNKNOWN:
                    	//Ye modified
                    	break;
                    case QUERY:
                        //sendGeoSMSReply(pack, address);
                    
                    default:
                        showSMSNotification(messageString, pack);
                    }
                }
                }
            }
        }

        public GeoSMS parseGeoSMS(String msg) {
            GeoSMS geosms = null;
            if (msg != null && !msg.equals("")) {
                try {
                    geosms = Parser.parse(msg);
                } catch (GeoSMSException e) {
                    e.printStackTrace();
                }
            }
            return geosms;
        }

        public boolean SaveSMSDB() {
            return false;
        }
    }

    public interface IGeoSMSService {
        public boolean isServiceStart();

        public GeoSMSService getService();

        public void setServiceNotification(boolean notify);

        public static abstract class Stub extends Binder implements
                IGeoSMSService {
            public abstract GeoSMSService getService();

            public abstract boolean isServiceStart();

            public abstract void setServiceNotification(boolean notify);
        }
    }

}
