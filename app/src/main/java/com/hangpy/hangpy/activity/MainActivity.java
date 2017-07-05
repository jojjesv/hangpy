package com.hangpy.hangpy.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.hangpy.hangpy.R;
import com.hangpy.hangpy.events.EventData;
import com.hangpy.hangpy.events.EventsListAdapter;
import com.hangpy.hangpy.events.filters.DefaultFilterAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    /**
     * Whether to skip fetching frontpage events and simulate them instead.
     */
    private static final boolean SIMULATE_FRONTPAGE_EVENTS = true;

    private ListView eventFilterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout();

        fetchFrontpageEvents();
    }

    /**
     * Performs setup from the layout aspect.
     */
    private void setupLayout(){
        setContentView(R.layout.frontpage);

        //  Add event filter buttons
        eventFilterList = (ListView)findViewById(R.id.event_filters);
        eventFilterList.setAdapter(new DefaultFilterAdapter(this));
    }

    /**
     * Starts fetching events for the frontpage.
     */
    private void fetchFrontpageEvents() {
        if (SIMULATE_FRONTPAGE_EVENTS) {
            //  For debugging purposes
            try {
                //  Read from raw file
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                getResources().openRawResource(R.raw.simulated_frontpage_events))
                );
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    json.append(line);
                }

                JSONArray array = new JSONArray(json.toString());
                onFetchedFrontpageEvents(array);
            } catch (Exception ex) {

            }
        }

        //  TODO: Change font
    }

    /**
     * Shows or hides the list of filter options.
     */
    public void toggleFilterList(View v){
        boolean isListHidden = eventFilterList.getVisibility() != View.VISIBLE;

        if (isListHidden){
            eventFilterList.setVisibility(View.VISIBLE);
        } else {
            eventFilterList.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the screen which prompts the user to choose a category to further explore.
     */
    public void showExploreCatergories(View v){
    }

    /**
     * Starts the intent for creating a new event.
     * @param v
     */
    public void showCreateEvent(@Nullable View v){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(this, CreateEventActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //  Setup default context menu
        getMenuInflater().inflate(R.menu.action_bar_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called once the frontpage events have been fetched.
     */
    private void onFetchedFrontpageEvents(JSONArray events) {
        int eventsLength = events.length();
        EventData[] data = new EventData[eventsLength];

        JSONObject event;
        try {
            for (int i = 0; i < eventsLength; i++) {
                event = events.getJSONObject(i);

                data[i] = new EventData(
                        event.getString("name"),
                        event.getInt("attendance_count"),
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)
                );
            }
        } catch (JSONException ex) {

        }

        EventsListAdapter frontpageAdapter = new EventsListAdapter(this, data);

        ListView frontpageList = (ListView)findViewById(R.id.events_list_main);
        frontpageList.setAdapter(frontpageAdapter);
    }
}
