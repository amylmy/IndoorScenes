package cn.edu.whu.indoorscene.Fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import cn.edu.whu.indoorscene.Data.Scene;
import cn.edu.whu.indoorscene.MainActivity;
import cn.edu.whu.indoorscene.R;
import cn.edu.whu.indoorscene.wifi.WiFiScan;

public class MapFragment extends Fragment
        implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, View.OnClickListener,
        OnMapReadyCallback, SensorEventListener, Runnable {
    // LMY-2017.01.14: When LOCATION is 0, the initial map will be LIESMARS Lab. Otherwise, it'll be Creative City.
    public static int LOCATION = 0;

    private GoogleMap mMap;
    private GroundOverlay mGroundOverlay;
    //创意城2007坐标
    private LatLngBounds newarkBoundsCyc = new LatLngBounds(new LatLng(30.5262000000, 114.3554600000), // South west corner
            new LatLng(30.5268680000, 114.3564919755));
    private LatLng CYCLAB = new LatLng(30.5265340000, 114.35597598775);

//    //实验室1楼坐标
//    private LatLngBounds newarkBoundsLab = new LatLngBounds(new LatLng(30.5267664747, 114.3597250432), // South west corner
//            new LatLng(30.5276634963, 114.3606530875));
//    private LatLng LMARSLAB = new LatLng(30.5270830036, 114.3604458869);

    //实验室大图坐标
    private LatLngBounds newarkBoundsLab = new LatLngBounds(new LatLng(30.5267810000, 114.3602860000), // South west corner
            new LatLng(30.52730000, 114.3606580000));
    private LatLng LMARSLAB = new LatLng(30.52709000, 114.360472000);
    private Marker positionMarker;

    private int[] floor_id = {R.drawable.lmars_f2, R.drawable.lmars_f3, R.drawable.lmars_f4};
    private int[] cycFloorId = {R.drawable.cyc20_office};

    private static View view;

    private ImageButton upButton;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton downButton;
    private ImageButton recordButton;
    private ImageButton sceneRecognitionBtn;
    private ImageButton chooseLabelBtn;
    private ImageButton currentPositionButton;
    private ImageButton upstairsButton;
    private ImageButton downstairsButton;
    private static TextView[] results = new TextView[5];

//    private LatLng currentLatLng;
    private String currentScene;

    private List<ScanResult> wifiList;
    private List<String> wifiMacs;
    private List<LatLng> positions;
    private List<Marker> markers;

    private Marker marker;

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final String TAG = "BeaconList";
    private Location mCurrLocation = new Location("dummyprovider");
//    private static final Region ALL_BEACONS_REGION = new Region("customRegionName", null, null, null);
//    private static final Region TEST_ALL_BEACONS_REGION = new Region("customRegionName", null, 2, null);

    private WifiManager wifiManager = null;
    private MainActivity mainActivity;
    private boolean bStartRecord = false;
    private boolean bStartSingleRecord = false;

//    private Timer mTimer;
    private int recordCount = 0;
//    private OnFragmentInteractionListener mListener;
    private Timer timer = null;
    private TimerTask timerTask = null;
    // LMY-2017.01.14: Old handler, maybe need to revise.
    private Handler handler = null;

    private boolean bPos = false;

    private double brng = 0; //current bearing
    public static WiFiScan mWFS = null;
    public static LatLng mCurrentWifiPos = new LatLng(0.0, 0.0);
    public static String mCurrentScene = null;

    private int floorNum;
    private int updateMapCnt = 0;

    private static ArrayList<Scene> top5Scenes = new ArrayList<>(5);

    // number of recorded reference points.
    int num = 0;

    // state of showing Reference points in the map.
    boolean isShownRP = false;


//    private int[] arrows_marker = new int[]{R.drawable.marker_s, R.drawable.marker_s, R.drawable.marker_s};

    private int mHeadingIndex;

    public MapFragment() {
        // Required empty public constructor
    }

    public static Handler wifiHandler = new Handler() { // Handle for pdr.
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) { // message sent when WifiScan done. // LMY: sent from positioning mode.
                if (mWFS == null) return;
                if (mWFS.getCurrentRS().latlng.latitude != 0.0 && mWFS.getCurrentRS().latlng.longitude != 0.0) {
                    mCurrentWifiPos = mWFS.getCurrentRS().latlng;
                }
            } else if (msg.what == 1) {
                // LMY-2017.01.14: This is used to receive response from web.
                JSONObject jsonResult = (JSONObject) msg.obj;
                try {
                    JSONArray labels = jsonResult.getJSONArray("labels");
                    JSONArray scores = jsonResult.getJSONArray("scores");
                    top5Scenes.clear();
                    top5Scenes = showAndSaveRecognitionResults(labels, scores);
                    mCurrentScene = top5Scenes.get(0).getSceneLabel();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    public static ArrayList<Scene> showAndSaveRecognitionResults(JSONArray labels, JSONArray scores) {
        ArrayList<Scene> recResults = new ArrayList<>();
        String recognitionResult;
        for(int i = 0; i<labels.length(); i++) {
            try {
                recognitionResult = labels.getString(i) + ", " + scores.getString(i);
                results[i].setText(recognitionResult);

                Scene scene = new Scene(labels.getString(i), scores.getDouble(i));
                if (!recResults.contains(scene)) {
                    recResults.add(scene);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return recResults;
    }


    //    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment MapFragment.
//     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        handler.post(this);
        mCurrLocation.reset();
        setFloorLevel(1);
        mainActivity = (MainActivity) getActivity();
        mWFS = mainActivity.mWiFi;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_map, container, false);
//        SupportMapFragment mapFragment =
//                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//        return view;

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
            init(view);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        upButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        recordButton.setOnClickListener(this);
        upstairsButton.setOnClickListener(this);
        downstairsButton.setOnClickListener(this);
        chooseLabelBtn.setOnClickListener(this);
        sceneRecognitionBtn.setOnClickListener(this);
        currentPositionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upButton: {
                if (marker != null) {
                    LatLng latLng = marker.getPosition();
                    marker.setPosition(new LatLng(latLng.latitude + 0.000001, latLng.longitude));
                    mCurrLocation.setLatitude(marker.getPosition().latitude);
                    mCurrLocation.setLongitude(marker.getPosition().longitude);
                    mainActivity.mWiFi.setTouchPoint(latLng);
//                    currentLatLng = marker.getPosition();
                }
                break;
            }
            case R.id.downButton: {
                if (marker != null) {
                    LatLng latLng = marker.getPosition();
                    marker.setPosition(new LatLng(latLng.latitude - 0.000001, latLng.longitude));
                    mCurrLocation.setLatitude(marker.getPosition().latitude);
                    mCurrLocation.setLongitude(marker.getPosition().longitude);
                    mainActivity.mWiFi.setTouchPoint(latLng);
//                    currentLatLng = marker.getPosition();
                }
                break;
            }
            case R.id.leftButton: {
                if (marker != null) {
                    LatLng latLng = marker.getPosition();
                    marker.setPosition(new LatLng(latLng.latitude, latLng.longitude - 0.000001));
                    mCurrLocation.setLatitude(marker.getPosition().latitude);
                    mCurrLocation.setLongitude(marker.getPosition().longitude);
                    mainActivity.mWiFi.setTouchPoint(latLng);
//                    currentLatLng = marker.getPosition();
                }
                break;
            }
            case R.id.rightButton: {
                if (marker != null) {
                    LatLng latLng = marker.getPosition();
                    marker.setPosition(new LatLng(latLng.latitude, latLng.longitude + 0.000001));
                    mCurrLocation.setLatitude(marker.getPosition().latitude);
                    mCurrLocation.setLongitude(marker.getPosition().longitude);
                    mainActivity.mWiFi.setTouchPoint(latLng);
//                    currentLatLng = marker.getPosition();
                }
                break;
            }
            case R.id.recordButton: {
                Toast.makeText(getActivity(), "Record a location", Toast.LENGTH_SHORT).show();
                num++;
                savePoints(mCurrLocation, num);
                break;
            }
            case R.id.upstairs: {
                mGroundOverlay = updateFloorPlan(getFloorLevel(), 1, true);
                Toast.makeText(MainActivity.context, floor_id[getFloorLevel()-1], Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.downstairs: {
                mGroundOverlay = updateFloorPlan(getFloorLevel(), 2, true);
                Toast.makeText(MainActivity.context, floor_id[getFloorLevel()-1], Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.chooseLabelBtn: {
                String dialogTitle = "choose a sub-scene: ";
                String mainScene = MainActivity.mainSceneValue;
                DialogFragment subSceneChooseDialog = SceneChooseDialogFragment
                        .newInstance(mainActivity.loadSubScene(mainScene), dialogTitle, "Sub");
                subSceneChooseDialog.show(getFragmentManager(), "subSceneChooseDialog");
                break;
            }
            case R.id.scene_recognition_btn: {
                //TODO: LMY-2017.01.14: Capture a picture and upload to web server.
//                Toast.makeText(getActivity(), "Scene Recognition button test~", Toast.LENGTH_SHORT).show();
                isShownRP = showPoints(isShownRP);
                break;
            }
            case R.id.currentPositionBtn: {
                if (LOCATION==0) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LMARSLAB, 19));
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CYCLAB, 19));
                }

                break;
            }
        }
    }

    private void init(View view) {
        wifiMacs = new ArrayList<>();
        positions = new ArrayList<>();
        markers = new ArrayList<>();

        upButton = (ImageButton) view.findViewById(R.id.upButton);
        downButton = (ImageButton) view.findViewById(R.id.downButton);
        leftButton = (ImageButton) view.findViewById(R.id.leftButton);
        rightButton = (ImageButton) view.findViewById(R.id.rightButton);
        recordButton = (ImageButton) view.findViewById(R.id.recordButton);

        upstairsButton = (ImageButton) view.findViewById(R.id.upstairs);
        downstairsButton = (ImageButton) view.findViewById(R.id.downstairs);
        chooseLabelBtn = (ImageButton) view.findViewById(R.id.chooseLabelBtn);
        sceneRecognitionBtn = (ImageButton) view.findViewById(R.id.scene_recognition_btn);
        currentPositionButton = (ImageButton) view.findViewById(R.id.currentPositionBtn);

        int[] ids = {R.id.result1, R.id.result2, R.id.result3, R.id.result4, R.id.result5};
        for (int i = 0; i < 5; i++) {
            results[i] = (TextView) view.findViewById(ids[i]);
        }

//        mMap.setIndoorEnabled(true);
//        mMap.setBuildingsEnabled(false);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LMARSLAB, 19));

        // add NRC floor plan layer
//        mGroundOverlay = updateFloorPlan(getFloorLevel(), 0, true);
//
//        if (marker == null) {
//            marker = mMap.addMarker(new MarkerOptions()
//                    .position(LMARSLAB)
//                    .title("")
//                    .snippet("")
//                    .icon(BitmapDescriptorFactory
//                            .fromResource(R.drawable.marker)));
//            marker.setVisible(false);
//        }

        //below callback function is used for changing marker size when zoom in or zoom out map, as well as update marker orientation when we rotate the map
//        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//            MainActivity act = (MainActivity) getActivity();
//            @Override
//            public void onCameraChange(CameraPosition pos) {
//                if (!act.mWiFi.getScanMode()) {    // not scanned yet, this is for clicking map for selecting the correct point
//                    if (marker != null) {
//                        int markerId = 0;
//                        if (pos.zoom >= 18.5 && pos.zoom < 20.5) {
//                            markerId = 1;
//                        } else if (pos.zoom < 18.5) {
//                            markerId = 2;
//                        }
//                        marker.setIcon(BitmapDescriptorFactory
//                                .fromResource(arrows_marker[markerId])); // set position icon
//                    }
//                }
//            }
//        });
        com.google.android.gms.maps.MapFragment mapFragment =
                (com.google.android.gms.maps.MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                TAMUCC, 19));
        if (LOCATION == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LMARSLAB, 19));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    CYCLAB, 19));
        }
        AddOffice();
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    private void AddOffice() {
        mMap.setBuildingsEnabled(false);
        //创意城2007地图
//        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
//                .image(BitmapDescriptorFactory
//                        .fromResource(R.drawable.cyc2007))
//                .positionFromBounds(newarkBounds);
        //实验室1楼地图
//        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
//                .image(BitmapDescriptorFactory
//                        .fromResource(R.drawable.sys2l))
//                .positionFromBounds(newarkBounds);
        if (LOCATION == 0) {
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.lmars_f2))
                .positionFromBounds(newarkBoundsLab);
            mGroundOverlay = mMap.addGroundOverlay(newarkMap);
        }
        else {
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.cyc20_office))
                    .positionFromBounds(newarkBoundsCyc);
            mGroundOverlay = mMap.addGroundOverlay(newarkMap);
        }
        mGroundOverlay.setVisible(true);
    }

    @Override
    public void onMapClick(LatLng latLng) {
       // AddMarker(latLng);
//        MainActivity act = (MainActivity) getActivity();
        if (null != mainActivity.mWiFi) {
            if (!mainActivity.mWiFi.getScanMode()) {    // when wifi NOT scan, may perform finger printing training
                Toast.makeText(mainActivity, "Lat:" + Double.toString(latLng.latitude) + "\nLon:" + Double.toString(latLng.longitude), Toast.LENGTH_SHORT).show();
                mainActivity.mWiFi.setTouchPoint(latLng);
//                LMARSLAB = latLng;
                if (marker != null) {
                    marker.remove();
                }
                // check the current zoom in level and select the corresponding marker
                float zoomLevel = mMap.getCameraPosition().zoom;
                int markerId = 0;
                if (zoomLevel >= 18.5 && zoomLevel < 20.5) {
                    markerId = 1;
                } else if (zoomLevel < 18.5) {
                    markerId = 2;
                }
                marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("")
                        .snippet("")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_round)));
//                marker = mMap.addMarker(new MarkerOptions()
//                        .position(LMARSLAB)
//                        .title("")
//                        .snippet("")
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_round)));
//                                .fromResource(arrows_marker[markerId])));
                marker.setVisible(!mainActivity.mWiFi.getPosMode());

//                currentLatLng = latLng;

                mCurrLocation.setLatitude(latLng.latitude);
                mCurrLocation.setLongitude(latLng.longitude);
            } else {
//                        if (act.cntxEng.sl.getDrawPolygonStatus() && !act.getSensorStatus()) {    //in this case (wifi still scan and polygon checkbox is enabled and sensor not launched), perform polygon drawing
//                            act.cntxEng.sl.points.add(new LatLng(point.latitude, point.longitude));
//                            act.cntxEng.sl.polyEdgeNumber += 1;
//                            act.cntxEng.sl.setFloorLevel(getFloorLevel());     //transfer this floor number value to Significant Location class
//
//                            if (polygon != null) {
//                                polygon.remove();     //clear the old polygon in case the draw is not finished
//                            }
//
//                            // Get back the mutable Polygon
//                            PolygonOptions polyOptions = new PolygonOptions().addAll(act.cntxEng.sl.points);    //this is for drawing polygon on map
//                            polygon = mMap.addPolygon(polyOptions);
//                            polygon.setStrokeWidth(4);
//                            polygon.setStrokeColor(Color.BLUE);
//                        }
            }
        }
    }

    private void AddMarker(LatLng latLng) {
//        if (marker != null) {
//            marker.remove();
//        }
        Marker m = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("")
                .snippet("")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red)));
        markers.add(m);
//        marker.setVisible(true);
//        currentLatLng = latLng;
    }

    public void updateMapView() {
        MainActivity act = ((MainActivity) getActivity());
        if (!act.mWiFi.getPosMode()) {
            return;
        }

        if (act.getSensorStatus()) {
            //this is to test if the user is indoors
//            Location gps = act.mLoc.posData.getGpsLocation();
//            double gpsAcc = gps.getAccuracy();
//
//            setIndoorStatus(IndoorOutdoorDetection(gpsAcc));
//            if (!getIndoorStatus()) {
//                setFloorLevel(1);
//            }

            LatLng ll = mWFS.getCurrentRS().latlng;
//            LatLng ll = mCurrentWifiPos;
            mCurrLocation.setLatitude(ll.latitude);
            mCurrLocation.setLongitude(ll.longitude);

            if (mCurrLocation.getLatitude() != 0.0
                    && mCurrLocation.getLongitude() != 0.0) {
                //heading index for location marker
                double mapBearing = 0;    //map rotation orientation
                mapBearing = mMap.getCameraPosition().bearing;
                mHeadingIndex = getHeading((float) (Math.toDegrees(brng) - mapBearing));

                float zoomLevel = mMap.getCameraPosition().zoom;
                SetMarker(zoomLevel);
                mGroundOverlay = updateFloorPlan(act.mWiFi.getFloorLevel(), 3, true);

//                write positioning info to file
//                LogData(ll, getIndoorStatus());


            } else {
                if (marker != null) marker.setVisible(false);
            }
        }
    }

    private void SetMarker(float zoom) {
        // TODO Auto-generated method stub
        if (marker != null) {
            marker.setIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.marker_round)); // set position icon

//            if (zoom >= 20.5) {
//                marker.setIcon(BitmapDescriptorFactory
//                        .fromResource(arrows_m[mHeadingIndex])); // set position icon
//            } else if (zoom < 20.5 && zoom >= 18.5) {
//                marker.setIcon(BitmapDescriptorFactory
//                        .fromResource(arrows_m[mHeadingIndex])); // set position icon
//            } else {
//                marker.setIcon(BitmapDescriptorFactory
//                        .fromResource(arrows_s[mHeadingIndex])); // set position icon
//            }

            marker.setPosition(new LatLng(mCurrLocation.getLatitude(),
                    mCurrLocation.getLongitude())); // move the icon of the
            // current location.
            marker.setVisible(true);
        }
    }

    // this decides which arrow icon to be used for current location
    public int getHeading(float heading) {
        if (heading < 0) heading += 360;
        int index = Math.round(heading / 360 * 16);
        while (index > 15) {
            index -= 16;
        }

        while (index < 0) {
            index += 16;
        }

        return index;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    //磁场更新
    @Override
    public void onSensorChanged(SensorEvent event) {
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiList = wifiManager.getScanResults();
        if (wifiList == null) {
            Toast.makeText(getActivity(), "wifi未打开！", Toast.LENGTH_LONG).show();
        }

//        textWifi.setText("wifi数:" + String.valueOf(wifiList.size()));

        if (bStartRecord || bStartSingleRecord || bPos) {
//            magneticValues.add(magValue);
        }

//        if (bStartRecord) {
//            if (recordCount == RECORD_COUNT) {
//                bStartRecord = false;
//                recordCount = 0;
//                Toast.makeText(getActivity(), "训练数据记录结束", Toast.LENGTH_SHORT).show();
//            } else {
//                List<ScanResult> cloneWifiList = new ArrayList<ScanResult>();
//                cloneWifiList.addAll(wifiList);
//                List<Beacon> cloneBeaconList = new ArrayList<Beacon>();
//                cloneBeaconList.addAll(beaconList);
//                MagneticValue cloneMagValue = new MagneticValue(magValue.getXMean(), magValue.getYMean(), magValue.getZMean());
//                recordData(cloneWifiList, cloneBeaconList, cloneMagValue, wifiMacs, beaconMacs, currentLatLng, positions);
//                recordCount++;
//            }
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wifiHandler.removeCallbacksAndMessages(this);
        handler.removeCallbacks(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void run() {
        handler.postDelayed(this, 100);
        MainActivity act = (MainActivity) getActivity();
//        act.cntxEng.sl.setFloorLevel(getFloorLevel());     //transfer this floor number value to Significant Location class

        if (act.getSensorStatus()) {
            updateMapCnt += 1;
            long accTs = act.mAcc.sensorDataAcc.getAccTimeStamp();
//            act.mGyro.sensorDataGyro.speed =
//                    act.mAcc.sensorDataAcc.UpdateMotionState(act.mGyro.sensorDataGyro.speed, accTs);
            if (updateMapCnt == 10) {    // every 1 second, update user position, map view and significant activity
                act.mWiFi.checkWiFiState();
                updateMapView();

                //here is to update significant activity textview
//                if (act.cntxEng.getActID() >= 0) {
//                    c = Calendar.getInstance();
//                    String str = String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(c.get(Calendar.MINUTE)) + ":"
//                            + String.valueOf(c.get(Calendar.SECOND));
//
//                    actText.setText(str + "\n"
//                            + "Current Activity: " + act.cntxEng.cp.actID2string(act.cntxEng.getActID()) + "\n"
//                            //+ String.valueOf(act.cntxEng.oseq.get(act.cntxEng.oseq.size() - 1)) + "\n"
//                            //+ Arrays.toString(act.cntxEng.getStateSequence()) + "\n"
//                            + "Current Location: " + act.cntxEng.cp.locID2string(act.cntxEng.getLocID()) + "\n"
//                            + "Predicted Activity: " + act.cntxEng.cp.actID2string(act.cntxEng.getPredActID()));
//                    actText.setVisibility(View.VISIBLE);
//                } else {
//                    actText.setVisibility(View.GONE);
//                }

                updateMapCnt = 0;
            }
        } else {
            if (marker != null && act.mWiFi.getPosMode()) marker.setVisible(false);
        }

    }
//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

    private GroundOverlay updateFloorPlan(int fNum, int indx, boolean isIndoor) {
        boolean bUpdate = true;

        switch (indx) {
            case 0: // floor layer initialization
                if (mGroundOverlay != null) {
                    mGroundOverlay.remove();
                }
                break;

            case 1: // going upstairs
                mGroundOverlay.remove();
                setFloorLevel(fNum + 1);
                if (getFloorLevel() > 3) {
                    setFloorLevel(3);
                }
                mMap.setBuildingsEnabled(false);
                break;

            case 2: // going downstairs
                mGroundOverlay.remove();
                setFloorLevel(fNum - 1);
                if (getFloorLevel() < 1) {
                    setFloorLevel(1);
                }
                mMap.setBuildingsEnabled(false);
                break;

            case 3: // this is wifi positioning mode
                if (getFloorLevel() != fNum && fNum > 0) {
                    mGroundOverlay.remove();
                    setFloorLevel(fNum);
                } else {
                    bUpdate = false;
                }
                break;
        }

        if (bUpdate) {
            mMap.setBuildingsEnabled(false);
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(floor_id[getFloorLevel() - 1]))
                    .positionFromBounds(newarkBoundsLab);
            mGroundOverlay = mMap.addGroundOverlay(newarkMap);
            if (indx == 0) {
                mGroundOverlay.setVisible(false);
                mMap.setBuildingsEnabled(true);
            } else {
                mGroundOverlay.setVisible(true);
            }
        }

        if (!isIndoor) {
            mMap.setBuildingsEnabled(true);
            mGroundOverlay.setVisible(false);
        }

        if (isIndoor && indx != 0) {
            mMap.setBuildingsEnabled(false);
            mGroundOverlay.setVisible(true);
        }

        ((MainActivity) getActivity()).mWiFi.setFloorLevel(getFloorLevel());
        return mGroundOverlay;
    }

    public synchronized void setFloorLevel(int num) {
        floorNum = num;
    }

    public synchronized int getFloorLevel() {
        return floorNum;
    }

    public ArrayList<Scene> getSceneRecResults() {
        // LMY-2017.01.18 Need to check if the results are newest.
        return top5Scenes;
    }


    private void savePoints(Location mCurrLocation, int num) {
        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Indoor/WiFi");
        if (!root.exists()) {
            root.mkdirs();
        }
        File testPointFile = new File(root, "RP_locations.txt");
        String eol = System.getProperty("line.separator");
        String str = "@" + num + ";" + mCurrLocation.getLatitude() + ";" + mCurrLocation.getLongitude() + eol ;
        Writer writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(testPointFile, true)));
            writer.write(str);
            writer.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public boolean showPoints (boolean hasShown) {
        if (hasShown) {
            for (int i = 0; i < markers.size(); i++) {
                markers.get(i).remove();
            }
            markers.clear();
            return false;
        } else {
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Indoor/WiFi");
            File file = new File(root, "RP_locations.txt");
            ArrayList<LatLng> RPLocations = new ArrayList<>();
            if (file.exists()) {
                file.setWritable(true);
                try {
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() == 0) {
                            break;
                        } else {
                            StringTokenizer st = new StringTokenizer(line, ";");
                            if (line.charAt(0) == '@') {
                                String id = st.nextToken();
                                String lat = st.nextToken();
                                String lng = st.nextToken();

                                id = id.trim();
                                id = id.substring(1, id.length());  // remove the @

                                LatLng location = new LatLng(Double.parseDouble(lat.trim()), Double.parseDouble(lng.trim()));
                                AddMarker(location);
                                if (!RPLocations.contains(location)) {
                                    RPLocations.add(location);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(mainActivity, "file not exits, record some points first~", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

    }

}
