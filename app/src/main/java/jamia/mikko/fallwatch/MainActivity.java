package jamia.mikko.fallwatch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    public static final String USER_PREFERENCES = "UserPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);

        String username = prefs.getString("username", null);
        String contact1 = prefs.getString("contact1", null);

        if(username == null && contact1 == null) {

            Intent registerIntent = new Intent(this, RegisterActivity.class);
            startActivity(registerIntent);

        }
        
    }
}