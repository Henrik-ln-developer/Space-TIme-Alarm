package developer.ln.henrik.spacetimealarm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class SpaceTimeAlarm implements Serializable {

    private String id;
    private String caption;
    private String location_Id;
    private String location_Name;
    private Double location_Lat;
    private Double location_Lng;
    private Long startTime;
    private Long endTime;

    public SpaceTimeAlarm() {
    }

    public SpaceTimeAlarm(String id, String caption, String location_Id, String location_Name, Double location_Lat, Double location_Lng, Long startTime, Long endTime) {
        this.id = id;
        this.caption = caption;
        this.location_Id = location_Id;
        this.location_Name = location_Name;
        this.location_Lat = location_Lat;
        this.location_Lng = location_Lng;
        this.startTime = startTime;
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getLocation_Id() { return location_Id; }

    public void setLocation_Id(String location_Id) { this.location_Id = location_Id; }

    @Override
    public String toString() {
        return "SpaceTimeAlarm{" +
                "id='" + id + '\'' +
                ", caption='" + caption + '\'' +
                ", location_Name='" + location_Name + '\'' +
                ", location_Lat=" + location_Lat +
                ", location_Lng=" + location_Lng +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("caption", caption);
        result.put("location_Id", location_Id);
        result.put("location_Name", location_Name);
        result.put("location_Lat", location_Lat);
        result.put("location_Lng", location_Lng);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        return result;
    }

}
