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
    private long lastUpdate = 0;

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


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();

        if(sensorType == Sensor.TYPE_GRAVITY) {
            double force = sensorEvent.values[0];
            long currentTime = System.currentTimeMillis();

            double startValue = 0.0;
            double gravityThreshold = 2.0;
            int eventFrequency = 500;

            if((currentTime - lastUpdate) > eventFrequency) {
                lastUpdate = currentTime;
                double currentValue = force * -1.0;
                double valueDifference = startValue - currentValue;

                if(valueDifference > gravityThreshold && currentValue < startValue) {

                    Log.i("Gravity", "You have fallen");
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    handler.sendMessage(msg);

                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
