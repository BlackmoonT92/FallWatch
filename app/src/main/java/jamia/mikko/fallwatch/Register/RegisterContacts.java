package jamia.mikko.fallwatch.Register;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import jamia.mikko.fallwatch.R;
import jamia.mikko.fallwatch.SidebarFragments.SettingsFragment;

public class RegisterContacts extends AppCompatActivity {
    private CursorAdapter myAdapter;
    private ContentResolver cr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_contacts);

        Intent originIntent = getIntent();
        final String username = originIntent.getStringExtra("username");
        final String contact1 = originIntent.getStringExtra("contact1");
        final String contact2 = originIntent.getStringExtra("contact2");
        final String origin = originIntent.getStringExtra("origin");

        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };

        int[] toLayouts = { R.id.contactName, R.id.contactNumber };

        cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        myAdapter = new SimpleCursorAdapter(this, R.layout.contact_list_item, cursor, projection, toLayouts);

        ListView listView = (ListView) findViewById(R.id.contactList);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                long contactId = l;

                String phoneNumber = getNumberById(contactId);

                if(origin.equals("registerActivity")) {
                    Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);

                    registerIntent.putExtra("username", username);

                    if(contact1.equals("")) {
                        registerIntent.putExtra("contact1", phoneNumber);
                        registerIntent.putExtra("contact2", contact2);
                    } else if(contact2.equals("")) {
                        registerIntent.putExtra("contact1", contact1);
                        registerIntent.putExtra("contact2", phoneNumber);
                    } else {
                        registerIntent.putExtra("contact1", contact1);
                        registerIntent.putExtra("contact2", contact2);
                    }

                    startActivity(registerIntent);
                }

                if(origin.equals("settingsFragment")) {
                    Intent settingsIntent = new Intent(getApplicationContext(), SettingsFragment.class);

                    startActivity(settingsIntent);
                }
            }
        });
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
