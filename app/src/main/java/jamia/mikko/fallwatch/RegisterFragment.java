package jamia.mikko.fallwatch;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;


public class RegisterFragment extends Fragment {

    private EditText userName, contact1, contact2;
    private Button submitButton;
    public static final String USER_PREFERENCES = "UserPreferences";
    private int permissionReadContactsKey = 1;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_register, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, permissionReadContactsKey);

        }


        userName = (EditText) getActivity().findViewById(R.id.yourNameEdit);
        contact1 = (EditText) getActivity().findViewById(R.id.firstContactEdit);
        contact2 = (EditText) getActivity().findViewById(R.id.secondContactEdit);
        submitButton = (Button) getActivity().findViewById(R.id.submit);

        Bundle args = getArguments();

        try {

            userName.setText(args.getString("username"));
            contact1.setText(args.getString("contact1"));
            contact2.setText(args.getString("contact2"));

        } catch (Exception e) {

        }


        contact1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                ReadContactsFragment fragment = new ReadContactsFragment();

                Bundle args = new Bundle();
                args.putString("username", userName.getText().toString());
                args.putString("contact1", contact1.getText().toString());
                args.putString("contact2", contact2.getText().toString());

                fragment.setArguments(args);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        contact2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                ReadContactsFragment fragment = new ReadContactsFragment();

                Bundle args = new Bundle();
                args.putString("username", userName.getText().toString());
                args.putString("contact1", contact1.getText().toString());
                args.putString("contact2", contact2.getText().toString());

                fragment.setArguments(args);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveToPreferences("username", userName.getText().toString());
                saveToPreferences("contact1", contact1.getText().toString());
                saveToPreferences("contact2", contact2.getText().toString());

                Toast.makeText(getContext(), "Saved to preferences", Toast.LENGTH_SHORT).show();

                Intent mainIntent = new Intent(getContext(), MainSidebarActivity.class);
                startActivity(mainIntent);
            }
        });
    }

    public void saveToPreferences(String key, String value) {
        SharedPreferences prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putString(key, value);

        prefsEditor.commit();
    }

}
