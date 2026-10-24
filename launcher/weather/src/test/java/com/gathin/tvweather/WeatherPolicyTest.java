package com.gathin.tvweather;
public final class WeatherPolicyTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    @org.junit.Test public void validatesWeatherAndAirQuality() {
        check(WeatherPolicy.fresh(100, 200, 1000));
        check(!WeatherPolicy.fresh(0, 200, 1000));
        check(!WeatherPolicy.fresh(100, 50, 1000));
        check(!WeatherPolicy.fresh(100, 1100, 1000));
        check(WeatherPolicy.validTemperature(21.5));
        check(!WeatherPolicy.validTemperature(Double.NaN));
        check(!WeatherPolicy.validTemperature(999));
        check(WeatherPolicy.condition(0).equals("Clear"));
        check(WeatherPolicy.condition(63).equals("Rain"));
        check(WeatherPolicy.condition(95).equals("Thunderstorm"));
        check(WeatherPolicy.condition(45).equals("Fog"));
        check(WeatherPolicy.condition(-1).equals("Conditions unavailable"));
        check(WeatherPolicy.validAqi(0));
        check(WeatherPolicy.validAqi(1000));
        check(!WeatherPolicy.validAqi(-1));
        check(!WeatherPolicy.validAqi(1000.1));
        check(!WeatherPolicy.validAqi(Double.NaN));
        check(!WeatherPolicy.validAqi(Double.POSITIVE_INFINITY));
        check(WeatherPolicy.aqiCategory(50).equals("Good"));
        check(WeatherPolicy.aqiCategory(51).equals("Moderate"));
        check(WeatherPolicy.aqiCategory(100).equals("Moderate"));
        check(WeatherPolicy.aqiCategory(101).equals("Unhealthy for sensitive groups"));
        check(WeatherPolicy.aqiCategory(150).equals("Unhealthy for sensitive groups"));
        check(WeatherPolicy.aqiCategory(151).equals("Unhealthy"));
        check(WeatherPolicy.aqiCategory(200).equals("Unhealthy"));
        check(WeatherPolicy.aqiCategory(201).equals("Very unhealthy"));
        check(WeatherPolicy.aqiCategory(300).equals("Very unhealthy"));
        check(WeatherPolicy.aqiCategory(301).equals("Hazardous"));
        check(WeatherPolicy.aqiCategory(1000).equals("Hazardous"));
        System.out.println("Weather policy checks passed");
    }
}
