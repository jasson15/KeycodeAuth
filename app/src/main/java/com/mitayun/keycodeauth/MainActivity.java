package com.mitayun.keycodeauth;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.content.pm.ActivityInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {

    private static final String TAG = "KEYCODE_AUTH";

    private static final String POSTFIX_ACC = "_acc";
    private static final String POSTFIX_GYRO = "_gyro";
    private static final String POSTFIX_POS = "_pos";
    private static final String POSTFIX_ALL = "_all";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyro;

    private boolean started = false;

    private List<String> accValues = new ArrayList<>();
    private List<String> gyroValues = new ArrayList<>();
    private List<String> positionValues = new ArrayList<>();
    private List<String> allValues = new ArrayList<>();

    private EditText fileNameEditText;
    private ToggleButton isRightToggleButton;
    private ToggleButton isTableToggleButton;
    private ToggleButton isThumbToggleButton;
    private ToggleButton isNailToggleButton;
    private View targetView;
    private View boundView;

    private int count = 0;
    private String previousFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileNameEditText = (EditText) findViewById(R.id.filename);
        isRightToggleButton = (ToggleButton) findViewById(R.id.isRightHand);
        isTableToggleButton = (ToggleButton) findViewById(R.id.isTable);
        isThumbToggleButton = (ToggleButton) findViewById(R.id.isThumb);
        isNailToggleButton = (ToggleButton) findViewById(R.id.isThumb);
        targetView = findViewById(R.id.targetView);
        boundView = findViewById(R.id.boundView);
        boundView.setOnTouchListener(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void startRecording(View view) {
        if (fileNameEditText.getText() != null && fileNameEditText.getText().length() > 0) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);

            started = true;
        }
    }

    public void stopRecording(View view) {
        // unregister
        sensorManager.unregisterListener(this);
        started = false;

        // write arraylist to file
        String fileName = fileNameEditText.getText().toString() + "_" +
                (isRightToggleButton.isChecked() ? "r" : "l") +
                (isTableToggleButton.isChecked() ? "d" : "h") +
                (isNailToggleButton.isChecked() ? "n" : "f") +
                (isThumbToggleButton.isChecked() ? "t" : "i");


        if (fileName.equals(previousFileName)) {
            count++;
        } else {
            previousFileName = fileName;
        }
        fileName = fileName + count;

        int count1 = 0;
        int l = accValues.size();
        int j = 0;
        int k = 0;
        List<String> temp = new ArrayList<>();
        for (int i = 0; i < l; i++){
            if (accValues.get(i)=="tag"){
                if (k == 0) {
                    k = i;
                    continue;
                }else{
                    temp.clear();
                    for (int p = j; p < (k + i) / 2; p ++){
                        if (accValues.get(p)!="tag")
                            temp.add(accValues.get(p));
                    }
                    writeToFile(fileName + count1 + POSTFIX_ACC, temp);
                    j = (k + i) / 2;
                    k = i;
                    count1 ++;
                }
            }
        }
        if (temp.size()!=0){
            temp.clear();
            for (int p = j; p < l; p ++){
                if (accValues.get(p)!="tag")
                    temp.add(accValues.get(p));
            }
            writeToFile(fileName + count1 + POSTFIX_ACC, temp);
        }

        count1 = 0;
        j = 0;
        k = 0;
        l = gyroValues.size();
        temp.clear();
        for (int i = 0; i < l; i++){
            if (gyroValues.get(i)=="tag"){
                if (k == 0) {
                    k = i;
                    continue;
                }else{
                    temp.clear();
                    for (int p = j; p < (k + i) / 2; p ++){
                        if (gyroValues.get(p)!="tag")
                            temp.add(gyroValues.get(p));
                    }
                    writeToFile(fileName + count1 + POSTFIX_GYRO, temp);
                    j = (k + i) / 2;
                    k = i;
                    count1 ++;
                }
            }
        }
        if (temp.size()!=0){
            temp.clear();
            for (int p = j; p < l; p ++){
                if (gyroValues.get(p)!="tag")
                    temp.add(gyroValues.get(p));
            }
            writeToFile(fileName + count1 + POSTFIX_GYRO, temp);
        }


        writeToFile(fileName + POSTFIX_ACC, accValues);
        writeToFile(fileName + POSTFIX_GYRO, gyroValues);
        writeToFile(fileName + POSTFIX_POS, positionValues);
        writeToFile(fileName + POSTFIX_ALL, allValues);

        // clear arraylist
        accValues.clear();
        gyroValues.clear();
        positionValues.clear();
        allValues.clear();

        // clear filename edittext
//        fileNameEditText.setText("");
    }

    private void writeToFile(String fileName, List<String> listToWrite) {
        File sdCard = Environment.getExternalStorageDirectory();
        File file = new File(sdCard.getAbsolutePath(), fileName);
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            for (String s : listToWrite) {
                fileOutputStream.write(s.getBytes());
            }
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected Point getRelativePosition(View v, MotionEvent event) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        float screenX = event.getRawX();
        float screenY = event.getRawY();
        float viewX = screenX - location[0] - v.getWidth()/2;
        float viewY = screenY - location[1] - v.getHeight()/2;
        return new Point((int) viewX, (int) viewY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        List<String> targetList = null;
        String typeString = "";
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                targetList = accValues;
                typeString = "acc";
                break;
            case Sensor.TYPE_GYROSCOPE:
                targetList = gyroValues;
                typeString = "gyro";
                break;
        }
        if (targetList != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String result = typeString + "," + event.timestamp / 1000000 + "," + x + "," + y + "," + z + "\n";
            //Log.d(TAG, "Sensor " + event.sensor.getType() + ": " + result);
            targetList.add(result);
            allValues.add(result);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: " + event.getAction());
        if (event.getAction() == MotionEvent.ACTION_DOWN && started) {
            Point p = getRelativePosition(boundView, event);
            String result = "pos," + event.getEventTime() + "," + p.x + "," + p.y + "\n";
            String tag = "tag";
            Log.d(TAG, "onTouch: " + result);
            positionValues.add(result);
            allValues.add(result);
            accValues.add(tag);
            gyroValues.add(tag);
            Log.d(TAG, "totalPos: " + positionValues.size());
        }
        return false;
    }
}
