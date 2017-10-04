package jamia.mikko.fallwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import jamia.mikko.fallwatch.Register.RegisterActivity;
import jamia.mikko.fallwatch.SidebarFragments.AboutFragment;
import jamia.mikko.fallwatch.SidebarFragments.HelpFragment;
import jamia.mikko.fallwatch.SidebarFragments.HomeFragment;
import jamia.mikko.fallwatch.SidebarFragments.SettingsFragment;


public class MainSidebarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static final String USER_PREFERENCES = "UserPreferences";
    private static FragmentManager fragmentManager;
    private static Intent service;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sidebar);

        SharedPreferences prefs = getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);

        String username = prefs.getString("username", null);
        String contact1 = prefs.getString("contact1", null);

        if (username == null && contact1 == null) {

            Intent registerIntent = new Intent(this, RegisterActivity.class);
            startActivity(registerIntent);
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = null;
            fragmentClass = HomeFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_container, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toolbar.findViewById(R.id.logged_user);

        View header = navigationView.getHeaderView(0);
        TextView loggedUser = (TextView) header.findViewById(R.id.logged_user);
        loggedUser.setText(username);

        enableLocationRequest();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_home) {
            fragmentClass = HomeFragment.class;
        } else if (id == R.id.nav_settings) {
            fragmentClass = SettingsFragment.class;
        } else if (id == R.id.nav_help) {
            fragmentClass = HelpFragment.class;
        } else if (id == R.id.nav_license) {
            fragmentClass = AboutFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.nav_container, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void saveTrackingStateToPreferences(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putBoolean(key, value);

        prefsEditor.commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this,
                                "Location Service not Enabled, application works properly only if you enable Location Service",
                                Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    public void connectToService() {
        service = new Intent(this, FallDetectionService.class);

        if(!FallDetectionService.IS_SERVICE_RUNNING) {
            service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            FallDetectionService.IS_SERVICE_RUNNING = true;
        }

        startService(service);
    }

    public void stopService() {
        service = new Intent(this, FallDetectionService.class);
        FallDetectionService.IS_SERVICE_RUNNING = false;
        stopService(service);
    }

    public void enableLocationRequest() {


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(ApplicationClass.getLocationRequest());

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(ApplicationClass.getGoogleApiHelper().apiClient, builder.build());

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
                        try {
                            status.startResolutionForResult(MainSidebarActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }
}
