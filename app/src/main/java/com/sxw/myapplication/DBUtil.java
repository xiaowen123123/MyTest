package com.sxw.myapplication;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String url = "jdbc:mysql://192.168.42.28:3306/mysql_db?useSSL=false&serverTimezone=UTC";
    private static final String username = "xiaowen";
    private static final String password = "123456";
    private static final String TAG = "xiaowen";

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.e(TAG, "加载JDBC驱动成功！");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return (Connection) DriverManager.getConnection(url, username, password);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
