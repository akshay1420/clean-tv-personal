package com.gathin.tvweather;
/** A coarse area chosen by the user, never a bundled personal location. */
final class WeatherLocation {
    final String city;
    final double latitude, longitude;
    WeatherLocation(String city, double latitude, double longitude) {
        if (city==null || city.trim().isEmpty() || city.trim().length()>180
                || !Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Invalid area");
        this.city=city.trim(); this.latitude=latitude; this.longitude=longitude;
    }
}
