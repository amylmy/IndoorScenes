package cn.edu.whu.indoorscene.wifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import cn.edu.whu.indoorscene.MainActivity;
import cn.edu.whu.indoorscene.Fragment.MapFragment;
import cn.edu.whu.indoorscene.R;

public class WiFiScan {
	private WifiManager mWifiManager;
	private BroadcastReceiver mReceiver;
	private  MainActivity mAct;

	private List<ScanResult> mWiFiList = new ArrayList<ScanResult>();

	// LoadFingerPrints
	private FingerPrintDB mFP = new FingerPrintDB();
	private ReferencePoint mCurrWifiRp = new ReferencePoint(); // hold the current RS of
															// the fingerprinting
															// results. It is null
															// if no RS fund.
	private ArrayList<ReferencePoint> ClusterRS;
	private ArrayList<ReferencePoint> tmpRS;
	
	private long mLastWiFiScanTime = 0L;
	private long mLastLastWiFiScanTime = 0L;
	private boolean mPosMode = true;//default: positioning mode (true), false = training mode
	private boolean mScanMode = true;//default: scanning mode
	private LatLng mTouchPoint = new LatLng(0.0,0.0);
	private long mNumRS = 0L; //No Reference Stations in the Fingerprint Database.
	private boolean mDBUpdated = false;//Finger print database updated.
	private int mFloorLevel = 1;
	private double mHeight = 0.0;
	private int mNumScans = 0;
	private int mCurDirection = 1; //1...4, 1-direction close to north. Direction to scan (wifi) for the current RS.

	//Default Finerprintinting Sentting, overwrite by readSettings() from init file.
	private long max_scan_interval = 10000;//reboot the WiFi manager after elapsing this time interval (in million seconds)since last scan, .
	private double mFLHeading = -70.0; //Floor Heading (first scanning direction at a RS). Each RS can store 4 directions.
	private double pos_criteria = 10.0; // RS with score exceed this number will be excluded (the small the score, the better).
	private double max_var = 25.0;
	private double min_rssi = -90.0;
	private double max_rssi = 0.0;
	private double max_cluster_dis = 6;//5;//distance in meters in both Lat and Lng directions (rectangle).
	private boolean bApplyOffset = false;//calculate and apply an offset between the scanning RSSI tuple and RSSI tuple in database.
	private int max_cluster_size = 8; // 5 //number of RS with best scores to check for cluster. // LMY-2017.01.21: The origin value is 5.
	private int min_cluster_size = 1; // 3 //min number of RS points to define a Cluster.
	private int min_matched_AP = 1; // 3 //minimum number of matched for the solutions.
	private double min_matched_percentage = 0.3; // 0.5 //50%
	private double strong_signal_treshold = -65.0; // any signal exceed this number is considered as a strong signal.
	private int num_strong_signal_threshold =1;//threshold for the number of strong signal in the current scan.
	
	private double score_criteria = 1.0e+10; // a number
	private double cluster_range_lat = max_cluster_dis/111319.61646563507933; //all are considered as one cluster.

	//For creating FP database
	private ArrayList<String> mMacID = new ArrayList<String>(); //a list of MAC add. of the APs
	private ArrayList<Double> mRssi = new ArrayList<Double>();//the current average of RSSI for each AP.
	private ArrayList<Double> mVariance = new ArrayList<Double>();//the current average of RSSI for each AP.
	private ArrayList<Integer> mScanCount = new ArrayList<Integer>();//total scan accounts for each AP (can be different for different APs).
	//For storing the top 5 RS with highest best scores (shorter weighted signal distances).
	private ArrayList<ReferencePoint> mTopRS = new ArrayList<ReferencePoint>(); // Store the top five RS.

	private  Handler fpHandler = new Handler() { // Handle for fingerprinting.
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) { // message sent when WifiScan done.
				if(getPosMode()) {
					FingerPrinting(); //Positioning mode, do the finger printing
					MapFragment.wifiHandler.sendEmptyMessage(0);//notify the PDR class that a FingerPrinting results is aviaible.
				} else {             // Training mode, create finger print database. 
					UpdateFingerPrints();
				}
			} else if (msg.what == 1) {
				TextView tv = (TextView) mAct.findViewById(R.id.textViewScanNum);
				if( null!=tv) tv.setText(String.valueOf(getScanNum()));
			}else {
			
			}
		}
	};	
	
	public WiFiScan() {
		mWifiManager = null;
		mReceiver = null;
		mAct = null;
	}

	public void Init (MainActivity act) {
		mAct = act;
		// LMY:cluster的范围根据什么确定？
		cluster_range_lat = max_cluster_dis/111319.61646563507933;
		readSettings();
		mNumRS = mFP.LoadFP();
		if(0 == mNumRS) { 
			Toast.makeText(act, "Finger printing database file not exist, or empty!", Toast.LENGTH_SHORT).show();	//DB file not exisit,
		}
		onResume(); // Start the WiFi scanning
	}

	public void readSettings() {
		InputStream instream;
		File rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		File fileDir = new File(rootDir, "Indoor/WiFi");
		boolean check = rootDir.canWrite();
		if (check){
			Toast.makeText(MainActivity.context, "Storage permissions: Yes", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.context, "Storage permissions: No", Toast.LENGTH_SHORT).show();
		}
		if(!fileDir.exists()) {
			fileDir.mkdirs();
		}
		File WiFiSettings = new File(fileDir, "WiFiSettings.txt"); // LMY: 该文件不存在时，使用前面设置的默认值
		if(!WiFiSettings.exists()){
			Toast.makeText(MainActivity.context, "WiFiSettings.txt doesn't exist!", Toast.LENGTH_SHORT).show();
		}
		try {
			instream = new BufferedInputStream(new FileInputStream(WiFiSettings));
			// if the file is available for reading
			if (instream != null) {
				// prepare the file for reading
				InputStreamReader inputReader = new InputStreamReader(instream);
				BufferedReader buffReader = new BufferedReader(inputReader);
				String line = "";
				while ((line = buffReader.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0) {
						// empty line, new record start
					} else {
						StringTokenizer st = new StringTokenizer(line, ";");
						String parameter = st.nextToken(); //存储参数名称
						String setting = st.nextToken(); //存储参数值
						int bOffset = 0;
						bApplyOffset = false;
						if (parameter.equals("max_scan_interval")) {max_scan_interval = Long.parseLong(setting);}
						else if (parameter.equals("mFLHeading")) {mFLHeading = Double.parseDouble(setting);}
						else if (parameter.equals("pos_criteria")) {pos_criteria = Double.parseDouble(setting);}
						else if (parameter.equals("max_var")) {max_var = Double.parseDouble(setting);}
						else if (parameter.equals("min_rssi")) {min_rssi = Double.parseDouble(setting);}
						else if (parameter.equals("max_rssi")) {max_rssi = Double.parseDouble(setting);}
						else if (parameter.equals("max_cluster_dis")) {max_cluster_dis = Double.parseDouble(setting);}
						else if (parameter.equals("bApplyOffset")) {bOffset = Integer.parseInt(setting);}
						else if (parameter.equals("max_cluster_size")) {max_cluster_size = Integer.parseInt(setting);}
						else if (parameter.equals("min_matched_AP")) {min_matched_AP = Integer.parseInt(setting);}
						else if (parameter.equals("min_matched_percentage")) {min_matched_percentage = Double.parseDouble(setting);}
						else if (parameter.equals("strong_signal_treshold")) {strong_signal_treshold = Double.parseDouble(setting);}
						else if (parameter.equals("num_strong_signal_threshold")) {num_strong_signal_threshold= Integer.parseInt(setting);}
						else {}
						if(bOffset != 0) bApplyOffset = true;
					}
				}
				instream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Receiver for information on the network info
	public void onResume() {
		onPause();
		// register this class as a listener for the orientation and
		mWifiManager = (WifiManager) mAct.getSystemService(Context.WIFI_SERVICE);
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
		mReceiver = new WifiReceiver();
		mAct.registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		Scan();
	}

	public void onPause() { // Pause WiFi Scanning
		if (mReceiver != null) {
			mAct.unregisterReceiver(mReceiver);
		}
		mReceiver = null;
		mWifiManager = null;
	}

	// WiFi Reciever Class
	class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			List<ScanResult> list = mWifiManager.getScanResults();
			updateWiFi(list); //Need to add this function
		}
	}

	private void updateWiFi(List<ScanResult> list) {
		setScanResults(list);
		setLastLastScanTime(getScanTime());
		setScanTime(System.currentTimeMillis());
		getHandler().sendEmptyMessage(0);// Notify the FingerPrinter.
	}

	//property set/get functions.
	private synchronized void setScanResults(List<ScanResult> list) {
		mWiFiList.clear();	//clear the previous scanned results.
		if (!list.isEmpty()) {
			mWiFiList.addAll(list);
		}
	}

	public void Scan() {
		if (null != mWifiManager) {
			mWifiManager.startScan();
		}
	}

	// FingerPrinting // LMY: Positioning mode
	@SuppressLint("UseValueOf")
	public void FingerPrinting() {
		Scan(); // Scan it again.
		if(!mAct.getSensorStatus()) return; //Not in positioning mode.
		if(0 == mNumRS) return; //no finger printing file.
		// This is the only function that use the mFP = FingerPrint Database, no
		// synch is needed.
		long tb = System.currentTimeMillis();	
		List<ScanResult> srList = new ArrayList<ScanResult>(); // List of scan results
		List<Double> db_rssi_list = new ArrayList<Double>();// matched rssi from DB
		List<Double> db_var_list = new ArrayList<Double>();// variance of the matched rssi
		List<Double> sc_rssi_list = new ArrayList<Double>();// matched rssi from scan result (for the same AP)
		//define the search ran
//		long dt = System.currentTimeMillis() - getScanTime();
//		double searchRange =0.0;
//		if(dt < 8000 && getCurrentRS().latlng.latitude != 0.0 && getCurrentRS().latlng.longitude !=0.0) { // can define a search area.
//			double dis = dt*mAct.mAcc.sensorDataAcc.getSpeed();
//			if(dis < 15) dis = 15.0; //minimum search distance 10.0 meters.
//			double dis = 15.0;
//			searchRange = dis*0.0000089831427; //57.29571483438896/6378137.0 = 0.0000089831427	
//		}
		if (!getScanResults().isEmpty()) srList.addAll(getScanResults());
		if (srList.isEmpty()) {
			return; // No WiFi Scanning result.
		}
		ArrayList<WiFiFingerPrint> fp_db = mFP.getFP_DB();
		int num_RS = fp_db.size(); // number of reference stations in the radio map
		int num_AP_SC = srList.size(); // number of APs in the scaning list
		// Log.i("FP-DB Size: ", String.valueOf(mFP.FP_DB.size()));
		mTopRS.clear(); //Clear the list.
		for (int i = 0; i < num_RS; i++) {
			fp_db.get(i).rs.SetFPScore(score_criteria); // reset the fpScore
			//Check if the i-th RS within the search range // LMY: 这里的search range可以替换成scene的范围
//			if(searchRange > 0.0) { //we have a search range
//				LatLng llRS =getCurrentRS().latlng;
//				LatLng llDB = mFP.getFP_DB().get(i).rs.latlng;
//				if (Math.abs(llRS.latitude - llDB.latitude) > searchRange) break;				
//				if (Math.abs(llRS.longitude - llDB.longitude) > searchRange) break;	
//			}
			// LMY-2017.01.21: Check if the i-th RS within the current scene.
//			if(MapFragment.mCurrentScene!=null) {
//				if(!fp_db.get(i).rs.sceneLabel.equals(MapFragment.mCurrentScene)) {
//					fp_db.remove(i);
//				}
//			}
			int num_AP_RS = fp_db.get(i).list_RSSI.size(); // numbe APs for the current RS
			db_rssi_list.clear();// rssi of the matched APs from DB.
			// LMY: db_var_list没有清空
			sc_rssi_list.clear();// rssi of the matched APs from current scanning results.
			// LMY: 寻找匹配的AP点 （这个匹配算法效率有点低，后续改进）
			for (int k = 0; k < num_AP_SC; k++) { // scan the whole list of APs at
				ScanResult sr = srList.get(k);	// this AP
				for (int j = 0; j < num_AP_RS; j++) { // For each AP in the scanning result
					String bssid = fp_db.get(i).list_SSID.get(j);// AP Mac address
					double rssi = fp_db.get(i).list_RSSI.get(j);// AP RSSI
					double var = fp_db.get(i).list_Variance.get(j); //AP Variance
					if (compareMacAddress(sr.BSSID, bssid) &&  // find the matched BSSID
							(rssi > min_rssi) && //within the RSSI range
							(rssi < max_rssi) && 
							(var < max_var)) { //variance large than this number will not be used.
						db_rssi_list.add(rssi);
						db_var_list.add(var);
						sc_rssi_list.add((double)sr.level);
						break; // break for loop of j
					}
				}
			}
			int len = sc_rssi_list.size();
			int m = 0;
			int num_strong_signal = 0;
			for (m = 0;m < len; m++) {
				if(sc_rssi_list.get(m) > strong_signal_treshold) num_strong_signal++;
			}
			// LMY: 挑选出符合条件的AP信号值，计算signal distance，挑选备选点
			if((len >= min_matched_AP) && //number of matched AP has to exceed the threshold
			  (double)len / (double)num_AP_RS > min_matched_percentage && //number of matched scanned APs has to be more than x% of the total AP in the DB.
			  num_strong_signal >= num_strong_signal_threshold) { //num of APs with strong signals has to exceed this threshold.
				double offset = 0.0;
				if(bApplyOffset) {
					for (m = 0; m < len; m++) {
						double diff = db_rssi_list.get(m) - sc_rssi_list.get(m);
						offset += diff;
					}
					offset /= len;
					for (m = 0; m < len; m++) {
						sc_rssi_list.set(m, sc_rssi_list.get(m) + offset); // normalized the RSSI values by adding an offset
					}
				}
				double scale = 1.0e+10;
				double score = 0.0;
				if (len >= 10) { // 10 or more matched APs
					scale = 1.0;
				} else { // less than 10 matched APs
					scale = 100.0 / (len * len);
				}
				for (m = 0; m < len; m++) {
					double diff = 0.0;
					double db_ss = db_rssi_list.get(m);
					double db_var = db_var_list.get(m);
					double sc_ss = sc_rssi_list.get(m);
					if(db_var == 0.0) db_var = max_var; //there are cases in DB, only two samples. 
					diff = db_ss - sc_ss;
	//				score += diff * diff; //un-weight solutions
					score +=diff*diff*db_var;
				}
				score /= len * len; // average signal distance^2
				score = scale * score; // 
				if(score < pos_criteria) {
					mFP.getFP_DB().get(i).rs.SetFPScore(score); // save the score in the
					addToClusterList(mFP.getFP_DB().get(i).rs);// check if its score is in top 5, if it is, then add to list.
				}
			}
		}
		// find the Reference Station with best score
		int kk = getBestRSFromCluster();
//		if (MapFragment.mCurrentScene!=null) { //need a more reasonable condition
//			int kk = getSceneMatchedRS();
//		} else {
//			int kk = getBestRSFromCluster();
//		}
		Log.d("Finger printing: ", "Ref. Sta ID: " + String.valueOf(getCurrentRS().ID) +
					"  score: " + String.valueOf(getCurrentRS().fpScore) +
					"  heading: " + String.valueOf(getCurrentRS().FloorHeading) +
					"  num RS in cluster: " + String.valueOf(kk) +
					"  Processing Time (ms):"+ String.valueOf(System.currentTimeMillis() - tb));
	}

	private void addToClusterList(ReferencePoint rp) {
		int num = mTopRS.size();
		if(rp.fpScore == score_criteria) return; //do not add this element to the list.
		if(num < max_cluster_size) { //Just add to end
			mTopRS.add(rp);
			return;
		}
		int k =0; //idx with a maximum score	
		double maxScore = mTopRS.get(0).fpScore;
		// LMY: i应该小于mTopRS.size(),否则可能会超出上界，原始代码i<max_cluster_size
		for (int i = 1; i< mTopRS.size(); i++) { //find the RS in the current list with maximum score.
			if (mTopRS.get(i).fpScore > maxScore) {
				k = i;
				maxScore = mTopRS.get(i).fpScore;
			}
		}
		// LMY:替换掉距离最远的那个点
		if(rp.fpScore < maxScore) mTopRS.set(k, rp);//replace the element with maximum score
	}

	private int getBestRSFromCluster() { //Select the best possible station from a largest cluster.
		double dla =0.0;
		double dln =0.0;
		double best_score = 0.0;
		double cs = 0.0;
		double cluster_range_lng = 0.0;
		if (mTopRS.isEmpty()) {
			setCurrentRS(new ReferencePoint());
			//Toast.makeText(mAct, "No WiFi Position", Toast.LENGTH_SHORT).show();
			return 0;
		}
		cs = Math.cos(mTopRS.get(0).latlng.latitude/57.29571483438896);
		cluster_range_lng = Math.abs(cluster_range_lat/cs);
		ClusterRS = new ArrayList<ReferencePoint>(); // Store the top five RS.
		tmpRS = new ArrayList<ReferencePoint>(); // Store the top five RS.
		ReferencePoint rs = new ReferencePoint();
		int nrs = mTopRS.size();
		//find the RS with the best score in the mTopRS list.
		int k = 0;
		int n = 0;
		best_score  = mTopRS.get(0).fpScore;
		for (n = 1; n < nrs; n++) {
			if(mTopRS.get(n).fpScore < best_score) { // LMY: fpScore越小，距离越近
				k = n;
				best_score = mTopRS.get(n).fpScore;
			}
		}
		rs = mTopRS.get(k);
		//find cluster
		for (int i = 0; i < nrs; i++) {
			if(!tmpRS.isEmpty()) tmpRS.clear();
			tmpRS.add(mTopRS.get(i));
			for (int j = 0; j < nrs; j++) {
				if(i !=j) {
					// LMY: 两点之间的距离小于某个范围就属于同一个cluster
					dla = (mTopRS.get(j).latlng.latitude - mTopRS.get(i).latlng.latitude);
					dln = (mTopRS.get(j).latlng.longitude - mTopRS.get(i).latlng.longitude);
					if((Math.abs(dla) < cluster_range_lat) && (Math.abs(dln) < cluster_range_lng)) tmpRS.add(mTopRS.get(j));
				}
			}
			if(ClusterRS.size() < tmpRS.size()) {
				ClusterRS.clear();
				ClusterRS.addAll(tmpRS);
			}
		}
		//Find the best in the cluster.
		if(ClusterRS.size() >= min_cluster_size ) { //three or more RS in a cluster in the cluster.
			k = 0;
			best_score = ClusterRS.get(0).fpScore;
			for (n = 1; n < ClusterRS.size(); n++) {
				if(ClusterRS.get(n).fpScore < best_score) {
					k = n;
					best_score = ClusterRS.get(n).fpScore;
				}
			}
			rs = ClusterRS.get(k);
		}
		if(ClusterRS.size() >= min_cluster_size ) {
			// LMY: 这里的score和前面的score意义不同，前面是距离，这里是cluster的大小
			rs.SetFPScore((double) ClusterRS.size()); //Add the number cluster to the solution, the higher the score, the better the quality.
		}else {
			rs.SetFPScore(0.0); //Unreliable solution.			
		}
		setCurrentRS(rs);
		setFloorLevel(rs.floor_level); //set the floor level for automatic map uploading. 
		return ClusterRS.size(); //return the number of RSs in the cluster.
	}
	
	public boolean compareMacAddress(String add1, String add2) {
		if(add1.charAt(0) != add2.charAt(0)) return false;
		if(add1.charAt(1) != add2.charAt(1)) return false;
		if(add1.charAt(3) != add2.charAt(3)) return false;
		if(add1.charAt(4) != add2.charAt(4)) return false;
		if(add1.charAt(6) != add2.charAt(6)) return false;
		if(add1.charAt(7) != add2.charAt(7)) return false;
		if(add1.charAt(9) != add2.charAt(9)) return false;
		if(add1.charAt(10) != add2.charAt(10)) return false;	
		if(add1.charAt(12) != add2.charAt(12)) return false;
		if(add1.charAt(13) != add2.charAt(13)) return false;
		if(add1.charAt(15) != add2.charAt(15)) return false;
		if(add1.charAt(16) != add2.charAt(16)) return false;
		return true;
	}

	// LMY: executing when PosMode off, create fingerprints
	public void UpdateFingerPrints() {
		List<ScanResult> scList = new ArrayList<ScanResult>(); // List of scan results
		if (!getScanResults().isEmpty()) scList.addAll(getScanResults()); //copy the current scanned results.
		int i = 0;
		int j = 0;
		int num_AP_SC = scList.size(); // number of APs in the scaning list		
		if(mMacID.isEmpty()) { //First scan 
			for (i = 0; i < num_AP_SC; i++) {
				mMacID.add(scList.get(i).BSSID);
				mRssi.add((double)scList.get(i).level);
				mScanCount.add(1);
				mVariance.add(0.0);
			}
		} else { //consecutive scan
			int numAPInList = mMacID.size();
			for (i = 0; i < num_AP_SC; i++) {
				String mac = scList.get(i).BSSID;
				double lev = (double) scList.get(i).level;
				boolean bFound = false;
				for (j=0;j<numAPInList;j++) {
					if(mac.matches(mMacID.get(j))) { //Find the AP in the list
						double mLev = mRssi.get(j);
						double var = mVariance.get(j);
						int count = mScanCount.get(j);
						var = (count-1)*var/count + (lev - mLev)*(lev - mLev)/(count +1); // LMY: 更新方差
						mLev = mLev*count/(count+1) + lev/(count + 1); // LMY: 求rssi的平均值，作为新的信号强度值
						mRssi.set(j, mLev);
						mVariance.set(j,var);
						mScanCount.set(j,count +1);
						bFound = true;
						break;	
					}
				}
				if(!bFound) { //can not find the i-th AP in the current list
					mMacID.add(new String(mac));
					mRssi.add(new Double(lev));
					mScanCount.add(1);
					mVariance.add(0.0);
				}
			}	
		}
		setScanNum(getScanNum()+1);
		getHandler().sendEmptyMessage(1); //Send a message to update the number of scans
		Scan(); // Scan it again.
	}
	
	public void ChangeScanMode(boolean sm)  {
		setScanMode(sm);
		if (sm) {
			onResume();//Turn it on		
			if(!getPosMode()){ //initiate the training phase, empty the data records.
				mMacID.clear();
				mRssi.clear();
				mScanCount.clear();
				mVariance.clear();
				setScanNum(0);
				if(!mAct.getSensorStatus()) { //Just turn on the sensors, 
					mAct.setSensorStatus(mAct.sensorsSwitch());
				}
			}
		} else {
			onPause(); //Turn it off	
			if(!getPosMode()) {//Training phase for the current RS end, write the finger prints to the data base.
				CreateFPRecord();
			}
		}
	}
	
	public void ChangePosMode(boolean pm)  {
		setPosMode(pm);
		if(pm) { //change to positioning mode
			onResume();//Turn it on
			if(mDBUpdated) {
				mNumRS = mFP.LoadFP();
				if(0 == mNumRS) { 
					Toast.makeText(mAct, "Finger printing database file not exist, or empty!", Toast.LENGTH_SHORT).show();	//DB file not exisit,
				}
			}
		}else { //change to training mode
			onPause();
			//clean up.
			mMacID.clear();
			mRssi.clear();
			mScanCount.clear();
			mVariance.clear();
			setScanNum(0);
		}
	}
	
	public void CreateFPRecord() {
		if(mMacID.isEmpty()) return;
		if(mRssi.isEmpty()) return;
		if(mScanCount.isEmpty()) return;
		if(mVariance.isEmpty()) return;
		if(getCurDirection() > 4) return; //invalid direction, do not record.
		LatLng ll = getTouchPoint();
	  	if(0.0 == ll.latitude && 0.0 == ll.longitude) return;
	  	String eol = System.getProperty("line.separator");
	  	String zeroMac = "00:00:00:00:00:00";
	  	String rec ="";
	  	int numAP = mMacID.size();
	  	mNumRS = mNumRS +1;
		String currentScene = MainActivity.sceneLabel; // LMY:新增加的场景label
	  	rec = String.format("@%d;%s;%f;%f;%d;%f;%f;%f;%f", mNumRS, currentScene,
				ll.latitude,ll.longitude,
	  			getFloorLevel(),
	  			getHeight(), 
	  			getFLHeading() + 90 * (getCurDirection() - 1),
	  			0.0, 0.0) + eol;

	  	for (int i = 0; i < numAP; i++) {
	  		if(!zeroMac.matches(mMacID.get(i)) && (mScanCount.get(i) > 2) ) { //for those AP that has less than 3 samples, ignore it.
	  		rec = rec + mMacID.get(i) + ";" + String.format("%f;%f;%d", mRssi.get(i), mVariance.get(i), mScanCount.get(i)) + eol;
	  		}
	  	}
	  	mFP.addToDatabase(rec);
	  	mDBUpdated = true;
	}

	//TODO:Add a scene label in fingerprints database
//	private synchronized String getSceneLabel() {
//		return "scene";
//	}

	public void checkWiFiState() { // reboot WiFi if the it is more than 6 seconds since last scan
		if(getScanMode()) {
			if((System.currentTimeMillis() - getScanTime()) > max_scan_interval) {
				onResume();
				//Toast.makeText(mAct, "WiFi scan started!", Toast.LENGTH_SHORT).show();	//DB file not exisit,
			}			
		}
	}
	
	public boolean validatePos(LatLng ll) {
		double dis =0.0;
		double dla = 0.0;
		double dln = 0.0;
		double minDis = 1.0e+10;
		double DegToMeter = 3.1415962*6378137.0/180;//degree to meter
		LatLng llDB = new LatLng(0.0,0.0);
		int numRS = mFP.getFP_DB().size();
		
		if(mFP.getFP_DB().isEmpty()) {
			return false;
		}
		
		int k = 0;
		
		for (int i = 0; i < numRS; i++) {
			llDB = mFP.getFP_DB().get(i).rs.latlng;
			double cs= Math.cos(ll.latitude*3.1415962/180);
			dla = (llDB.latitude - ll.latitude)*DegToMeter;
			dln = (llDB.longitude - ll.longitude)*cs*DegToMeter;
			dis = dla*dla + dln*dln;
			if (dis < minDis) {
				k=i;
				minDis = dis;
			}
		}
		
		return true;
	}
	

	
	public synchronized Handler getHandler() {
		return fpHandler;
	}
	
	public synchronized List<ScanResult> getScanResults() {
		List<ScanResult> list = mWiFiList;
		return list;
	}
	
	public synchronized void setScanTime(long st) {
		mLastWiFiScanTime = st;
	}
	
	public synchronized void setLastLastScanTime(long st) {
		mLastLastWiFiScanTime = st;
	}
	
	public synchronized long getScanTime() {
		return mLastWiFiScanTime;
	}
	
	public synchronized long getLastLastScanTime() {
		return mLastLastWiFiScanTime;
	}
	
	public synchronized void setPosMode(boolean pm) {
		mPosMode = pm;
	}
	
	public synchronized boolean getPosMode() {
		return mPosMode;
	}	
	
	public synchronized void setScanMode(boolean sm) {
		mScanMode = sm;
		if(sm) {
			setCurDirection(getCurDirection() + 1);
		}
	}
	
	public synchronized boolean getScanMode() {
		return mScanMode;
	}
	
	public synchronized void setScanNum(int ns) {
		mNumScans = ns;
	}
	
	public synchronized int getScanNum() {
		return mNumScans;
	}
	
	public synchronized void setFloorLevel(int fl) {
		mFloorLevel = fl;
	}
	
	public synchronized int getFloorLevel() {
		return mFloorLevel;
	}

	public synchronized void setHeight(double h) {
		mHeight = h;
	}

	public synchronized void setCurrentRS(ReferencePoint rs) {
		mCurrWifiRp = rs;
	}	
	
	public synchronized ReferencePoint getCurrentRS() {
		return mCurrWifiRp;
	}

	public synchronized double getHeight() {
		return mHeight;
	}
	
	public synchronized void setTouchPoint(LatLng tp) {
		mTouchPoint = tp;
		setCurDirection(0); //The first direction (close to north) to scan (wifi) for in the current RS 
	}
	
	public synchronized LatLng getTouchPoint() {
		return mTouchPoint;
	}
	
	public synchronized double getFLHeading() {
		return mFLHeading;
	}
	
	public synchronized void setFLHeading(double hd) {
		mFLHeading = hd;
	}
	
	public synchronized void setCurDirection(int cd) {
		mCurDirection = cd;
		if(cd > 4) {
			Toast.makeText(mAct, "You have scanned 4 direction already!", Toast.LENGTH_SHORT).show();	//DB file not exisit,
		}
	}
	
	public synchronized int getCurDirection() {
		return mCurDirection;
	}
	
	public synchronized int getClusterSize() {
		if (null == ClusterRS) return 0;
		return ClusterRS.size();
	}
	
	public synchronized double getFloorHeading() {
		return mCurrWifiRp.FloorHeading;
	}
}