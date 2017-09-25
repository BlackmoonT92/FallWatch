package jamia.mikko.fallwatch;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.mbientlab.metawear.DataToken;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.builder.filter.Comparison;
import com.mbientlab.metawear.builder.filter.ThresholdOutput;
import com.mbientlab.metawear.builder.function.Function1;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Led;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by jamiamikko on 25/09/2017.
 */

public class ExternalDetectionClient implements Runnable, ServiceConnection {

    private MetaWearBoard mwBoard;
    private Accelerometer accelerometer;
    private Context context;
    private BluetoothManager btManager;
    private Handler uiHandler;


    public ExternalDetectionClient(Context context, BluetoothManager btManager, Handler uiHandler) {
        this.context = context;
        this.btManager = btManager;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        try {
            start();
            Log.i("bind", "connected");

        } catch (Exception e) {
            Log.i("Error", e.toString());
        }
    }

    public void start() {
        Intent bindIntent = new Intent(context, BtleService.class);
        context.bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        context.unbindService(this);
        accelerometer.stop();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        BtleService.LocalBinder serviceBinder = (BtleService.LocalBinder) service;

        String mwMacAddress= "E6:F3:22:B3:2C:4E";
        BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(mwMacAddress);

        mwBoard = serviceBinder.getMetaWearBoard(btDevice);

        mwBoard.connectAsync().onSuccessTask(new Continuation<Void, Task<Route>>() {

            @Override
            public Task<Route> then(Task<Void> task) throws Exception {

                accelerometer = mwBoard.getModule(Accelerometer.class);
                accelerometer.configure()
                        .odr(25f)
                        .commit();

                return accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.map(Function1.RSS).average((byte) 4).filter(ThresholdOutput.BINARY, 0.5f)
                                .multicast()
                                .to().filter(Comparison.EQ, -1).react(new RouteComponent.Action() {
                            @Override
                            public void execute(DataToken token) {
                                Led led = mwBoard.getModule(Led.class);
                                led.editPattern(Led.Color.BLUE, Led.PatternPreset.SOLID).commit();
                                led.play();
                            }
                        }).to().filter(Comparison.EQ, 1).react(new RouteComponent.Action() {
                            @Override
                            public void execute(DataToken token) {
                                Led led = mwBoard.getModule(Led.class);
                                led.stop(true);
                            }
                        }).end();
                    }
                });

            }
        }).continueWith(new Continuation<Route, Void>() {

            @Override
            public Void then(Task<Route> task) throws Exception {
                if(task.isFaulted()) {
                    Log.i("Freefall", String.valueOf(task.getError()));
                } else {
                    Log.i("Freefall", "Great success");
                    accelerometer.acceleration().start();
                    accelerometer.start();
                }

                return null;
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

}
