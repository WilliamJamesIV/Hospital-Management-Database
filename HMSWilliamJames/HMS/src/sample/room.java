package sample;

/**
 * Created by will on 12/13/2016.
 */

import javafx.beans.property.SimpleStringProperty;

//class for room information
public class room {

    //convert data into strings
    private SimpleStringProperty idProperty = new SimpleStringProperty();
    private SimpleStringProperty staffIDProperty = new SimpleStringProperty();
    private SimpleStringProperty roomStatusProperty = new SimpleStringProperty();
    private SimpleStringProperty departmentIDProperty = new SimpleStringProperty();
    private SimpleStringProperty hospitalIDProperty = new SimpleStringProperty();

    //setters
    public void setIdProperty(String s) {
        this.idProperty.set(s);
    }

    public void setStaffIDProperty(String s) {
        this.staffIDProperty.set(s);
    }

    public void setRoomStatusProperty(String s) {
        this.roomStatusProperty.set(s);
    }

    public void setHospitalIDProperty(String s) {
        this.hospitalIDProperty.set(s);
    }

    public void setDepartmentIDProperty(String s) {
        this.departmentIDProperty.set(s);
    }
    //getters

    public String getIdProperty() {
        return idProperty.get();
    }

    public String getStaffIDProperty() {
        return staffIDProperty.get();
    }

    public String getDepartmentIDProperty() {return departmentIDProperty.get();}

    public String getRoomStatusProperty() {
        return roomStatusProperty.get();
    }

    public String getHospitalIDProperty() {
        return hospitalIDProperty.get();
    }



}
