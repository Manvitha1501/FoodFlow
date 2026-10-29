package com.foodflow.service;

import com.foodflow.document.GeoCoordinates;

public interface DistanceCalculationService {
    double calculateDistance(GeoCoordinates start, GeoCoordinates end);
    double estimateTravelTimeMinutes(double distanceKm);
}
