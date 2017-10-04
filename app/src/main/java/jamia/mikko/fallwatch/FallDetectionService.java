package jamia.mikko.fallwatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;

import java.util.ArrayList;

/**
 * Created by jamiamikko on 27/09/2017.
 */


public class FallDetectionService extends Service {

    private Thread thread;
    private InternalDetectionClient internalDetectionClient;
    private ExternalDetectionClient externalDetectionClient;
    private SensorManager sensorManager;
    public static boolean IS_SERVICE_RUNNING = false;

    public static boolean IS_RUNNING_EXTERNAL = false;
    public static boolean IS_RUNNING_INTERNAL = false;

    private SmsManager smsManager = SmsManager.getDefault();
    private String location, user, contact1;
    private BluetoothManager bluetoothManager;

    public FallDetectionService() {
    }

    private Handler messageHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                ArrayList<String> data;
                data = (ArrayList<String>) msg.obj;
                contact1 = data.get(0);
                user = data.get(1);
                location = data.get(2);
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

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        internalDetectionClient = new InternalDetectionClient(sensorManager, messageHandler, context);
        externalDetectionClient = new ExternalDetectionClient(context, bluetoothManager, messageHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(IS_RUNNING_EXTERNAL) {

            thread = new Thread(externalDetectionClient);

        } else {

            thread = new Thread(internalDetectionClient);

        }

        thread.start();
        showNotification();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        thread = null;
        internalDetectionClient.stop();
        externalDetectionClient.stop();
    }

    private void showNotification() {

        Intent notificationIntent = new Intent(this, MainSidebarActivity.class);

        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);

        Intent stopIntent = new Intent(this, FallDetectionService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle("Fallwatch")
                .setSmallIcon(R.drawable.ic_menu_settings)
                .setContentText("Detecting");

        Intent resultIntent = new Intent(this, MainSidebarActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    }

    private void showAlert() {

        String message = "You have fallen down, please confirm that you are okay or send an alert.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle("Fallwatch")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentText(message)
                .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainSidebarActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        builder.setContentIntent(pendingIntent);

        Intent yesReceive = new Intent(this, AlertReceiver.class);
        yesReceive.setAction(Constants.ACTION.YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action YES_ACTION = new NotificationCompat.Action(0, "I'm okay", pendingIntentYes);
        builder.addAction(YES_ACTION);

        Intent alertReceive = new Intent(this, AlertReceiver.class).putExtra("alertReceive", "alerting");
        alertReceive.putExtra("userName", user);
        alertReceive.putExtra("number", contact1);
        alertReceive.putExtra("location", location);
        alertReceive.setAction(Constants.ACTION.ALERT_ACTION);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(this, 12345, alertReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action ALERT_ACTION = new NotificationCompat.Action(0, "Alert!", pendingIntentNo);
        builder.addAction(ALERT_ACTION);

        NotificationManager notifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    }

    public void sendSMS(String number, String username, String location) {

        String uri = "http://google.com/maps/place/" + location;
        smsManager.getDefault();
        StringBuffer smsBody = new StringBuffer();
        smsBody.append(Uri.parse(uri));
        smsManager.sendTextMessage(number, null, username + " needs help " + smsBody.toString(), null, null);
    }
}
