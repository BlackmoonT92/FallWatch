package jamia.mikko.fallwatch.Detection;

/**
 * Created by jamiamikko on 28/09/2017.
 */

public class Constants {
    //Define all ACTION strings used in broadcasting and filtering service.
    public interface ACTION {
        String MAIN_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.main";
        String STARTFOREGROUND_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.startforeground";
        String STOPFOREGROUND_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.stopforeground";
        String MESSAGE_RECEIVED = "jamia.mikko.fallwatch.falldetectionservice.action.messagereceived";
        String ALERT_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.alertaction";
        String YES_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.yesaction";
        String TIMER_REGISTERED = "jamia.mikko.fallwatch.falldetectionservice.action.timerregistered";

    }

    //Foreground service id.
    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
