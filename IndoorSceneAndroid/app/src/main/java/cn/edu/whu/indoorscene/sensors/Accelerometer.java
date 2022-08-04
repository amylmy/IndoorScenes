package cn.edu.whu.indoorscene.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import cn.edu.whu.indoorscene.MainActivity;
import cn.edu.whu.indoorscene.integration.SensorDataAcc;

public class Accelerometer implements SensorEventListener {
	private SensorManager sensorManager;
	private MainActivity mAct;
	public SensorDataAcc sensorDataAcc = new SensorDataAcc();

	public Accelerometer(MainActivity act) {
		sensorManager = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);
		mAct = act;
		sensorDataAcc.mAct = act;
		onResume(); // Start the sensor
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			sensorDataAcc.updateAcc(event.values[0], event.values[1], event.values[2], event.timestamp);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onResume() {
		// register this class as a listener for the orientation and
		// accelerometer sensors
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void onPause() {
		// unregister listener
		sensorManager.unregisterListener(this);
	}
}