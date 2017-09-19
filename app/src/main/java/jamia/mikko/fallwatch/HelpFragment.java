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

public class HelpFragment extends Fragment {

    public HelpFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_help, container, false);

        getActivity().setTitle("Help");

        TextView tv = (TextView) view.findViewById(R.id.help_text);
        tv.setText("HELP PAGE");

        Log.i("DEBUG", "Help fragment");


        return view;
    }
}
