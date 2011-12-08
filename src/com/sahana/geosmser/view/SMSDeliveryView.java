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

import java.util.ArrayList;
import java.util.EventListener;

import com.OpenGeoSMS.GeoLocation;
import com.sahana.geosmser.R;
import com.sahana.geosmser.GeoSMSPack;
import com.sahana.geosmser.WhereToMeet;
import com.sahana.geosmser.TeamCommunication;
import com.sahana.geosmser.gps.Geocoder;
import com.sahana.geosmser.widget.AutoCompleteSMSTextView;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.AbsSavedState;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ToggleButton;

public class SMSDeliveryView extends RelativeLayout {
	public final String SENT_SMS_ACTION = "GEOSMS_SEND_SMS_ACTION";
	public final String DELIVERY_SMS_ACTION = "GEOSMS_DELIVERY_SMS_ACTION";
	private final static int CHARS_REMAINING_COUNTER_LIMIT = 25;
	public final static int LENGTH_MINIMUM_PHONENUMBER = 10;
	public final static int LENGTH_MINIMUM_FOREIGN_PHONENUMBER = 12;
	public final static int LENGTH_MINIMUM_MESSAGE = 0;
	public final static int COLOR_HINT_LIMITED = Color.rgb(255, 147, 147);
	
	private AutoCompleteSMSTextView autoedtPhoneNumber;
	private TextView tvMessageCounter, tv;
	private EditText edtMessage;
	private EditText edtReverseCode;
	private Button btnSMSSend;
	private Button btnReverseCodeAppend;
	private ToggleButton ttnReverseCodeShow;
	
	private SendSMSReceiver sendSMSReceiver;
	private DeliverySMSReceiver deliverySMSReceiver;
	
	private Context baseContext;
	private String positionHeaderInfo, messageContent, msgFieldString, contactNumber;
	private GeoLocation positionLocation;
	private int messageCount, messageSize;
	private SmsManager smsManager;
	private GeoSMSPack positionPack;
	private boolean isMessageSent;
	private boolean mIsSMSReceiverRegistered;
	private Handler hanMessageSent;
	
	public ISMSDeliveryRenderer.OnSourceBindingListener sourceBindingListener;
	private volatile boolean mIsInflated = false;
	private static volatile int curSMSReceivedNumber; 
	
	public SMSDeliveryView(Context context) {
		super(context);
		baseContext = context;
	}
	
	public SMSDeliveryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseContext = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
		bindInflatedComponent();
		mIsInflated = true;
	}
	
	@Override
	protected void onAttachedToWindow() {
		if(mIsInflated) {
			initEvent();
			driveBindSource();
			render();
			
			if(!isSMSReceiverRegistered()) {
				registerSMSSendDeliveryReceiver();
			}
		}
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		if(mIsInflated) {
			hanMessageSent.sendEmptyMessage(TeamCommunication.DIALOG_SMS_DELIVERY_CANCEL);
		}
		super.onDetachedFromWindow();
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		GeoSMSPackParcelableWrapper w = new GeoSMSPackParcelableWrapper(p);
		w.mPack = positionPack;
		w.mIsMessageSent = isMessageSent;
		w.mCurSMSReceivedNumber = curSMSReceivedNumber;
		return w;
	}
	
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		GeoSMSPackParcelableWrapper w = (GeoSMSPackParcelableWrapper) state;
		positionPack = w.mPack;
		isMessageSent = w.mIsMessageSent;
		curSMSReceivedNumber = w.mCurSMSReceivedNumber;
		super.onRestoreInstanceState(null);
	}
	
	private void init() {
		contactNumber = "";
		msgFieldString = "";
		isMessageSent = false;
		curSMSReceivedNumber = 0;
		mIsSMSReceiverRegistered = false;
	}
	
	private void bindInflatedComponent() {
		autoedtPhoneNumber = (AutoCompleteSMSTextView) findViewById(R.id.AutoEditTextSMSPhoneNumber);
		edtMessage = (EditText) findViewById(R.id.EditTextSMSMessage);
		edtReverseCode = (EditText) findViewById(R.id.EditTextReverseCode);
		tvMessageCounter = (TextView) findViewById(R.id.TextViewSMSMessageCounter);
		
		btnSMSSend = (Button) findViewById(R.id.ButtonSMSSend);
		btnReverseCodeAppend = (Button) findViewById(R.id.ButtonReverseCodeAppend);
		ttnReverseCodeShow = (ToggleButton) findViewById(R.id.ToggleButtonReverseCodeShow);
	}
	
	private void initEvent() {
		btnSMSSend.setOnClickListener(new EvtOnClickSMSDelivery());
		autoedtPhoneNumber.setOnItemClickListener(new EvtOnItemClickAutoedtPhoneNumber());
		autoedtPhoneNumber.addTextChangedListener(new DelContactTextWatcher());
		edtMessage.addTextChangedListener(new DelMessageTextWatcher());
		ttnReverseCodeShow.setOnClickListener(new EvtOnClickReverseCodeShow());
		btnReverseCodeAppend.setOnClickListener(new EvtOnClickReverseCodeAppend());
	}

	protected void driveBindSource() {	
	    
		if(sourceBindingListener != null) {
			if(positionPack == null) {
				positionPack = new GeoSMSPack();
				sourceBindingListener.onSourceBind(positionPack);
			}
			
			if(positionPack != null) {
				positionHeaderInfo = positionPack.getHeaderString();
				positionLocation = positionPack.getGeoLocation();
				String text = positionPack.getText();
				// setMessageFieldText((text != null) ? text : "");
			}
			else {
				closeView();
			}
		}
		else {
			closeView();
		}
	}
	
	private void render() {
		updateMessageTextCounter();
		updateSendSMSButtonState();
		tvMessageCounter.setTextColor(Color.WHITE);
    	tvMessageCounter.setVisibility(View.VISIBLE);
	}
	
	public boolean isSMSReceiverRegistered() {
		return mIsSMSReceiverRegistered;
	}
	
	public void registerSMSSendDeliveryReceiver() {
		mIsSMSReceiverRegistered = true;
		sendSMSReceiver = new SendSMSReceiver();
		deliverySMSReceiver = new DeliverySMSReceiver();
		baseContext.registerReceiver(sendSMSReceiver, new IntentFilter(SENT_SMS_ACTION));
		baseContext.registerReceiver(deliverySMSReceiver, new IntentFilter(DELIVERY_SMS_ACTION));
	}
	
	public void unregisterSMSSendDeliveryReceiver() {
		if(sendSMSReceiver != null) baseContext.unregisterReceiver(sendSMSReceiver);
		if(deliverySMSReceiver != null ) baseContext.unregisterReceiver(deliverySMSReceiver);
		mIsSMSReceiverRegistered = false;
	}
	
	private String getRString(int id) {
		return baseContext.getString(id);
	}
	
	private void closeView() {
		handleDialog(WhereToMeet.CODE_DISMISS_DIALOG_SMS_DELIVERY);
	}
	
	public void clearInputField() {
		autoedtPhoneNumber.setText("");
		edtMessage.setText("");
		edtReverseCode.setText("");
		ttnReverseCodeShow.setChecked(false);
	}
	
	public void clearSavedState() {
		positionPack = null;
	}
	
	public String getReverseCodeFieldText() {
	    return edtReverseCode.getText().toString();
	}
	
	public String getPhoneFieldText() {
		return autoedtPhoneNumber.getText().toString();
	}
	
	public void setPhoneFieldText(String phone) {
		autoedtPhoneNumber.setText(phone);
	}
	
	public String getMessageFieldText() {
		return edtMessage.getText().toString();
	}
	
	public void setMessageFieldText(String msg) {
		edtMessage.setText(msg);
	}
	
	public void setOnSourceBindingListener(ISMSDeliveryRenderer.OnSourceBindingListener listener) {
		sourceBindingListener = listener;
	}
	
	public void setMessageSentHandler(Handler handler) {
		hanMessageSent = handler;
	}
	
	private void updateFieldInfo() {
		contactNumber = autoedtPhoneNumber.getText().toString();
		msgFieldString = edtMessage.getText().toString();
		positionPack.setText((msgFieldString != "") ? msgFieldString : null);
	}
	
	private void updateSendSMSButtonState() {
		boolean enable = false;
		
		/*int dlen = LENGTH_MINIMUM_PHONENUMBER;
		if(contactNumber.matches("\\d{" + dlen + ",}") || contactNumber.matches(".*<\\d{" + dlen + ",}>")) {
			if(messageCount >= 1 && msgFieldString.length() >= LENGTH_MINIMUM_MESSAGE) {
				enable = true;
			}
		}*/
		
		if(contactNumber.length() > 0 && msgFieldString.length() >= LENGTH_MINIMUM_MESSAGE) {
			enable = true;
		}
		
		btnSMSSend.setEnabled(enable);
	}
	
	private void updateMessageTextCounter() {
		String text = "";
		if(!msgFieldString.equals("")) text = "\n" + positionPack.getText();
		messageContent = positionHeaderInfo + text;
	
		
		int[] params = SmsMessage.calculateLength(messageContent, false);
		messageCount = params[0];
		messageSize = params[1];
		Integer remaining = params[2];
	    
		tvMessageCounter.setText(String.valueOf(remaining) + "/" +String.valueOf(messageCount));
	}
	
	private void sendSMS(String pPhoneNumber, String pMessage) {
		PendingIntent piSendSMS = PendingIntent.getBroadcast(baseContext, 0, new Intent(SENT_SMS_ACTION), 0);
		//PendingIntent piDeliverySMS = PendingIntent.getBroadcast(baseContext, 0, new Intent(DELIVERY_SMS_ACTION), 0);
		
		handleDialog(WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENDING);
		isMessageSent = true;
		//smsManager.sendTextMessage(pPhoneNumber, null, pMessage, piSendSMS, piDeliverySMS);
		smsManager = SmsManager.getDefault();
		
		ArrayList<String> msgList = smsManager.divideMessage(pMessage);
		ArrayList<PendingIntent> piList = new ArrayList<PendingIntent>();
		for(int i=0; i<msgList.size(); i++) {
			piList.add(piSendSMS);
		}
		// Modify by Korth 20101203
		if (pPhoneNumber.charAt(0) != '+') {
		    smsManager.sendMultipartTextMessage(pPhoneNumber, null, msgList, piList, null);
		} else {
		    smsManager.sendMultipartTextMessage(pPhoneNumber.substring(1), null, msgList, piList, null);
		}
	}
	
	private void handleDialog(int code) {
		if(hanMessageSent != null) {
			Message msg = hanMessageSent.obtainMessage(code);
			hanMessageSent.sendMessage(msg);
		}
	}
	
	private class EvtOnClickReverseCodeAppend implements OnClickListener {
        @Override
        public void onClick(View arg0) {
         // TODO Bind ReverseCode into Message EditText
            if (!edtReverseCode.getText().equals("")) {
                edtMessage.append(edtReverseCode.getText().toString());
            } 
        }
	    
	}
	
	private class EvtOnClickReverseCodeShow implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (ttnReverseCodeShow.isChecked() && positionLocation != null) {
             // TODO show ReverseCode in EditText
                String RLocation = Geocoder.reverseGeocode(positionLocation);
                edtReverseCode.setText(RLocation);
            } else {
                edtReverseCode.setText("");
            }
        }
	}
	
	private class EvtOnClickSMSDelivery implements OnClickListener {
		@Override
		public void onClick(View v) {
			  String phoneNumber = autoedtPhoneNumber.getValue();
              String message = edtMessage.getText().toString();
              
              if(!contactNumber.matches("\\d{" + LENGTH_MINIMUM_PHONENUMBER + ",}") &&
                      !contactNumber.matches(".*<\\d{" + LENGTH_MINIMUM_PHONENUMBER + ",}>") && 
                      // Modify by Korth 20101203
                      !contactNumber.matches("^.?\\d{" + LENGTH_MINIMUM_PHONENUMBER + ",}")) {
            	  Toast.makeText(baseContext, "The phone number is unusable.", Toast.LENGTH_SHORT).show();
            	  return;
              }
              
              if (message.length() >= LENGTH_MINIMUM_MESSAGE) {
            		  btnSMSSend.setEnabled(false);
                      sendSMS(phoneNumber, messageContent);
              }
              else
                  Toast.makeText(baseContext, "Word length is no less than " + LENGTH_MINIMUM_MESSAGE, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private class EvtOnItemClickAutoedtPhoneNumber implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(baseContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				autoedtPhoneNumber.isClicked = true;
				autoedtPhoneNumber.dismissDropDown();
				//Cursor cursor = (Cursor) contactListAdapter.getAdapter().getItem(position);
				//autoedtPhoneNumber.setText(cursor.getString(3));
			}
		}
	}
	
	
	private class DelContactTextWatcher implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
			updateFieldInfo();
			updateSendSMSButtonState();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }
	}
	
	
	private class DelMessageTextWatcher implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) { }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			updateFieldInfo();
			updateMessageTextCounter();
			updateSendSMSButtonState();
		}
	}
	
	private class SendSMSReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context pContext, Intent pIntent) {
			boolean ishandleMsg = true;
			
			if(isMessageSent) {
				curSMSReceivedNumber += 1;
				HanMessageSentDialogPack pack = new HanMessageSentDialogPack();
				pack.isFinish = false; 
				
				switch (getResultCode()) {
	                case Activity.RESULT_OK:
	                	ishandleMsg = false;
	                	if(curSMSReceivedNumber >= messageCount) {
	                		curSMSReceivedNumber = 0;
	        				pack.isFinish = true;
	        				
	        				pack.message = getRString(R.string.message_sent_result_ok);
		                	clearInputField();
		                	ishandleMsg = true;
	                	}
	                    break;
	                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                	pack.message = getRString(R.string.message_sent_result_error_generic_failure);
	                    break;
	                case SmsManager.RESULT_ERROR_NO_SERVICE:
	                	pack.message = getRString(R.string.message_sent_result_error_no_service);
	                    break;
	                case SmsManager.RESULT_ERROR_NULL_PDU:
	                	pack.message = getRString(R.string.message_sent_result_error_null_pdu);
	                    break;
	                case SmsManager.RESULT_ERROR_RADIO_OFF:
	                	pack.message = getRString(R.string.message_sent_result_error_radio_off);
	                    break;
	                default:
	                	pack.message = getRString(R.string.message_sent_result_error_unknown);
	            }
				
				if(ishandleMsg) {
					isMessageSent = false;
					handleMessageSent(pack);
				}
			}
		}
		
		private void handleMessageSent(HanMessageSentDialogPack pack) {
			if(hanMessageSent != null) {
				Message msg = hanMessageSent.obtainMessage(WhereToMeet.DIALOG_SMS_DELIVERY_MESSAGESENT, pack);
				hanMessageSent.sendMessage(msg);
			}
		}
	}
	
	
	private class DeliverySMSReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context pContext, Intent pIntent) {
			switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(baseContext, R.string.message_delivery_result_ok, Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(baseContext, R.string.message_delivery_result_cancel, Toast.LENGTH_SHORT).show();
                    break;                        
            }
		}
	}
	
	
	public static class HanMessageSentDialogPack implements Parcelable {
		public String message = null;
		public boolean isFinish = false;
		public Handler handler;
		
		public static final Parcelable.Creator<HanMessageSentDialogPack> CREATOR = new Parcelable.Creator<HanMessageSentDialogPack>() {
			public HanMessageSentDialogPack createFromParcel(Parcel in) {
				return new HanMessageSentDialogPack(in);
	        }

	        public HanMessageSentDialogPack[] newArray(int size) {
	            return new HanMessageSentDialogPack[size];
	        }
		};
		
		public HanMessageSentDialogPack() {}
		
		public HanMessageSentDialogPack(Parcel in) {
			readFromParcel(in);
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(message);
			out.writeBooleanArray(new boolean[] {isFinish});
			out.writeValue(handler);
		}
		
		public void readFromParcel(Parcel in) {
			message = in.readString();
			boolean[] bAry = new boolean[] {};
			in.readBooleanArray(bAry);
			isFinish = bAry[0];
			handler = (Handler) in.readValue(Handler.class.getClassLoader());
		}
	}
	
	
	public interface ISMSDeliveryRenderer {
		public interface OnSourceBindingListener extends EventListener {
			public void onSourceBind(GeoSMSPack pack);
		}
	}
	
	
	public static class GeoSMSPackParcelableWrapper extends AbsSavedState {
		public static final Parcelable.Creator<GeoSMSPackParcelableWrapper> CREATOR = new Parcelable.Creator<GeoSMSPackParcelableWrapper>() {
	        public GeoSMSPackParcelableWrapper createFromParcel(Parcel in) {
	            return new GeoSMSPackParcelableWrapper(in);
	        }
	
	        public GeoSMSPackParcelableWrapper[] newArray(int size) {
	            return new GeoSMSPackParcelableWrapper[size];
	        }
	    };
		
		public GeoSMSPackParcelableWrapper(Parcel source) {
	        super(source);
	        readFromParcel(source);
	    }
	
		public GeoSMSPackParcelableWrapper(Parcelable superState) {
			super(superState);
		}

		public GeoSMSPack mPack;
		public boolean mIsMessageSent;
		public int mCurSMSReceivedNumber;
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(mPack, flags);
		}
		
		public void readFromParcel(Parcel in) {
			mPack = in.readParcelable(GeoSMSPack.class.getClassLoader());
		}
	}
}
