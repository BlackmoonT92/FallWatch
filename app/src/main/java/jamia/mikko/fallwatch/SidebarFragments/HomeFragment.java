package jamia.mikko.fallwatch.SidebarFragments;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;

import jamia.mikko.fallwatch.FallDetector;
import jamia.mikko.fallwatch.MainSidebarActivity;
import jamia.mikko.fallwatch.R;

/**
 * Created by rrvil on 18-Sep-17.
 */

public class HomeFragment extends Fragment {

    private ImageSwitcher imgSwitcher;
    private Button onOff;
    private ImageView statusOn;
    private ImageView statusOff;
    private FallDetector fallDetector;

    public HomeFragment(){}

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();

        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.nav_home, container, false);

        getActivity().setTitle("Home");

        statusOn = (ImageView) view.findViewById(R.id.status_on);
        statusOff = (ImageView) view.findViewById(R.id.status_off);

        fallDetector = ((MainSidebarActivity) getActivity()).fallDetector;

        Switch trackerSwitch = (Switch) view.findViewById(R.id.tracking_switch);
        //final ImageView statusImg = (ImageView) view.findViewById(R.id.image_tracking_status);
        trackerSwitch.setChecked(false);

        trackerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(isChecked) {
                    Log.i("DEBUG", "Switch on");
                    ((Animatable) statusOn.getDrawable()).start();
                    statusOff.setVisibility(View.INVISIBLE);
                    statusOn.setVisibility(View.VISIBLE);
                    fallDetector.start();
                }else {
                    statusOff.getDrawable();
                    Log.i("DEBUG", "Switch off");
                    statusOn.setVisibility(View.INVISIBLE);
                    statusOff.setVisibility(View.VISIBLE);
                    fallDetector.stop();
                }
            }
        });

        return view;
    }

}
