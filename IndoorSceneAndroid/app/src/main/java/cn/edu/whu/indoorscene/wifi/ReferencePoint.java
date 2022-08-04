package cn.edu.whu.indoorscene.wifi;

import com.google.android.gms.maps.model.LatLng;

public class ReferencePoint {
	public LatLng latlng;
	public double Height;
	public int floor_level;
	public double FloorHeading;
	public double light; //light intensity
	public double magnet;//total magnet
	public long ID;
	public double fpScore;// Finger Printing score
	public double North;//North Coordinate
	public double East;//East Coordinate
	public double FloorHeight;//Floor height relative to the first floor.
	public double CeilingHeight = 3.0; //Height of each floor
    public String sceneLabel; //Add by LMY, the scene which a reference point belongs to.
	public double sceneScore;

	public ReferencePoint() {
		latlng = new LatLng(0.0, 0.0);
		Height = 0.0;
		ID = 0L;
		floor_level = 0;
		FloorHeading = 0.0;
		light =0.0;
		magnet = 0.0;
		fpScore = 0.0;
		North = 0.0;
		East = 0.0;
		FloorHeight = 0.0;
		sceneLabel = null;
	}

	public ReferencePoint(ReferencePoint rp) {
		latlng = new LatLng(rp.latlng.latitude, rp.latlng.longitude);
		Height = rp.Height;
		floor_level = rp.floor_level;
		ID = rp.ID;
		FloorHeading = rp.FloorHeading;
		light = rp.light;
		magnet = rp.magnet;		
		fpScore = rp.fpScore;
		North = rp.North;
		East = rp.East;
		FloorHeight = rp.FloorHeight;
		sceneLabel = rp.sceneLabel;
	}

	//Add by LMY, add a scene label parameter.
	public ReferencePoint(LatLng ll, int fl, double h, double hd, double lt, double ma, long id, String sl) {
		latlng = ll;
		Height = h;
		FloorHeading = hd;
		ID = id;
		fpScore = 0.0;
		floor_level = fl;
		light = lt;
		magnet = ma;
		North = 0.0;
		East = 0.0;
		FloorHeight = 0.0;
		sceneLabel = sl;
	}

	public ReferencePoint(LatLng ll, int fl, double h, double hd, double lt, double ma, long id) {
		latlng = ll;
		Height = h;
		FloorHeading = hd;
		ID = id;
		fpScore = 0.0;
		floor_level = fl;
		light = lt;
		magnet = ma;
		North = 0.0;
		East = 0.0;
		FloorHeight = 0.0;
	}
	
	public ReferencePoint(LatLng ll, double h, long id) {
		latlng = ll;
		Height = h;
		ID = id;
		fpScore = 0.0;
		floor_level = 0;
		FloorHeading = 0.0;
		light =0.0;
		magnet =0.0;
		North = 0.0;
		East = 0.0;
		FloorHeight = 0.0;
	}

	public ReferencePoint(LatLng ll, int fl, long id) {
		latlng = ll;
		Height = 0.0;
		FloorHeading = 0.0;
		ID = id;
		fpScore = 0.0;
		floor_level = fl;
		light =0.0;
		magnet =0.0;
		North = 0.0;
		East = 0.0;
		FloorHeight = 0.0;
	}
	
	public ReferencePoint(LatLng ll, long id) {
		latlng = ll;
		Height = 0.0;
		ID = id;
		fpScore = 0.0;
		FloorHeading = 0.0;
		floor_level = 0;
		light =0.0;
		magnet =0.0;
		North = 0.0;
		East = 0.0;
		FloorHeight = 0.0;
	}
	
	public void SetFPScore(double sc) {
		fpScore = sc;
	}

	public double GetFPScore() {
		return fpScore;
	}

	public void SetSceneScore(double sc) {
		sceneScore = sc;
	}

	public double GetSceneScore() {
		return sceneScore;
	}
	
	public boolean CalNEU(LatLng origin) {
		if(latlng.latitude == 0.0 && latlng.longitude == 0.0) return false;
		double DegToMeter = 3.1415962*6378137.0/180;//degree to meter
		double cs= Math.cos(origin.latitude*3.1415962/180);
		North = (latlng.latitude - origin.latitude)*DegToMeter;
		East = (latlng.longitude - origin.longitude)*cs*DegToMeter;
		FloorHeight = (floor_level - 1)*CeilingHeight;
		return true;
	}
}
