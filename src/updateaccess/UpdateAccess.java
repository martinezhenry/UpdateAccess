/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updateaccess;

/**
 *
 * @author hmartinez
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ucanaccess.converters.TypesMap.AccessType;
import net.ucanaccess.ext.FunctionType;
import net.ucanaccess.jdbc.UcanaccessConnection;
import net.ucanaccess.jdbc.UcanaccessDriver;


public class UpdateAccess {

    /**
     * @param args the command line arguments
     */
    
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateAccess.class.getName());
    
    Properties prop;
    
    @FunctionType(functionName = "justconcat", argumentTypes = {AccessType.TEXT, AccessType.TEXT}, returnType = AccessType.TEXT)
    public static String concat(String s1, String s2) {
        return s1 + " >>>>" + s2;
    }

    private static void dump(ResultSet rs, String exName)
            throws SQLException {
        System.out.println("-------------------------------------------------");
        System.out.println();
        System.out.println(exName + " result:");
        System.out.println();
        while (rs.next()) {
            System.out.print("| ");
            int j = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= j; ++i) {
                Object o = rs.getObject(i);
                System.out.print(o + " | ");
            }
            System.out.println();
            System.out.println();
        }
    }

    private static Connection getUcanaccessConnection(String pathNewDB) throws SQLException,
            IOException {
        //String url = UcanaccessDriver.URL_PREFIX + pathNewDB + ";newDatabaseVersion=V2003";
        String url = UcanaccessDriver.URL_PREFIX + pathNewDB + "";

        return DriverManager.getConnection(url, "sa", "");
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

        try {
            if (args.length == 0) {
                System.err.println("You must specify the Query");
                System.exit(99);
                //return;
            }
            UpdateAccess ex = new UpdateAccess();
            //ex.createTablesExample();
            //ex.insertData();
           
            //ex.executeQueryWithFunctions();
            //ex.executeQueryWithCustomFunction();
            //ex.executeLikeExample();
            //ex.showExtensions();
            //ex.transaction();
            String sql = args[0];
            
            ex.aplicateUpdateQuery(sql);
            ex.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Connection ucaConn;

    public UpdateAccess() {
        try {
            
            this.prop = new Properties();
            prop.load(new FileInputStream("config/updateAccess.properties"));
            String pathNewDB = prop.getProperty("pathMDB");
            if (pathNewDB != null) {
            this.ucaConn = getUcanaccessConnection(pathNewDB);
            } else {
                System.exit(99);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTablesExample() throws SQLException {
        Statement st = this.ucaConn.createStatement();
        st.execute("CREATE TABLE example1 (id  COUNTER PRIMARY KEY,descr text(400), number numeric(12,3), date0 datetime) ");
        st.close();

        st = this.ucaConn.createStatement();
        st.execute("CREATE TABLE example2 (id  COUNTER PRIMARY KEY,descr text(400))");
        st.close();

        st = this.ucaConn.createStatement();
        st.execute("CREATE TABLE example3 (id LONG PRIMARY KEY,descr text(400))");
        st.close();

        st = this.ucaConn.createStatement();
        st.execute("CREATE TABLE example4 (id  LONG PRIMARY KEY,descr text(400))");
        st.close();
    }

    private void executeLikeExample() throws SQLException {
        Statement st = null;
        try {
            st = this.ucaConn.createStatement();
            ResultSet rs = st.executeQuery("select descr from example2 where descr like 'P%'");
            dump(rs, "executeLikeExample STEP 1: like with standard  % jolly");

            st = this.ucaConn.createStatement();
            rs = st.executeQuery("select descr from example2 where descr like 'P*'");
            dump(rs, "executeLikeExample STEP 2: like with access * jolly");

            st = this.ucaConn.createStatement();
            rs = st.executeQuery("select descr from example2 where descr like 'P[A-F]###'");
            dump(rs, "executeLikeExample STEP 3: number and interval patterns");

            rs = st.executeQuery("select descr from example2 where descr like 'C#V##'");
            dump(rs, "executeLikeExample STEP 4: number pattern");
        } finally {
            if (st != null) {
                st.close();
            }
        }

    }

    private void executeQuery() throws SQLException {
        Statement st = null;
        try {
            st = this.ucaConn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * from example4");
            dump(rs, "executeQuery");
        } finally {
            if (st != null) {
                st.close();
            }
        }

    }

    private void executeQueryWithCustomFunction() throws SQLException {
        Statement st = null;
        try {
            UcanaccessConnection uc = (UcanaccessConnection) this.ucaConn;
            uc.addFunctions(this.getClass());
            st = this.ucaConn.createStatement();
            ResultSet rs = st.executeQuery("SELECT justConcat(descr,''&now()) from example1");
            dump(rs, "executeQueryWithCustomFunction");
        } finally {
            if (st != null) {
                st.close();
            }
        }

    }

    private void executeQueryWithFunctions() throws SQLException {
        Statement st = null;
        try {
            st = this.ucaConn.createStatement();
            ResultSet rs = st.executeQuery("SELECT IIf(descr='Show must go off','tizio','caio&sempronio'&'&Marco Amadei'&' '&now()& RTRIM(' I''m proud of you   ')) from example1");
            dump(rs, "executeQueryWithFunctions");
        } finally {
            if (st != null) {
                st.close();
            }
        }

    }

    private void insertData() throws SQLException {
        Statement st = null;
        try {
            st = this.ucaConn.createStatement();
            st
                    .execute("INSERT INTO example1 (descr,number,date0)  VALUES( 'Show must go off',-1110.55446,#11/22/2003 10:42:58 PM#)");
            st
                    .execute("INSERT INTO example1 (descr,number,date0)  VALUES( \"Show must go up and down\",-113.55446,#11/22/2003 10:42:58 PM#)");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'dsdsds')");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'aa')");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'aBa')");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'aBBBa')");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'PB123')");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'C1V23')");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'aca')");
            st.execute("INSERT INTO example2 (descr)  VALUES( 'Ada')");

            st.execute("INSERT INTO example3 (ID, descr)  VALUES(1,'DALLAS')");
            st.execute("INSERT INTO example3 (ID, descr)  VALUES(2,'MILANO')");
            st.execute("INSERT INTO example4 (ID, descr)  VALUES(2,'PARIS')");
            st.execute("INSERT INTO example4 (ID, descr)  VALUES(3,'LONDON')");

        } finally {
            if (st != null) {
                st.close();
            }
        }

    }

    private void transaction() throws SQLException {
        Statement st = null;
        try {
            this.ucaConn.setAutoCommit(false);
            st = this.ucaConn.createStatement();
            st.executeUpdate("update example4 set descr='Lugo di Romagna'");
            ResultSet rs = st.executeQuery("SELECT * FROM example4 ");
            dump(rs, "transaction: before rollback");
            this.ucaConn.rollback();
            st.close();
            st = this.ucaConn.createStatement();
            rs = st.executeQuery("SELECT * FROM example4 ");
            dump(rs, "transaction: after rollback");
            st.executeUpdate("update example4 set descr='Lugo di Romagna'");
            st.execute("insert into example4 (ID, descr)  values(5,'DALLAS')");
            this.ucaConn.commit();
            rs = st.executeQuery("SELECT * FROM example4 ");
            dump(rs, "transaction: after commit");
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }
    
    private void aplicateUpdateQuery(String sql){
        Statement st = null;
        try {
        this.ucaConn.setAutoCommit(false);
        st = this.ucaConn.createStatement();
        st.executeUpdate(sql);
        this.ucaConn.commit();
        } catch (Exception ex){
            try {
                this.ucaConn.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(UpdateAccess.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }  finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UpdateAccess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    

    private void showExtensions() throws SQLException {
        Statement st = null;
        try {

            st = this.ucaConn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM example3 full outer join example4 on (example3.id=example4.id)");
            dump(rs, "showExtensions: full outer join");

            st = this.ucaConn.createStatement();
            rs = st.executeQuery("SELECT * FROM example2 order by id desc limit 5 offset 1");
            dump(rs, "showExtensions: limit  and offset ");
        } finally {
            if (st != null) {
                st.close();
            }
        }

    }


}
