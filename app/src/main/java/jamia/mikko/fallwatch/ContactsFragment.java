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

public class ContactsFragment extends Fragment {

    public ContactsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_contacts, container, false);

        getActivity().setTitle("Contacts");

        TextView tv = (TextView) view.findViewById(R.id.contacts_text);
        tv.setText("CONTACTS PAGE");

        Log.i("DEBUG", "Contracts fragment");

        return view;
    }
}
