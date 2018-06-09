package sample;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by will on 12/6/2016.
 */

//patient class
public class patient {

    //convert data into strings
    private SimpleStringProperty idProperty = new SimpleStringProperty();
    private SimpleStringProperty nameProperty = new SimpleStringProperty();
    private SimpleStringProperty ageProperty = new SimpleStringProperty();
    private SimpleStringProperty weightProperty = new SimpleStringProperty();
    private SimpleStringProperty genderProperty = new SimpleStringProperty();
    private SimpleStringProperty doctorIDProperty = new SimpleStringProperty();
    private SimpleStringProperty treatmentIDProperty = new SimpleStringProperty();
    private SimpleStringProperty roomIDProperty = new SimpleStringProperty();
    private SimpleStringProperty stayLengthProperty = new SimpleStringProperty();
    private SimpleStringProperty phoneProperty = new SimpleStringProperty();

    //setters
    public void setIdProperty(String s) {
        this.idProperty.set(s);
    }

    public void setNameProperty(String s) {
        this.nameProperty.set(s);
    }

    public void setAgeProperty(String s) {
        this.ageProperty.set(s);
    }

    public void setWeightProperty(String s) {
        this.weightProperty.set(s);
    }

    public void setGenderProperty(String s) {
        this.genderProperty.set(s);
    }

    public void setDoctorIDProperty(String s) {
        this.doctorIDProperty.set(s);
    }

    public void setStayLengthProperty(String s) {
        this.stayLengthProperty.set(s);
    }

    public void setPhoneProperty(String s) {
        this.phoneProperty.set(s);
    }

    public void setTreatmentIDProperty(String s) {
        this.treatmentIDProperty.set(s);
    }

    public void setRoomIDProperty(String s) {
        this.roomIDProperty.set(s);
    }
    //getters

    public String getIdProperty() {
        return idProperty.get();
    }

    public String getNameProperty() {
        return nameProperty.get();
    }

    public String getAgeProperty() {
        return ageProperty.get();
    }

    public String getWeightProperty() {
        return weightProperty.get();
    }

    public String getGenderProperty() {
        return genderProperty.get();
    }

    public String getDoctorIDProperty() {
        return doctorIDProperty.get();
    }

    public String getTreatmentIDProperty() {
        return treatmentIDProperty.get();
    }

    public String getRoomIDProperty() {
        return roomIDProperty.get();
    }

    public String getPhoneProperty() {
        return phoneProperty.get();
    }

    public String getStayLengthProperty() {
        return stayLengthProperty.get();
    }
}
