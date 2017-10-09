package jamia.mikko.fallwatch.Detection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by rrvil on 03-Oct-17.
 */

public class AlertReceiver extends BroadcastReceiver {

    private FallDetectionService fallDetectionService;
    private String username, contact1, contact2, location;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Initialize
        fallDetectionService = new FallDetectionService();

        String action = intent.getAction();
        String message = intent.getStringExtra("alertReceive");

        contact1 = intent.getStringExtra("number1");
        contact2 = intent.getStringExtra("number2");
        username = intent.getStringExtra("userName");
        location = intent.getStringExtra("location");

        if (Constants.ACTION.YES_ACTION.equals(action)) {
            //If user is okay, stop the timer.
            fallDetectionService.stopTimer();
        } else if (Constants.ACTION.ALERT_ACTION.equals(action)) {

            if (message != null) {
                //If user is not okay, send alert and stop timer.
                fallDetectionService.sendSMS(contact1, username, location);
                fallDetectionService.sendSMS(contact2, username, location);
                fallDetectionService.stopTimer();
                fallDetectionService.alertSentNotification(context);
            }
        }
    }
}
