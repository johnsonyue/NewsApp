package johnsonyue.newscapturer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.PropertyResourceBundle;

public class DBHelper {
	private String url;
	private String user;
	private String password;
	
	private Connection conn;
	private PropertyResourceBundle bundle;
	
	public DBHelper(){
		try{
			bundle = new PropertyResourceBundle(DBHelper.class
				.getResourceAsStream("DBConfig.properties"));
			url=bundle.getString("url");
			user=bundle.getString("user");
			password=bundle.getString("password");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public Connection getConnection(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection(url, user, password);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return conn;
	}
	
	public void closeDB(){
		try {
			conn.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
