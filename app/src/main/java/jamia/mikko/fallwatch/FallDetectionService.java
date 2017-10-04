package jamia.mikko.fallwatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by jamiamikko on 27/09/2017.
 */


public class FallDetectionService extends Service {

    private Thread thread;
    private InternalDetectionClient internalDetectionClient;
    private SensorManager sensorManager;
    public static boolean IS_SERVICE_RUNNING = false;
    private SmsManager smsManager = SmsManager.getDefault();
    private LocationManager locationManager;
    private String location;

    public FallDetectionService() {
    }

    private Handler messageHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                location = String.valueOf(msg.obj);
                showAlert();
                Intent alert = new Intent(Constants.ACTION.MESSAGE_RECEIVED).putExtra("alert", "oh noes");
                alert.putExtra("location", location);
                getApplicationContext().sendBroadcast(alert);
                stopSelf();
                stopForeground(true);
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

        Context context = getApplicationContext();

        internalDetectionClient = new InternalDetectionClient(sensorManager, messageHandler, context);
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

        Intent stopIntent = new Intent(this, FallDetectionService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle("Fallwatch")
                .setSmallIcon(R.drawable.ic_menu_settings)
                .setContentText("Detecting")
                .addAction(R.drawable.ic_menu_home, "Stop", pstopIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    private void showAlert() {

        Intent notificationIntent = new Intent(this, MainSidebarActivity.class);
        notificationIntent.setAction(Constants.ACTION.ALERT_ACTION);

        Intent stopIntent = new Intent(this, FallDetectionService.class);
        stopIntent.setAction(Constants.ACTION.STOP_ALERT_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        String message = "You have fallen down, please confirm that are you okay.";

        Notification alert = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle("Fallwatch")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentText(message)
                .addAction(0, "I'm okay", pstopIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(1, alert);
    }

    public void sendSMS(String number, String username, String location) {

        String uri = "http://google.com/maps/place/" + location;
        smsManager.getDefault();
        StringBuffer smsBody = new StringBuffer();
        smsBody.append(Uri.parse(uri));
        smsManager.sendTextMessage(number, null, username + " needs help " + smsBody.toString(), null, null);
    }
}
