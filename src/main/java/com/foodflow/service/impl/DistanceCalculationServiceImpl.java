package com.foodflow.service.impl;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.service.DistanceCalculationService;
import org.springframework.stereotype.Service;

@Service
public class DistanceCalculationServiceImpl implements DistanceCalculationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_SPEED_KMH = 30.0; // Average city speed (km/h)

    @Override
    public double calculateDistance(GeoCoordinates start, GeoCoordinates end) {
        if (start == null || end == null || 
            start.getLatitude() == null || start.getLongitude() == null ||
            end.getLatitude() == null || end.getLongitude() == null) {
            return 999.0; // Return arbitrary high distance if missing coordinates
        }

        double lat1Rad = Math.toRadians(start.getLatitude());
        double lon1Rad = Math.toRadians(start.getLongitude());
        double lat2Rad = Math.toRadians(end.getLatitude());
        double lon2Rad = Math.toRadians(end.getLongitude());

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    @Override
    public double estimateTravelTimeMinutes(double distanceKm) {
        if (distanceKm >= 999.0) {
            return 999.0;
        }
        return (distanceKm / AVERAGE_SPEED_KMH) * 60.0;
    }
}
