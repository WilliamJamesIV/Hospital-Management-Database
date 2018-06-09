package sample;


/**
 * Created by will on 11/25/2016.
 */

import org.controlsfx.control.Notifications;
import sample.room;

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


//class for room controller
public class roomController implements Initializable{

    //for connection
    private Connection mysql = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private ResultSet rs;

    //assign names for room tab
    @FXML
    private Button btSave;
    @FXML
    private TextField roomSearchtf;
    @FXML
    private Button addRoombt;
    @FXML
    private ComboBox<String> searchcb;
    @FXML
    private TableView<room> roomTabletv;
    @FXML
    private TableColumn roomIDColumn;
    @FXML
    private TableColumn staffIDColumn;
    @FXML
    private TableColumn roomStatusColumn;
    @FXML
    private TableColumn departmentIDColumn;
    @FXML
    private TableColumn hospitalIDColumn;
    @FXML
    private TextField addRoomIDtf;
    @FXML
    private TextField addStaffIDtf;
    @FXML
    private TextField addRoomStatustf;
    @FXML
    private TextField addDepartmentIDtf;
    @FXML
    private TextField addHospitalIDtf;

    //creates lists for room
    private ObservableList<room> roomData = FXCollections.observableArrayList();
    private ObservableList<room> fData = FXCollections.observableArrayList();

    //initilizes everything
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createTable();
        setEditable();
        roomSearch();
        connectDB();
    }


    //creates the csv file
    public void generateRoomReports() throws IOException {

        try {
            Connection conn = null;
            FileWriter fw = new FileWriter("room.csv");
            mainController iRoom = new mainController();

            conn = iRoom.connectTheDB();

            //inner joins tables staff, department and hospital
            //It will produce the name of staff that corresponds with the staff id
            //the name of department that corresponds with the department ID
            //The location of the hospital that corresponds with the hospitalID
            String insertRoom = "select room.roomID, staff.staffName, room.roomStatus, department.departmentName, hospital.location as hospital_Location\n" +
                    "from room inner join staff on room.staffID = staff.staffID inner join department\n" +
                    "on room.departmentID = department.departmentID inner join hospital on room.hospitalID = hospital.hospitalID order by roomID Asc;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(insertRoom);
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
            System.out.println("CSV File is created successfully for room information.");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    //adds a room
    @FXML
    public void addRoomButton(){
        //if any text field is empty, show error
        if(addRoomIDtf.getText().equals("") || addStaffIDtf.getText().equals("") || addRoomStatustf.getText().equals("")
                || addDepartmentIDtf.getText().equals("") || addHospitalIDtf.getText().equals("")){
            Notifications.create().title("Error Adding Room Information")
                    .text("One of the Text fields is empty. Please insert valid information to all text fields!")
                    .showError();
        }
        //if room id already exists, show error
        else if (checkDuplicate(addRoomIDtf.getText())) {
            Notifications.create().title("Duplicate Error Adding Room ID")
                    .text("Room ID already exists. Please insert a valid Room ID that does not already exist!")
                    .showError();
        }
        //else add room info to table and database
        else{

            String roomID = addRoomIDtf.getText();
            String staffID = addStaffIDtf.getText();
            String roomStatus = addRoomStatustf.getText();
            String departmentID = addDepartmentIDtf.getText();
            String hospitalID = addHospitalIDtf.getText();


            room rm = new room();
            rm.setIdProperty(roomID);
            rm.setStaffIDProperty(staffID);
            rm.setRoomStatusProperty(roomStatus);
            rm.setDepartmentIDProperty(departmentID);
            rm.setHospitalIDProperty(hospitalID);
            roomData.add(rm);
            String addButton = "insert into room(roomID, staffID, roomStatus,departmentID ,HospitalID)"+"values (?,?,?,?,?)";
            try {
                ps = mysql.prepareStatement(addButton);
                ps.setString(1,roomID);
                ps.setString(2,staffID);
                ps.setString(3,roomStatus);
                ps.setString(4,departmentID);
                ps.setString(5,hospitalID);
                ps.executeUpdate();
                ps.close();
                ps = null; //might have to change this or take it away
            }
            catch (SQLException e) {
                //e.printStackTrace();

            }
            addRoomIDtf.clear();
            addStaffIDtf.clear();
            addRoomStatustf.clear();
            addDepartmentIDtf.clear();
            addHospitalIDtf.clear();

        }


    }

    //searches for data in room tab
    public void roomSearch(){
        searchcb.getItems().addAll("roomID","staffID","roomStatus", "departmentID", "hospitalID");
        searchcb.setValue("roomID");;
    }


    //deletes a room from list and database
    @FXML
    public void deleteRoomButton(){
        int sIndex = roomTabletv.getSelectionModel().getSelectedIndex();
        String sID = roomData.get(sIndex).getIdProperty();

        String deleteRoom = "delete from room where roomID = ?";
        try{
            ps = mysql.prepareStatement(deleteRoom);
            ps.setString(1,sID);
            ps.executeUpdate();
            ps.close();
            ps = null;

        } catch (SQLException e) {
            e.printStackTrace();
            //maybe more in this exception
        }

        roomData.remove(sIndex);
    }

    //initialize table with room information
    public void createTable(){
        roomIDColumn.setCellValueFactory(new PropertyValueFactory<>("idProperty"));
        staffIDColumn.setCellValueFactory(new PropertyValueFactory<>("StaffIDProperty"));
        roomStatusColumn.setCellValueFactory(new PropertyValueFactory<>("roomStatusProperty"));
        departmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("departmentIDProperty"));
        hospitalIDColumn.setCellValueFactory(new PropertyValueFactory<>("hospitalIDProperty"));

        FilteredList<room> fData = new FilteredList<>(roomData, p -> true);
        roomTabletv.setItems(fData);
        roomSearchtf.textProperty().addListener((observable, oldValue, newValue) -> {
            fData.setPredicate(rm -> {
                if(newValue == null || newValue.isEmpty())
                    return true;

                String lCaseFilter = newValue.toLowerCase();
                switch(searchcb.getValue()) {
                    case "roomID":
                        if(rm.getIdProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "staffID":
                        if(rm.getStaffIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                    case "roomStatus":
                        if(rm.getRoomStatusProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "departmentID":
                        if(rm.getDepartmentIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;

                    case "hospitalID":
                        if(rm.getHospitalIDProperty().toLowerCase().contains(lCaseFilter)){
                            return true;
                        }
                        break;
                }
                return false;
            });
        });

    }

    //conects the database and inserts room info to tables
    public void connectDB(){

        try{
            /*
            //commands to open up database
            Class.forName("com.mysql.jdbc.Driver"); //maybe add .newInstance();
            String connectionStringURL =  "jdbc:mysql://us-cdbr-azure-west-b.cleardb.com:3306/acsm_54270fa45d472fa";
            mysql = DriverManager.getConnection(connectionStringURL, "b1d0beb2ed12fc", "ba632151");
            //if no connection let user know it failed, if connect works print success
            */
            mainController mainRoom = new mainController(); //new
            mysql = mainRoom.connectTheDB(); //new
            if(mysql == null)
                System.out.println("Connection Failed to room");
            else
                System.out.println("Success connecting to room");

            String searchStatement = "select * from room";
            st = mysql.createStatement();
            rs = st.executeQuery(searchStatement);
            while(rs.next()){
                room rm = new room();
                rm.setIdProperty(rs.getString(1));
                rm.setStaffIDProperty(rs.getString(2));
                rm.setRoomStatusProperty(rs.getString(3));
                rm.setDepartmentIDProperty(rs.getString(4));
                rm.setHospitalIDProperty(rs.getString(5));
                roomData.add(rm);
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
                    TableView<room> roomTab;
                    roomTab = roomTabletv;
                    roomTab.getColumns().get(0).setVisible(false);
                    roomTab.getColumns().get(0).setVisible(true);

                });
            }
        }, 50);
    }



    //checks for duplicate
    public boolean checkDuplicate(String s){
        for (room rm : roomData){
            if(s.equals(rm.getIdProperty())){
                return true;
            }

        }
        return false;

    }

    //set edit table
    //need to insert errors on refresh
    public void setEditable(){
        roomTabletv.setEditable(true);
        //room id editable
        roomIDColumn.setEditable(true);
        roomIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        roomIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<room, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<room, String> t){
                room rm = ((room) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Room ID")
                            .text("Room ID Text Field is Empty, Please insert a valid Room ID that does not already exist!")
                            .showError();
                }
                //if room id already exists, show error
                else if (checkDuplicate(t.getNewValue())){
                    dataRefresh();
                    Notifications.create().title("Duplicate Error Updating Room ID")
                            .text("Room ID already exists, Please insert a valid Room ID that does not already exist!")
                            .showError();
                }
                else {
                    String dataUpdate = "update room set roomID = ? where staffID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, rm.getStaffIDProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    rm.setIdProperty(t.getNewValue());
                }
            }

        });
        //staff id editable
        staffIDColumn.setEditable(true);
        staffIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        staffIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<room, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<room, String> t){
                room rm = ((room) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Room's Staff ID")
                            .text("Room's Staff ID Text Field is Empty, Please insert a valid Room's Staff ID that exists!")
                            .showError();
                }
                 /*
                 //else if staff id doesn't exist, show error
                else if (checkDuplicate(t.getNewValue())){
                dataRefresh();
                Notifications.create().title("Duplicate Error Updating Treatment ID")
                        .text("Treatment ID already exists. Please insert a valid Treatment ID that does not already exist!!")
                        .showError();
                }
                 */
                 //else update staff id
                else {
                    String dataUpdate = "update room set staffID = ? where roomID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, rm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    rm.setStaffIDProperty(t.getNewValue());
                }
            }
        });
        //room status editbale
        roomStatusColumn.setEditable(true);
        roomStatusColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        roomStatusColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<room, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<room, String> t){
                room rm = ((room) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Room Status")
                            .text("Room Status Text Field is Empty, Please insert a valid Room Status(O or C)!")
                            .showError();
                }
                else {
                    String dataUpdate = "update room set roomStatus = ? where roomID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, rm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    rm.setRoomStatusProperty(t.getNewValue());
                }

            }
        });

        //department id editabel
        departmentIDColumn.setEditable(true);
        departmentIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        departmentIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<room, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<room, String> t){
                room rm = ((room) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Room's Department ID")
                            .text("Room's Department ID Text Field is Empty, Please insert a valid Room's Department ID!")
                            .showError();
                }
                 /*
                 //else if department ID doesn't exist, show error
                else if (checkDuplicate(t.getNewValue())){
                dataRefresh();
                Notifications.create().title("Duplicate Error Updating Treatment ID")
                        .text("Treatment ID already exists. Please insert a valid Treatment ID that does not already exist!!")
                        .showError();
                }
                 */
                 //else update department ID
                else {
                    String dataUpdate = "update room set departmentID = ? where roomID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, rm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    rm.setDepartmentIDProperty(t.getNewValue());
                }

            }
        });
        //hospital id editable
        hospitalIDColumn.setEditable(true);
        hospitalIDColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        hospitalIDColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<room, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<room, String> t){
                room rm = ((room) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                //if text field is empty, show error
                if (t.getNewValue().length() == 0){
                    dataRefresh();
                    Notifications.create().title("Error Updating Room's Hospital ID")
                            .text("Room's Hospital ID Text Field is Empty, Please insert a valid Room's Hospital ID!")
                            .showError();
                }
                 /*
                 //else if hospital ID doesn't exists, show errir
                else if (checkDuplicate(t.getNewValue())){
                dataRefresh();
                Notifications.create().title("Duplicate Error Updating Treatment ID")
                        .text("Treatment ID already exists. Please insert a valid Treatment ID that does not already exist!!")
                        .showError();
                }
                 */
                 //else update hospital ID
                else {
                    String dataUpdate = "update room set hospitalID = ? where roomID = ? ";
                    try {
                        ps = mysql.prepareStatement(dataUpdate);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, rm.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;

                    } catch (SQLException e) {
                        e.printStackTrace();
                        //maybe add an exception code here
                    }
                    rm.setHospitalIDProperty(t.getNewValue());
                }

            }
        });


    }


}
