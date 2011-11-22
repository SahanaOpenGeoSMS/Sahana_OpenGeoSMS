package com.sahana.geosmser.widget;

import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.OpenGeoSMS.exception.GeoSMSException;
import com.OpenGeoSMS.format.GeoSMSFormat;
import com.OpenGeoSMS.parser.GeoSMSParser;
import com.sahana.geosmser.R;
import com.sahana.geosmser.GeoSMSManagerAct;
import com.sahana.geosmser.GeoSMSPack;
import com.sahana.geosmser.MainAct;
import com.sahana.geosmser.GeoSMSManagerAct.ViewState;
import com.sahana.geosmser.database.QueryRepliedIDDBAdapter;
import com.sahana.geosmser.format.GeoSMSSahanaFormat;
import com.sahana.geosmser.parser.GeoSMSParserSahana;
import com.sahana.geosmser.parser.GeoSMSParserV2;
import com.sahana.geosmser.parser.GeoSMSParserV3;
import com.sahana.geosmser.view.GeoSMSListItemView;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CursorAdapter;
import android.widget.Toast;

public class SMSListAdapter extends CursorAdapter {
	public final static String VALUE_SMSINBOX_URISTRING = "content://sms/inbox";
	public String mSMSSortOrder = "date DESC";
	
	private ContentResolver contentResolver;
	private Context baseContext;
	private LayoutInflater layoutFactory;
	
	private Handler updateListStateHandler;
	private ScheduledThreadPoolExecutor mAsyncLoader;
	private final LoadingViewStackRunner loadingViewStackRunner;
	
	private Map<Integer, GeoSMSListItem> cachedListItems;
	private final Stack<Runnable> loadingViewStack;
	
	public GeoSMSParser mGeoSMSParser;
	private GeoSMSParserV2 mGeoSMSParserV2;
	private GeoSMSParserV3 mGeoSMSParserV3;
	
	private QueryRepliedIDDBAdapter mQueryRepliedIDDBAdapter;
	private GeoSMSParserSahana mGeoSMSParserSahana;
	
	public enum SMSColumn {
		ID("_id", 0), Address("address", 1), Date("date", 2), Body("body", 3);
		
		private String mValue; 
		private int mIndex;
		private SMSColumn(String value, int idx) {
			mValue = value;
			mIndex = idx;
		}
		
		public String value() {
			return mValue;
		}
		
		public int index() {
			return mIndex;
		}
		
		public static String[] getValueStrings() {
			String[] cols = new String[size()];
			
			int idx = 0;
			for(SMSColumn col : SMSColumn.values()) {
				cols[idx] = col.value();
				idx +=1;
			}
			return cols;
		}
		
		public static int size() {
			return SMSColumn.values().length;
		}
	}
	 
	public SMSListAdapter(Context pContext, Cursor c, SMSListAdapter oldListAdapter) {
		super(pContext, c, true);
		cachedListItems = new ConcurrentHashMap<Integer, GeoSMSListItem>();
		loadingViewStack = new Stack<Runnable>();
		loadingViewStackRunner = new LoadingViewStackRunner();
		init(pContext, oldListAdapter);
	}
	
	private void init(Context pContext, SMSListAdapter adapter) {
		baseContext = pContext;
		contentResolver = baseContext.getContentResolver();
		layoutFactory = LayoutInflater.from(pContext);
		//mGeoSMSParser = new GeoSMSParser(new GeoSMSFormatAdapter());
		mGeoSMSParser = new GeoSMSParser();
		mGeoSMSParserV2 = new GeoSMSParserV2();
		mGeoSMSParserV3 = new GeoSMSParserV3();
//		mGeoSMSParserSahana = new GeoSMSParserSahana();
		
		mQueryRepliedIDDBAdapter = new QueryRepliedIDDBAdapter(pContext);
		
		setAsyncLoader(adapter);
	}
	
	public void setAsyncLoader(SMSListAdapter adapter) {
		if(adapter != null) {
			ScheduledThreadPoolExecutor asyncLoader = adapter.getAsyncLoader();
			if (asyncLoader.getQueue().size() < 4) {
				this.mAsyncLoader = asyncLoader;
			}
			else {
				asyncLoader.shutdownNow();
			    mAsyncLoader = new ScheduledThreadPoolExecutor(3);
			}
		}
		else {
            mAsyncLoader = new ScheduledThreadPoolExecutor(3);
		}
	}
	
	public void setListViewStateHandler(Handler handler) {
		updateListStateHandler = handler;
	}
	
	public void setDefaultCursor() {
		Cursor cursor = contentResolver.query(Uri.parse(VALUE_SMSINBOX_URISTRING), SMSColumn.getValueStrings(), null, null, mSMSSortOrder);
		this.changeCursor(cursor);
	}	

	public ScheduledThreadPoolExecutor getAsyncLoader() {
		return mAsyncLoader;
	}
	
	public void removeCachedListItem(int id) {
		if(cachedListItems != null && cachedListItems.containsKey(id)) {
			cachedListItems.remove(id);
		}
	}
	
	public Map<Integer, GeoSMSListItem> getCachedListItems() {
		return cachedListItems;
	}
	
	public void setCachedListItems(Map<Integer, GeoSMSListItem> oldListItems) {
		cachedListItems = oldListItems;
	}
	
	public GeoSMSListItem getCachedListItem(Integer id) {
		return cachedListItems.get(id);
	}
	
	private GeoSMSListItem getCachedListItem(Cursor cursor) {
		Integer id = cursor.getInt(SMSColumn.ID.index());
		GeoSMSListItem item = cachedListItems.get(id);
		
		if(item == null) {
			item = new GeoSMSListItem(); 
			GeoSMSListItem newItem = new GeoSMSListItem();
			newItem.mID = id;
			newItem.mAddress = cursor.getString(SMSColumn.Address.index());
			newItem.setDate(cursor.getLong(SMSColumn.Date.index()));
			newItem.mSMSBody = cursor.getString(SMSColumn.Body.index());
			
			GeoSMSPack pack = null;
			try {
			    // No check machine in here
				pack = new GeoSMSPack(mGeoSMSParserV3.parse(newItem.mSMSBody));
				
			} catch (GeoSMSException e) {
				e.printStackTrace();
				try {
					pack = new GeoSMSPack(mGeoSMSParserV2.parse(newItem.mSMSBody));
				} catch (GeoSMSException e2) {
					e.printStackTrace();
				}
			}
				newItem.setGeoSMSPackAndBody(pack);
				cachedListItems.put(id, newItem);
			
		}

		return item;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view instanceof GeoSMSListItemView) {
			GeoSMSListItemView itemView = (GeoSMSListItemView) view;
			Integer id = cursor.getInt(SMSColumn.ID.index());
			
			GeoSMSListItem item = getCachedListItem(cursor);
			/*item.mID = cursor.getInt(SMSColumn.ID.index());
			item.mAddress = cursor.getString(SMSColumn.Address.index());
			item.mDate = cursor.getLong(SMSColumn.Date.index());
			item.mSMSBody = cursor.getString(SMSColumn.Body.index());*/
			
			itemView.bind(item);
		
			if(item.isEmpty()) {
				sendViewStateMessage(ViewState.showItemLoading, null);
				startAsyncDisplayListItem(itemView, item, id);
			}
			
			/*if(itemView.hasListItem()) {
				//itemView.bind(item);
				itemView.bind(itemView.listItem);
			}
			else {
				itemView.bindLoadingView();
				startAsyncDisplayListItem(itemView, item);
				//startAsyncDisplayListItem(itemView, cursor);
			}*/
			
			/*item.mID = cursor.getInt(SMSColumn.ID.index());
			item.mAddress = cursor.getString(SMSColumn.Address.index());
			item.mDate = cursor.getLong(SMSColumn.Date.index());
			item.mSMSBody = cursor.getString(SMSColumn.Body.index());
		
			itemView.bind(item);*/
		}
	}
	
	private void startAsyncDisplayListItem(final GeoSMSListItemView view, final GeoSMSListItem item, final Integer id) {
		 synchronized (loadingViewStack) {
			 loadingViewStack.push(new Runnable() {
				 @Override
	             public void run() {
                    	/*GeoSMSListItem item = new GeoSMSListItem();
                    	item.mID = cursor.getInt(SMSColumn.ID.index());
            			item.mAddress = cursor.getString(SMSColumn.Address.index());
            			item.mDate = cursor.getLong(SMSColumn.Date.index());
            			item.mSMSBody = cursor.getString(SMSColumn.Body.index());*/
                    	item.setListItem(getCachedListItem(id));
                    }
                });
	        }
		 //mAsyncLoader.execute(loadingViewStackRunner);
		 
		 try {
			 mAsyncLoader.schedule(loadingViewStackRunner, 200, TimeUnit.MICROSECONDS);
		 }
		 catch(Exception e) {
			 e.fillInStackTrace();
		 }
	}
	
	@Override
	public void changeCursor(Cursor cursor) {
		synchronized (loadingViewStack) {
			loadingViewStack.clear();
        }
		super.changeCursor(cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
	    String body = cursor.getString(SMSColumn.Body.index());
	    
//	    if(mGeoSMSParserV3.checkStructure(body)|| mGeoSMSParserV2.checkStructure(body)) {
	        return layoutFactory.inflate(R.layout.geosms_list_itemview, parent, false);
//	    }
//		return null;
	}
	
	private void sendViewStateMessage(ViewState state, String msg) {
		Message message = new Message();
		message.obj = new GeoSMSManagerAct.UpdateStatePack(state, msg);
    	updateListStateHandler.sendMessage(message);
	}
	
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		return super.runQueryOnBackgroundThread(constraint);
	}
	
	public void clear() {
		this.changeCursor(null);
		mAsyncLoader.shutdown();
	}
	
	
	private class LoadingViewStackRunner implements Runnable {

		@Override
		public void run() {
			Runnable runner = null;
            synchronized (loadingViewStack) {
                if (!loadingViewStack.empty()) {
                    runner = loadingViewStack.pop();
                }
            }
            if (runner != null) {
                runner.run();
            }
		}
	}


}
