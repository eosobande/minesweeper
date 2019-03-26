package com.minesweeper.kuro.minesweeper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private MineField mMineField;
    protected TextView marked, endText;
    private Button action;
    public ArrayAdapter<String> mAdapter;
    protected SquareGridView mField;
    private double[] gravity = new double[3], linear_acceleration = new double[3];
    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMineField = new MineField(this);

        TextView mines = findViewById(R.id.mines);
        mines.setText(getString(R.string.total_mines, mMineField.NO_OF_MINES));
        marked = findViewById(R.id.marked);
        marked.setText(getString(R.string.total_marked, mMineField.MARKED_MINES));
        endText = findViewById(R.id.end);

        Button reset = findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });

        action = findViewById(R.id.action);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mMineField.OVER) {
                    mMineField.isUncover = !mMineField.isUncover;
                    action.setText(mMineField.isUncover ? R.string.uncover : R.string.mark);
                }
            }
        });

        mAdapter = new ArrayAdapter<String>(this, R.layout.cell, R.id.cell, mMineField.mSurroundings) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View returnView = super.getView(position, convertView, parent);
                returnView.setMinimumHeight(mField.getColumnWidth());
                return returnView;
            }
        };

        mField = findViewById(R.id.mineField);
        mField.setAdapter(mAdapter);
        mField.setOnItemClickListener(mMineField);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensor != null) {
            mSensorManager.unregisterListener(this, mSensor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager = null;
    }

    private void resetGame() {
        mMineField.reset();
        mAdapter.notifyDataSetChanged();
        action.setText(R.string.uncover);
        marked.setText(getString(R.string.total_marked, 0));
        if (!mMineField.OVER) {
            endText.setText("");
        }
        Toast.makeText(this, R.string.game_reset, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        /**
         * obtained from https://developer.android.com/reference/android/hardware/SensorEvent.html
         *
         * In order to measure the real acceleration of the device,
         * the contribution of the force of gravity must be eliminated.
         * This can be achieved by applying a high-pass filter.
         * Conversely, a low-pass filter can be used to isolate the force of gravity.
         *
         */

        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final double alpha = 0.8;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

        linear_acceleration[0] = Math.pow(sensorEvent.values[0] - gravity[0], 2);
        linear_acceleration[1] = Math.pow(sensorEvent.values[1] - gravity[1], 2);
        linear_acceleration[2] = Math.pow(sensorEvent.values[2] - gravity[2], 2);

        if (Math.sqrt(linear_acceleration[0] + linear_acceleration[1] + linear_acceleration[2]) >= 9.80665) {
            resetGame();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
