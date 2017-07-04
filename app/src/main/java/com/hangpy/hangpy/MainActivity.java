package com.hangpy.hangpy;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.hangpy.hangpy.events.EventData;
import com.hangpy.hangpy.events.EventsListAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frontpage);
        fetchFrontpageEvents();
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
    }

    private void setupActionBar(){

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
