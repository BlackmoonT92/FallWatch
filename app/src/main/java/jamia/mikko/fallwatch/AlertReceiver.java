package jamia.mikko.fallwatch;

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
    private String username, contact1, location;

    @Override
    public void onReceive(Context context, Intent intent) {

        fallDetectionService = new FallDetectionService();

        String action = intent.getAction();

        String message = intent.getStringExtra("alertReceive");

        contact1 = intent.getStringExtra("number");
        username = intent.getStringExtra("userName");
        location = intent.getStringExtra("location");

        if (YES_ACTION.equals(action)) {
            fallDetectionService.stopTimer();
        } else if (ALERT_ACTION.equals(action)) {

            if (message != null) {

                fallDetectionService.sendSMS(contact1, username, location);
                fallDetectionService.stopTimer();
            }

            if (message == null) {
                Log.i("ERROR", "message is null");
            }
        }
    }
}
