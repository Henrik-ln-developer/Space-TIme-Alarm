package developer.ln.henrik.spacetimealarm;

import android.location.Location;

import java.io.Serializable;
import java.util.GregorianCalendar;


public class SpaceTimeAlarm implements Serializable {

    private String id;
    private String caption;
    private String location_Name;
    private Double location_Lat;
    private Double location_Lng;
    private Long starTime;
    private Long endTime;

    public SpaceTimeAlarm() {
    }

    public SpaceTimeAlarm(String caption, String location_Name, Double location_Lat, Double location_Lng, Long starTime, Long endTime) {
        this.caption = caption;
        this.location_Name = location_Name;
        this.location_Lat = location_Lat;
        this.location_Lng = location_Lng;
        this.starTime = starTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLocation_Name() {
        return location_Name;
    }

    public void setLocation_Name(String location_Name) {
        this.location_Name = location_Name;
    }

    public Double getLocation_Lat() {
        return location_Lat;
    }

    public void setLocation_Lat(Double location_Lat) {
        this.location_Lat = location_Lat;
    }

    public Double getLocation_Lng() {
        return location_Lng;
    }

    public void setLocation_Lng(Double location_Lng) {
        this.location_Lng = location_Lng;
    }

    public Long getStarTime() {
        return starTime;
    }

    public void setStarTime(Long starTime) {
        this.starTime = starTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "SpaceTimeAlarm{" +
                "id='" + id + '\'' +
                ", caption='" + caption + '\'' +
                ", location_Name='" + location_Name + '\'' +
                ", location_Lat=" + location_Lat +
                ", location_Lng=" + location_Lng +
                ", starTime=" + starTime +
                ", endTime=" + endTime +
                '}';
    }
}
