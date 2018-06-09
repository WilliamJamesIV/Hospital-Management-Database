package sample;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by will on 12/13/2016.
 */

//class for treatment information
public class treatment {

    //convert data into strings
    private SimpleStringProperty idProperty = new SimpleStringProperty();
    private SimpleStringProperty treatmentNameProperty = new SimpleStringProperty();
    private SimpleStringProperty medicineIDProperty = new SimpleStringProperty();
    private SimpleStringProperty departmentIDProperty = new SimpleStringProperty();
    private SimpleStringProperty diseaseIDProperty = new SimpleStringProperty();

    //setters
    public void setIdProperty(String s) {this.idProperty.set(s);}

    public void setTreatmentNameProperty(String s) {this.treatmentNameProperty.set(s);
    }

    public void setMedicineIDProperty(String s) {this.medicineIDProperty.set(s);
    }

    public void setDiseaseIDProperty(String s) {this.diseaseIDProperty.set(s);
    }

    public void setDepartmentIDProperty(String s) {this.departmentIDProperty.set(s);
    }
    //getters

    public String getIdProperty() {return idProperty.get();
    }

    public String getTreatmentNameProperty() {return treatmentNameProperty.get();
    }

    public String getDepartmentIDProperty() {return departmentIDProperty.get();}

    public String getMedicineIDProperty() {return medicineIDProperty.get();
    }

    public String getDiseaseIDProperty() {return diseaseIDProperty.get();
    }


}
