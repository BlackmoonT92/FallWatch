package jamia.mikko.fallwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by rrvil on 03-Oct-17.
 */

public class AlertReceiver extends BroadcastReceiver{

    public static final String ALERT_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.alertaction";
    public static final String YES_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.yesaction";
    private FallDetectionService fallDetectionService;
    private String username, contact1, location;

    @Override
    public void onReceive(Context context, Intent intent) {

        fallDetectionService = new FallDetectionService();

        String action = intent.getAction();

        if (YES_ACTION.equals(action)){
            Log.i("ACTION", "You are OK");
        }
        else if (ALERT_ACTION.equals(action)){
            Log.i("ACTION", "Alerting");
            String message = intent.getStringExtra("alert");

            if (message != null) {
                contact1 = intent.getStringExtra("contact1");
                username = intent.getStringExtra("user");
                location = intent.getStringExtra("location");

                Log.i("Info", contact1 + " " + username + " " + location);

                fallDetectionService.sendSMS(contact1, username, location);
            }

        }
    }
}
