package com.sahana.geosmser.overlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.sahana.geosmser.overlay.IDel.IGeoSMSPackBinder;
import com.sahana.geosmser.overlay.IDel.ISourceBindable;

public class SingleLocationOverlay extends DoubleCircleOverlay implements ISourceBindable<IGeoSMSPackBinder> {
	protected DoubleCircleMark mark;
	protected IGeoSMSPackBinder sourceBinder;
	
	public SingleLocationOverlay() {
		init();
	}
	
	private void init() {		
		mark = new DoubleCircleMark();
		initMark();
		this.setMark(mark);
	}
	
	public void initMark() {
		mark.setDefaultPaints();
	}
	
	public GeoPoint getLocation() {
		return this.Position;		
	}
	
	public boolean moveToLocation(MapView mapView) {
		if(mapView != null) {
			MapController mc = mapView.getController();
			GeoPoint gloc = this.getLocation();
			if(gloc != null) {
				mc.animateTo(gloc);
				return true;
			}
		}
		return false;
	}

	@Override
	public void setSourceBinder(IGeoSMSPackBinder binder) {
		sourceBinder = binder;
	}
	
}
