package jamia.mikko.fallwatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;

import java.util.ArrayList;

import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;

/**
 * Created by jamiamikko on 27/09/2017.
 */


public class FallDetectionService extends Service {

    public static boolean IS_SERVICE_RUNNING = false;
    public static boolean IS_RUNNING_EXTERNAL = false;
    public static boolean IS_RUNNING_INTERNAL = false;
    public static AlarmTimer timer;
    private Thread thread;
    private InternalDetectionClient internalDetectionClient;
    private ExternalDetectionClient externalDetectionClient;
    private SensorManager sensorManager;
    private SmsManager smsManager = SmsManager.getDefault();
    private String location, user, contact1, contact2;
    private BluetoothManager bluetoothManager;
    private NotificationManager notifyMgr;
    private Handler messageHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                timer.start();
                ArrayList<String> data;
                data = (ArrayList<String>) msg.obj;
                contact1 = data.get(0);
                contact2 = data.get(1);
                user = data.get(2);
                location = data.get(3);
                showAlert();
                Intent alert = new Intent(Constants.ACTION.MESSAGE_RECEIVED).putExtra("alert", "oh noes");
                alert.putExtra("location", location);
                getApplicationContext().sendBroadcast(alert);
                stopSelf();
                stopForeground(true);
            }
        }
    };

    public FallDetectionService() {
    }

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
        notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        timer = new AlarmTimer(60000, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (IS_RUNNING_EXTERNAL) {

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

    public void destroyNotificationsFromUi(Context context) {
        String notificationService = Context.NOTIFICATION_SERVICE;
        NotificationManager manager = (NotificationManager) context.getSystemService(notificationService);
        manager.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);
    }

    private void showNotification() {

        Intent notificationIntent = new Intent(this, MainSidebarActivity.class);

        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);

        Intent stopIntent = new Intent(this, FallDetectionService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_falling)
                .setContentText(getString(R.string.detecting));

        Intent resultIntent = new Intent(this, MainSidebarActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);


        notifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    }

    private void showAlert() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.fallen)))
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_falling)
                .setContentText(getString(R.string.fallen))
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainSidebarActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        builder.setContentIntent(pendingIntent);

        Intent yesReceive = new Intent(this, AlertReceiver.class);
        yesReceive.setAction(Constants.ACTION.YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action YES_ACTION = new NotificationCompat.Action(0, getString(R.string.imOkay), pendingIntentYes);
        builder.addAction(YES_ACTION);

        Intent alertReceive = new Intent(this, AlertReceiver.class).putExtra("alertReceive", "alerting");
        alertReceive.putExtra("userName", user);
        alertReceive.putExtra("number1", contact1);
        alertReceive.putExtra("number2", contact2);
        alertReceive.putExtra("location", location);
        alertReceive.setAction(Constants.ACTION.ALERT_ACTION);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(this, 12345, alertReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action ALERT_ACTION = new NotificationCompat.Action(0, getString(R.string.sendAlert), pendingIntentNo);
        builder.addAction(ALERT_ACTION);

        notifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    }

    private void alertSentNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("FallWatch")
                .setSmallIcon(R.drawable.ic_falling)
                .setContentText("Alert sent!");

        notifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    }

    public void alertSentNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("FallWatch")
                .setSmallIcon(R.drawable.ic_falling)
                .setContentText("Alert sent!");

        String notificationService = Context.NOTIFICATION_SERVICE;
        NotificationManager manager = (NotificationManager) context.getSystemService(notificationService);
        manager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());

    }

    public void sendSMS(String number, String username, String location) {

        String uri = "http://google.com/maps/place/" + location;
        smsManager.getDefault();
        StringBuffer smsBody = new StringBuffer();
        smsBody.append(Uri.parse(uri));
        smsManager.sendTextMessage(number, null, username + " needs help " + smsBody.toString(), null, null);
    }

    public void stopTimer() {
        timer.cancel();
    }

    private class AlarmTimer extends CountDownTimer {
        public AlarmTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            Intent timerIntent = new Intent(Constants.ACTION.TIMER_REGISTERED).putExtra("tick", l);
            getApplicationContext().sendBroadcast(timerIntent);
        }

        @Override
        public void onFinish() {
            sendSMS(contact1, user, location);
            sendSMS(contact2, user, location);
            alertSentNotification();
        }
    }
}
