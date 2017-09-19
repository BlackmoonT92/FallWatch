package jamia.mikko.fallwatch;

import android.net.Uri;
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

public class HomeFragment extends Fragment {

    public HomeFragment(){}

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();

        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle("Home");

        TextView tv = (TextView) view.findViewById(R.id.home_text);
        tv.setText("HOME PAGE");

        Log.i("DEBUG", "Home fragment");

        return view;
    }
}
