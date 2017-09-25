package jamia.mikko.fallwatch.SidebarFragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import jamia.mikko.fallwatch.R;
import jamia.mikko.fallwatch.Register.RegisterContacts;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class SettingsFragment extends Fragment {

    public static final String USER_PREFERENCES = "UserPreferences";
    private EditText editUsername, editContact1, editContact2;
    private Switch internalSensor, externalSensor;
    private SharedPreferences prefs;
    private Button submitButton;
    private InputMethodManager inputMethodManager;

    public SettingsFragment(){}

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

        prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        editUsername = (EditText) getActivity().findViewById(R.id.yourNameEdit);
        editContact1 = (EditText) getActivity().findViewById(R.id.firstContactEdit);
        editContact2 = (EditText) getActivity().findViewById(R.id.secondContactEdit);
        internalSensor = (Switch) getActivity().findViewById(R.id.useInternal);
        externalSensor = (Switch) getActivity().findViewById(R.id.useExternal);
        submitButton = (Button) getActivity().findViewById(R.id.submitButton);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);

        String name = prefs.getString("username", null);
        String contact1 = prefs.getString("contact1", null);
        String contact2 = prefs.getString("contact2", null);
        final boolean useInternal = prefs.getBoolean("internalSensor", true);
        final boolean useExternal = prefs.getBoolean("externalSensor", true);

        if(name != null) {
            editUsername.setText(name.toString());
        }

        if (contact1 != null) {
            editContact1.setText(contact1.toString());
        }

        if (contact2 != null) {
            editContact2.setText(contact2.toString());
        }

        if(useInternal) {
            internalSensor.setChecked(true);
        }

        if(useExternal) {
            externalSensor.setChecked(true);
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validEdit()) {
                    prefs.edit().putString("username", editUsername.getText().toString()).apply();
                    prefs.edit().putString("contact1", editContact1.getText().toString()).apply();
                    prefs.edit().putString("contact2", editContact2.getText().toString()).apply();
                    prefs.edit().putBoolean("internalSensor", internalSensor.isChecked()).apply();
                    prefs.edit().putBoolean("externalSensor", externalSensor.isChecked()).apply();

                    Toast.makeText(getContext(), getString(R.string.savedToPreferences), Toast.LENGTH_SHORT).show();
                }

            }
        });

        editContact1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                                }
                            });

                    AlertDialog dialog = builder.create();

                    dialog.show();
                }
            }
        });
    }

    private boolean validEdit() {

        if(!editUsername.getText().toString().equals("") && !editContact1.getText().toString().equals("") && !editContact2.getText().toString().equals("")) {
            return true;
        } else {
            return false;
        }

    }

}
