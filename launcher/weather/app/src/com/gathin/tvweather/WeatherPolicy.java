package com.gathin.tvweather;

final class WeatherPolicy {
    private WeatherPolicy() { }
    static boolean fresh(long updated, long now, long lifetime) {
        return updated > 0 && now >= updated && now - updated < lifetime;
    }
    static boolean validTemperature(double value) { return !Double.isNaN(value) && value >= -100 && value <= 70; }
    static boolean validAqi(double value) { return !Double.isNaN(value) && value >= 0 && value <= 1000; }
    static String aqiCategory(int value) {
        if (value <= 50) return "Good";
        if (value <= 100) return "Moderate";
        if (value <= 150) return "Unhealthy for sensitive groups";
        if (value <= 200) return "Unhealthy";
        if (value <= 300) return "Very unhealthy";
        return "Hazardous";
    }
    static String condition(int code) {
        switch (code) {
            case 0: return "Clear";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 56: case 57: return "Freezing drizzle";
            case 61: case 63: case 65: return "Rain";
            case 66: case 67: return "Freezing rain";
            case 71: case 73: case 75: case 77: return "Snow";
            case 80: case 81: case 82: return "Rain showers";
            case 85: case 86: return "Snow showers";
            case 95: case 96: case 99: return "Thunderstorm";
            default: return "Conditions unavailable";
        }
    }
}
