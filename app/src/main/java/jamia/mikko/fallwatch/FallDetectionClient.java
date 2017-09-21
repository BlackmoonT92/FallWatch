package jamia.mikko.fallwatch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.logging.Handler;

/**
 * Created by jamiamikko on 21/09/2017.
 */

public class FallDetectionClient implements Runnable  {

    private Handler handler;
    private SensorManager sm;
    private Sensor gravity;
    private long lastUpdate = 0;
    private SensorEventListener eventListener;

    public FallDetectionClient(SensorManager sensorManager) {
        this.sm = sensorManager;
        this.gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    public void run() {

        try {

            eventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    int sensorType = sensorEvent.sensor.getType();

                    if(sensorType == Sensor.TYPE_GRAVITY) {
                        double force = sensorEvent.values[0];
                        long currentTime = System.currentTimeMillis();

                        double startValue = 0.0;
                        double gravityThreshold = 2.0;
                        int eventFrequency = 300;

                        if((currentTime - lastUpdate) > eventFrequency) {
                            lastUpdate = currentTime;
                            double currentValue = force * -1.0;
                            double valueDifference = startValue - currentValue;

                            if(valueDifference > gravityThreshold && currentValue < startValue) {

                                Log.i("Gravity", "You have fallen");
                            }
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };

            sm.registerListener(eventListener, gravity, SensorManager.SENSOR_DELAY_NORMAL);

        } catch (Exception e) {
            Log.i("Error", e.toString());
        }

    }

    public void stop() {
        sm.unregisterListener(eventListener);
    }
}
