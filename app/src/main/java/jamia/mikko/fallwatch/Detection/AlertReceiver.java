package jamia.mikko.fallwatch.Detection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by rrvil on 03-Oct-17.
 */

public class AlertReceiver extends BroadcastReceiver {

    public static final String ALERT_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.alertaction";
    public static final String YES_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.yesaction";
    private FallDetectionService fallDetectionService;
    private String username, contact1, contact2, location;

    @Override
    public void onReceive(Context context, Intent intent) {

        fallDetectionService = new FallDetectionService();

        String action = intent.getAction();

        String message = intent.getStringExtra("alertReceive");

        contact1 = intent.getStringExtra("number1");
        contact2 = intent.getStringExtra("number2");
        username = intent.getStringExtra("userName");
        location = intent.getStringExtra("location");

        if (YES_ACTION.equals(action)) {
            fallDetectionService.stopTimer();
        } else if (ALERT_ACTION.equals(action)) {

            if (message != null) {

                fallDetectionService.sendSMS(contact1, username, location);
                fallDetectionService.sendSMS(contact2, username, location);
                fallDetectionService.stopTimer();
                fallDetectionService.alertSentNotification(context);
            }

            if (message == null) {
                Log.i("ERROR", "message is null");
            }
        }
    }
}
