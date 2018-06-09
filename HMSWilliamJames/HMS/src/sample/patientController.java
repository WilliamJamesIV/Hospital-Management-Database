package sample;

/**
 * Created by will on 11/25/2016.
 */

import org.controlsfx.control.Notifications;
import sample.patient;

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

//patient controller class
public class patientController implements Initializable {

    //for connection
    private Connection mysql;
    private PreparedStatement ps = null;
    private Statement st = null;

    private ResultSet rs;

    //assign names for patient tab
    @FXML
    private Button btSave;
    @FXML
    private TextField patientSearchtf;
    @FXML
    private Button addPatientBT;
    @FXML
    private ComboBox<String> searchcb;
    @FXML
    private TableView<patient> patientTabletv;
    @FXML
    private TableColumn patientIdColumn;
    @FXML
    private TableColumn patientNameColumn;
    @FXML
    private TableColumn patientAgeColumn;
    @FXML
    private TableColumn patientWeightColumn;
    @FXML
    private TableColumn genderColumn;
    @FXML
    private TableColumn patientPhoneColumn;
    @FXML
    private TableColumn treatmentIDColumn;
    @FXML
    private TableColumn roomIDColumn;
    @FXML
    private TableColumn doctorIDColumn;
    @FXML
    private TableColumn stayLengthColumn;
    @FXML
    private TextField addPatientIDtf;
    @FXML
    private TextField addPatientNametf;
    @FXML
    private TextField addPatientAgetf;
    @FXML
    private TextField addPatientWeighttf;
    @FXML
    private TextField addGendertf;
    @FXML
    private TextField addPatientPhonetf;
    @FXML
    private TextField addTreatmentIDtf;
    @FXML
    private TextField addDoctorIDtf;
    @FXML
    private TextField addRoomIDtf;
    @FXML
    private TextField addStayLengthtf;

    //lists for patient data
    private ObservableList<patient> patientData = FXCollections.observableArrayList();
    private ObservableList<patient> fData = FXCollections.observableArrayList();

    //initializable
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createTable();
        setEditable();
        patientSearch();
        connectDB();
    }

    //creates the csv file
    public void generatePatientReports() throws IOException {

        try {
            Connection conn = null;
            FileWriter fw = new FileWriter("patient.csv");
            mainController iPat = new mainController();

            conn = iPat.connectTheDB();

            String insertPat = "select patient.patientID, patient.patientName, patient.patientAge, patient.patientWeight as Weight_In_LBS, patient.gender, patient.patientPhone, treatment.treatmentName, doctor.doctorName, patient.roomID as room_number, patient.stayLength as stay_Length_Days\n" +
                    "from patient inner join treatment on patient.treatmentID = treatment.treatmentID\n" +
                    "inner join doctor on patient.doctorID = doctor.doctorID group by patientID ASC;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(insertPat);
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
                fw.append(',');
                fw.append(rs.getString(6));
                fw.append(',');
                fw.append(rs.getString(7));
                fw.append(',');
                fw.append(rs.getString(8));
                fw.append(',');
                fw.append(rs.getString(9));
                fw.append(',');
                fw.append(rs.getString(10));
                fw.append('\n');

            }
            fw.flush();
            fw.close();
            conn.close();
            System.out.println("CSV File is created successfully for patient information.");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    //adds patient to database and table
    @FXML
    public void addPatientButton(){
        //if any field is left empty while adding, show error
        if(addDoctorIDtf.getText().equals("") || addPatientNametf.getText().equals("") || addPatientAgetf.getText().equals("")
                || addPatientWeighttf.getText().equals("") || addGendertf.getText().equals("") || addTreatmentIDtf.getText().equals("")
                || addDoctorIDtf.getText().equals("") || addRoomIDtf.getText().equals("") || addPatientPhonetf.getText().equals("")
                || addStayLengthtf.getText().equals("")){

            Notifications.create().title("Error Adding Patient")
                    .text("Fields Cannot be left empty, please make sure all fields have valid information")
                    .showError();
        }
        //else if Patient ID already exists show error
        else if (checkDuplicate(addPatientIDtf.getText())) {
            Notifications.create().title("Duplicate Patient ID Error")
                    .text("A Patient already exits with this ID, please enter an ID that does not already exist!")
                    .showError();
        }
        //else insert info to table and database
        else{

            String patientID = addPatientIDtf.getText();
            String patientName = addPatientNametf.getText();
            String patientAge = addPatientAgetf.getText();
            String patientWeight = addPatientWeighttf.getText();
            String doctorID = addDoctorIDtf.getText();
            String treatmentID = addTreatmentIDtf.getText();
            String roomID = addRoomIDtf.getText();
            String stayLength = addStayLengthtf.getText();
            String patientPhone = addPatientPhonetf.getText();
            String gender = addGendertf.getText();

            patient pat = new patient();

            pat.setIdProperty(patientID);
            pat.setNameProperty(patientName);
            pat.setAgeProperty(patientAge);
            pat.setWeightProperty(patientWeight);
            pat.setPhoneProperty(patientPhone);
            pat.setGenderProperty(gender);
            pat.setStayLengthProperty(stayLength);
            pat.setDoctorIDProperty(doctorID);
            pat.setRoomIDProperty(roomID);
            pat.setTreatmentIDProperty(treatmentID);

            patientData.add(pat);

            String addButton = "insert into patient(patientID, patientName, patientAge, patientWeight, gender, patientPhone, treatmentID, doctorID,roomID ,stayLength )"
                    +"values (?,?,?,?,?,?,?,?,?,?)";
            try {
                ps = mysql.prepareStatement(addButton);
                ps.setString(1,patientID);
                ps.setString(2,patientName);
                ps.setString(3,patientAge);
                ps.setString(4,patientWeight);
                ps.setString(5,gender);
                ps.setString(6,patientPhone);
                ps.setString(7,treatmentID);
                ps.setString(8,doctorID);
                ps.setString(9,roomID);
                ps.setString(10,stayLength);

                ps.executeUpdate();
                ps.close();
                ps = null; //might have to change this or take it away
            }
            catch (SQLException e) {
                //e.printStackTrace();

            }
            addPatientIDtf.clear();
            addPatientNametf.clear();
            addPatientAgetf.clear();
            addGendertf.clear();
            addPatientPhonetf.clear();
            addPatientWeighttf.clear();
            addStayLengthtf.clear();
            addDoctorIDtf.clear();
            addRoomIDtf.clear();
            addTreatmentIDtf.clear();

        }

    }

    //search patient info
    public void patientSearch(){
        searchcb.getItems().addAll("patientID","patientName","patientAge", "patientWeight", "gender", "patientPhone", "treatmentID", "doctorID", "roomID", "stayLength");
        searchcb.setValue("patientID");
    }

    //deletes a patient from list and database
    @FXML
    public void deletePatientButton(){
        int sIndex = patientTabletv.getSelectionModel().getSelectedIndex();
        String sID = patientData.get(sIndex).getIdProperty();

        String deletePatient = "delete from patient where patientID = ?";
        try{
            ps = mysql.prepareStatement(deletePatient);
            ps.setString(1,sID);
            ps.executeUpdate();
            ps.close();
            ps = null;

        } catch (SQLException e) {
            e.printStackTrace();
            //maybe more in this exception
        }

        patientData.remove(sIndex);
    }

    //initialize table
    public void createTable(){
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("idProperty"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("nameProperty"));
        patientAgeColumn.setCellValueFactory(new PropertyValueFactory<>("ageProperty"));
        patientWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weightProperty"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("genderProperty"));
        patientPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneProperty"));
        treatmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("treatmentIDProperty"));
        doctorIDColumn.setCellValueFactory(new PropertyValueFactory<>("doctorIDProperty")); // why is there an error here
        roomIDColumn.setCellValueFactory(new PropertyValueFactory<>("roomIDProperty"));
        stayLengthColumn.setCellValueFactory(new PropertyValueFactory<>("stayLengthProperty"));

        //patient data to list
        FilteredList<patient> fData = new FilteredList<>(patientData, p -> true);
        patientTabletv.setItems(fData);
        patientSearchtf.textProperty().addListener((observable, oldValue, newValue) -> {
            fData.setPredicate(pat -> {
                if(newValue == null || newValue.isEmpty())
                    return true;

                //search for patient info using combo box
                String lCaseFilter = newValue.toLowerCase();
                switch(searchcb.getValue()) {
                    case "patientID":
                        if(pat.getIdProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "patientName":
                        if(pat.getNameProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "patientAge":
                        if(pat.getAgeProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "patientWeight":
                        if(pat.getWeightProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "gender":
                        if(pat.getGenderProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "patientPhone":
                        if(pat.getPhoneProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "treatmentID":
                        if(pat.getTreatmentIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "doctorID":
                        if(pat.getDoctorIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "roomID":
                        if(pat.getRoomIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "stayLength":
                        if(pat.getStayLengthProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                }
                return false;
            });
        });

    }

    //connects the database and puts patient data into table
    public void connectDB(){

        try{
            /*
            //commands to open up database
            Class.forName("com.mysql.jdbc.Driver"); //maybe add .newInstance();
            String connectionStringURL =  "jdbc:mysql://us-cdbr-azure-west-b.cleardb.com:3306/acsm_54270fa45d472fa";
            mysql = DriverManager.getConnection(connectionStringURL, "b1d0beb2ed12fc", "ba632151");
            //if no connection let user know it failed, if connect works print success
            */
            mainController mainPat = new mainController(); //new
            mysql = mainPat.connectTheDB(); //new

            if(mysql == null)
                System.out.println("Connection Failed");
            else
                System.out.println("Success connection for patient");



            String searchStatement = "select * from patient";
            st = mysql.createStatement();
            rs = st.executeQuery(searchStatement);
            while(rs.next()){
                patient pat = new patient();
                pat.setIdProperty(rs.getString(1));
                pat.setNameProperty(rs.getString(2));
                pat.setAgeProperty(rs.getString(3));
                pat.setWeightProperty(rs.getString(4));
                pat.setGenderProperty(rs.getString(5));
                pat.setPhoneProperty(rs.getString(6));
                pat.setTreatmentIDProperty(rs.getString(7));
                pat.setDoctorIDProperty(rs.getString(8));
                pat.setRoomIDProperty(rs.getString(9));
                pat.setStayLengthProperty(rs.getString(10));
                patientData.add(pat);
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
                    TableView<patient> patTab;
                    patTab = patientTabletv;
                    patTab.getColumns().get(0).setVisible(false);
                    patTab.getColumns().get(0).setVisible(true);

                });
            }
        }, 50);
    }



    //checks for duplicate
    public boolean checkDuplicate(String s){
        for (patient pat : patientData){
            if(s.equals(pat.getIdProperty())){
                return true;
            }
        }
        return false;

    }

    //set edit table
    //need to insert errors on refresh
    public void setEditable(){
        patientTabletv.setEditable(true);
        //patient id editable
        patientIdColumn.setEditable(true);
        patientIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        patientIdColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if field is empty show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient ID")
                            .text("Text Fields must not be left Empty, please insert a valid Patient ID that does not already exist!")
                            .showError();
                }
                //if ID is a duplicate, show error
                else if (checkDuplicate(t.getNewValue())){
                    dataRefresh();
                    Notifications.create().title("Duplicate Error Updating Patient ID")
                            .text("A Patient with this ID already exists, please insert a valid Patient ID that does not already exist!")
                            .showError();
                }
                //else update patient ID
                else {
                    String dataUpdate = "update patient set patientID = ? where patientName = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getNameProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setIdProperty(t.getNewValue());
                }
            }

        });
        //patient Name editable
        patientNameColumn.setEditable(true);
        patientNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        patientNameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if field is empty show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient Name")
                            .text("Patient Name Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Name!")
                            .showError();
                }
                //else update patient name
                else {
                    String dataUpdate = "update patient set patientName = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setNameProperty(t.getNewValue());
                }
            }
        });
        //patient age editable
        patientAgeColumn.setEditable(true);
        patientAgeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        patientAgeColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient Age")
                            .text("Patient Age Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Age!")
                            .showError();
                }
                //else update patient age
                else {
                    String dataUpdate = "update patient set patientAge = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setAgeProperty(t.getNewValue());
                }

            }
        });
        /*
        //gender editable
        genderColumn.setEditable(true);
        genderColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        genderColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    //insert error here
                }
                else {
                    String dataUpdate = "update patient set gender = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setGenderProperty(t.getNewValue());
                }

            }
        }); */
        //patient weight editable
        patientWeightColumn.setEditable(true);
        patientWeightColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        patientWeightColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if field is empty,show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient Weight")
                            .text("Patient Weight Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Weight!")
                            .showError();
                }
                //else update patient weight
                else {
                    String dataUpdate = "update patient set patientWeight = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setWeightProperty(t.getNewValue());
                }

            }
        });
        //patient gender editable
        genderColumn.setEditable(true);
        genderColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        genderColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient Gender")
                            .text("Patient Gender Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Gender(M or F)!")
                            .showError();
                }
                //else update Gender
                else {
                    String dataUpdate = "update patient set gender = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setGenderProperty(t.getNewValue());
                }

            }
        });

        patientPhoneColumn.setEditable(true);
        patientPhoneColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        patientPhoneColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient Phone Number")
                            .text("Patient Phone Number Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Phone Number!")
                            .showError();
                }
                //else update patient phone number
                else {
                    String dataUpdate = "update patient set patientPhone = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setPhoneProperty(t.getNewValue());
                }

            }
        });

        treatmentIDColumn.setEditable(true);
        treatmentIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        treatmentIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient Treatment ID")
                            .text("Patient's Treatment ID Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Treatment ID!")
                            .showError();
                }
                /*
                else if (t.getNewValue() > number of treatment ID's){
                    //show error saying this treatment does not exists in database
                }
                */
                //else update patient treatment ID
                else {
                    String dataUpdate = "update patient set treatmentID = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setTreatmentIDProperty(t.getNewValue());
                }

            }
        });

        doctorIDColumn.setEditable(true);
        doctorIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient's Doctor ID")
                            .text("Patient's Doctor ID Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Doctor ID!")
                            .showError();
                }
                 /*
                else if (t.getNewValue() > number of treatment ID's){
                    //show error saying this treatment does not exists in database
                }
                */
                //else update doctor ID
                else {
                    String dataUpdate = "update patient set doctorID = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setDoctorIDProperty(t.getNewValue());
                }

            }
        });

        roomIDColumn.setEditable(true);
        roomIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        roomIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient's Room ID")
                            .text("Patient's Room ID Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Room ID!")
                            .showError();
                }
                 /*
                else if (t.getNewValue() > number of treatment ID's){
                    //show error saying this treatment does not exists in database
                }
                */
                 //else update patient room id
                else {
                    String dataUpdate = "update patient set roomID = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setRoomIDProperty(t.getNewValue());
                }

            }
        });


        stayLengthColumn.setEditable(true);
        stayLengthColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        stayLengthColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<patient, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<patient, String> t){
                patient pat = ((patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Patient's Stay Length")
                            .text("Patient's Stay Length Text Field is Empty! Text Fields must not be left empty, please insert a valid Patient Stay Length(days)!")
                            .showError();
                }
                //else update stay length
                else {
                    String dataUpdate = "update patient set stayLength = ? where patientID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, pat.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    pat.setStayLengthProperty(t.getNewValue());
                }

            }
        });



    }


}
