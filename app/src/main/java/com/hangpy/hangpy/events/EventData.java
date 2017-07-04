package com.hangpy.hangpy.events;

import android.graphics.Bitmap;

/**
 * Structure for basic presentational event date.
 * Created by Johan on 2017-07-04.
 */
public class EventData {
    private String eventName;
    private int attendanceCount;
    private Bitmap coverImage;

    public EventData(String eventName, int attendanceCount, Bitmap coverImage) {
        this.eventName = eventName;
        this.attendanceCount = attendanceCount;
        this.coverImage = coverImage;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getAttendanceCount() {
        return attendanceCount;
    }

    public void setAttendanceCount(int attendanceCount) {
        this.attendanceCount = attendanceCount;
    }

    public Bitmap getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Bitmap coverImage) {
        this.coverImage = coverImage;
    }
}
