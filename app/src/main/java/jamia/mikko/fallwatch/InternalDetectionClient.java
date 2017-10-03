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

public class InternalDetectionClient implements Runnable, SensorEventListener {

    private Handler handler;
    private SensorManager sm;
    private Sensor accelaration;
    private long lastTime = 0;
    private float lastX, lastY, lastZ;
    private static final int THRESHOLD = 300;
    private Context context;
    private GoogleApiClientHelper clientHelper;

    public InternalDetectionClient(SensorManager sensorManager, Handler handler, Context context) {
        this.sm = sensorManager;
        this.accelaration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.handler = handler;
        this.context = context;
        this.clientHelper = new GoogleApiClientHelper(context);
    }

    @Override
    public void run() {

        try {

            Thread.sleep(500);
            sm.registerListener(this, accelaration, SensorManager.SENSOR_DELAY_NORMAL);

            Thread.sleep(500);
            clientHelper.connect();

            Thread.sleep(500);
            clientHelper.requestPermissions();

        } catch (Exception e) {
            Log.i("Error", e.toString());
        }

    }

    public void stop() {
        sm.unregisterListener(this);
        clientHelper.disconnect();
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

                    //msg.obj = getLocation();

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

}
