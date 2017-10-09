package jamia.mikko.fallwatch.Detection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by jamiamikko on 04/10/2017.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static GoogleApiClient apiClient;
    public static LocationRequest locationRequest;
    public Location mLastLocation;
    private Context context;

    public GoogleApiHelper(Context context) {
        this.context = context;

        //Create LocationRequest that is used from MainSidebarActivity.
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Create new GoogleApiClient and connect.
        this.apiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (apiClient != null) {
            apiClient.connect();
        }

    }

    public void disconnect() {
        stopLocationUpdates();
    }

    public boolean isConnected() {
        return apiClient != null && apiClient.isConnected();
    }

    public void receiveLocationUpdates() {
        //If apiClient is connected, check permissions and start receiving location updates from API.
        if (isConnected()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        receiveLocationUpdates();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        //Last know location is always what we receive from API.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }


    private void stopLocationUpdates() {
        if (isConnected()) {
            apiClient.disconnect();
        }
    }

    public String getLocation() {


        //Initialize new location string used in FallDetectionService.
        String lat = Double.toString(mLastLocation.getLatitude());
        String lng = Double.toString(mLastLocation.getLongitude());

        String location = lat + "," + lng;

        return location;
    }


}
