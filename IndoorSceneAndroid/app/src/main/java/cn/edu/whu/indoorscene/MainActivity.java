package cn.edu.whu.indoorscene;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.google.android.material.navigation.NavigationView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import cn.edu.whu.indoorscene.Fragment.MagneticFragment;
import cn.edu.whu.indoorscene.Fragment.MapFragment;
import cn.edu.whu.indoorscene.Fragment.SceneCaptureFragment;
import cn.edu.whu.indoorscene.Fragment.SceneChooseDialogFragment;
import cn.edu.whu.indoorscene.sensors.Accelerometer;
import cn.edu.whu.indoorscene.wifi.WiFiScan;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SceneChooseDialogFragment.sceneChooseListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public TextView tvSceneTag;
    private FrameLayout frameSmall, frameBig;

    private FragmentManager fragmentManager;

    // Sensors
    public Accelerometer mAcc = null;
//    public Magnetometer mMagnetometer = null;
    public WiFiScan mWiFi = null;
    public boolean mIsSensorsLaunched;
    public static Context context;

    public static String mainSceneValue;
    public static String sceneLabel;

    // Acquire system permission.
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions (Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        // LMY: Initialize View
        setContentView(R.layout.activity_main);
        setTitle("Indoor Scene");
        frameSmall = (FrameLayout) findViewById(R.id.small_window_id);
        frameBig = (FrameLayout) findViewById(R.id.big_window_id);
        tvSceneTag = (TextView)findViewById(R.id.scene_tag_id);
        tvSceneTag.setVisibility(View.INVISIBLE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // LMY: Verify permission
        verifyStoragePermissions(this);

        // LMY: Default Fragment
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.small_window_id, SceneCaptureFragment.newInstance())
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.big_window_id, MapFragment.newInstance())
                .commit();

        // LMY: Initialize Sensors
        if (mWiFi == null) {
            mWiFi = new WiFiScan();
            mWiFi.Init(this);
        }
        setSensorStatus(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnoffSensors();
        if (mWiFi != null) {
            mWiFi.onPause();
            mWiFi = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            String dialogTitle = "Choose a main scene: ";
            DialogFragment sceneChooseDialog = SceneChooseDialogFragment.newInstance(loadMainScene(), dialogTitle, "Main");
            sceneChooseDialog.show(getFragmentManager(), "sceneChooseDialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            if(frameSmall.getVisibility() == View.INVISIBLE) {
                frameSmall.setVisibility(View.VISIBLE);
                fragmentManager.beginTransaction()
                        .replace(R.id.big_window_id, MapFragment.newInstance())
                        .commit();
                fragmentManager.beginTransaction()
                        .replace(R.id.small_window_id, SceneCaptureFragment.newInstance())
                        .commit();
            }
        } else if (id == R.id.nav_camera) {
            // Handle the camera action
            frameSmall.setVisibility(View.INVISIBLE);
            fragmentManager.beginTransaction()
                    .replace(R.id.big_window_id, SceneCaptureFragment.newInstance())
                    .commit();
        } else if (id == R.id.nav_map) {
            frameSmall.setVisibility(View.INVISIBLE);
            fragmentManager.beginTransaction()
                    .replace(R.id.big_window_id, MapFragment.newInstance())
                    .commit();

        } else if (id == R.id.nav_magnetic) {
            frameSmall.setVisibility(View.INVISIBLE);
            fragmentManager.beginTransaction()
                    .add(R.id.big_window_id, MagneticFragment.newInstance())
                    .commit();
        } else if (id == R.id.nav_record) {
            if(frameSmall.getVisibility() == View.INVISIBLE) {
                frameSmall.setVisibility(View.VISIBLE);
                fragmentManager.beginTransaction()
                        .replace(R.id.big_window_id, MapFragment.newInstance())
                        .commit();
                fragmentManager.beginTransaction()
                        .replace(R.id.small_window_id, SceneCaptureFragment.newInstance())
                        .commit();
            }
            fragmentManager.beginTransaction()
                    .add(R.id.content_main, MagneticFragment.newInstance())
                    .commit();
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSceneChooseListener(String currentScene, String sceneType) {
        if (currentScene!=null) {
            tvSceneTag.setText(currentScene);
            if(sceneType.equals("Main")) {
                mainSceneValue = currentScene;
            }
            sceneLabel = currentScene;
        }
    }

    public void turnoffSensors() {
        if (mAcc != null) {
            mAcc.onPause();
        }
    }

    public boolean sensorsSwitch() {
        boolean isSensorsOn;
        // Sensor
        if (mAcc == null) {
            mAcc = new Accelerometer(this);
        } else {
            mAcc.onPause();
            mAcc = null;
        }
//        if (mMagnetometer == null) {
//            mMagnetometer = new Magnetometer(this);
//        } else {
//            mMagnetometer.onPause();
//            mMagnetometer = null;
//        }
        if (mAcc != null) {
            isSensorsOn = true;
        } else {
            isSensorsOn = false;
        }

//		if(isSensorsOn) {
//			PDR.onResume(mWiFi, mBeacon, mAcc.sensorDataAcc, mCompass.sensorDataOri, mGyro.sensorDataGyro, mLoc.posData,mMagnetometer.sensorDataMagnet);
//		} else {
//			PDR.onPause();
//		}
        return isSensorsOn;
    }

    public synchronized void setSensorStatus(boolean status) {           //true is launched. false is turned off
        mIsSensorsLaunched = status;
    }

    public synchronized boolean getSensorStatus() {
        return mIsSensorsLaunched;
    }

    // below two functions are for settings fragment to switch on/off toggle buttons !!
    public void onTogglePosModeClicked(View view) {
        ToggleButton toggle = (ToggleButton) view.findViewById(R.id.togglePosMode);
        boolean pm = toggle.isChecked();
        if(mWiFi != null) mWiFi.ChangePosMode(pm);
        if(pm){
            // LMY-2017.01.14: When positioning mode is on, disable the Scene Choosing.
            tvSceneTag.setVisibility(View.INVISIBLE);
        } else {
            tvSceneTag.setVisibility(View.VISIBLE);
        }
    }

    public void OnToggleScanClicked(View view) {
        ToggleButton toggle = (ToggleButton) view.findViewById(R.id.toggleScan);
        boolean sm = toggle.isChecked();
        if(mWiFi != null) mWiFi.ChangeScanMode(sm);
    }

    public ArrayList<String> loadMainScene() {
        ArrayList<String> fileNameList = new ArrayList<>();
        try {
            String[] fileNames = getAssets().list("scenes");
            for (int i = 0; i < fileNames.length; i++) {
                if (!fileNameList.contains(fileNames[i])) {
                    fileNameList.add(fileNames[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNameList;
    }

    public ArrayList<String> loadSubScene(String currentSceneValue){
        ArrayList<String> fileNameList = loadMainScene();
        ArrayList<String> sceneList = new ArrayList<>();
        try {
            if(fileNameList.contains(currentSceneValue)) {
                InputStream is = getAssets().open("scenes/" + currentSceneValue);
                InputStreamReader reader = new InputStreamReader(is, "utf-8");
                BufferedReader bufferedReader = new BufferedReader(reader);
                String label;
                while((label = bufferedReader.readLine())!=null) {
                    if (!sceneList.contains(label)) {
                        sceneList.add(label);
                    }
                }
            } else {
                sceneList.add("No sub scene");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sceneList;
    }
}
