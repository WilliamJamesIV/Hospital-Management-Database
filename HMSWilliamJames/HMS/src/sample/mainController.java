package sample;

import javafx.fxml.Initializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

//main controller class
public class mainController implements Initializable {

    private static Connection mysql;
    private static PreparedStatement pst;
    private static ResultSet data;

    private final String driver = "com.mysql.jdbc.Driver";
    private final String connectionStringURl = "";
    private final String password = "" ;
    private final String username = "";


    //connects to the database
    //maybe i need to return the mysql connection
    //also I might have to close it somehow
    public void connectDB(){
        try{
            //commands to open up database
            Class.forName("com.mysql.jdbc.Driver");
            String connectionStringURL =  "jdbc:mysql://us-cdbr-azure-west-b.cleardb.com:3306/acsm_54270fa45d472fa";
            mysql = DriverManager.getConnection(connectionStringURL, "b1d0beb2ed12fc", "ba632151");
            //if no connection let user know it failed, if connect works print success
            if(mysql == null)
                System.out.println("Connection Failed");
            else
                System.out.println("Success to main");
        }
        //exception
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    //connects the database and returns the connection
    public static Connection connectTheDB(){
        try{
            //commands to open up database
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String connectionStringURL =  "jdbc:mysql://us-cdbr-azure-west-b.cleardb.com:3306/acsm_54270fa45d472fa";
            mysql = DriverManager.getConnection(connectionStringURL, "b1d0beb2ed12fc", "ba632151");
            //if no connection let user know it failed, if connect works print success
            if(mysql == null)
                System.out.println("Connection Failed");
            else {
                System.out.println("Success to main");

            }

        }
        //exception
        catch(Exception ex){
            ex.printStackTrace();
        }
        return mysql;
    }

    //may not need
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*connectDB();*/
    }
}
