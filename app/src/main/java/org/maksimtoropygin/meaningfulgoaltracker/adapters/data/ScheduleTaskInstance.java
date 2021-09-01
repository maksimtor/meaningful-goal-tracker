package org.maksimtoropygin.meaningfulgoaltracker.adapters.data;

import android.util.Log;

public class ScheduleTaskInstance implements Comparable<ScheduleTaskInstance>{
    String scheduleTaskId, title, desc, goal;
    Time start, finish;
    int day, eventId;

    public ScheduleTaskInstance(String scheduleTaskId, String title, Time start, Time finish, int day, String desc, String goal, int eventId) {
        this.scheduleTaskId = scheduleTaskId;
        this.title = title;
        this.start = start;
        this.finish = finish;
        this.day = day;
        this.desc = desc;
        this.goal = goal;
        this.eventId = eventId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getScheduleTaskId() {
        return scheduleTaskId;
    }

    public void setScheduleTaskId(String scheduleTaskId) {
        this.scheduleTaskId = scheduleTaskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Time getStart() {
        return start;
    }

    public void setStart(Time start) {
        this.start = start;
    }

    public Time getFinish() {
        return finish;
    }

    public void setFinish(Time finish) {
        this.finish = finish;
    }

    public void setDay(int Day) {
        this.day = day;
    }

    public int getDay() {return day;}

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    @Override
    public int compareTo(ScheduleTaskInstance otherTask) {
        int startHour = start.getHour();
        int startMinute = start.getMinute();
        int otherStartHour = otherTask.getStart().getHour();
        int otherStartMinute = otherTask.getStart().getMinute();
        Log.d("Sorting", "Here " + startHour + " and " + otherStartHour);
        int result = 0;
        if (startHour>otherStartHour){
            result = 1;
        }
        else if (startHour<otherStartHour) {
            result = -1;
        }
        else {
            if (startMinute > otherStartMinute) {
                result = 1;
            }
            else if (startMinute < otherStartMinute) {
                result = -1;
            }
        }
        Log.d("Sorting", result+"");
        return result;
    }
}
