package sample;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by will on 12/6/2016.
 */


//staff class
public class staff {

    //convert data into strings
    private SimpleStringProperty staffIDProperty = new SimpleStringProperty();
    private SimpleStringProperty staffNameProperty = new SimpleStringProperty();
    private SimpleStringProperty jobTypeProperty = new SimpleStringProperty();

    //setters
    public void setStaffIDProperty(String s) {
        this.staffIDProperty.set(s);
    }

    public void setStaffNameProperty(String s) {
        this.staffNameProperty.set(s);
    }

    public void setJobTypeProperty(String s) {
        this.jobTypeProperty.set(s);
    }
    //getters

    public String getStaffIDProperty() {
        return staffIDProperty.get();
    }

    public String getStaffNameProperty() {
        return staffNameProperty.get();
    }

    public String getJobTypeProperty() {return jobTypeProperty.get(); }
}
