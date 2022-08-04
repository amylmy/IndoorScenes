package cn.edu.whu.indoorscene.integration;

import android.location.Location;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import cn.edu.whu.indoorscene.MainActivity;

public class PosData {
	public MainActivity act;
	
	public double mLat;
	public double mLon;
	public double mAlt;
	public double mAccuracy;
	public double mBearing;
	public long mTimeStamp;
			
	public int mHour;
	public int mMinute;
	public int mSecond;

	public float mVelocity;
	public float mCourse;
	public int mSatnum_gps;
	public int mSatnum_glo;
				
	public String mStatus = "";
	public List<Integer> mPrn;
	public List<Integer> mCno;
	public List<Integer> mEle;
	public List<String> mP_sat;
		
	public double mGnss_outdr_prb;
	public double mGnss_indr_prb;
	public double mGnss_trns_prb;
	
	public int mLineIndex;
	public double mRatio;
		
	public PosData() {
		mLat = 0.0;
		mLon = 0.0;
		mAlt = 0.0;
		mTimeStamp = 0;
		
		mLineIndex = 0;
				
		mHour = -1;
		mMinute = -1;
		mSecond = -1;

		mVelocity = -1;
		mCourse = -1;
		mSatnum_gps = -1;
		mSatnum_glo = -1;
			
		mStatus = "";
		mPrn = new ArrayList<Integer>();
		mCno = new ArrayList<Integer>();
		mEle = new ArrayList<Integer>();
		mP_sat = new ArrayList<String>();
		
		mRatio = 0.0;
		mGnss_outdr_prb = 0.0;
		mGnss_trns_prb = 0.0;
		mGnss_indr_prb = 0.0;
	}
	
	public void ReFresh() {
		mHour = -1;
		mMinute = -1;
		mSecond = -1;
		
		mLineIndex = 0;

		mVelocity = -1;
		mCourse = -1;
		mSatnum_gps = -1;
		mSatnum_glo = -1;
		
		mStatus = "";
		
		mRatio = 0.0;
		mPrn.clear();
		mCno.clear();
		mEle.clear();
		mP_sat.clear();
	}
		
	public String ToStr() {
		String str = new String("Pos:");
		str += String.valueOf(mLat) + "   "+String.valueOf(mLon)+"   "+String.valueOf(mAlt);
		return str;
	}

	public void GetGnssDetector(List<Integer> Cno) {
		// TODO Auto-generated method stub
		double a0 = 19.4956;
		double a1 = 9.4354;
		double a2 = 27.8793;
		double a3 = 92.00;
		int counter_50 = 0;
		int counter = 0;

		for (Integer temp: Cno) {
			if (temp.intValue() != -1) {
				double N = a0 - a3;
				double D = (1 + Math.pow((double) (temp.intValue() / a2), a1));
				BigDecimal rslt = new BigDecimal(N / D + a3);
				rslt = rslt.setScale(1,BigDecimal.ROUND_HALF_UP);
				mP_sat.add(rslt.toString());
				counter += 1;
				
				if (rslt.intValue() >= 50) {
					counter_50 += 1;
				}				
			}
		}
		if (counter > 0) {
			mRatio = (double) counter_50 / counter;
			if (mRatio >= 0.3) {
				mGnss_outdr_prb = 0.9;
				mGnss_trns_prb = 0.1;
				mGnss_indr_prb = 0.0;
			} else if (mRatio <= 0.1) {
				mGnss_outdr_prb = 0.1;
				mGnss_trns_prb = 0.1;
				mGnss_indr_prb = 0.8;
			} else {
				mGnss_outdr_prb = 0.2;
				mGnss_trns_prb = 0.7;
				mGnss_indr_prb = 0.1;
			}
		} else {
			mGnss_outdr_prb = 0.0;
			mGnss_trns_prb = 0.0;
			mGnss_indr_prb = 1.0;
		}
	}

	public void ParseGpgga(String nmea0) {
		// TODO Auto-generated method stub
		String[] subnmea = nmea0.split("\\*");
		String nmea = subnmea[0];
		nmea = nmea.replaceAll("\\$GPGGA,", "");
		String[] nmeaSplit = nmea.split(",", -1);
		
		// UTC time of fix HHmmss.S
		if (!nmeaSplit[0].isEmpty()) {
			mHour = Integer.parseInt(nmeaSplit[0].substring(0, 2));
			mMinute = Integer.parseInt(nmeaSplit[0].substring(2, 4));
			mSecond = (int) Double.parseDouble(nmeaSplit[0].substring(4));
		}
		
		// latitude 
	/*	if (!nmeaSplit[1].isEmpty()) {
			mPosData.mLat = Integer.parseInt(nmeaSplit[1].substring(0, 2)) + 
					Double.parseDouble(nmeaSplit[1].substring(2)) / 60;
			if (nmeaSplit[2].equals("S")) mPosData.mLat = -mPosData.mLat;
		}
		else {
			mPosData.mLat = -1;
		}
		
		// longitude 
		if (!nmeaSplit[3].isEmpty()) {
			mPosData.mLon = Integer.parseInt(nmeaSplit[3].substring(0, 3)) + 
					Double.parseDouble(nmeaSplit[3].substring(3)) / 60;
			if (nmeaSplit[4].equals("W")) mPosData.mLon = -mPosData.mLon;
		}
		else {
			mPosData.mLon = -1;
		}*/
	}

	public void ParseGprmc(String nmea0) {
		// TODO Auto-generated method stub
		String[] subnmea = nmea0.split("\\*");
		String nmea = subnmea[0];
		nmea = nmea.replaceAll("\\$GPRMC,", "");
		String[] nmeaSplit = nmea.split(",", -1);
		
		if (nmeaSplit[1].equals("A")) {
			mStatus = "avtive";
		}
		else if (nmeaSplit[1].equals("V")) {
			mStatus = "void";
		}
	}

	public void ParseGpgsv(String nmea0) {
		// TODO Auto-generated method stub
		String[] subnmea = nmea0.split("\\*");
		String nmea = subnmea[0];
		nmea = nmea.replaceAll("\\$GPGSV,", "");
		String[] nmeaSplit = nmea.split(",", -1);
	
		mSatnum_gps = Integer.parseInt(nmeaSplit[2]);
		int iter = (nmeaSplit.length - 3) / 4;
		
		for (int j = 0; j < iter; j++){
	
			if (!nmeaSplit[4 * j + 3].isEmpty()){
				mPrn.add(Integer.parseInt(nmeaSplit[4 * j + 3]));
			} else 
			{
				mPrn.add(-1);
			}
			
			if (!nmeaSplit[4 * j + 4].isEmpty()){
				mEle.add(Integer.parseInt(nmeaSplit[4 * j + 4]));
			} else
			{
				mEle.add(-1);
			}
	
			if (!nmeaSplit[4 * j + 6].isEmpty()){
				mCno.add(Integer.parseInt(nmeaSplit[4 * j + 6]));
			} else
			{
				mCno.add(-1);
			}
		}
		mLineIndex += 1;
	}

	public void ParseGlgsv(String nmea0) {
		// TODO Auto-generated method stub
		String[] subnmea = nmea0.split("\\*");
		String nmea = subnmea[0];
		nmea = nmea.replaceAll("\\$GLGSV,", "");
		String[] nmeaSplit = nmea.split(",", -1);
		
		mSatnum_glo = Integer.parseInt(nmeaSplit[2]);
		int iter = (nmeaSplit.length - 3) / 4;
		
		for (int j = 0; j < iter; j++){
			
			if (!nmeaSplit[4 * j + 3].isEmpty()){
				mPrn.add(Integer.parseInt(nmeaSplit[4 * j + 3]));
			} else 
			{
				mPrn.add(-1);
			}
			
			if (!nmeaSplit[4 * j + 4].isEmpty()){
				mEle.add(Integer.parseInt(nmeaSplit[4 * j + 4]));
			} else
			{
				mEle.add(-1);
			}
	
			if (!nmeaSplit[4 * j + 6].isEmpty()){
				mCno.add(Integer.parseInt(nmeaSplit[4 * j + 6]));
			} else
			{
				mCno.add(-1);
			}
		}
		mLineIndex += 1;
		
		if (mLineIndex == (int) Math.ceil((double) mSatnum_gps / 4) + 
				(int) Math.ceil((double) (mSatnum_glo / 4))) {
			GetGnssDetector(mCno);
		}
	}

	public void ParseGpvtg(String nmea0) {
		// TODO Auto-generated method stub
		String[] subnmea = nmea0.split("\\*");
		String nmea = subnmea[0];
		nmea = nmea.replaceAll("\\$GPVTG,", "");
		String[] nmeaSplit = nmea.split(",", -1);
	
		if (!nmeaSplit[0].isEmpty()){
			mCourse = Float.parseFloat(nmeaSplit[0]);        //degree
		}
		
		if (!nmeaSplit[6].isEmpty()){
			mVelocity = Float.parseFloat(nmeaSplit[6]) * (10 / 36);        //m/s
		}
	}

	public synchronized void updateLocation(Location Loc) {
		// TODO Auto-generated method stub
		mLat = Loc.getLatitude();
		mLon = Loc.getLongitude();
		mAlt = Loc.getAltitude();
		mBearing = Loc.getBearing();
		mAccuracy = Loc.getAccuracy();
		mTimeStamp = Loc.getTime();
	}
	
	public synchronized Location getGpsLocation() {
		Location loc = new Location("dummyprovider");
		loc.setLatitude(mLat);
		loc.setLongitude(mLon);
		loc.setAltitude(mAlt);
		loc.setBearing((float) mBearing);
		loc.setAccuracy((float) mAccuracy);
		
		return loc;
	}
}