package jamia.mikko.fallwatch.Register;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import jamia.mikko.fallwatch.MainSidebarActivity;
import jamia.mikko.fallwatch.R;


public class RegisterFragment extends Fragment {

    private EditText userName, contact1, contact2;
    private Button submitButton;
    private int permissionReadContactsKey = 1;
    private RegisterActivity activity;
    private InputMethodManager inputMethodManager;

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
        activity = ((RegisterActivity) getActivity());
        inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        setInputValues();

        contact1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if(b) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.yes, null)
                            .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                                    ReadContactsFragment fragment = new ReadContactsFragment();

                                    Bundle args = createBundle();
                                    fragment.setArguments(args);

                                    activity.toContactsFragment(fragment);
                                }
                            });

                    AlertDialog dialog = builder.create();

                    dialog.show();


                }
            }
        });

        contact2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage(R.string.typeNumberYourSelf)
                            .setTitle(R.string.contactPhoneNumber)
                            .setNegativeButton(R.string.yes, null)
                            .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                                    ReadContactsFragment fragment = new ReadContactsFragment();

                                    Bundle args = createBundle();
                                    fragment.setArguments(args);

                                    activity.toContactsFragment(fragment);
                                }
                            });

                    AlertDialog dialog = builder.create();

                    dialog.show();


                }

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activity.saveToPreferences("username", userName.getText().toString());
                activity.saveToPreferences("contact1", contact1.getText().toString());
                activity.saveToPreferences("contact2", contact2.getText().toString());

                Toast.makeText(getContext(), getString(R.string.savedToPreferences), Toast.LENGTH_SHORT).show();

                Intent mainIntent = new Intent(getContext(), MainSidebarActivity.class);
                startActivity(mainIntent);
            }
        });
    }



    public void setInputValues() {
        Bundle args = getArguments();

        try {

            userName.setText(args.getString("username"));
            contact1.setText(args.getString("contact1"));
            contact2.setText(args.getString("contact2"));

        } catch (Exception e) {}
    }

    public Bundle createBundle() {
        Bundle args = new Bundle();
        args.putString("username", userName.getText().toString());
        args.putString("contact1", contact1.getText().toString());
        args.putString("contact2", contact2.getText().toString());

        return args;
    }
}

