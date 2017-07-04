package com.hangpy.hangpy.events;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hangpy.hangpy.R;

/**
 * List adapter for the list of event present at the front page.
 * Created by Johan on 2017-07-04.
 */
public class EventsListAdapter extends ArrayAdapter<EventData> {

    //  List item views are cached and used in getView()
    private static TextView eventNameView;
    private static TextView attendanceCountView;

    public EventsListAdapter(Context context, EventData[] objects) {
        //  "int resource" already handled thus not needed
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = getContext();
        EventData dataAtPosition = getItem(position);

        if (convertView == null){
            //  First call
            convertView = LayoutInflater.from(context).inflate(R.layout.frontpage_entry, parent, false);
            eventNameView = (TextView)convertView.findViewById(R.id.entry_name);
            attendanceCountView = (TextView)convertView.findViewById(R.id.entry_attendance_count);
        }

        TextView eventName = eventNameView;
        eventName.setText(dataAtPosition.getEventName());

        TextView attendanceCount = attendanceCountView;
        attendanceCount.setText("" + dataAtPosition.getAttendanceCount());

        convertView.setBackground(new BitmapDrawable(context.getResources(), dataAtPosition.getCoverImage()));

        return convertView;
    }
}
