package cn.edu.whu.indoorscene.wifi;

import java.util.ArrayList;

public class WiFiFingerPrint {
	public ArrayList<String> list_SSID = new ArrayList<String>(); //List of AP Mac address
	public ArrayList<Double> list_RSSI = new ArrayList<Double>(); //List of RSSI
	public ArrayList<Double> list_Variance = new ArrayList<Double>(); //List of variance
	public ArrayList<Integer> list_SampleCount = new ArrayList<Integer>(); //List of sample accounts when create DB
	public ReferencePoint rs = new ReferencePoint();

	public WiFiFingerPrint() {

	}

	public WiFiFingerPrint(ReferencePoint a, ArrayList<String> ssid,
			ArrayList<Double> rssi, ArrayList<Double> var, ArrayList<Integer> count) {
		rs = a;
		list_SSID.addAll(ssid);
		list_RSSI.addAll(rssi);
		list_Variance.addAll(var);
		list_SampleCount.addAll(count);
	}
}