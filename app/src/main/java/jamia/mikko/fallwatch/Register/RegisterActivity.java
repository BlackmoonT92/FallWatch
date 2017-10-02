package jamia.mikko.fallwatch.Register;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import jamia.mikko.fallwatch.MainSidebarActivity;
import jamia.mikko.fallwatch.R;
import static android.content.DialogInterface.*;

public class RegisterActivity extends AppCompatActivity {

    public static final String USER_PREFERENCES = "UserPreferences";
    private EditText userName, contact1, contact2;
    private Button submitButton;
    private InputMethodManager inputMethodManager;
    private CursorAdapter myAdapter;
    private ContentResolver cr;
    private Cursor cursor;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        checkAndRequestPermissions();

        final String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };

        final int[] toLayouts = { R.id.contactName, R.id.contactNumber };

        try {
            cr = getContentResolver();

            cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        }catch (Exception e){
            Log.i("ERROR", e.toString());
        }

        userName = (EditText) findViewById(R.id.yourNameEdit);
        contact1 = (EditText) findViewById(R.id.firstContactEdit);
        contact2 = (EditText) findViewById(R.id.secondContactEdit);
        submitButton = (Button) findViewById(R.id.submit);
        inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        inflater = getLayoutInflater();

        contact1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                                    View convertView = inflater.inflate(R.layout.contacts, null);

                                    final PopupWindow popupWindow = new PopupWindow(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                                    popupWindow.showAtLocation(convertView, Gravity.CENTER, 0, 0);

                                    ListView lv = (ListView) convertView.findViewById(R.id.contactList);
                                    myAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.contact_list_item, cursor, projection, toLayouts);
                                    lv.setAdapter(myAdapter);

                                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            long contactId = l;
                                            String phoneNumber = getNumberById(contactId);

                                            contact1.setText(phoneNumber);
                                            popupWindow.dismiss();
                                        }
                                    });
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
                    final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                                    inflater = getLayoutInflater();

                                    View convertView = inflater.inflate(R.layout.contacts, null);

                                    final PopupWindow popupWindow = new PopupWindow(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                                    popupWindow.showAtLocation(convertView, Gravity.CENTER, 0, 0);

                                    ListView lv = (ListView) convertView.findViewById(R.id.contactList);
                                    myAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.contact_list_item, cursor, projection, toLayouts);
                                    lv.setAdapter(myAdapter);

                                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            long contactId = l;

                                            String phoneNumber = getNumberById(contactId);

                                            contact2.setText(phoneNumber);

                                            popupWindow.dismiss();
                                        }
                                    });
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
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.INTERNET
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

    public String getNumberById(long id) {
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone._ID + " = " + id, null, null);

        cursor.moveToFirst();

        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }
}