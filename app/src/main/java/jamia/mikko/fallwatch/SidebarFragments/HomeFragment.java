package jamia.mikko.fallwatch.SidebarFragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
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
import jamia.mikko.fallwatch.ExternalDetectionClient;
import jamia.mikko.fallwatch.InternalDetectionClient;
import jamia.mikko.fallwatch.FallDetectionService;
import jamia.mikko.fallwatch.MainSidebarActivity;
import jamia.mikko.fallwatch.R;
import static android.content.Context.BLUETOOTH_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class HomeFragment extends Fragment {

    private ImageView statusOn, statusOff;
    private Thread t;
    private SensorManager sensorManager;
    private PopupWindow popupWindow;
    private SmsManager smsManager = SmsManager.getDefault();
    public static final String USER_PREFERENCES = "UserPreferences";
    private String username, contact1;
    private boolean useInternal, useExternal;
    public Switch trackerSwitch;
    private MainSidebarActivity activity;
    private SharedPreferences prefs;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver messageReceiver;
    private FallDetectionService fallDetectionService;

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();

        return homeFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle(R.string.titleHome);

        activity = ((MainSidebarActivity) getActivity());

        statusOn = (ImageView) view.findViewById(R.id.status_on);
        statusOff = (ImageView) view.findViewById(R.id.status_off);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        trackerSwitch = (Switch) view.findViewById(R.id.tracking_switch);
        fallDetectionService = new FallDetectionService();
        final Intent service = new Intent(getContext(), FallDetectionService.class);

        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("alert");

                if(message != null) {
                    showPopupDialog();
                    activity.stopService(service);
                }
            }
        };



        trackerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {



                if (!mBluetoothAdapter.isEnabled() && useExternal) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }


                if (isChecked) {

                    ((Animatable) statusOn.getDrawable()).start();
                    statusOff.setVisibility(View.INVISIBLE);
                    statusOn.setVisibility(View.VISIBLE);

                    if(!FallDetectionService.IS_SERVICE_RUNNING) {
                        service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                        FallDetectionService.IS_SERVICE_RUNNING = true;
                    }

                    IntentFilter intentFilter = new IntentFilter(Constants.ACTION.MESSAGE_RECEIVED);
                    activity.registerReceiver(messageReceiver, intentFilter);

                    activity.startService(service);
                    activity.saveTrackingStateToPreferences("tracking_state", true);


                } else {
                    statusOff.getDrawable();
                    statusOn.setVisibility(View.INVISIBLE);
                    statusOff.setVisibility(View.VISIBLE);


                    activity.stopService(service);
                    activity.unregisterReceiver(messageReceiver);

                    activity.saveTrackingStateToPreferences("tracking_state", false);
                }
            }
        });

        SharedPreferences prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);

        username = prefs.getString("username", null);
        contact1 = prefs.getString("contact1", null);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        username = prefs.getString("username", null);
        contact1 = prefs.getString("contact1", null);
        useInternal = prefs.getBoolean("internalSensor", true);
        useExternal = prefs.getBoolean("externalSensor", true);

        boolean switchOn = prefs.getBoolean("tracking_state", true);

        if (switchOn) {
            trackerSwitch.setChecked(true);
        }
    }

    public Boolean sensorExists() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Log.i("Sensor", "löyty");
            return true;
        } else {
            return false;
        }
    }

    public void showPopupDialog() {
        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.popup_home, (ViewGroup) getActivity().findViewById(R.id.popup));

            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            dimBehind(popupWindow);

            final TextView timer = (TextView) layout.findViewById(R.id.alert_countdown);

            final CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    timer.setText(Long.toString(millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    Toast.makeText(getContext(), getString(R.string.alertSent), Toast.LENGTH_SHORT).show();
                    timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                    timer.setText(getString(R.string.waiting_for_help));
                    //fallDetectionService.sendSMS(contact1,username);
                }
            }.start();

            final Button close = (Button) layout.findViewById(R.id.btn_im_okay);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    popupWindow.dismiss();
                    countDownTimer.cancel();
                }
            });

            final Button sendAlert = (Button) layout.findViewById(R.id.btn_need_help);
            sendAlert.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Toast.makeText(getContext(), getString(R.string.alertSent), Toast.LENGTH_SHORT).show();
                    countDownTimer.cancel();
                    timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                    timer.setText(getString(R.string.waiting_for_help));
                    close.setVisibility(View.INVISIBLE);
                    sendAlert.setVisibility(View.INVISIBLE);
                    //fallDetectionService.sendSMS(contact1, username);
                    countDownTimer.cancel();
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
