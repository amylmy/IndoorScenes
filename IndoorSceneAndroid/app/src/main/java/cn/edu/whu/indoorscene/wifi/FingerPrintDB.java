package cn.edu.whu.indoorscene.wifi;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.StringTokenizer;

@SuppressLint("UseValueOf")
public class FingerPrintDB {
	// A list contains all WiFi FingerPrints
	private ArrayList<WiFiFingerPrint> FP_DB = new ArrayList<WiFiFingerPrint>();
	private BufferedReader buffReader;
	File fileFP = null;
	ArrayList<String> ssid = new ArrayList<String>();
	ArrayList<Double> rssi = new ArrayList<Double>();
	ArrayList<Double> var = new ArrayList<Double>();
	ArrayList<Integer> count = new ArrayList<Integer>();
 	ReferencePoint rs = new ReferencePoint();

	public FingerPrintDB() {}

	// LMY: 返回Reference points总数，为指纹库FP_DB赋值
	public long LoadFP() {
		InputStream instream;
		long numRS = 0;
		// LMY:打开存储指纹的txt文件，如果不存在就创建该文件
		if(fileFP == null) OpenDBFile(); //Open the file.
		try {
			instream = new BufferedInputStream(new FileInputStream(fileFP));// new FileInputStream(file);
			// if the file available for reading
			if (instream != null) {
				// prepare the file for reading
				InputStreamReader inputReader = new InputStreamReader(instream);
				buffReader = new BufferedReader(inputReader);
				String line="";
				while ((line = buffReader.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0) {
						// empty line, new record start
					} else {
						StringTokenizer st = new StringTokenizer(line, ";");
						if (line.charAt(0) == '@') {
							numRS++; //find a reference station
							if (!ssid.isEmpty()) { // LMY: 说明已经被赋值过了，将这些值存储到FP_DB中
								                   // 上一层循环保存在各个变量中的数据
								getFP_DB().add(new WiFiFingerPrint(rs, ssid, rssi,var,count));
								// LMY: empty ArrayList to store next WiFiFingerprint.
								rssi.clear();
								ssid.clear();
								var.clear();
								count.clear();
								rs = new ReferencePoint();// empty the RS
							}
							String id = st.nextToken(); //ID of the reference point
							String sceneLabel = st.nextToken(); //Add by LMY, current scene label.
							String lat = st.nextToken(); //Latitude of the reference point
							String lng = st.nextToken(); //Longitude of the reference point
							String fl = st.nextToken(); //floor-level of the reference point
							String h = st.nextToken(); //height of the reference point
							String hd = st.nextToken(); //floor heading.
							String lt = st.nextToken(); //ambient light intensity							
							String ma = st.nextToken(); //magnet.

							id = id.trim();
							id = id.substring(1, id.length()); // remove the @
							// LMY:参考点赋值，rs -> reference point, 即采样位置
							rs = new ReferencePoint(new LatLng(Double.parseDouble(lat.trim()), Double.parseDouble(lng.trim())),
									Integer.parseInt(fl.trim()),
									Double.parseDouble(h.trim()),
									Double.parseDouble(hd.trim()),
									Double.parseDouble(lt.trim()),
									Double.parseDouble(ma.trim()),
									Long.parseLong(id),
									sceneLabel);
						} else {
							// LMY:不是以@开头，说明是AP的rssi信息；每个AP的信息包括mac地址、信号强度、方差、采样个数
							ssid.add(st.nextToken());
							Double level = Double.parseDouble(st.nextToken().trim());
							rssi.add(level);
							Double variance = Double.parseDouble(st.nextToken().trim());
							var.add(variance);
							Integer cnt = Integer.parseInt(st.nextToken().trim());
							count.add(cnt);
						}
					}
				}
				if (!ssid.isEmpty()){
					// LMY：循环结束后，需要把最后一个指纹数据也存储起来
					// add the last block of fingerprints to  the list
					getFP_DB().add(new WiFiFingerPrint(rs, ssid, rssi,var,count));
				}
				instream.close();
			}
		} catch (Exception ex) {
			// print stack trace.
			ex.printStackTrace();
		}
		//calculate the NEU coordinates. FloorHeight is used for UP (relative to 1-floor
		int i = 0;
		for (i = 0; i < getFP_DB().size(); i++) {
			//Calculate the NEU coordinates, using the first RS as origin.
			// LMY: 坐标转换
			getFP_DB().get(i).rs.CalNEU(getFP_DB().get(0).rs.latlng);
		}
		//calculate the Corridor heading. (two and only two neighbor points in a line).
		double RtoD = 180/3.1415962;
		for (i=0; i<getFP_DB().size();i++) {
			int numNBRS = 0;
			double hd1 = 0.0;
			double hd2 = 0.0;
			double dN = 0.0;
			double dE = 0.0;
			getFP_DB().get(i).rs.FloorHeading  = 0.0;//re-set the floor heading.
			for(int j = 0; j<getFP_DB().size(); j++) {
				// the same floor only
				if(i!=j && getFP_DB().get(j).rs.floor_level == getFP_DB().get(i).rs.floor_level) {
					dN = getFP_DB().get(j).rs.North - getFP_DB().get(i).rs.North;
					dE = getFP_DB().get(j).rs.East - getFP_DB().get(i).rs.East;
					if((dN*dN + dE*dE) < 25.0) { //distance less than 5 meters.
						numNBRS++;
						if (numNBRS == 1) {
							hd1 = Math.atan2(dE, dN)*RtoD;
						} else if (numNBRS == 2) {
							hd2 = Math.atan2(dE, dN)*RtoD;
						}else {
							break;
						}						
					}
				}	
			}
	
			if(numNBRS == 2) {
				if(hd1 < 0) hd1 += 360;
				if(hd2 < 0)	hd2 += 360;
				double diff = (hd2>hd1)?hd2-hd1:hd1-hd2;
				if(Math.abs(diff-180) < 30) {
					getFP_DB().get(i).rs.FloorHeading  = hd1;
				}
			}
		}
		return numRS;
	}

	// LMY: return ArrayList of WiFi fingerprints.
	public ArrayList<WiFiFingerPrint> getFP_DB() {
		return FP_DB;
	}

	public void setFP_DB(ArrayList<WiFiFingerPrint> fP_DB) {
		FP_DB = fP_DB;
	}
	
	public void addToDatabase(String str) {
	    // Get end-of-line separator for system (On Linux systems this is always \n, but it is better to check...
	  	String eol = System.getProperty("line.separator");
	   	Writer writer = null;
	  	// Append result to file, or print exception if fail.
      	try {
      		writer = new PrintWriter(new BufferedWriter(new FileWriter(fileFP, true)));
      		writer.write(str + eol);
      		writer.close();
      	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public void OpenDBFile() {
		File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Indoor/WiFi");
		if(!fileDir.exists()) {
			fileDir.mkdirs();
		}
		fileFP = new File(fileDir, "WLAN_FPDB.txt");
		fileFP.setWritable(true);
		if(!fileFP.exists()) {
			try{
				fileFP.createNewFile();
			}catch (Exception ex) {
				// print stack trace.
				ex.printStackTrace();
			}
		}
	}
}