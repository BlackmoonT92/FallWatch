package jamia.mikko.fallwatch;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by jamiamikko on 27/09/2017.
 */


public class FallDetectionService extends Service {

    private Thread thread;
    private InternalDetectionClient internalDetectionClient;
    private SensorManager sensorManager;
    public static boolean IS_SERVICE_RUNNING = false;


    private Handler messageHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Log.i("Detector", "You have fallen");

                Intent alert = new Intent(Constants.ACTION.MESSAGE_RECEIVED).putExtra("alert", "oh noes");
                getApplicationContext().sendBroadcast(alert);

            }
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        internalDetectionClient = new InternalDetectionClient(sensorManager, messageHandler);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        thread = new Thread(internalDetectionClient);
        thread.start();
        showNotification();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        thread = null;
        internalDetectionClient.stop();
    }

    private void showNotification() {

        Intent notificationIntent = new Intent(this, MainSidebarActivity.class);

        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent stopIntent = new Intent(this, FallDetectionService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Fallwatch")
                .setSmallIcon(R.drawable.ic_menu_settings)
                .setContentText("Detecting")
                .addAction(R.drawable.ic_menu_home, "Stop", pstopIntent)
                .build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
    }

}
