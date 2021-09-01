package org.maksimtoropygin.meaningfulgoaltracker.adapters.data;

import java.util.Date;

public class ScheduleEvent {
    Date date;
    String status;

    public ScheduleEvent(Date date, String status) {
        this.date = date;
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
