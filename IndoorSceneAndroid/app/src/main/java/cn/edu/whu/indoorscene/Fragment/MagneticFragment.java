package cn.edu.whu.indoorscene.Fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cn.edu.whu.indoorscene.R;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Mengyun Liu on 2017/2/15.
 *
 */

public class MagneticFragment extends Fragment implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] magValue = {0.0f, 0.0f, 0.0f};
    private long timeStamp;

    // TextView  to show the value of magnetic
    private TextView magnetic_tv, magSum_tv;
    private Button recordStartBtn, recordEndBtn;
//    private OnFragmentInteractionListener mListener;

    public MagneticFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MagneticFragment.
     */
    public static MagneticFragment newInstance() {
        return new MagneticFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        if (sensorManager == null) {
            sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        }
        if (sensor == null) {
            sensor= sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_magnetic, container, false);
        magnetic_tv = (TextView) view.findViewById(R.id.magnetic_tv);
        magSum_tv = (TextView) view.findViewById(R.id.magnetic_sum_tv);
        recordStartBtn = (Button) view.findViewById(R.id.record_start_btn);
        recordEndBtn = (Button) view.findViewById(R.id.record_end_btn);

        recordStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recordEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(this, sensor);
        super.onPause();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        magValue[0] = event.values[0];
        magValue[1] = event.values[1];
        magValue[2] = event.values[2];
        timeStamp = event.timestamp;
        double magSum = Math.sqrt(magValue[0] * magValue[0] + magValue[1] * magValue[1] + magValue[2] * magValue[2]);
        String str = "(" + magValue[0] + "," + magValue[1] + "," + magValue[2] + ")" ;
        magnetic_tv.setText(str);
        magSum_tv.setText("磁场强度：" + magSum);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

}
