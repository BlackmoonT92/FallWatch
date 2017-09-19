package jamia.mikko.fallwatch;

import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class ReadContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks, AdapterView.OnClickListener {

    private ArrayList<String> testData;
    private SimpleCursorAdapter mCursorAdapter;
    private final static String[] FROM_COLUMNS = {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };
    private final static int[] TO_IDS = {
            android.R.id.text1
    };

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

        this.testData = new ArrayList<>();

        this.testData.add("Näi");
        this.testData.add("Noi");


        ListView listView = new ListView(getContext());

        // Gets a CursorAdapter
        mCursorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, null, FROM_COLUMNS, TO_IDS, 0);
        listView.setAdapter(mCursorAdapter);

    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onClick(View view) {

    }
}
