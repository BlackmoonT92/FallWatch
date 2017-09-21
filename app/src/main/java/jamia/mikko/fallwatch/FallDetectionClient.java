package jamia.mikko.fallwatch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;


/**
 * Created by jamiamikko on 21/09/2017.
 */

public class FallDetectionClient implements Runnable, SensorEventListener  {

    private Handler handler;
    private SensorManager sm;
    private Sensor gravity;
    private long lastTime = 0;
    private long currentTime;
    private SensorEventListener eventListener;
    private int eventFrequency;
    private double gravityThreshold;
    private double forceDifference;
    private double gravityBase;

    public FallDetectionClient(SensorManager sensorManager, Handler handler) {
        this.sm = sensorManager;
        this.gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        this.handler = handler;
    }

    @Override
    public void run() {

        try {

            Thread.sleep(1000);
            sm.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);


        } catch (Exception e) {
            Log.i("Error", e.toString());
        }

    }

    public void stop() {
        sm.unregisterListener(this);
    }


    public void resetValues() {
        this.eventFrequency = 500;
        this.gravityThreshold = 2.0;
        this.gravityBase = 0.0;
        this.forceDifference = 0.0;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();

        if(sensorType == Sensor.TYPE_GRAVITY) {

            currentTime = System.currentTimeMillis();


            if((currentTime - lastTime) > eventFrequency) {
                lastTime = currentTime;

                double force = sensorEvent.values[0];

                if(force > 0.0) {

                forceDifference = (gravityBase - force);

                if((forceDifference * -1) > gravityThreshold) {

                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = "You have fallen!!";
                    handler.sendMessage(msg);
                }

            }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
