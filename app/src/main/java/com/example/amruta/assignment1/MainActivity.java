package com.example.amruta.assignment1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;




public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    static Double sensorLatitude, sensorLongitude;
    int level,numberGPS=0,count=0;
    static long GPSTime;
    public static Float currentAcceleration = 0.0F;
    public static Float currentDirection = 0.0F;
    public static Float CurrentSpeed = 0.0F;
    public static Float distanceTravelled = 0.0F;
    private Object sync = new Object();
    Dialog alertDialog;
    SensorManager sensorManager;
    Sensor accelerometer, rotation, lightsensor;
    float[] gravity = new float[3];
    float[] velocity = new float[3];
    private float timestamp;
    float[] magVal = null;
    EditText latitude,longitude,latacc,longacc,dist;
    double gravityX = 0.0, gravityY = 0.0, gravityZ = 0.0;
    Handler sensorHandler = new Handler();
    static Double radius = 6378D;
    static Double oldLat, oldLong;
    static Boolean IsFirst = true,night=true;
    private static String TAG="Assignment1";

    float[] prevValues;
    List<Float> X=new ArrayList<Float>();
    List<Float> Y=new ArrayList<Float>();
    List<Float> Z=new ArrayList<Float>();

    float prevTime, currentTime, changeTime, distanceX, distanceY, distanceZ;
    WifiManager wifi;
    Float lightLevel=0f;
    double distanceValue;

    LocationManager locationManager;
    public static Float priorAcceleration = 0.0F;
    public static Float priorSpeed = 0.0F;
    public static Float priorDistance = 0.0F;

    Handler locationHandler=new Handler();
    Boolean First, initSensor = true;
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    int mSignalStrength = 0;
    Double lat1, long1;
    boolean flag=false, lightFlag=false,isGPSEnabled=false,isNetworkEnabled=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(getApplicationContext(), "Launched Successfully!", Toast.LENGTH_LONG).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        First = initSensor = true;
        prevValues = new float[3];
        latitude=(EditText)findViewById(R.id.editText);
        longitude=(EditText)findViewById(R.id.editText2);
        latacc=(EditText)findViewById(R.id.editText3);
        longacc=(EditText)findViewById(R.id.editText4);
        dist=(EditText)findViewById(R.id.editText5);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        wifi = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        lightsensor=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this,lightsensor,SensorManager.SENSOR_DELAY_FASTEST);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        isGPSEnabled =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    @Override
    protected void onResume() {
        super.onResume();

        int levels = 5;
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), levels);
        Log.i(TAG,"LEVEL"+level);
        if(!wifi.isWifiEnabled()){
            Log.i(TAG,"onCreate : WIFI DISABLED");
        }
        else{

            if (level<-90){
                //outdoors
                synchronized (sync) {
                    numberGPS += 1;
                    if (count > 0) {
                        count--;
                    }
                }

            }
            else{
                synchronized (sync) {
                    count++;
                    if (numberGPS > 0) {
                        numberGPS--;
                    }
                }
            }
        }
    }
    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            flag=true;
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength.getGsmSignalStrength();
            mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm

            if (mSignalStrength>-90){
                synchronized (sync) {
                    numberGPS += 1;
                    if (count > 0) {
                        count--;
                    }
                }

            }
            else{
                synchronized (sync) {
                    if (count <= 5) {
                        count += 1;
                    } else if (numberGPS > 0) {
                        numberGPS--;
                    }
                }

            }
        }
    }

    public void calculateAccuracy(double latitude1, double long1, double lat2, double lng2) {
        distanceValue = 0;
        double dLat = Math.toRadians(lat2 - latitude1);
        double dLon = Math.toRadians(lng2 - long1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        distanceValue = Math.round(radius * c*1000);
        dist.setText(new Double(distanceValue).toString()+" m");
    }

    public class Method1 implements Runnable{

        @Override
        public void run() {
            latitude.setText(lat1.toString());
            longitude.setText(long1.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //TAKING VALUES FROM GPS/NETWORK FOR THE FIRST TIME
        if(First) {
            oldLat = location.getLatitude();
            oldLong = location.getLongitude();
            GPSTime = location.getTime();
            sensorManager.registerListener(this,rotation,SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        }
        lat1=location.getLatitude();
        long1=location.getLongitude();
        locationHandler.post(new Method1());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        alertDialog.dismiss();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(), "Location services off. Turn on Location", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Not Active");
        builder.setMessage("Please enable Location Services");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    public class Method2 implements Runnable{

        @Override
        public void run() {
            gravityX=(float)0.98*gravityX+(float)0.02*gravity[0];
            gravityY=(float)0.98*gravityY+(float)0.02*gravity[1];
            gravityZ=(float)0.98*gravityZ+(float)0.02*gravity[2];
            gravity[0]-=gravityX;
            gravity[1]-=gravityY;
            gravity[2]-=gravityZ;

            //this is the time when the data is taken from the gps/network provider
            if(First){
                prevValues = gravity;
                X.add(gravity[0]);
                Y.add(gravity[1]);
                Z.add(gravity[2]);
                prevTime = timestamp / 1000000000;
                First = false;
                distanceX = distanceY= distanceZ = 0;
            }
            else{

                currentTime = timestamp / 1000000000.0f;
                changeTime = currentTime - prevTime;
                prevTime = currentTime;
                X.add(gravity[0]);
                Y.add(gravity[1]);
                Z.add(gravity[2]);
                    //average values are calculated
                if(X.size()>49){
                    float avgravityX=0,avgravityY=0,avgravityZ=0;
                    for (int i=0;i<50;i++){
                        avgravityX+=X.get(i);
                        avgravityY+=Y.get(i);
                        avgravityZ+=Z.get(i);
                    }
                    X.clear();
                    Y.clear();
                    Z.clear();
                    gravity[0]=avgravityX/50;
                    gravity[1]=avgravityY/50;
                    gravity[2]=avgravityZ/50;

                    calculateDistance(gravity, changeTime);
                    currentAcceleration =  (float) Math.sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2]);
                    CurrentSpeed = (float) Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1] + velocity[2] * velocity[2]);
                    distanceTravelled = (float) Math.sqrt(distanceX *  distanceX + distanceY * distanceY +  distanceZ * distanceZ);
                    distanceTravelled = distanceTravelled / 1000;
                }


                if(initSensor){
                    priorAcceleration = currentAcceleration;
                    priorDistance = distanceTravelled;
                    priorSpeed = CurrentSpeed;
                    initSensor = false;
                }
                prevValues = gravity;

            }
            if(currentAcceleration != priorAcceleration || CurrentSpeed != priorSpeed || priorDistance != distanceTravelled){

                if (gravity != null && magVal != null && currentAcceleration != null) {
                    float RT[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(RT, I, gravity,
                            magVal);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(RT, orientation);
                        float azimut = (float) Math.round(Math.toDegrees(orientation[0]));
                        currentDirection =(azimut+ 360) % 360;
                        calculatePosition(distanceTravelled,currentDirection);
                    }
                    priorAcceleration = currentAcceleration;
                    priorSpeed = CurrentSpeed;
                    priorDistance = distanceTravelled;
                }
            }
        }
        public void calculateDistance(float[] values,float dT){
            float[] distance = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                velocity[i] = values[i] * dT;
                distance[i] = velocity[i] * dT + values[i] * dT * dT / 2;
            }
            distanceX = distance[0];
            distanceY = distance[1];
            distanceZ = distance[2];
        }

        public void calculatePosition(Float distanceTravelled, Float currentDirection){
            Log.i(TAG,"calculatePosition");

            //gps/network provider is used for the first time.
            if(IsFirst){
                sensorLatitude = oldLat;
                sensorLongitude = oldLong;
                IsFirst  = false;
                return;
            }

            Date CurrentTime = new Date();

            if(CurrentTime.getTime() - GPSTime > 0) {
                //Convert Variables to Radian for the Formula
                oldLat = Math.PI * oldLat / 180;
                oldLong = Math.PI * oldLong / 180;
                currentDirection = (float) (Math.PI * currentDirection / 180.0);

                //Formula to Calculate the NewLAtitude and NewLongtiude
                Double newLatitude = Math.asin(Math.sin(oldLat) * Math.cos(distanceTravelled / radius) +
                        Math.cos(oldLat) * Math.sin(distanceTravelled / radius) * Math.cos(currentDirection));
                Double newLongitude = oldLong + Math.atan2(Math.sin(currentDirection) * Math.sin(distanceTravelled / radius)
                        * Math.cos(oldLat), Math.cos(distanceTravelled / radius)
                        - Math.sin(oldLat) * Math.sin(newLatitude));
                //Convert Back from radians
                newLatitude = 180 * newLatitude / Math.PI;
                newLongitude = 180 * newLongitude / Math.PI;
                currentDirection = (float) (180 * currentDirection / Math.PI);
                latacc.setText(newLatitude.toString());
                longacc.setText(newLongitude.toString());

                //Update old Latitude and Longitude
                oldLat = newLatitude;
                oldLong = newLongitude;

                sensorLatitude = oldLat;
                sensorLongitude = oldLong;
                calculateAccuracy(lat1, long1, sensorLatitude, sensorLongitude);

            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            gravity=event.values;
            timestamp=event.timestamp;
            sensorHandler.post(new Method2());
        }
        //using light sensors
        if(event.sensor.getType()==Sensor.TYPE_LIGHT){
            lightFlag=true;
            lightLevel=event.values[0];
            //activate light sensor
            if(lightLevel>3000){
                //this means it is daylight and we are outdoor so gps should be enabled
                numberGPS+=1;
                if (count > 0) {
                    count--;
                }
            }
            else if (lightLevel<5 && night){
                //outdoor
                synchronized (sync) {
                    numberGPS += 1;
                    if (count > 0) {
                        count--;
                    }
                }

            }
            else{
                synchronized (sync) {
                    count += 1;
                    if (numberGPS > 0) {
                        numberGPS--;
                    }
                }

            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if (numberGPS > count) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                //Toast.makeText(getApplicationContext(), "GPS Selected", Toast.LENGTH_LONG).show();
                Log.i(TAG, "gps selected");
                numberGPS = 0;
                count = 0;
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                // Toast.makeText(getApplicationContext(), "Network Selected", Toast.LENGTH_LONG).show();
                Log.i(TAG, "nw selected");
                numberGPS = 0;
                count = 0;
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magVal = event.values;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

}
