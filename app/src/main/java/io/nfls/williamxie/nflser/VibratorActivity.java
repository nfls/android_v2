package io.nfls.williamxie.nflser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

public class VibratorActivity extends AppCompatActivity {

    private TextView mode_spinner_hint;
    private Spinner mode_spinner;
    private TextView time_picker_hint;
    private NumberPicker time_picker;
    private TextView amplitude_picker_hint;
    private NumberPicker amplitude_picker;
    private Button vibrate_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibrator);
        mode_spinner_hint = (TextView) findViewById(R.id.mode_spinner_hint);
        mode_spinner = (Spinner) findViewById(R.id.mode_spinner);
        time_picker_hint = (TextView) findViewById(R.id.time_picker_hint);
        time_picker = (NumberPicker) findViewById(R.id.time_picker);
        amplitude_picker_hint = (TextView) findViewById(R.id.amplitude_picker_hint);
        amplitude_picker = (NumberPicker) findViewById(R.id.amplitude_picker);
        vibrate_button = (Button) findViewById(R.id.vibrate_button);
    }
}
