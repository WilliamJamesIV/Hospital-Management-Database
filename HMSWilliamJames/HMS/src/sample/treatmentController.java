package sample;

/**
 * Created by will on 11/25/2016.
 */


import org.controlsfx.control.Notifications;
import sample.treatment;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
//import org.controlsfx.control.Notifications;
//import org.controlsfx.dialog.Dialogs;

//class for treatment controller
public class treatmentController implements Initializable {

    //for connection
    private Connection mysql;
    private PreparedStatement ps = null;
    private Statement st = null;

    private ResultSet rs;

    //assign names for treatment tab
    @FXML
    private Button btSave;
    @FXML
    private TextField treatmentSearchtf;
    @FXML
    private Button addTreatmentbt;
    @FXML
    private ComboBox<String> searchcb;
    @FXML
    private TableView<treatment> treatmentTabletv;
    @FXML
    private TableColumn treatmentIDColumn;
    @FXML
    private TableColumn treatmentNameColumn;
    @FXML
    private TableColumn medicineIDColumn;
    @FXML
    private TableColumn departmentIDColumn;
    @FXML
    private TableColumn diseaseIDColumn;
    @FXML
    private TextField addTreatmentIDtf;
    @FXML
    private TextField addTreatmentNametf;
    @FXML
    private TextField addMedicineIDtf;
    @FXML
    private TextField addDepartmentIDtf;
    @FXML
    private TextField addDiseaseIDtf;

    //lists for treatment data
    private ObservableList<treatment> treatmentData = FXCollections.observableArrayList();
    private ObservableList<treatment> fData = FXCollections.observableArrayList();

    //initilizable
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createTable();
        setEditable();
        treatmentSearch();
        connectDB();
    }


    //creates the csv file for treatment
    public void generateTreatmentReports() throws IOException {

        try {
            Connection conn = null;
            FileWriter fw = new FileWriter("treatment.csv");
            mainController iTreat = new mainController();

            conn = iTreat.connectTheDB();

            //msql statement for getting treatment information
            //inner joins medicine, department and disease
            //uses treatments medicine id to get medicines name
            //uses treatments department id to get department name
            //uses treatments disease id to ge the name of the dieases

            String insertTreat = "select treatment.treatmentID, treatment.treatmentName, medicine.medicineName, department.departmentName, disease.diseaseName\n" +
                    "from treatment inner join medicine on treatment.medicineID = medicine.medicineID\n" +
                    "inner join department on treatment.departmentID = department.departmentID\n" +
                    "inner join disease on treatment.diseaseID = disease.diseaseID group by treatmentName ASC;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(insertTreat);
            while (rs.next()) {
                fw.append(rs.getString(1));
                fw.append(',');
                fw.append(rs.getString(2));
                fw.append(',');
                fw.append(rs.getString(3));
                fw.append(',');
                fw.append(rs.getString(4));
                fw.append(',');
                fw.append(rs.getString(5));
                fw.append('\n');
            }
            fw.flush();
            fw.close();
            conn.close();
            System.out.println("CSV File is created successfully for treatment information.");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    //adds a treatment info to the database and table
    @FXML
    public void addTreatmentButton(){
        //if any text field is empty, show error
        if(addTreatmentIDtf.getText().equals("") || addTreatmentNametf.getText().equals("") || addMedicineIDtf.getText().equals("")
                || addDepartmentIDtf.getText().equals("") || addDiseaseIDtf.getText().equals("")){

            Notifications.create().title("Error Adding Treatment")
                    .text("One of the Text fields is empty, Please insert valid information to all text fields!")
                    .showError();
        }
        //if treatment id is a duplicate, show error
        else if (checkDuplicate(addTreatmentIDtf.getText())) {
            Notifications.create().title("Error Adding Treatment ID")
                    .text("Treatment ID already exists. Please enter a valid Treatment ID that does not already exist!")
                    .showError();
        }
        //else insert data
        else{

            String treatmentID = addTreatmentIDtf.getText();
            String treatmentName = addTreatmentNametf.getText();
            String medicineID = addMedicineIDtf.getText();
            String departmentID = addDepartmentIDtf.getText();
            String diseaseID = addDiseaseIDtf.getText();


            treatment tm = new treatment();
            tm.setIdProperty(treatmentID);
            tm.setTreatmentNameProperty(treatmentName);
            tm.setMedicineIDProperty(medicineID);
            tm.setDepartmentIDProperty(departmentID);
            tm.setDiseaseIDProperty(diseaseID);
            treatmentData.add(tm);
            String addButton = "insert into treatment(treatmentID, treatmentName, medicineID,departmentID ,DiseaseID)"+"values (?,?,?,?,?)";
            try {
                ps = mysql.prepareStatement(addButton);
                ps.setString(1,treatmentID);
                ps.setString(2,treatmentName);
                ps.setString(3,medicineID);
                ps.setString(4,departmentID);
                ps.setString(5,diseaseID);
                ps.executeUpdate();
                ps.close();
                ps = null; //might have to change this or take it away
            }
            catch (SQLException e) {
                e.printStackTrace();

            }
            addTreatmentIDtf.clear();
            addTreatmentNametf.clear();
            addMedicineIDtf.clear();
            addDepartmentIDtf.clear();
            addDiseaseIDtf.clear();

        }
    }

    //searches through treatment information
    public void treatmentSearch(){
        searchcb.getItems().addAll("treatmentID","treatmentName","medicineID", "departmentID", "diseaseID");
        searchcb.setValue("treatmentID");;
    }

    //deletes a doctor from list and database
    @FXML
    public void deleteTreatmentButton(){
        int sIndex = treatmentTabletv.getSelectionModel().getSelectedIndex();
        String sID = treatmentData.get(sIndex).getIdProperty();

        String deleteTreatment = "delete from treatment where treatmentID = ?";
        try{
            ps = mysql.prepareStatement(deleteTreatment);
            ps.setString(1,sID);
            ps.executeUpdate();
            ps.close();
            ps = null;

        } catch (SQLException e) {
            e.printStackTrace();
            //maybe more in this exception
        }

        treatmentData.remove(sIndex);
    }

    //initialize table
    public void createTable(){
        treatmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("idProperty"));
        treatmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("treatmentNameProperty"));
        medicineIDColumn.setCellValueFactory(new PropertyValueFactory<>("medicineIDProperty"));
        departmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("departmentIDProperty"));
        diseaseIDColumn.setCellValueFactory(new PropertyValueFactory<>("diseaseIDProperty"));

        FilteredList<treatment> fData = new FilteredList<>(treatmentData, p -> true);
        treatmentTabletv.setItems(fData);
        treatmentSearchtf.textProperty().addListener((observable, oldValue, newValue) -> {
            fData.setPredicate(rm -> {
                if(newValue == null || newValue.isEmpty())
                    return true;

                String lCaseFilter = newValue.toLowerCase();
                switch(searchcb.getValue()) {
                    case "treatmentID":
                        if(rm.getIdProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "treatmentName":
                        if(rm.getTreatmentNameProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "medicineID":
                        if(rm.getMedicineIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "departmentID":
                        if(rm.getDepartmentIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "diseaseID":
                        if(rm.getDiseaseIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                }
                return false;
            });
        });

    }

    //conects the database and puts treatment info into table
    public void connectDB(){

        try{
            /*
            //commands to open up database
            Class.forName("com.mysql.jdbc.Driver"); //maybe add .newInstance();
            String connectionStringURL =  "jdbc:mysql://us-cdbr-azure-west-b.cleardb.com:3306/acsm_54270fa45d472fa";
            mysql = DriverManager.getConnection(connectionStringURL, "b1d0beb2ed12fc", "ba632151");
            //if no connection let user know it failed, if connect works print success
            */
            mainController mainTreat = new mainController(); //new
            mysql = mainTreat.connectTheDB(); //new

            if(mysql == null)
                System.out.println("Connection Failed to treatment");
            else
                System.out.println("Success connecting to treatment");

            String searchStatement = "select * from treatment";
            st = mysql.createStatement();
            rs = st.executeQuery(searchStatement);
            while(rs.next()){
                treatment tm = new treatment();
                tm.setIdProperty(rs.getString(1));
                tm.setTreatmentNameProperty(rs.getString(2));
                tm.setMedicineIDProperty(rs.getString(3));
                tm.setDepartmentIDProperty(rs.getString(4));
                tm.setDiseaseIDProperty(rs.getString(5));
                treatmentData.add(tm);
            }
            st.close();
            st = null;
        }
        //exception and maybe more exceptions
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    //refreshes data
    public void dataRefresh(){
        new java.util.Timer().schedule(new java.util.TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    TableView<treatment> treatmentTab;
                    treatmentTab = treatmentTabletv;
                    treatmentTab.getColumns().get(0).setVisible(false);
                    treatmentTab.getColumns().get(0).setVisible(true);

                });
            }
        }, 50);
    }



    //checks for duplicate
    public boolean checkDuplicate(String s){
        for (treatment tm : treatmentData){
            if(s.equals(tm.getIdProperty())){
                return true;
            }

        }
        return false;

    }

    //set edit table
    //need to insert errors on refresh
    public void setEditable(){
        treatmentTabletv.setEditable(true);
        //treatment id editable
        treatmentIDColumn.setEditable(true);
        treatmentIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        treatmentIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<treatment, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<treatment, String> t){
                treatment tm = ((treatment) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Treatment ID")
                            .text("Treatment ID text field is empty. Text fields may not be left empty. Please insert a valid Treatment ID that does not already exist!!")
                            .showError();
                }
                //if Treatment ID is a duplicate, show error
                else if (checkDuplicate(t.getNewValue())){
                    dataRefresh();
                    Notifications.create().title("Duplicate Error Updating Treatment ID")
                            .text("Treatment ID already exists. Please insert a valid Treatment ID that does not already exist!!")
                            .showError();
                }
                //else update treatment ID
                else {
                    String dataUpdate = "update treatment set treatmentID = ? where treatmentName = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, tm.getTreatmentNameProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    tm.setIdProperty(t.getNewValue());
                }
            }

        });
        //treatmentName editable
        treatmentNameColumn.setEditable(true);
        treatmentNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        treatmentNameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<treatment, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<treatment, String> t){
                treatment tm = ((treatment) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Treatment Name")
                            .text("Treatment Name text field is empty. Text fields may not be left empty. Please insert a valid Treatment Name!")
                            .showError();
                }
                else {
                    String dataUpdate = "update treatment set treatmentName = ? where treatmentID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, tm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    tm.setTreatmentNameProperty(t.getNewValue());
                }
            }
        });
        //medicine id editable
        medicineIDColumn.setEditable(true);
        medicineIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        medicineIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<treatment, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<treatment, String> t){
                treatment tm  = ((treatment) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Treatment's Medicine ID")
                            .text("Treatment's Medicine ID text field is empty. Text fields may not be left empty. Please insert a valid Treatment's Medicine ID!")
                            .showError();
                }
                /*
                else if (checkDuplicate(t.getNewValue())){
                dataRefresh();
                Notifications.create().title("Duplicate Error Updating Treatment ID")
                        .text("Treatment ID already exists. Please insert a valid Treatment ID that does not already exist!!")
                        .showError();
                }
                 */
                //else update medicine id
                else {
                    String dataUpdate = "update treatment set medicineID = ? where treatmentID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, tm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    tm.setMedicineIDProperty(t.getNewValue());
                }

            }
        });

        //department id editable
        departmentIDColumn.setEditable(true);
        departmentIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        departmentIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<treatment, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<treatment, String> t){
                treatment tm = ((treatment) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Treatment's Department ID")
                            .text("Treatment's Department ID text field is empty. Text fields may not be left empty. Please insert a valid Treatment Department ID!")
                            .showError();
                }
                 /*
                else if (checkDuplicate(t.getNewValue())){
                dataRefresh();
                Notifications.create().title("Duplicate Error Updating Treatment's ID")
                        .text("Treatment ID already exists. Please insert a valid Treatment ID that does not already exist!!")
                        .showError();
                }
                 */
                //else update department ID
                else {
                    String dataUpdate = "update treatment set departmentID = ? where treatmentID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, tm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    tm.setDepartmentIDProperty(t.getNewValue());
                }

            }
        });
        //disease id editable
        diseaseIDColumn.setEditable(true);
        diseaseIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        diseaseIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<treatment, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<treatment, String> t){
                treatment tm = ((treatment) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Treatment's Disease ID")
                            .text("Treatment's Disease ID text field is empty. Text fields may not be left empty. Please insert a valid Treatment Disease ID!")
                            .showError();
                }
                 /*
                else if (checkDuplicate(t.getNewValue())){
                dataRefresh();
                Notifications.create().title("Duplicate Error Updating Treatment ID")
                        .text("Treatment ID already exists. Please insert a valid Treatment ID that does not already exist!!")
                        .showError();
                }
                 */
                //else update disease ID
                else {
                    String dataUpdate = "update treatment set diseaseID = ? where treatmentID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, tm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    tm.setDiseaseIDProperty(t.getNewValue());
                }
            }
        });
    }
}
