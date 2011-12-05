package com.sahana.geosmser.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sahana.geosmser.R;
import com.sahana.geosmser.GeoSMSPack;
import com.sahana.geosmser.WhereToMeet;
import com.sahana.geosmser.view.GeoSMSListItemView;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GeoSMSListItem implements Parcelable {
	public int mID;
	public String mAddress;
	public String mSMSBody;
	public Long mDate;
	public String mDateString;
	public GeoSMSPack mGeoSMSPack;
	public int mImageSourceID;
	
    private final Object mListItemLock = new Object();
    private GeoSMSListItemView loadingView;
	
    
    private final static DateFormat dateFormatLong = new SimpleDateFormat("yyyy/MM/d HH:mm");
    private final static DateFormat dateFormatShort = new SimpleDateFormat("MM/d HH:mm");
	
    public static final Parcelable.Creator<GeoSMSListItem> CREATOR = new Parcelable.Creator<GeoSMSListItem>() {
		public GeoSMSListItem createFromParcel(Parcel in) {
			return new GeoSMSListItem(in);
        }

        public GeoSMSListItem[] newArray(int size) {
            return new GeoSMSListItem[size];
        }
	};
    
	public GeoSMSListItem() {
		mID = -1;
	}
	
	public GeoSMSListItem(Parcel in) {
		readFromParcel(in);
	}

	public boolean isEmpty() {
		if(mID == -1) {
			return true;
		}
		return false;
	}
	
	public String formatDate(Long pDate) {
		Date date = new Date(pDate);
		int year = date.getYear();
		String ds;
		
		if(year == new Date().getYear()) {
			ds = dateFormatShort.format(date);
		}
		else {
			ds = dateFormatLong.format(date);
		}
		return ds;
	}
	
	public String getSMSBodyAbstract() {
		
		return null;
	}
	
	public void setDate(Long date) {
		mDate = date;
		mDateString = formatDate(mDate);
	}
	
	public void setListItem(GeoSMSListItem item) {
		synchronized (mListItemLock) {
			mID = item.mID;
			mAddress = item.mAddress;
			mGeoSMSPack = item.mGeoSMSPack;
			mImageSourceID = item.mImageSourceID;
			mSMSBody = item.mSMSBody;
			mDate = item.mDate;
			mDateString = item.mDateString;
			runLoadingViewCallback();
        }
	}
	
	public void setLoadingView(GeoSMSListItemView view) {
		synchronized (mListItemLock) {
			loadingView = view;
			runLoadingViewCallback();
		}
	}
	
	private void runLoadingViewCallback() {
		 synchronized (mListItemLock) {
			 if(loadingView != null && !isEmpty()) {
				 loadingView.onListItemLoaded(this);
				 loadingView = null;
			 }
		 }
	 }
	//Ye Revised 
	public void setGeoSMSPackAndBody(GeoSMSPack pack) {		
		mGeoSMSPack = pack;
		try {
			if(pack != null) {
				switch (mGeoSMSPack.getGeoSMSFormat()) {
					case BASIC:
						String text = pack.getText();
						mSMSBody = (text != null) ? text : pack.getGeoLocation().toString();
						mImageSourceID = R.drawable.format_b_icon;
						return;
//					case POI:
//						mSMSBody = pack.getText();
//						mImageSourceID = R.drawable.format_p_icon;
//						return;
					case EXTENDED:
						mSMSBody = pack.getText();
						mImageSourceID = R.drawable.format_e_icon;
						return;
					case QUERY:
						mSMSBody = "Query";
						mImageSourceID = R.drawable.format_q_icon;
						return;
//					case AGPS:
//						mSMSBody = pack.getText();
//						mImageSourceID = R.drawable.format_un_icon;
//						return;
					//Ye New add
					case TASK:
						mSMSBody = pack.getText();
						mImageSourceID = R.drawable.format_t_icon;
						return;
					case INCIDENT:
						mSMSBody = pack.getText();
						mImageSourceID = R.drawable.format_i_icon;
						return;
					case UNKNOWN:
						mSMSBody = pack.getText();
						mImageSourceID = R.drawable.format_un_icon;
						return;

				}
			}
			
			mImageSourceID = R.drawable.format_un_icon;
			mSMSBody = "Unknown Error";
		}
		catch(Exception e) {
			mImageSourceID = R.drawable.format_un_icon;
			e.fillInStackTrace();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mID);
		dest.writeString(mAddress);
		dest.writeLong(mDate);
		dest.writeString(mSMSBody);
		dest.writeParcelable(mGeoSMSPack, flags);
	}
	
	public void readFromParcel(Parcel in) {
		mID = in.readInt();
		mAddress = in.readString();
		setDate(in.readLong());
		mSMSBody = in.readString();
		GeoSMSPack pack = in.readParcelable(GeoSMSPack.class.getClassLoader());
		setGeoSMSPackAndBody(pack);
	}
}
