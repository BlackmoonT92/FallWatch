package jamia.mikko.fallwatch;

import android.content.Context;
import android.content.SharedPreferences;
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

    public InternalDetectionClient(SensorManager sensorManager, Handler handler, Context context) {
        this.sm = sensorManager;
        this.accelaration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.handler = handler;
        this.context = context;
        prefs = context.getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
    }

    @Override
    public void run() {

        try {

            Thread.sleep(500);
            if (sensorExists()) {
                sm.registerListener(this, accelaration, SensorManager.SENSOR_DELAY_NORMAL);
            }

        } catch (Exception e) {
            Log.i("Error", e.toString());
        }

    }

    public void stop() {
        sm.unregisterListener(this);
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

                    ArrayList<String> messages = new ArrayList<>();
                    Message msg = handler.obtainMessage();

                    messages.add(prefs.getString("contact1", null));
                    messages.add(prefs.getString("username", null));
                    messages.add(ApplicationClass.getGoogleApiHelper().getLocation());

                    msg.what = 0;

                    msg.obj = messages;

                    //msg.obj =
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

    public Boolean sensorExists() {
        if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            return true;
        } else {
            return false;
        }
    }

}
