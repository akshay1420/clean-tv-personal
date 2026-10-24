package com.gathin.tvweather;
import org.junit.Test;
import static org.junit.Assert.*;
public class WeatherLocationTest {
    @Test public void supportsEitherHemisphereAndTrimsCity() {
        WeatherLocation location=new WeatherLocation("  Example  ",-33.8,151.2);
        assertEquals("Example",location.city);
        assertEquals(-33.8,location.latitude,0);
    }
    @Test public void rejectsMissingOrNonFiniteAreas() {
        bad("",0,0); bad(" ",0,0); bad(null,0,0);
        bad("Example",Double.NaN,0); bad("Example",0,Double.POSITIVE_INFINITY);
        bad("Example",90.1,0); bad("Example",0,-180.1);
    }
    private void bad(String city,double lat,double lon) {
        try { new WeatherLocation(city,lat,lon); fail("Invalid area accepted"); }
        catch(IllegalArgumentException expected) { }
    }
}
