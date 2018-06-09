package sample;

/**
 * Created by will on 11/25/2016.
 */
import org.controlsfx.control.Notifications;
import sample.doctor;

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


//class for doctor controller
public class doctorController implements Initializable{

    //connection information
    private Connection mysql;
    private PreparedStatement ps = null;
    private Statement st = null;

    private ResultSet rs;

    //assigns names for doctor tab
    @FXML
    private Button btSave;
    @FXML
    private TextField doctorSearchtf;
    @FXML
    private Button addDoctorbt;
    @FXML
    private ComboBox<String> searchcb;
    @FXML
    private TableView<doctor> doctorTabletv;
    @FXML
    private TableColumn doctorIdColumn;
    @FXML
    private TableColumn doctorNameColumn;
    @FXML
    private TableColumn departmentColumn;
    @FXML
    private TextField addDoctorIDtf;
    @FXML
    private TextField addDoctorNametf;
    @FXML
    private TextField addDepartmenttf;

    //lists that holds doctor data
    private ObservableList<doctor> doctorData = FXCollections.observableArrayList();
    private ObservableList<doctor> fData = FXCollections.observableArrayList();

    //initializes everything
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createTable();
        setEditable();
        doctorSearch();
        connectDB();
    }

    //creates the csv file
    public void generateDocReports() throws IOException {

        try {
            Connection conn = null;
            FileWriter fw = new FileWriter("doctor.csv");
            mainController iDoc = new mainController();

            conn = iDoc.connectTheDB();

            String insertDoc = "select * from doctor";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(insertDoc);
            while (rs.next()) {
                fw.append(rs.getString(1));
                fw.append(',');
                fw.append(rs.getString(2));
                fw.append(',');
                fw.append(rs.getString(3));
                fw.append('\n');
            }
            fw.flush();
            fw.close();
            conn.close();
            System.out.println("CSV File is created successfully for Doctor.");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    //adds a doctor to list then database
    @FXML
    public void addDoctorButton(){
        if(addDoctorIDtf.getText().equals("") || addDoctorNametf.getText().equals("") || addDepartmenttf.getText().equals("")){
            Notifications.create().title("Error Adding Doctor")
                    .text("All Text Field must be with Information relating to field!")
                    .showError();
        }
        else if (checkDuplicate(addDoctorIDtf.getText())) {
            Notifications.create().title("Duplicate ID Error")
                .text("A Doctor already exits with this ID, please enter an ID that does not already exist!")
                .showError();
        }

        else{

            String docID = addDoctorIDtf.getText();
            String docName = addDoctorNametf.getText();
            String departmentName = addDepartmenttf.getText();
            doctor doc = new doctor();
            doc.setIdProperty(docID);
            doc.setNameProperty(docName);
            doc.setDepartmentNameProperty(departmentName);
            doctorData.add(doc);
            String addButton = "insert into doctor(doctorID, doctorName, departmentName)"+"values (?,?,?)";
            try {
                ps = mysql.prepareStatement(addButton);
                ps.setString(1,docID);
                ps.setString(2,docName);
                ps.setString(3,departmentName);

                ps.executeUpdate();
                ps.close();
                ps = null; //might have to change this or take it away
            }
            catch (SQLException e) {
                //e.printStackTrace();

            }
            addDoctorIDtf.clear();
            addDoctorNametf.clear();
            addDepartmenttf.clear();

        }


    }

    //searches for items in doctor
    public void doctorSearch(){
        searchcb.getItems().addAll("doctorID","doctorName","departmentName");
        searchcb.setValue("doctorID");;
    }


    //deletes a doctor from list and database
    @FXML
    public void deleteDoctorButton(){
        int sIndex = doctorTabletv.getSelectionModel().getSelectedIndex();
        String sID = doctorData.get(sIndex).getIdProperty();

        String deleteDoctor = "delete from doctor where doctorID = ?";
        try{
            ps = mysql.prepareStatement(deleteDoctor);
            ps.setString(1,sID);
            ps.executeUpdate();
            ps.close();
            ps = null;

        } catch (SQLException e) {
            e.printStackTrace();
            //maybe more in this exception
        }

        doctorData.remove(sIndex);
    }

    //initialize table
    public void createTable(){
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("idProperty"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("nameProperty"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("departmentNameProperty"));

        FilteredList<doctor> fData = new FilteredList<>(doctorData, p -> true);
        doctorTabletv.setItems(fData);
        doctorSearchtf.textProperty().addListener((observable, oldValue, newValue) -> {
            fData.setPredicate(doc -> {
                if(newValue == null || newValue.isEmpty())
                    return true;

                String lCaseFilter = newValue.toLowerCase();
                switch(searchcb.getValue()) {
                    case "doctorID":
                        if(doc.getIdProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "doctorName":
                        if(doc.getNameProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "departmentName":
                        if(doc.getDepartmentNameProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                }
                return false;
            });
        });

    }

    //connects the database
    public void connectDB(){

        try{
            /*
            //commands to open up database
            Class.forName("com.mysql.jdbc.Driver"); //maybe add .newInstance();
            String connectionStringURL =  "jdbc:mysql://us-cdbr-azure-west-b.cleardb.com:3306/acsm_54270fa45d472fa";
            mysql = DriverManager.getConnection(connectionStringURL, "b1d0beb2ed12fc", "ba632151");
            //if no connection let user know it failed, if connect works print success
              */
            //Connection conn = null; //new
            mainController mainDoc = new mainController(); //new
            mysql = mainDoc.connectTheDB(); //new

            if(mysql == null) //maybe mysql
                System.out.println("Connection Failed");
            else
                System.out.println("Success Connecting to Doctor");

            String searchStatement = "select * from doctor";
            //st = mysql.createStatement(); //old
            st = mysql.createStatement(); //new

            rs = st.executeQuery(searchStatement);
            while(rs.next()){
                doctor doc = new doctor();
                doc.setIdProperty(rs.getString(1));
                doc.setNameProperty(rs.getString(2));
                doc.setDepartmentNameProperty(rs.getString(3));
                doctorData.add(doc);
            }
            st.close();
            st = null;
            //conn.close();
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
                    TableView<doctor> docTab;
                    docTab = doctorTabletv;
                    docTab.getColumns().get(0).setVisible(false);
                    docTab.getColumns().get(0).setVisible(true);

                });
            }
        }, 50);
    }



    //checks for duplicate used when editing or inputing data
    public boolean checkDuplicate(String s){
        for (doctor doc : doctorData){
            if(s.equals(doc.getIdProperty())){
                return true;
            }

        }
        return false;

    }

    //set edit table

    public void setEditable(){
        doctorTabletv.setEditable(true);
        //doctor id editable
        doctorIdColumn.setEditable(true);
        doctorIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorIdColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<doctor, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<doctor, String> t){
                doctor doc = ((doctor) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Doctor ID")
                            .text("This field cannot be left empty, please choose a valid ID number")
                            .showError();
                }
                //else if ID is duplicate show error
                else if (checkDuplicate(t.getNewValue())){
                    dataRefresh();
                    Notifications.create().title("Duplicate ID Error while Updating")
                            .text("An ID already exists for this number, please choose an ID that does not exist yet!")
                            .showError();
                }
                //else update data to dtabase and table
                else {
                    String dataUpdate = "update doctor set doctorID = ? where doctorName = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, doc.getNameProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    doc.setIdProperty(t.getNewValue());
                }
            }

        });
        //doctor name editable
        doctorNameColumn.setEditable(true);
        doctorNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorNameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<doctor, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<doctor, String> t){
                doctor doc = ((doctor) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if field is left empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Doctor Name")
                            .text("This field cannot be left empty, please choose a valid Name")
                            .showError();
                }
                //else update data to table and database
                else {
                    String dataUpdate = "update doctor set doctorName = ? where doctorID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, doc.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    doc.setNameProperty(t.getNewValue());
                }
            }
        });
        //doctor edpartment editable
        departmentColumn.setEditable(true);
        departmentColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        departmentColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<doctor, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<doctor, String> t){
                doctor doc = ((doctor) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if field is left empty error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating DoctorDepartment")
                            .text("This field cannot be left empty, please choose a valid Department Name")
                            .showError();
                }
                //else update information to table and datbase
                else {
                    String dataUpdate = "update doctor set departmentName = ? where doctorID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, doc.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    doc.setDepartmentNameProperty(t.getNewValue());
                }
            }
        });
        }
}
