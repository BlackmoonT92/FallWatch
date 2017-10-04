package jamia.mikko.fallwatch.SidebarFragments;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
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
import android.widget.Toast;

import jamia.mikko.fallwatch.Constants;
import jamia.mikko.fallwatch.FallDetectionService;
import jamia.mikko.fallwatch.MainSidebarActivity;
import jamia.mikko.fallwatch.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class HomeFragment extends Fragment {

    private ImageView statusOn, statusOff;
    private PopupWindow popupWindow;
    public static final String USER_PREFERENCES = "UserPreferences";
    private String username, contact1;
    private boolean useExternal;
    public Switch trackerSwitch;
    private MainSidebarActivity activity;
    private SharedPreferences prefs;
    private BluetoothAdapter mBluetoothAdapter;
    private FallDetectionService fallDetectionService;
    private String receivedLocation;
    private static CountDownTimer countDownTimer;

    public HomeFragment() {

    }

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("alert");
            receivedLocation = intent.getStringExtra("location");

            if(message != null) {
                showPopupDialog(receivedLocation);
                activity.stopService();
                getActivity().unregisterReceiver(this);
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle(R.string.titleHome);

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

                if (!mBluetoothAdapter.isEnabled() && useExternal) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }

                activity.enableLocationRequest();

                if (isChecked) {

                    activity.enableLocationRequest();
                    ((Animatable) statusOn.getDrawable()).start();
                    statusOff.setVisibility(View.INVISIBLE);
                    statusOn.setVisibility(View.VISIBLE);

                    IntentFilter intentFilter = new IntentFilter(Constants.ACTION.MESSAGE_RECEIVED);
                    getActivity().registerReceiver(messageReceiver, intentFilter);

                    activity.connectToService();
                    activity.saveTrackingStateToPreferences("tracking_state", true);


                } else {
                    statusOff.getDrawable();
                    statusOn.setVisibility(View.INVISIBLE);
                    statusOff.setVisibility(View.VISIBLE);

                    try {
                        getActivity().unregisterReceiver(messageReceiver);
                    } catch (Exception e) {}

                    activity.stopService();
                    activity.saveTrackingStateToPreferences("tracking_state", false);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        username = prefs.getString("username", null);
        contact1 = prefs.getString("contact1", null);
        useExternal = prefs.getBoolean("externalSensor", true);

        boolean switchOn = prefs.getBoolean("tracking_state", true);

        if (switchOn) {

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

    public void showPopupDialog(final String location) {
        try {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.popup_home, (ViewGroup) getActivity().findViewById(R.id.popup));

            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            dimBehind(popupWindow);

            final TextView timer = (TextView) layout.findViewById(R.id.alert_countdown);

            countDownTimer = new CountDownTimer(30000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    timer.setText(Long.toString(millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    Toast.makeText(getContext(), getString(R.string.alertSent), Toast.LENGTH_SHORT).show();
                    timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                    timer.setText(getString(R.string.waiting_for_help));
                    fallDetectionService.sendSMS(contact1, username, location);
                    this.cancel();
                    popupWindow.dismiss();
                }
            }.start();

            final Button close = (Button) layout.findViewById(R.id.btn_im_okay);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    countDownTimer.cancel();
                    popupWindow.dismiss();
                }
            });

            final Button sendAlert = (Button) layout.findViewById(R.id.btn_need_help);
            sendAlert.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    countDownTimer.onFinish();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
