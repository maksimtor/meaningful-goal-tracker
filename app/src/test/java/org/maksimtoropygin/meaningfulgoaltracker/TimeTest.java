package org.maksimtoropygin.meaningfulgoaltracker;

import org.junit.Test;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeTest {
    private Time time = new Time(0,0);

    @Test
    public void testTimeToString() {
        time.setHour(5);
        time.setMinute(10);
        assertEquals(time.timeToString(), "05:10");

        time.setHour(23);
        time.setMinute(0);
        assertEquals(time.timeToString(), "23:00");
    }

    @Test
    public void testSetTimeFromString() {
        time.setTimeFromString("05:10");
        assertEquals(time.getHour(),5);
        assertEquals(time.getMinute(),10);

        time.setTimeFromString("23:05");
        assertEquals(time.getHour(),23);
        assertEquals(time.getMinute(),5);
    }

    @Test
    public void testGreaterThan() {
        time.setHour(10);
        time.setMinute(5);
        assertFalse(time.greaterThan(new Time(10,6)));
        assertFalse(time.greaterThan(new Time(10,5)));
        assertTrue(time.greaterThan(new Time(9,6)));
    }
}
