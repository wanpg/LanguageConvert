package com.language.dao;

import com.language.model.Trans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by wangjinpeng on 16/4/19.
 */
public class LanguageDao extends Dao {

    public void writeToSqlite(ArrayList<Trans> list){
        // create a database connection
        try {
            ct = ConnDB.getConn();
            ct.setAutoCommit(false);

            String sql1 = "SELECT * FROM Langeage";
            ArrayList<Trans> listExists = new ArrayList<>();
            sm = ct.createStatement();
            rs = sm.executeQuery(sql1);
            while(rs.next()){
                listExists.add(parse(rs, false));
            }

//            String sql = "insert into Langeage (chinese, english) values(?, ?)";
            String sql = "replace into Langeage (chinese, english, traditional_chinese) values(?, ?, ?)";

            PreparedStatement statement = ct.prepareStatement(sql);
            for(Trans model : list){
                statement.setString(1, model.cnValue);
                statement.setString(2, model.enValue);
                statement.setString(3, model.twValue);
                statement.addBatch();
            }
            int[] updatedArray = statement.executeBatch();
            ct.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private Trans parse(ResultSet rs, boolean hasContent) throws SQLException{
        return Trans.create(rs.getString("chinese"), rs.getString("english"), rs.getString("traditional_chinese"));
    }
}
