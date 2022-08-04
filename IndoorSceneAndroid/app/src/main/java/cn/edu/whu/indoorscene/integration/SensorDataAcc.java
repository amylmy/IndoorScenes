package cn.edu.whu.indoorscene.integration;

import java.io.File;

import cn.edu.whu.indoorscene.MainActivity;
import cn.edu.whu.indoorscene.util.FileIO;


public class SensorDataAcc {
	//Data Acc
	public double mCurrentMeanAcc = 0.0; //Mean of a window-size
	public double mAccGravity = 9.42;
	//double mAccGravity =9.46;
	public double mCurrentAcc = 0.0; //Mean of a window-size
	public int mAccWindowSize = 30;
	public double [] mTotalAcc = new double[mAccWindowSize];
	public double[][] mEnuAcc = new double [3][1];
	public float[] mLinearAcceleration = new float[3];
	//Step parameters
	public long mLastStepTimeStamp = 0;
	public double mStepLength = 0.0;
	public double mStepFreq = 0.0;
	public boolean bPeakHit = false;
	
	// computing user orientation 
	public float[] mDcm_g = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	public float[] mOrientation_g = {0, 0, 0};
	
	// acc direct reading
	public float mAccX;
	public float mAccY;
	public float mAccZ;
	public long mTimeStamp;
	public MainActivity mAct;
	public double mCurSpeed = 0;
	public long accPeakTime = 0;
	//public int ccc=0;

	public static File fileDataAcc = null;
	public static StringBuilder DataAccStreamLog = null;
	public static int DataAccBuffLen = 100;
	public static int DataAccCount = DataAccBuffLen;
	
	public SensorDataAcc() {
		mAccX = (float) 0.0;
		mAccY = (float) 0.0;
		mAccZ = (float) 0.0;
		mTimeStamp = 0;
	}
	
	public SensorDataAcc(float ax, float ay, float az, long ts){
		mAccX = ax;
		mAccY = ay;
		mAccZ = az;
		mTimeStamp = ts;
	}

	public String ToStr() {
		String str = new String("Acc: ");
		str += String.valueOf(mAccX) + "   " + String.valueOf(mAccY) + "   " + String.valueOf(mAccZ);
		return str;
	}
	
	public float getHorizontalAcc(float ax, float ay, float az) { // input parameter is the vector components of the gravity (at static)
		float hA = (float) 0.0;
		float fc = 0;
		fc = (ax * mAccX + ay * mAccY + az * mAccZ) / (ax * ax + ay * ay + az * az);
		ax = mAccX - fc * ax;
		ay = mAccY - fc * ay;
		az = mAccZ - fc * az;
		hA = (float) Math.sqrt(ax * ax + ay * ay + az * az);
		return hA;
	}

	public void MagnitudeAcc(float x, float y, float z) {
		// TODO Auto-generated method stub
		// compute mean acc prior to getting correct speed info
		double last = mTotalAcc[0]; //save it.
		mCurrentAcc = Math.sqrt(x * x + y * y + z * z) - mAccGravity;
		for (int i = 1; i < mAccWindowSize; i++) {
			mTotalAcc[i-1] = mTotalAcc[i]; //shift the data forward - remove from head
		}
		mTotalAcc[mAccWindowSize - 1] = mCurrentAcc; //add tail
		mCurrentMeanAcc += (mCurrentAcc - last) / mAccWindowSize;


	}

	public double UpdateMotionState(double spd, long accTs) {
		// TODO Auto-generated method stub
		int tmp = (int)(5 * mCurrentMeanAcc);
		long dt = 0;
		double oldSpeed = spd;
		double curSpd = getSpeed();
		if(tmp > 8 ) tmp = 8; //Maximum level
		if(tmp < 1 ) tmp = 1;
 		switch (tmp) {
			case 1:
				dt = accTs - mLastStepTimeStamp;
				if(bPeakHit) {
					mStepFreq = 1.0e+9 / dt; //time stamp in milliseconds.
					if(mStepFreq < 1.0) mStepFreq = 1.0; //minimum step frequency = 1.0
					if(mStepFreq > 2.5) mStepFreq = 2.5; //maximum step frequency = 2.5
					mStepLength = 0.7 + 0.227 * (mStepFreq - 1.8); //Model derived from Chen (2011). Assuming pedestrian height = 1.75m.
					curSpd = mStepFreq * mStepLength;
					mLastStepTimeStamp = accTs;
					curSpd = (oldSpeed + curSpd) / 2; //Take the average of two speeds.
					bPeakHit = false; //reset the peak detection
					accPeakTime = accTs;
				} else {
					if(dt > 1.0e+9) curSpd = 0.0; //if the user stay static for longer 1s, speed set to zero.
				}
			break;
			case 2: 
			break;
			case 3: 
			break;
			case 4: bPeakHit = true;
			break;
			case 5: bPeakHit = true;
			break;
			case 6: bPeakHit = true;
			break;
			case 7: bPeakHit = true;
			break;
			case 8: bPeakHit = true;
			break;
			default:
			break;	  		
		}

		tmp = (int)(4 * curSpd);
		if(tmp < 1) tmp = 1;
		if(tmp > 8) tmp = 8;
		
		setSpeed(curSpd);
		return curSpd;
	}


	public void LogDataAcc(float x, float y, float z, long ts)
	{
		if(fileDataAcc==null)
		{
			fileDataAcc = new File(FileIO.newDir("UNav"), "RawDataAcc.log");

			fileDataAcc.setReadable(true);
			StringBuilder DataAccStreamLog = new StringBuilder();
			DataAccStreamLog.setLength(0);

			DataAccCount = DataAccBuffLen;
		}

		DataAccCount --;

		DataAccStreamLog.append(ts + ";  ");
		DataAccStreamLog.append(x + ";  ");
		DataAccStreamLog.append(y + ";  ");
		DataAccStreamLog.append(z + ";  ");
		DataAccStreamLog.append(mCurrentAcc + "\r\n ");


		if (DataAccCount <= 0){
			FileIO.writeFileToStorage(DataAccStreamLog.toString(), fileDataAcc);
			DataAccCount = DataAccBuffLen;
			DataAccStreamLog.setLength(0);
		}


	}

	public synchronized void updateAcc(float x, float y, float z, long ts) {
		// TODO Auto-generated method stub
		mAccX = x;
		mAccY = y;
		mAccZ = z;
		mTimeStamp = ts;
		MagnitudeAcc(x, y, z);

//		LogDataAcc(x, y, z, ts);

		/*ccc+=1;
		if (ccc==100){
			String str1="\n"+String.valueOf(ts)+","+String.valueOf(PDR.getCurrentPos().latitude)+","+String.valueOf(PDR.getCurrentPos().longitude)
					+","+String.valueOf(getSpeed())+"\n";
			writeData(str1);		
			ccc=0;
		}
		
		String str = String.valueOf(ts)+","+String.valueOf(getAccX())+","+String.valueOf(getAccY())+","+String.valueOf(getAccZ())
				+","+String.valueOf(mAct.mGyro.sensorDataGyro.getGyroX())+","+String.valueOf(mAct.mGyro.sensorDataGyro.getGyroY())+","+String.valueOf(mAct.mGyro.sensorDataGyro.getGyroZ())
				+","+String.valueOf(mAct.mMagnetometer.sensorDataMagnet.getMagX())+","+String.valueOf(mAct.mMagnetometer.sensorDataMagnet.getMagY())+","+String.valueOf(mAct.mMagnetometer.sensorDataMagnet.getMagZ())
				+","+String.valueOf(mAct.mCompass.sensorDataOri.getOriPitch())+","+String.valueOf(mAct.mCompass.sensorDataOri.getOriAz())+","+String.valueOf(mAct.mCompass.sensorDataOri.getOriRoll())
				+","+String.valueOf(mAct.mBarometer.sensorDataBarometer.getBarometer())+","+String.valueOf(mAct.mLight.sensorDataLight.getLight())+"\n";
		writeData(str);*/
	}
	
	public synchronized float getAccX() {
		return mAccX;
	}
	
	public synchronized float getAccY() {
		return mAccY;
	}
	
	public synchronized float getAccZ() {
		return mAccZ;
	}
	
	public synchronized float getAcc() {
		return (float) Math.sqrt(getAccX() * getAccX() + getAccY() * getAccY() + getAccZ() * getAccZ());
	}
	
	public synchronized long getAccTimeStamp() {
		return mTimeStamp;
	}
	
	public synchronized double getSpeed() {
		return mCurSpeed;
	}
	
	public synchronized void setSpeed(double sp) {
		mCurSpeed = sp;
	}
	
	/*public synchronized void writeData(String temp) {
		int ii = temp.length();
		byte[] byBuffer = new byte[ii];
		byBuffer = temp.getBytes();
		try {
			mAct.fos.write(byBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}