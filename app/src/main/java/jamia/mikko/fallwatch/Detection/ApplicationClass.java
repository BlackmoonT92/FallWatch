package jamia.mikko.fallwatch.Detection;

import android.app.Application;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by jamiamikko on 04/10/2017.
 */

public class ApplicationClass extends Application {

    //Using Application extending class to make sure we only have one instance of GoogleAPiHelper.
    private static GoogleApiHelper googleApiHelper;
    private static ApplicationClass mInstance;

    public static synchronized ApplicationClass getInstance() {
        return mInstance;
    }

    public static GoogleApiHelper getGoogleApiHelperInstance() {
        return googleApiHelper;
    }

    public static GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }

    public static LocationRequest getLocationRequest() {
        return getInstance().getGoogleApiHelperInstance().locationRequest;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize
        mInstance = this;
        googleApiHelper = new GoogleApiHelper(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}