package org.maksimtoropygin.meaningfulgoaltracker;

import org.junit.Test;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.DaytimeListAdapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DaytimeListAdapterTest {

    @Test
    public void testIsNumeric(){
        assertTrue(DaytimeListAdapter.isNumeric("10"));
        assertFalse(DaytimeListAdapter.isNumeric("a"));
    }

}
