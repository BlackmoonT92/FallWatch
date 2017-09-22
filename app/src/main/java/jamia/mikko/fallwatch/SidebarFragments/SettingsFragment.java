package jamia.mikko.fallwatch.SidebarFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import jamia.mikko.fallwatch.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class SettingsFragment extends Fragment {

    public static final String USER_PREFERENCES = "UserPreferences";
    private EditText editUsername, editContact1, editContact2;
    private Switch internalSensor, externalSensor;


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

        SharedPreferences prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        editUsername = (EditText) getActivity().findViewById(R.id.yourNameEdit);
        editContact1 = (EditText) getActivity().findViewById(R.id.firstContactEdit);
        editContact2 = (EditText) getActivity().findViewById(R.id.secondContactEdit);
        internalSensor = (Switch) getActivity().findViewById(R.id.useInternal);


        String name = prefs.getString("username", null);
        String contact1 = prefs.getString("contact1", null);
        String contact2 = prefs.getString("contact2", null);
        boolean useInternal = prefs.getBoolean("internalSensor", true);

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
    }
}
