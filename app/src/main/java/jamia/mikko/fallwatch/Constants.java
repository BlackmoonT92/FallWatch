package jamia.mikko.fallwatch;

/**
 * Created by jamiamikko on 28/09/2017.
 */

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.main";
        public static String STARTFOREGROUND_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.stopforeground";
        public static String MESSAGE_RECEIVED = "jamia.mikko.fallwatch.falldetectionservice.action.messagereceived";
        public static String ALERT_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.alert";
        public static String STOP_ALERT_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.stopalert";
        public static String START_ALERT_ACTION = "jamia.mikko.fallwatch.falldetectionservice.action.startalert";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
