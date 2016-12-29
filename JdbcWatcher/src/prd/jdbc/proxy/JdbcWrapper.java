package prd.jdbc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;



public class JdbcWrapper {

	public static JdbcWrapper INSTANCE = new JdbcWrapper();
	
	static final AtomicInteger USED_CONNECTION_NUM = new AtomicInteger();
	static final AtomicLong TRANSACTION_NUM = new AtomicLong();
	static final AtomicInteger ACTIVE_CONNECTION_NUM = new AtomicInteger();
	
	private JdbcWrapper(){}

	public Connection createConnectionProxy(Connection connection) {
		InvocationHandler handler = new ConnectionInvocationHandler(connection);
		Connection result = (Connection)Proxy.newProxyInstance(connection.getClass().getClassLoader(),
				new Class[]{Connection.class}, handler);
		if(result != connection) {
			USED_CONNECTION_NUM.incrementAndGet();
			TRANSACTION_NUM.incrementAndGet();
		}
		return result;
	}
	
	private Statement createStatementProxy(String sql, Statement statement) {
		InvocationHandler handler = new StatementInvocationHandler(sql, (Statement)statement);
		return (Statement)Proxy.newProxyInstance(statement.getClass().getClassLoader(), new Class[]{Statement.class,PreparedStatement.class}, handler);
		
	}
	
	private class StatementInvocationHandler implements InvocationHandler {

		private String sql;
		private Statement statement;
		
		public StatementInvocationHandler(String sql, Statement statement) {
			this.sql = sql;
			this.statement = statement;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(method.getName().startsWith("execute")) {
				if(args != null && args.length > 0 && args[0] instanceof String) {
					this.sql = (String)args[0];
				}
				this.sql = String.valueOf(this.sql);
			}
			ACTIVE_CONNECTION_NUM.incrementAndGet();
			try{
				return method.invoke(statement, args);
			} finally {
				ACTIVE_CONNECTION_NUM.decrementAndGet();
			}
		}
		
	}
	
	private class ConnectionInvocationHandler implements InvocationHandler {

		private Connection connection;
		
		public ConnectionInvocationHandler(Connection connection) {
			this.connection = connection;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try{
				String sql = null;
				if(method.getName().equals("prepareStatement") || 
						method.getName().equals("prepareCall")) {
					sql = (String)args[0];
				}
				
				Object result = method.invoke(connection, args);
				if(result instanceof Statement) {
					result = createStatementProxy(sql, (Statement)result);
				}
				return result;
			} finally {
				if(method.getName().equals("close")) {
					USED_CONNECTION_NUM.decrementAndGet();
				}
			}
		}
		
	}

	public static int getUSED_CONNECTION_NUM() {
		return USED_CONNECTION_NUM.get();
	}

	public static long getTRANSACTION_NUM() {
		return TRANSACTION_NUM.get();
	}

	public static int getACTIVE_CONNECTION_NUM() {
		return ACTIVE_CONNECTION_NUM.get();
	}
}
