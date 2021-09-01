package org.maksimtoropygin.meaningfulgoaltracker.adapters.data;

public class Time {
    int minute, hour;

    public Time(int hour, int minute) {
        this.minute = minute;
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public String timeToString() {
        String hour;
        if (this.hour>9)
            hour = ""+this.hour;
        else
            hour="0"+this.hour;
        String minute;
        if (this.minute>9)
            minute = ""+this.minute;
        else
            minute="0"+this.minute;
        return hour + ":" + minute;
    }

    public void setTimeFromString(String time) {
        String[] parts = time.split(":");
        this.hour = Integer.parseInt(parts[0]);
        this.minute = Integer.parseInt(parts[1]);
    }

    public boolean greaterThan(Time time) {
        if (hour>time.getHour())
            return true;
        else if (hour==time.getHour())
            if (minute>time.getMinute())
                return true;

        return false;
    }
}
