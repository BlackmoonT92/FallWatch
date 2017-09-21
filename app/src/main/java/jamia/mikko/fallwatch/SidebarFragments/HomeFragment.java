package jamia.mikko.fallwatch.SidebarFragments;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;

import jamia.mikko.fallwatch.FallDetectionClient;
import jamia.mikko.fallwatch.MainSidebarActivity;
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

    public HomeFragment(){}

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();

        return homeFragment;
    }

    private Handler uiHandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            if (msg.what == 0) {
                Log.i("Sensor", String.valueOf(msg.obj));
                showPopupDialog();
                fallDetectionClient.stop();
                fallDetectionClient.resetValues();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle("Home");

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
                    Log.i("DEBUG", "Switch on");
                    ((Animatable) statusOn.getDrawable()).start();
                    statusOff.setVisibility(View.INVISIBLE);
                    statusOn.setVisibility(View.VISIBLE);

                    t = new Thread(fallDetectionClient);
                    t.start();

                }else {
                    statusOff.getDrawable();
                    Log.i("DEBUG", "Switch off");
                    statusOn.setVisibility(View.INVISIBLE);
                    statusOff.setVisibility(View.VISIBLE);

                    fallDetectionClient.stop();
                    fallDetectionClient.resetValues();
                }
            }
        });

        return view;
    }

    public Boolean sensorExists() {
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            return true;
        } else {
            return false;
        }
    }

    public void showPopupDialog() {
        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_home, (ViewGroup) getActivity().findViewById(R.id.popup));

            popupWindow = new PopupWindow(layout, 300, 370, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            Button close = (Button) getActivity().findViewById(R.id.close_popup);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
