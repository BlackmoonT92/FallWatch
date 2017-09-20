package jamia.mikko.fallwatch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by jamiamikko on 20/09/2017.
 */

public class FallDetector implements SensorEventListener {

    private SensorManager sm;
    private Sensor gravity;
    private MainSidebarActivity activity;
    private long lastUpdate = 0;

    public FallDetector(SensorManager sensorManager, MainSidebarActivity activity) {
        this.sm = sensorManager;
        this.gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        this.activity = activity;
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

                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected void onStart() {
        sm.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        sm.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onStop() {
        sm.unregisterListener(this);
    }
}
