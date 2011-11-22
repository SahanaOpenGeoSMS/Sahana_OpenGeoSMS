package com.sahana.geosmser.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class DoubleCircleOverlay extends Overlay {
	public DoubleCircleMark overlayMark = null;
	public GeoPoint Position = null;
	public Point pixelsPoint;
	public RectF areaCircle = null;
	
	public DoubleCircleOverlay() {}
	
	public DoubleCircleOverlay(DoubleCircleMark mark) {
		setMark(mark);
	}
	
	public void setMark(DoubleCircleMark mark) {
		overlayMark = mark;
	}
	
	public void setPosition(GeoPoint point) {
		Position = point;
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		if(Position != null && overlayMark != null) {
			pixelsPoint = new Point();
			mapView.getProjection().toPixels(Position, pixelsPoint);
			overlayMark.drawDoubleCircleMark(canvas, pixelsPoint);
			areaCircle = overlayMark.areaCircleRectF;
			return true;
		}
		return super.draw(canvas, mapView, shadow, when);
	}
}
