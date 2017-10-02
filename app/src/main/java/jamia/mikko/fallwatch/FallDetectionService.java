package jamia.mikko.fallwatch;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.SensorManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by jamiamikko on 27/09/2017.
 */


public class FallDetectionService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private Thread thread;
    private InternalDetectionClient internalDetectionClient;
    private SensorManager sensorManager;
    public static boolean IS_SERVICE_RUNNING = false;
    private SmsManager smsManager = SmsManager.getDefault();
    private LocationManager locationManager;
    private String provider, lastKnownLocation;
    private GoogleApiClient googleApiClient;
    private Location mLastLocation;
    private LocationRequest locationRequest;

    public FallDetectionService(){}

    private Handler messageHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Log.i("Detector", "You have fallen");
                showAlert();
                Intent alert = new Intent(Constants.ACTION.MESSAGE_RECEIVED).putExtra("alert", "oh noes");
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

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        internalDetectionClient = new InternalDetectionClient(sensorManager, messageHandler);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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

    private void showAlert(){

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        enableLocationRequest();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    private void enableLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    //All location settings are satisfied
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    //Not all location settings are satisfied
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Show dialog user a dialog to enable location
                        PendingIntent pI = status.getResolution();
                        googleApiClient.getContext().startActivity(new Intent(googleApiClient.getContext(), MainSidebarActivity.class)
                                .putExtra("resolution", pI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
}