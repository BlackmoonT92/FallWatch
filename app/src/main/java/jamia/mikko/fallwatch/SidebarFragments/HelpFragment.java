package jamia.mikko.fallwatch.SidebarFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jamia.mikko.fallwatch.R;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class HelpFragment extends Fragment {

    public HelpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_help, container, false);

        getActivity().setTitle(getString(R.string.titleHelp));


        return view;
    }
}
