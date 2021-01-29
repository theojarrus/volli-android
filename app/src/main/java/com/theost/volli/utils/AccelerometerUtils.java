package com.theost.volli.utils;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerUtils {

    public static final int MOVEMENT_NONE = 0;
    public static final int MOVEMENT_PITCH_TOP = 1;
    public static final int MOVEMENT_PITCH_BOTTOM = 3;
    public static final int MOVEMENT_ROLL_RIGHT = 2;
    public static final int MOVEMENT_ROLL_LEFT = 4;

    public static void addAccelerometerListener(SensorEventListener sensorListener, SensorManager sensorManager) {
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static void removeAccelerometerListener(SensorEventListener sensorListener, SensorManager sensorManager) {
        sensorManager.unregisterListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    public static int[] getOrientationData(float[] g) {
        double gNorm = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);
        g[0] = (float) (g[0] / gNorm);
        g[1] = (float) (g[1] / gNorm);
        g[2] = (float) (g[2] / gNorm);
        int pitch = (int) Math.round(Math.toDegrees(Math.acos(g[2])));
        int roll = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[2])));
        int azimuth = (int) Math.round(Math.toDegrees(-Math.atan2(g[0], g[1])));
        return new int[]{pitch, roll, azimuth};
    }

    public static int getMovement(int[] orientationData, int[] orientationCacheData, int angle) {
        if (orientationCacheData != null && orientationCacheData.length > 1) {
            int pitchDifference = orientationData[0] - orientationCacheData[0];
            if (pitchDifference > 10 && orientationData[1] - Math.abs(orientationCacheData[0]) >= angle) {
                return MOVEMENT_PITCH_TOP;
            } else if (pitchDifference > 10 && pitchDifference < 40 && Math.abs(orientationData[1]) < 20) {
                return MOVEMENT_PITCH_BOTTOM;
            } else if (orientationData[0] < 90 && Math.abs(orientationData[1]) >= angle
                    && Math.abs(orientationData[1]) - Math.abs(orientationCacheData[1]) > 10) {
                if (orientationData[1] > 0) {
                    return MOVEMENT_ROLL_RIGHT;
                } else {
                    return MOVEMENT_ROLL_LEFT;
                }
            }
        }
        return MOVEMENT_NONE;
    }

}
