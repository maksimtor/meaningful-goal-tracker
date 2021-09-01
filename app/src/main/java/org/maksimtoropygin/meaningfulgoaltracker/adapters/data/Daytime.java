package org.maksimtoropygin.meaningfulgoaltracker.adapters.data;

public class Daytime {
    String day;
    Time start, finish;

    public Daytime(String day, Time start, Time finish) {
        this.day = day;
        this.start = start;
        this.finish = finish;
    }

    public Daytime(int day, Time start, Time finish) {
        this.day = intDayToString(day);
        this.start = start;
        this.finish = finish;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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

    public int dayToInt() {
        switch (day){
            case "Monday":
                return 0;
            case "Tuesday":
                return 1;
            case "Wednesday":
                return 2;
            case "Thursday":
                return 3;
            case "Friday":
                return 4;
            case "Saturday":
                return 5;
            case "Sunday":
                return 6;
            default:
                return 7;
        }
    }

    public String intDayToString(int day) {
        switch (day){
            case 0:
                return "Monday";
            case 1:
                return "Tuesday";
            case 2:
                return "Wednesday";
            case 3:
                return "Thursday";
            case 4:
                return "Friday";
            case 5:
                return "Saturday";
            case 6:
                return "Sunday";
            default:
                return "Error";
        }
    }
}
