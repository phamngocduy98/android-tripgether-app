package cf.bautroixa.tripgether.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * https://stackoverflow.com/a/5271532/9385297
 */
public class ShakePhoneHelper implements SensorEventListener {
    private static final float SHAKE_THRESHOLD = 800;
    Context context;
    SensorManager sensorMgr;
    private final Sensor mAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private int shakeCount = 0;

    OnShakeListener onShakeListener;

    public ShakePhoneHelper(Context context, OnShakeListener onShakeListener) {
        this.context = context;
        this.onShakeListener = onShakeListener;
        sensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mDelay = sensorMgr.getDefaultSensor(SensorManager.SENSOR_DELAY_GAME);
    }

    public void start(){
        sensorMgr.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop(){
        sensorMgr.unregisterListener(this, mAccelerometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[1];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    if (shakeCount++ > 3) {
                        shakeCount = 0;
                        onShakeListener.onShake();
                        Log.d("sensor", "shake");
                    }
                    Log.d("sensor", "shake detected w/ speed: " + speed);
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnShakeListener {
        void onShake();
    }
}
