package prd.jdbc.proxy;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

public class JdbcDriver implements Driver{
	
	private static JdbcDriver MYDRIVER = new JdbcDriver();
	
	static{
		try {
			DriverManager.registerDriver(MYDRIVER);
			
			for(Driver driver : Collections.list(DriverManager.getDrivers())) {
				if(driver != MYDRIVER) {
					DriverManager.deregisterDriver(driver);
					DriverManager.registerDriver(driver);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public JdbcDriver(){}
	
	public Connection connect(String url, Properties info) {
		String dummyDriver = info.getProperty("dummydriver");
		if(dummyDriver != null) {
			return null;
		}
		
		String driver = info.getProperty("driver");
		if(driver != null) {
			try {
				Class.forName(driver, true, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		Properties temp = (Properties)info.clone();
		temp.put("dummydriver","dummydriver");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, temp);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return JdbcWrapper.INSTANCE.createConnectionProxy(connection);
	}

	public boolean acceptsURL(String url) throws SQLException {
		return false;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return null;
	}

	public int getMajorVersion() {
		return 0;
	}

	public int getMinorVersion() {
		return 0;
	}

	public boolean jdbcCompliant() {
		return false;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	

}
