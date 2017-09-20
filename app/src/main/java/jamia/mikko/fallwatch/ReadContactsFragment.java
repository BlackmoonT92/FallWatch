package jamia.mikko.fallwatch;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import jamia.mikko.fallwatch.SidebarFragments.RegisterFragment;


public class ReadContactsFragment extends ListFragment {

    private CursorAdapter myAdapter;
    private ContentResolver cr;
    private String username, contact1, contact2;
    private RegisterActivity activity;

    public ReadContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.contacts_list_view, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = ((RegisterActivity) getActivity());

        Bundle args = getArguments();

        username = args.getString("username");
        contact1 = args.getString("contact1");
        contact2 = args.getString("contact2");

        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };

        int[] toLayouts = { R.id.contactName, R.id.contactNumber };

        cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        myAdapter = new SimpleCursorAdapter(getContext(), R.layout.contact_list_item, cursor, projection, toLayouts);

        setListAdapter(myAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        long contactId = id;

        String phoneNumber = getNumberById(contactId);

        RegisterFragment fragment = new RegisterFragment();

        Bundle args = new Bundle();

        args.putString("username", username);

        if(contact1.equals("")) {
            args.putString("contact1", phoneNumber);
            args.putString("contact2", contact2);
        } else if(contact2.equals("")) {
            args.putString("contact1", contact1);
            args.putString("contact2", phoneNumber);
        } else {
            args.putString("contact1", contact1);
            args.putString("contact2", contact2);
        }

        fragment.setArguments(args);

        activity.toRegisterFragment(fragment);
    }

    public String getNumberById(long id) {
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone._ID + " = " + id, null, null);

        cursor.moveToFirst();

        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }
}
