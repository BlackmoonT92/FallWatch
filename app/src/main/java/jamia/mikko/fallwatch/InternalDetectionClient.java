package jamia.mikko.fallwatch;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by jamiamikko on 21/09/2017.
 */

public class InternalDetectionClient implements Runnable, SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private Handler handler;
    private SensorManager sm;
    private Sensor accelaration;
    private long lastTime = 0;
    private float lastX, lastY, lastZ;
    private static final int THRESHOLD = 300;
    private GoogleApiClient googleApiClient;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private Location mLastLocation;
    private Context context;

    public InternalDetectionClient(SensorManager sensorManager, Handler handler, LocationManager locationManager, Context context) {
        this.sm = sensorManager;
        this.accelaration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.handler = handler;

        this.locationManager = locationManager;
        this.context = context;

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void run() {

        try {

            Thread.sleep(500);
            sm.registerListener(this, accelaration, SensorManager.SENSOR_DELAY_NORMAL);

            googleApiClient.connect();

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }

        } catch (Exception e) {
            Log.i("Error", e.toString());
        }

    }

    public void stop() {
        sm.unregisterListener(this);
        stopLocationUpdates();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastTime) > 250) {
                long timeDifference = currentTime - lastTime;
                lastTime = currentTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDifference * 10000;

                if (speed > THRESHOLD) {
                    Message msg = handler.obtainMessage();
                    msg.what = 0;

                    msg.obj = getLocation();

                    handler.sendMessage(msg);
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        enableLocationRequest();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    private void enableLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    //All location settings are satisfied
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    //Not all location settings are satisfied
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Show dialog user a dialog to enable location
                        PendingIntent pI = status.getResolution();
                        googleApiClient.getContext().startActivity(new Intent(googleApiClient.getContext(), MainSidebarActivity.class)
                                .putExtra("resolution", pI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private String getLocation(){

        String lat = Double.toString(mLastLocation.getLatitude());
        String lng = Double.toString(mLastLocation.getLongitude());

        String location = lat + "," + lng;

        return location;

    }
}
