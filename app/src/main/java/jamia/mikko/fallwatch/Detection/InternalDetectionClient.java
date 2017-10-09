package jamia.mikko.fallwatch.Detection;

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

    private static final String USER_PREFERENCES = "UserPreferences";
    private static final int THRESHOLD = 800;
    private Handler handler;
    private SensorManager sm;
    private Sensor accelaration;
    private long lastTime = 0;
    private float lastX, lastY, lastZ;
    private SharedPreferences prefs;

    public InternalDetectionClient(SensorManager sensorManager, Handler handler, Context context) {

        //Initialize
        this.sm = sensorManager;
        this.accelaration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.handler = handler;
        prefs = context.getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
    }

    @Override
    public void run() {

        try {

            Thread.sleep(500);

            //If sensor exists, register sensor event listener.
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

            //Get values from sensor
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            //Get current time of the system
            long currentTime = System.currentTimeMillis();

            //Compare currentTime to lastTime. If gap is larger that 250 ms, analyze the data.
            if ((currentTime - lastTime) > 250) {
                long timeDifference = currentTime - lastTime;
                lastTime = currentTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDifference * 10000;

                //If speed is larger than threshold, send message to service.
                if (speed > THRESHOLD) {

                    //Build message array.
                    ArrayList<String> messages = new ArrayList<>();
                    Message msg = handler.obtainMessage();

                    messages.add(prefs.getString("contact1", null));
                    messages.add(prefs.getString("contact2", null));
                    messages.add(prefs.getString("username", null));
                    messages.add(ApplicationClass.getGoogleApiHelper().getLocation());

                    msg.what = 0;
                    msg.obj = messages;

                    //Send message to service
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
