package com.sahana.geosmser.overlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.sahana.geosmser.GeoSMSPack;

import android.view.MenuItem.OnMenuItemClickListener;

public interface IDel {
	public interface IOverlayLocation<T> {
		public T getOverlay();
		public void setOverlay(T overlay);
		public GeoPoint getLocation();
		public void setLocation(GeoPoint location);
		public void moveToLocation();
		public void bindMapView(MapView mapView);
		public GeoSMSPack getSource();
		public void setSource(GeoSMSPack pack);
	}
	
	public interface IMenuEvtOverlayLocation<T> extends IOverlayLocation<T>, OnMenuItemClickListener { }  
	
	public interface ISourceBindable<T> {
		public void setSourceBinder(T binder);
	}
	
	public interface IGeoSMSPackBinder {
		public void onBind(GeoSMSPack pack);
	}
}
