package jamia.mikko.fallwatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class SettingsFragment extends Fragment {

    public SettingsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_settings, container, false);

        getActivity().setTitle("Settings");

        TextView tv = (TextView) view.findViewById(R.id.settings_text);
        tv.setText("SETTINGS PAGE");

        Log.i("DEBUG", "Settings fragment");


        return view;
    }
}
