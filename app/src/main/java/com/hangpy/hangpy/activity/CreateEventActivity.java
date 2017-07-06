package com.hangpy.hangpy.activity;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hangpy.hangpy.R;
import com.hangpy.hangpy.backend.ResponseCodes;
import com.hangpy.hangpy.fragment.CategoryInputFragment;
import com.hangpy.hangpy.location.LocationAdapter;
import com.hangpy.hangpy.backend.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Activity for creating an event.
 */
public class CreateEventActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1657;
    private static final int READ_EXT_PERMISSION_REQUEST = 2252;
    private static final int IMAGE_CAPTURE_REQUEST = 1758;

    private boolean use24HourFormat;
    /**
     * Default event time span, in minutes.
     */
    private static int DEFAULT_EVENT_TIME_SPAN = 60 * 7;

    /**
     * Fragment dialog.
     */
    private CategoryInputFragment categoryInputFragment;
    private String eventCategory;
    private TimePickerDialog startPickerDialog;
    private TimePickerDialog endPickerDialog;
    private int startMinutes, endMinutes;
    private double eventLat, eventLong;
    private GoogleMap map;
    private ArrayList<Bitmap> eventGallery = new ArrayList<>();
    private File galleryImageFile;
    private Uri galleryImageFileUri;

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
                    eventCategory = input;
                    TextView currentCategory = (TextView) findViewById(R.id.current_category);
                    currentCategory.setText(input);
                    currentCategory.getParent().requestLayout();
                }
            };
        }
        categoryInputFragment.show(getFragmentManager(), null);
    }

    private boolean shouldUse24HourClock() {
        return true;
    }

    /**
     * Performs map setup.
     */
    private void setupMap() {
        LinearLayout container = (LinearLayout) findViewById(R.id.map_container);
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
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
        int startHours = (int) Math.floor(startMin / 60);
        int endHours = (int) Math.floor(endMin / 60);

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

        //  Update time delta

        int deltaMinutes = endMin - startMin;
        int relativeMinutes = deltaMinutes % 60;
        int deltaHours = (int) Math.ceil(deltaMinutes / 60);

        boolean includeMinutes = relativeMinutes > 0;
        boolean approximateDuration = includeMinutes && deltaMinutes % 30 > 0;
        boolean roundToHalfHour = Math.abs(relativeMinutes - 30) < 15;

        if (approximateDuration && !roundToHalfHour) {
            //  Minutes won't be shown
            includeMinutes = false;
            if (relativeMinutes > 30) {
                //  Round to next hour
                deltaHours++;
            }
            relativeMinutes = 0;
        }

        int durationResId = approximateDuration ?
                includeMinutes ? R.string.event_duration_minutes_approx : R.string.event_duration_approx :
                includeMinutes ? R.string.event_duration_minutes : R.string.event_duration;

        Resources res = getResources();

        //  Args for string res
        Object[] durationArgs = new Object[includeMinutes ? 4 : 2];
        durationArgs[0] = deltaHours;
        durationArgs[1] = res.getQuantityString(R.plurals.hours, deltaHours);

        if (includeMinutes) {
            durationArgs[2] = 30;   //  approximate: 30 or 0
            durationArgs[3] = res.getQuantityString(R.plurals.minutes, deltaMinutes);
        }

        ((TextView) findViewById(R.id.event_duration)).setText(getString(durationResId, durationArgs));
    }

    private String formatTime(int hours, int minutes) {
        StringBuilder builder = new StringBuilder();

        if (hours < 9) {
            builder.append('0');
        }
        builder.append(hours);

        builder.append(':');
        if (minutes < 9) {
            builder.append('0');
        }
        builder.append(minutes);

        return builder.toString();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;

        //  Initial zoom
        map.setMinZoomPreference(2);

        requestCurrentLocation();
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            Marker mainMarker;

            @Override
            public void onMapClick(LatLng latLng) {
                eventLat = latLng.latitude;
                eventLong = latLng.longitude;

                if (mainMarker == null) {
                    //  First call
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng);
                    mainMarker = googleMap.addMarker(markerOptions);
                } else {
                    mainMarker.setPosition(latLng);
                }
            }
        });

        //  Fix scrolling issue
        final ScrollView rootScroll = (ScrollView) findViewById(R.id.scroll_root);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                //  Prevent root scrollview from scrolling
                rootScroll.requestDisallowInterceptTouchEvent(true);
            }
        });
    }

    /**
     * Begins an intent for taking an image for the event.
     */
    public void dispatchGalleryIntent(View v) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, READ_EXT_PERMISSION_REQUEST);
            //  Required
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {

            try {
                //  https://stackoverflow.com/questions/6448856/android-camera-intent-how-to-get-full-sized-photo
                //  Need to temporarily save file, otherwise thumbnail is returned
                galleryImageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "gallery.jpg");
                galleryImageFile.delete();
            } catch (Exception ex) {
                //  No SD card?
                //  TODO: Handle error
                return;
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, galleryImageFileUri = Uri.fromFile(galleryImageFile));

            startActivityForResult(intent, IMAGE_CAPTURE_REQUEST);
        }
    }

    private void onGalleryImageAdded(Bitmap image) {
        eventGallery.add(image);

        ImageView thumbnailView = new ImageView(this);
        thumbnailView.setImageBitmap(image);

        Resources res = getResources();
        int w = res.getDimensionPixelSize(R.dimen.gallery_width_create);
        int h = res.getDimensionPixelSize(R.dimen.gallery_height_create);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);

        LinearLayout galleryView = (LinearLayout) findViewById(R.id.gallery);
        galleryView.addView(thumbnailView, 0, params);

        galleryView.requestLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        boolean ok = resultCode == RESULT_OK;

        switch (requestCode) {
            case IMAGE_CAPTURE_REQUEST:
                if (ok) {
                    try {
                        File gallery = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "gallery.jpg");
                        Bitmap resultImage = BitmapFactory.decodeFile(gallery.toString());
                        onGalleryImageAdded(resultImage);

                        return;
                    } catch (Exception ex) {
                        //  Unexpected
                        //  TODO: Handle error
                        return;
                    }
                }
                break;
        }
    }

    /**
     * Submits the first piece of entered data, and creates the event.
     *
     * @param v
     */
    public void doInitialSubmit(@Nullable View v) {
        if (true){
            submitGallery();
            return;
        }
        double lat = eventLat, lng = eventLong;
        String category = eventCategory;
        CharSequence eventName = ((EditText) findViewById(android.R.id.text1)).getText();

        StringBuilder postBuilder = new StringBuilder();
        postBuilder.append("lat=").append(lat).append("&long=").append(lng)
                .append("&name=").append(User.encode(eventName.toString()))
                .append("&category=").append(User.encode(category));

        appendGalleryAsPostData(postBuilder);

        User.request("action=create_event", postBuilder.toString(), new User.RequestCallback() {
            @Override
            public void onResponse(String response) {
                parseSubmitResponse(response);
            }

            @Override
            public void onFailed() {
                onResponse("" + ResponseCodes.FAILED);
            }
        });
    }

    /**
     * Performs next submission step; submitting the gallery.
     */
    public void submitGallery() {
        Bitmap image;
        byte[] imageBytes;
        ByteArrayOutputStream stream;
        for (int i = 0, n = eventGallery.size(); i < n; i++) {
            image = eventGallery.get(i);
            stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            imageBytes = stream.toByteArray();
            User.request("action=append_gallery", imageBytes, "jpg", new User.RequestCallback() {
                @Override
                public void onResponse(String response) {

                }

                @Override
                public void onFailed() {

                }
            });
        }


    }

    /**
     * Appends each gallery item as post data to a string builder.
     *
     * @param postBuilder
     */
    private void appendGalleryAsPostData(StringBuilder postBuilder) {
    }

    private void parseSubmitResponse(String response) {

    }

    /**
     * Requests the current user location and positions
     */
    private void requestCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST);
        } else {
            //  Is granted
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 12));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //  Granted
                requestCurrentLocation();
            }
        } else if (requestCode == READ_EXT_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //  Granted
                dispatchGalleryIntent(null);
            }
        }
    }

    public void showStartTimeDialog(View v) {
        startPickerDialog.show();
    }

    public void showEndTimeDialog(View v) {
        endPickerDialog.show();
    }
}