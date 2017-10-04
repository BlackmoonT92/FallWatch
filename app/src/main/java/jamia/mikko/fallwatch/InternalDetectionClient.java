package jamia.mikko.fallwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

import android.util.Log;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by jamiamikko on 21/09/2017.
 */

public class InternalDetectionClient implements Runnable, SensorEventListener {

    private Handler handler;
    private SensorManager sm;
    private Sensor accelaration;
    private long lastTime = 0;
    private float lastX, lastY, lastZ;
    private static final int THRESHOLD = 200;
    private Context context;
    private SharedPreferences prefs;
    public static final String USER_PREFERENCES = "UserPreferences";
    private GoogleApiClientHelper clientHelper;

    public InternalDetectionClient(SensorManager sensorManager, Handler handler, Context context) {
        this.sm = sensorManager;
        this.accelaration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.handler = handler;
        this.context = context;
        //this.clientHelper = new GoogleApiClientHelper(context);
    }

    @Override
    public void run() {

        try {

            Thread.sleep(500);
            sm.registerListener(this, accelaration, SensorManager.SENSOR_DELAY_NORMAL);

            //prefs = context.getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);

            /*Thread.sleep(500);
            clientHelper.connect();

            Thread.sleep(500);
            clientHelper.requestPermissions();*/

        } catch (Exception e) {
            Log.i("Error", e.toString());
        }

    }

    public void stop() {
        sm.unregisterListener(this);
        //clientHelper.disconnect();
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

                    ArrayList<String> messages = new ArrayList<String>();
                    Message msg = handler.obtainMessage();

                    msg.what = 0;

                    //messages[0] = prefs.getString("contact1", null);
                    //messages[1] = prefs.getString("username", null);
                    //messages[2] = clientHelper.getLocation();
                    messages.add("0445092182");
                    messages.add("peraroori");
                    messages.add("62.00,24.00");

                    msg.obj = messages;

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
