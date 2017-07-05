package com.hangpy.hangpy.activity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.hangpy.hangpy.R;
import com.hangpy.hangpy.fragment.CategoryInputFragment;

/**
 * Activity for creating an event.
 */
public class CreateEventActivity extends FragmentActivity implements OnMapReadyCallback {

    private boolean use24HourFormat;
    /**
     * Default event time span, in minutes.
     */
    private static int DEFAULT_EVENT_TIME_SPAN = 60 * 7;

    /**
     * Fragment dialog.
     */
    private CategoryInputFragment categoryInputFragment;
    private String categoryInput;
    private TimePickerDialog startPickerDialog;
    private TimePickerDialog endPickerDialog;
    private int startMinutes, endMinutes;

    public CreateEventActivity() {
        use24HourFormat = shouldUse24HourClock();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_event);
        setDefaultTimeSpan();
        setupMap();
    }

    public void showCategoryInput(View v) {
        if (categoryInputFragment == null) {
            //  First time
            categoryInputFragment = new CategoryInputFragment() {
                @Override
                protected void handleInputOnDismiss(String input) {
                    categoryInput = input;
                    TextView currentCategory = (TextView) findViewById(R.id.current_category);
                    currentCategory.setText(input);
                    currentCategory.getParent().requestLayout();
                }
            };
        }
        categoryInputFragment.show(getFragmentManager(), null);
    }

    private boolean shouldUse24HourClock(){
        return true;
    }

    /**
     * Performs map setup.
     */
    private void setupMap() {
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
    }

    /**
     * Sets the default event start and end. Also initializes dialogs, so this is required.
     */
    private void setDefaultTimeSpan() {
        int minutesPerDay = 60 * 24;
        int currentMins = (int) (System.currentTimeMillis() / 60000);    //  Current mins since midnight
        while (currentMins > minutesPerDay) {
            currentMins -= minutesPerDay;
        }

        int startMinutes = (int) (30 * Math.ceil(currentMins / 30));  //  Next 30min snap
        int endMinutes = startMinutes + DEFAULT_EVENT_TIME_SPAN;

        if (endMinutes > minutesPerDay) {
            endMinutes -= minutesPerDay;
        }
        onEventTimeChanged(startMinutes, endMinutes);
    }

    /**
     * Called once the start and end time for the event has changed, and atleast once.
     *
     * @param startMin Start minutes since midnight
     * @param endMin   End minutes since midnight
     */
    private void onEventTimeChanged(int startMin, final int endMin) {
        int startHours = (int)Math.floor(startMin / 60);
        int endHours = (int)Math.floor(endMin / 60);

        //  Relative minutes
        int startRelMinutes = startMin % 60;
        int endRelMinutes = endMin % 60;

        if (startPickerDialog == null) {
            //  First call, from setDefaultTimeSpan()
            startPickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    //  We only need minutes
                    onEventTimeChanged(minute + hourOfDay * 60, endMinutes);
                }
            }, startHours, startMin % 60, use24HourFormat);

            endPickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    //  We only need minutes
                    onEventTimeChanged(startMinutes, minute + hourOfDay * 60);
                }
            }, endHours, endMin % 60, use24HourFormat);
        }

        this.startMinutes = startMin;
        this.endMinutes = endMin;

        TextView startText = (TextView) findViewById(R.id.start_text);
        TextView endText = (TextView) findViewById(R.id.end_text);

        startText.setText(getString(R.string.event_start, formatTime(startHours, startRelMinutes)));
        endText.setText(getString(R.string.event_end, formatTime(endHours, endRelMinutes)));
    }

    private String formatTime(int hours, int minutes){
        StringBuilder builder = new StringBuilder();

        if (hours < 9){
            builder.append('0');
        }
        builder.append(hours);

        builder.append(':');
        if (minutes < 9){
            builder.append('0');
        }
        builder.append(minutes);

        return builder.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
    }

    public void showStartTimeDialog(View v) {
        startPickerDialog.show();
    }

    public void showEndTimeDialog(View v) {
        endPickerDialog.show();
    }
}