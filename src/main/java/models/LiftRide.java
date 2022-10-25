package models;

public class LiftRide {
    public String skierID;
    public String liftID;
    public String resortID;
    public String seasonID;
    public String dayID;
    public String time;

    public LiftRide(String skierID, String liftID, String resortID, String seasonID, String dayID, String time) {
        this.skierID = skierID;
        this.liftID = liftID;
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.time = time;
    }

    public String getSkierID() {
        return skierID;
    }

    public void setSkierID(String skierID) {
        this.skierID = skierID;
    }

    public String getLiftID() {
        return liftID;
    }

    public void setLiftID(String liftID) {
        this.liftID = liftID;
    }

    public String getResortID() {
        return resortID;
    }

    public void setResortID(String resortID) {
        this.resortID = resortID;
    }

    public String getSeasonID() {
        return seasonID;
    }

    public void setSeasonID(String seasonID) {
        this.seasonID = seasonID;
    }

    public String getDayID() {
        return dayID;
    }

    public void setDayID(String dayID) {
        this.dayID = dayID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "LiftRide{" +
                "skierID='" + skierID + '\'' +
                ", liftID='" + liftID + '\'' +
                ", resortID='" + resortID + '\'' +
                ", seasonID='" + seasonID + '\'' +
                ", dayID='" + dayID + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
