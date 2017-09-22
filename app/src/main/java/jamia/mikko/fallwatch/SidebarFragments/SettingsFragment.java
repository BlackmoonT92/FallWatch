package jamia.mikko.fallwatch.SidebarFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jamia.mikko.fallwatch.R;

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

        getActivity().setTitle(R.string.titleSettings);

        TextView tv = (TextView) view.findViewById(R.id.settings_text);
        tv.setText("SETTINGS PAGE");

        return view;
    }
}
