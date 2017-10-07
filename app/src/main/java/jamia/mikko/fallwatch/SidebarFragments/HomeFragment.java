package jamia.mikko.fallwatch.SidebarFragments;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import jamia.mikko.fallwatch.Detection.Constants;
import jamia.mikko.fallwatch.Detection.FallDetectionService;
import jamia.mikko.fallwatch.Main.MainSidebarActivity;
import jamia.mikko.fallwatch.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class HomeFragment extends Fragment {

    private static final String USER_PREFERENCES = "UserPreferences";
    private PopupWindow popupWindow;
    private Switch trackerSwitch;
    private ImageView statusOn, statusOff;
    private String username, contact1, contact2;
    private boolean useExternal;
    private MainSidebarActivity activity;
    private SharedPreferences prefs;
    private BluetoothAdapter mBluetoothAdapter;
    private FallDetectionService fallDetectionService;
    private String receivedLocation;

    //Broadcast falling down alerts from service.
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("alert");
            receivedLocation = intent.getStringExtra("location");

            if (message != null) {
                //Show popup and close service. Also unregister receiver.
                showPopupDialog(receivedLocation);
                activity.stopService();
                getActivity().unregisterReceiver(this);
            }
        }
    };

    public HomeFragment() {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle(R.string.titleHome);

        //Initialize
        fallDetectionService = new FallDetectionService();
        activity = ((MainSidebarActivity) getActivity());
        statusOn = (ImageView) view.findViewById(R.id.status_on);
        statusOff = (ImageView) view.findViewById(R.id.status_off);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        trackerSwitch = (Switch) view.findViewById(R.id.tracking_switch);
        prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);

        trackerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                //If we use external sensor, check that bluetooth is enabled.
                if (!mBluetoothAdapter.isEnabled() && useExternal) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }

                //Make sure that locations are enabled.
                activity.enableLocationRequest();

                if (isChecked) {

                    //Icon animation
                    ((Animatable) statusOn.getDrawable()).start();
                    statusOff.setVisibility(View.INVISIBLE);
                    statusOn.setVisibility(View.VISIBLE);

                    //Register receiver and create filter for broadcast.
                    IntentFilter intentFilter = new IntentFilter(Constants.ACTION.MESSAGE_RECEIVED);
                    getActivity().registerReceiver(messageReceiver, intentFilter);


                    //Start service and save tracking state to shared preferences.
                    activity.connectToService();
                    activity.saveTrackingStateToPreferences("tracking_state", true);


                } else {
                    //Icon animation
                    statusOff.getDrawable();
                    statusOn.setVisibility(View.INVISIBLE);
                    statusOff.setVisibility(View.VISIBLE);

                    //Try to unregister receiver, as it might be already unregistered in receiver itself.
                    try {
                        getActivity().unregisterReceiver(messageReceiver);
                    } catch (Exception e) {
                    }

                    //Stop service and save tracking state.
                    activity.stopService();
                    activity.saveTrackingStateToPreferences("tracking_state", false);

                    //Kill all notifications
                    fallDetectionService.destroyNotificationsFromUi(getContext());
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize
        username = prefs.getString("username", null);
        contact1 = prefs.getString("contact1", null);
        contact2 = prefs.getString("contact2", null);
        useExternal = prefs.getBoolean("externalSensor", true);

        //Set switch to correct position if tracking state is true.
        boolean switchOn = prefs.getBoolean("tracking_state", true);

        if (switchOn) {

            //Animation
            ((Animatable) statusOn.getDrawable()).start();
            statusOff.setVisibility(View.INVISIBLE);
            statusOn.setVisibility(View.VISIBLE);
            trackerSwitch.setChecked(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showPopupDialog(final String location) {
        try {

            //Initialize popup
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.popup_home, (ViewGroup) getActivity().findViewById(R.id.popup));

            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            dimBehind(popupWindow);

            //Broadcast timer from service.
            final TextView timer = (TextView) layout.findViewById(R.id.alert_countdown);

            final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    long time = intent.getLongExtra("tick", 0);
                    timer.setText(Long.toString(time / 1000));
                }
            };

            //Create filter for timer broadcast and register receiver.
            IntentFilter intentFilter = new IntentFilter(Constants.ACTION.TIMER_REGISTERED);
            getActivity().registerReceiver(timeReceiver, intentFilter);

            //When user is okay, unregister timer and destroy popup and notifications.
            final Button close = (Button) layout.findViewById(R.id.btn_im_okay);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    getActivity().unregisterReceiver(timeReceiver);
                    popupWindow.dismiss();
                    fallDetectionService.stopTimer();
                    fallDetectionService.destroyNotificationsFromUi(getContext());
                }
            });

            //If user is not okay, send alert and stop broadcast.
            final Button sendAlert = (Button) layout.findViewById(R.id.btn_need_help);
            sendAlert.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    fallDetectionService.sendSMS(contact1, username, location);
                    fallDetectionService.sendSMS(contact2, username, location);
                    getActivity().unregisterReceiver(timeReceiver);
                    popupWindow.dismiss();
                    fallDetectionService.stopTimer();
                    fallDetectionService.alertSentNotification(getContext());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Dim background area of popup window.
    private void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.5f;
        wm.updateViewLayout(container, p);
    }
}
