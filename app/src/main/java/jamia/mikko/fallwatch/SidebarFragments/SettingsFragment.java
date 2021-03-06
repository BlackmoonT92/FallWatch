package jamia.mikko.fallwatch.SidebarFragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.Toast;

import jamia.mikko.fallwatch.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class SettingsFragment extends Fragment {

    private static final String USER_PREFERENCES = "UserPreferences";
    private EditText editUsername, editContact1, editContact2;
    private Switch internalSensor, externalSensor;
    private SharedPreferences prefs;
    private Button submitButton;
    private InputMethodManager inputMethodManager;
    private CursorAdapter contactListAdapter;
    private ContentResolver cr;
    private Cursor cursor;
    private LayoutInflater inflater;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_settings, container, false);

        getActivity().setTitle(R.string.titleSettings);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize
        prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        editUsername = (EditText) getActivity().findViewById(R.id.yourNameEdit);
        editContact1 = (EditText) getActivity().findViewById(R.id.firstContactEdit);
        editContact2 = (EditText) getActivity().findViewById(R.id.secondContactEdit);
        internalSensor = (Switch) getActivity().findViewById(R.id.useInternal);
        externalSensor = (Switch) getActivity().findViewById(R.id.useExternal);
        submitButton = (Button) getActivity().findViewById(R.id.submitButton);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        cr = getActivity().getContentResolver();
        cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        inflater = getActivity().getLayoutInflater();

        final String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };

        final int[] toLayouts = {R.id.contactName, R.id.contactNumber};

        //Initialize current settings
        String name = prefs.getString("username", null);
        String contact1 = prefs.getString("contact1", null);
        String contact2 = prefs.getString("contact2", null);
        final boolean useInternal = prefs.getBoolean("internalSensor", true);
        final boolean useExternal = prefs.getBoolean("externalSensor", true);

        if (name != null) {
            editUsername.setText(name.toString());
        }

        if (contact1 != null) {
            editContact1.setText(contact1.toString());
        }

        if (contact2 != null) {
            editContact2.setText(contact2.toString());
        }

        if (useInternal) {
            internalSensor.setChecked(true);
        }

        if (useExternal) {
            externalSensor.setChecked(true);
        }

        //Show popup on editText focus. Display contact list from device and return contact to editText
        editContact1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                                    View convertView = inflater.inflate(R.layout.contacts, null);

                                    final PopupWindow popupWindow = new PopupWindow(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                                    popupWindow.showAtLocation(convertView, Gravity.CENTER, 0, 0);

                                    ListView lv = (ListView) convertView.findViewById(R.id.contactList);
                                    contactListAdapter = new SimpleCursorAdapter(getContext(), R.layout.contact_list_item, cursor, projection, toLayouts);
                                    lv.setAdapter(contactListAdapter);

                                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            long contactId = l;

                                            String phoneNumber = getNumberById(contactId);

                                            editContact1.setText(phoneNumber);

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

        //Show popup on editText focus. Display contact list from device and return contact to editText
        editContact2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                                    View convertView = inflater.inflate(R.layout.contacts, null);

                                    final PopupWindow popupWindow = new PopupWindow(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                                    popupWindow.showAtLocation(convertView, Gravity.CENTER, 0, 0);

                                    ListView lv = (ListView) convertView.findViewById(R.id.contactList);
                                    contactListAdapter = new SimpleCursorAdapter(getContext(), R.layout.contact_list_item, cursor, projection, toLayouts);
                                    lv.setAdapter(contactListAdapter);

                                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            long contactId = l;

                                            String phoneNumber = getNumberById(contactId);

                                            editContact2.setText(phoneNumber);

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

        //Make sure external and internal can't be checked at the same time.
        internalSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    externalSensor.setChecked(false);
                }
            }
        });

        //Make sure external and internal can't be checked at the same time.
        externalSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    internalSensor.setChecked(false);
                }
            }
        });

        //Validate and submit / edit shared preferences.
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validEdit()) {
                    prefs.edit().putString("username", editUsername.getText().toString()).apply();
                    prefs.edit().putString("contact1", editContact1.getText().toString()).apply();
                    prefs.edit().putString("contact2", editContact2.getText().toString()).apply();
                    prefs.edit().putBoolean("internalSensor", internalSensor.isChecked()).apply();
                    prefs.edit().putBoolean("externalSensor", externalSensor.isChecked()).apply();

                    Toast.makeText(getContext(), getString(R.string.savedToPreferences), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private String getNumberById(long id) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone._ID + " = " + id, null, null);

        cursor.moveToFirst();

        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }


    private boolean validEdit() {

        if (!editUsername.getText().toString().equals("") && !editContact1.getText().toString().equals("") && !editContact2.getText().toString().equals("")) {
            return true;
        } else {
            return false;
        }

    }
}
