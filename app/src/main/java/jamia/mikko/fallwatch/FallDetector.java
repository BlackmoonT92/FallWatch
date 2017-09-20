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

    public FallDetector(SensorManager sensorManager, MainSidebarActivity activity) {
        this.sm = sensorManager;
        this.gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        this.activity = activity;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float force = sensorEvent.values[0];

        Log.i("Gravity", String.valueOf(force));
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
