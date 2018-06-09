package sample;

/**
 * Created by will on 12/6/2016.
 */

//doctor class
import javafx.beans.property.SimpleStringProperty;


//class for doctor information
public class doctor {

    //convert data into strings
    private SimpleStringProperty idProperty = new SimpleStringProperty();
    private SimpleStringProperty nameProperty = new SimpleStringProperty();
    private SimpleStringProperty departmentNameProperty = new SimpleStringProperty();

    //setters
    public void setIdProperty(String s) {
        this.idProperty.set(s);
    }

    public void setNameProperty(String s) {
        this.nameProperty.set(s);
    }

    public void setDepartmentNameProperty(String s) {
        this.departmentNameProperty.set(s);
    }
    //getters

    public String getIdProperty() {
        return idProperty.get();
    }

    public String getNameProperty() {
        return nameProperty.get();
    }

    public String getDepartmentNameProperty() {
        return departmentNameProperty.get();

    }
}

