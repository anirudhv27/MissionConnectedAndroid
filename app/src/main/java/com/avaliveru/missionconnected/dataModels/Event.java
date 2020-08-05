package com.avaliveru.missionconnected.dataModels;

import java.util.Date;

public class Event implements Comparable<Event>{

    public String eventName;
    public String eventClub;
    public String eventDescription;
    public String eventImageURL;
    public String eventID;
    public String eventPreview;
    public Date eventDate;
    public int numberOfAttendees;

    @Override
    public int compareTo(Event event) {
        if (eventID.equals(event.eventID)) return 0;
        else if (eventDate.equals(event.eventDate)){
            return eventName.compareTo(event.eventName);
        } else {
            return event.eventDate.compareTo(eventDate);
        }
    }
}
