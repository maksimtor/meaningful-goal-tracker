package org.maksimtoropygin.meaningfulgoaltracker;

import org.junit.Before;
import org.junit.Test;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Daytime;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Time;

import static org.junit.Assert.assertEquals;

public class DaytimeTest {
    private Time start = new Time(0,0);
    private Time finish = new Time(0,0);
    private Daytime daytime = new Daytime("None", null, null);

    @Test
    public void testDayToInt() {
        daytime.setDay("Monday");
        assertEquals(daytime.dayToInt(), 0);

        daytime.setDay("Wednesday");
        assertEquals(daytime.dayToInt(), 2);

        daytime.setDay("Something else");
        assertEquals(daytime.dayToInt(), 7);
    }

    @Test
    public void testIntDayToString() {
        assertEquals(daytime.intDayToString(0), "Monday");
        assertEquals(daytime.intDayToString(2), "Wednesday");
        assertEquals(daytime.intDayToString(6), "Sunday");
        assertEquals(daytime.intDayToString(123), "Error");
    }
}
