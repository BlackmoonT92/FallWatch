package jamia.mikko.fallwatch.Register;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jamia.mikko.fallwatch.MainSidebarActivity;
import jamia.mikko.fallwatch.R;

public class RegisterActivity extends AppCompatActivity {

    public static final String USER_PREFERENCES = "UserPreferences";
    private EditText userName, contact1, contact2;
    private Button submitButton;
    private InputMethodManager inputMethodManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        checkAndRequestPermissions();

        userName = (EditText) findViewById(R.id.yourNameEdit);
        contact1 = (EditText) findViewById(R.id.firstContactEdit);
        contact2 = (EditText) findViewById(R.id.secondContactEdit);
        submitButton = (Button) findViewById(R.id.submit);
        inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        Intent dataIntent = getIntent();

        setInputValues(dataIntent);

        contact1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                                    Intent contactIntent = createArguments(RegisterContacts.class);

                                    startActivity(contactIntent);
                                }
                            });

                    AlertDialog dialog = builder.create();

                    dialog.show();

                }
            }
        });

        contact2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

                                    Intent contactIntent = createArguments(RegisterContacts.class);

                                    startActivity(contactIntent);
                                }
                            });

                    AlertDialog dialog = builder.create();

                    dialog.show();


                }

            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validRegister()) {
                    saveStringToPreferences("username", userName.getText().toString());
                    saveStringToPreferences("contact1", contact1.getText().toString());
                    saveStringToPreferences("contact2", contact2.getText().toString());
                    saveBooleanToPreferences("internalSensor", true);
                    saveBooleanToPreferences("externalSensor", false);

                    Toast.makeText(getApplicationContext(), getString(R.string.savedToPreferences), Toast.LENGTH_SHORT).show();

                    Intent mainIntent = new Intent(getApplicationContext(), MainSidebarActivity.class);
                    startActivity(mainIntent);
                    finish();

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void saveStringToPreferences(String key, String value) {
        SharedPreferences prefs = getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putString(key, value);

        prefsEditor.commit();
    }

    private void saveBooleanToPreferences(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putBoolean(key, value);

        prefsEditor.commit();
    }


    private void checkAndRequestPermissions() {
        String [] permissions=new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS
        };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission:permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission )!= PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    private boolean validRegister() {

        if(!userName.getText().toString().equals("") && !contact1.getText().toString().equals("") && !contact2.getText().toString().equals("")) {
            return true;
        } else {
            return false;
        }

    }


    private void setInputValues(Intent dataIntent) {
        userName.setText(dataIntent.getStringExtra("username"));
        contact1.setText(dataIntent.getStringExtra("contact1"));
        contact2.setText(dataIntent.getStringExtra("contact2"));
    }

    private Intent createArguments(Class destination) {
        Intent dataIntent = new Intent(getApplicationContext(), destination);

        dataIntent.putExtra("username", userName.getText().toString());
        dataIntent.putExtra("contact1", contact1.getText().toString());
        dataIntent.putExtra("contact2", contact2.getText().toString());
        dataIntent.putExtra("origin", "registerActivity");

        return dataIntent;
    }
}