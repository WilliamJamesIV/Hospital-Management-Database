package sample;

/**
 * Created by will on 11/25/2016.
 */

import org.controlsfx.control.Notifications;
import sample.staff;

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

//class for staff controller
public class staffController implements Initializable {

    private Connection mysql;
    private PreparedStatement ps = null;
    private Statement st = null;

    private ResultSet rs;

    @FXML
    private Button btSave;
    @FXML
    private TextField staffSearchtf;
    @FXML
    private Button addStaffbt;
    @FXML
    private ComboBox<String> searchcb;
    @FXML
    private TableView<staff> staffTabletv;
    @FXML
    private TableColumn staffIdColumn;
    @FXML
    private TableColumn staffNameColumn;
    @FXML
    private TableColumn jobTypeColumn;
    @FXML
    private TextField addStaffIDtf;
    @FXML
    private TextField addStaffNametf;
    @FXML
    private TextField addJobTypetf;

    //lists that hold staff information
    private ObservableList<staff> staffData = FXCollections.observableArrayList();
    private ObservableList<staff> fsData = FXCollections.observableArrayList();

    //initializes everything
    @Override
    public void initialize(URL location, ResourceBundle resources) {

       createTable();
       setEditable();
        staffSearch();
        connectDB();
    }

    //creates csv reports for staff information
    public void generateStaffReports() throws IOException {

        try {
            Connection conn = null;
            FileWriter fw = new FileWriter("staff.csv");
            mainController iStaff = new mainController();

            conn = iStaff.connectTheDB();

            String insertStaff = "select * from staff";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(insertStaff);
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
            System.out.println("CSV File is created successfully.");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //adds a staff to database and list
    @FXML
    public void addStaffButton(){
        //if any text field is empty,show error
        if(addStaffIDtf.getText().equals("") || addStaffNametf.getText().equals("") || addJobTypetf.getText().equals("")){
            Notifications.create().title("Error Adding Staff Information")
                    .text("One of the Text fields is empty, Please insert valid information to all text fields!")
                    .showError();
        }
        //else if staff ID is a duplicate, show error
        else if (checkDuplicate(addStaffIDtf.getText())) {
            Notifications.create().title("Duplicate Error Adding Staff ID")
                    .text("Staff ID already exists. Please insert valid Staff ID that does not already exist!")
                    .showError();
        }

        else{

            String staffID = addStaffIDtf.getText();
            String staffName = addStaffNametf.getText();
            String jobType = addJobTypetf.getText();
            staff staf = new staff();
            staf.setStaffIDProperty(staffID);
            staf.setStaffNameProperty(staffName);
            staf.setJobTypeProperty(jobType);
            staffData.add(staf);
            String addButton = "insert into staff(staffID, staffName, jobType)"+"values (?,?,?)";
            try {
                ps = mysql.prepareStatement(addButton);
                ps.setString(1,staffID);
                ps.setString(2,staffName);
                ps.setString(3,jobType);

                ps.executeUpdate();
                ps.close();
                ps = null; //might have to change this or take it away
            }
            catch (SQLException e) {
                //e.printStackTrace();

            }
            addStaffIDtf.clear();
            addStaffNametf.clear();
            addJobTypetf.clear();

        }
    }

    //searches through staff information
    public void staffSearch(){
        searchcb.getItems().addAll("staffID","staffName" ,"jobType");
        searchcb.setValue("staffID");;
    }


    //deletes a doctor from list and database
    @FXML
    public void deleteStaffButton(){
        int sIndex = staffTabletv.getSelectionModel().getSelectedIndex();
        String sID = staffData.get(sIndex).getStaffIDProperty();

        String deleteStaff = "delete from staff where staffID = ?";
        try{
            ps = mysql.prepareStatement(deleteStaff);
            ps.setString(1,sID);
            ps.executeUpdate();
            ps.close();
            ps = null;

        } catch (SQLException e) {
            e.printStackTrace();
            //maybe more in this exception
        }
        staffData.remove(sIndex);
    }

    //initialize table
    public void createTable(){
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("staffIDProperty"));
        staffNameColumn.setCellValueFactory(new PropertyValueFactory<>("staffNameProperty"));
        jobTypeColumn.setCellValueFactory(new PropertyValueFactory<>("jobTypeProperty"));

        FilteredList<staff> fData = new FilteredList<>(staffData, p -> true);
        staffTabletv.setItems(fData);
        staffSearchtf.textProperty().addListener((observable, oldValue, newValue) -> {
            fData.setPredicate(doc -> {
                if(newValue == null || newValue.isEmpty())
                    return true;

                String lCaseFilter = newValue.toLowerCase();
                switch(searchcb.getValue()) {
                    case "staffID":
                        if(doc.getStaffIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "staffName":
                        if(doc.getStaffNameProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "jobType":
                        if(doc.getJobTypeProperty().toLowerCase().contains(lCaseFilter)){
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
            mainController mainStaff = new mainController(); //new
            mysql = mainStaff.connectTheDB(); //new

            if(mysql == null)
                System.out.println("Connection Failed");
            else
                System.out.println("Success Connecting to Staff");

            String searchStatement = "select * from staff";
            st = mysql.createStatement();
            rs = st.executeQuery(searchStatement);
            while(rs.next()){
                staff staf = new staff();
                staf.setStaffIDProperty(rs.getString(1));
                staf.setStaffNameProperty(rs.getString(2));
                staf.setJobTypeProperty(rs.getString(3));
                staffData.add(staf);
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
                    TableView<staff> staffTab;
                    staffTab = staffTabletv;
                    staffTab.getColumns().get(0).setVisible(false);
                    staffTab.getColumns().get(0).setVisible(true);

                });
            }
        }, 50);
    }

    //checks for duplicate
    public boolean checkDuplicate(String s){
        for (staff staf : staffData){
            if(s.equals(staf.getStaffIDProperty())){
                return true;
            }
        }
        return false;
    }

    //set edit table
    //need to insert errors on refresh
    public void setEditable(){
        staffTabletv.setEditable(true);
        //for staff ID
        staffIdColumn.setEditable(true);
        staffIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        staffIdColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<staff, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<staff, String> t){
                staff staf = ((staff) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Staff ID")
                            .text("Staff ID Text field is empty. Please insert valid Staff ID that does not already exist!")
                            .showError();
                }
                //else if staff ID is a duplicate show error
                else if (checkDuplicate(t.getNewValue())){
                    dataRefresh();
                    Notifications.create().title("Duplicate Error Updating Staff ID")
                            .text("Staff ID already exists. Please insert valid Staff ID that does not already exist!")
                            .showError();
                }
                //else update staff id
                else {
                    String dataUpdate = "update staff set staffID = ? where staffName = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, staf.getStaffNameProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    staf.setStaffIDProperty(t.getNewValue());
                }
            }

        });
        //for staff name
        staffNameColumn.setEditable(true);
        staffNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        staffNameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<staff, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<staff, String> t){
                staff staf = ((staff) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Staff Name")
                            .text("Staff Name Text field is empty. Please insert valid Staff Name!")
                            .showError();
                }
                //else update staff name
                else {
                    String dataUpdate = "update staff set staffName = ? where staffID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, staf.getStaffIDProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    staf.setStaffNameProperty(t.getNewValue());
                }
            }
        });
        //for staff job type
        jobTypeColumn.setEditable(true);
        jobTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        jobTypeColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<staff, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<staff, String> t){
                staff staf = ((staff) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty,show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Staff JobType")
                            .text("Staff Job Type Text field is empty. Please insert valid Staff Job Type!")
                            .showError();
                }
                //else update staff job type
                else {
                    String dataUpdate = "update staff set jobType = ? where staffID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, staf.getStaffIDProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    staf.setJobTypeProperty(t.getNewValue());
                }

            }
        });
    }
}
