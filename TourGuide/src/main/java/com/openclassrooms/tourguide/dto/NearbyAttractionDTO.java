package com.openclassrooms.tourguide.dto;

public class NearbyAttractionDTO {

    private final String attractionName;
    private final double attractionLatitude;
    private final double attractionLongitude;
    private final double userLatitude;
    private final double userLongitude;
    private final double distance;
    private final int rewardPoints;

    public NearbyAttractionDTO(String attractionName, double attractionLatitude, double attractionLongitude,
                               double userLatitude, double userLongitude, double distance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public double getAttractionLatitude() {
        return attractionLatitude;
    }

    public double getAttractionLongitude() {
        return attractionLongitude;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public double getDistance() {
        return distance;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }
}
