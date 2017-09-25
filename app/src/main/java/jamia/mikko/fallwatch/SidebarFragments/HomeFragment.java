package jamia.mikko.fallwatch.SidebarFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
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

import jamia.mikko.fallwatch.FallDetectionClient;
import jamia.mikko.fallwatch.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class HomeFragment extends Fragment {

    private ImageView statusOn;
    private ImageView statusOff;
    private Thread t;
    public FallDetectionClient fallDetectionClient;
    private SensorManager sensorManager;
    private PopupWindow popupWindow;
    private SmsManager smsManager = SmsManager.getDefault();
    public static final String USER_PREFERENCES = "UserPreferences";
    private String username;
    private String contact1;

    public HomeFragment(){

    }

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();

        return homeFragment;
    }

    private Handler uiHandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            if (msg.what == 0) {
                showPopupDialog();
                fallDetectionClient.stop();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle(R.string.titleHome);

        SharedPreferences prefs = getActivity().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);

        username = prefs.getString("username", null);
        contact1 = prefs.getString("contact1", null);

        statusOn = (ImageView) view.findViewById(R.id.status_on);
        statusOff = (ImageView) view.findViewById(R.id.status_off);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorExists()) {
            fallDetectionClient = new FallDetectionClient(sensorManager, uiHandler);
        }

        Switch trackerSwitch = (Switch) view.findViewById(R.id.tracking_switch);
        trackerSwitch.setChecked(false);

        trackerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(isChecked) {
                    ((Animatable) statusOn.getDrawable()).start();
                    statusOff.setVisibility(View.INVISIBLE);
                    statusOn.setVisibility(View.VISIBLE);

                    t = new Thread(fallDetectionClient);
                    t.start();

                }else {
                    statusOff.getDrawable();
                    statusOn.setVisibility(View.INVISIBLE);
                    statusOff.setVisibility(View.VISIBLE);

                    fallDetectionClient.stop();
                }
            }
        });

        return view;
    }

    public Boolean sensorExists() {
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
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
                    smsManager.sendTextMessage(contact1, null, username + " needs help", null, null);
                    Toast.makeText(getContext(), "Alert sent!", Toast.LENGTH_SHORT).show();
                    timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                    timer.setText(getString(R.string.waiting_for_help));
                }
            }.start();

            Button close = (Button) layout.findViewById(R.id.btn_im_okay);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });

            Button sendAlert = (Button) layout.findViewById(R.id.btn_need_help);
            sendAlert.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    smsManager.sendTextMessage(contact1, null, username + " needs help", null, null);
                    Toast.makeText(getContext(), "Alert sent!", Toast.LENGTH_SHORT).show();
                    countDownTimer.cancel();
                    timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                    timer.setText(getString(R.string.waiting_for_help));
                }
            });

        }catch (Exception e) {
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
