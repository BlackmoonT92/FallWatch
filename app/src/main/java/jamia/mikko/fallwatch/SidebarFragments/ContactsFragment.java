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

public class ContactsFragment extends Fragment {

    public ContactsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_contacts, container, false);

        getActivity().setTitle(R.string.titleContacts);

        return view;
    }
}
