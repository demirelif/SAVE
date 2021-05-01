package com.example.saveandroid;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    Connection con;
    String uname, password, IP, port, database;


    public Connection connection(){
        IP = "";
        uname = "";
        port = "";
        database = "";
        password = "";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connection connection = null;
        String ConnectionURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + IP + ":" + port + ";databasename=" + database + ";user=" + uname + ";password=" + password + ";";
            connection = DriverManager.getConnection(ConnectionURL);
        }
        catch (Exception exception){
            Log.e("Database", exception.getMessage());
        }

        return connection;
    }

}
