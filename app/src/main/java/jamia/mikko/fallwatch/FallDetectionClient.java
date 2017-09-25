package jamia.mikko.fallwatch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * Created by jamiamikko on 21/09/2017.
 */

public class FallDetectionClient implements Runnable, SensorEventListener  {

    private Handler handler;
    private SensorManager sm;
    private Sensor accelaration;
    private long lastTime = 0;
    private float lastX, lastY, lastZ;
    private static final int THRESHOLD = 500;

    public FallDetectionClient(SensorManager sensorManager, Handler handler) {
        this.sm = sensorManager;
        this.accelaration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.handler = handler;
    }

    @Override
    public void run() {

        try {

            Thread.sleep(500);
            sm.registerListener(this, accelaration, SensorManager.SENSOR_DELAY_NORMAL);

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

        if(sensorType == Sensor.TYPE_ACCELEROMETER) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long currentTime = System.currentTimeMillis();

            if((currentTime - lastTime) > 250) {
                long timeDifference = currentTime - lastTime;
                lastTime = currentTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDifference * 10000;

                if (speed > THRESHOLD) {
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = "You have fallen!!";

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
