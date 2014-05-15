package edu.virginia.buildinglocation;


public class Building {

	//public String[] Course;
	public String building_name;
	public String latitude;
	public String longitude;
	public String distance;
	
	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getBuilding() {
		return building_name;
	}

	public void setBuilding(String building_name) {
		this.building_name = building_name;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	
	@Override
	public String toString() {
		return "Building: '" + building_name + "',  Latitude: " + latitude
				+", Longitude: " + longitude + ",  Distance:" + distance;
	}
	
}

