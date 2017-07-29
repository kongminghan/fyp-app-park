package com.example.minghan.park.Modal;

import java.util.HashMap;

/**
 * Created by MingHan on 3/20/2017.
 */

public class Car {
    private String CarNumber;
    private String LastEnterDate;
    private String LastEnterTime;
    private String Status;
    private Long timestamp;
    private String CMToken;
    private String CarLocation;


    public Car(){}

    public Car(String carNumber, String entDate, String entTime) {
        CarNumber = carNumber;
        LastEnterDate = entDate;
        LastEnterDate = entTime;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCarNumber() {
        return CarNumber;
    }

    public void setCarNumber(String carNumber) {
        CarNumber = carNumber;
    }

    public String getLastEnterDate() {
        return LastEnterDate;
    }

    public void setLastEnterDate(String lastEnterDate) {
        LastEnterDate = lastEnterDate;
    }

    public String getLastEnterTime() {
        return LastEnterTime;
    }

    public void setLastEnterTime(String lastEnterTime) {
        LastEnterTime = lastEnterTime;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getCMToken() {
        return CMToken;
    }

    public void setCMToken(String CMToken) {
        this.CMToken = CMToken;
    }

    public String getCarLocation() {
        return CarLocation;
    }

    public void setCarLocation(String location) {
        CarLocation = location;
    }
}
