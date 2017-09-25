package jamia.mikko.fallwatch.SidebarFragments;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import jamia.mikko.fallwatch.FallDetectionClient;
import jamia.mikko.fallwatch.R;

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
    private Switch trackerSwitch;

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
                trackerSwitch.setChecked(false);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle(R.string.titleHome);

        statusOn = (ImageView) view.findViewById(R.id.status_on);
        statusOff = (ImageView) view.findViewById(R.id.status_off);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorExists()) {
            fallDetectionClient = new FallDetectionClient(sensorManager, uiHandler);
        }

        trackerSwitch = (Switch) view.findViewById(R.id.tracking_switch);
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
            Log.i("Sensor", "löyty");
            return true;
        } else {
            return false;
        }
    }

    public void showPopupDialog() {
        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_home, (ViewGroup) getActivity().findViewById(R.id.popup));

            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            dimBehind(popupWindow);

            final TextView timer = (TextView) layout.findViewById(R.id.alert_countdown);

            new CountDownTimer(60000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    timer.setText(Long.toString(millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    timer.setText(getString(R.string.alerting));
                }
            }.start();

            Button close = (Button) layout.findViewById(R.id.btn_im_okay);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    popupWindow.dismiss();
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
