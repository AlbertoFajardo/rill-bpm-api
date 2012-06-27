package nu.com.rill.analysis.data.hive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.jdbc.HiveDatabaseMetaData;
import org.apache.hadoop.hive.jdbc.HivePreparedStatement;
import org.apache.hadoop.hive.jdbc.HiveStatement;
import org.apache.hadoop.hive.service.HiveClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;

public class HiveDriver extends org.apache.hadoop.hive.jdbc.HiveDriver implements ApplicationContextAware {

	private static final Log LOGGER = LogFactory.getLog(HiveDriver.class);
	
	public Connection connect(String url, Properties info) throws SQLException {
		
		return (Connection) Proxy.newProxyInstance(
				ClassUtils.getDefaultClassLoader(),
				new Class[] {Connection.class},
				new ConnectionIgnoreMethodNotSupportedHandler(new SpringHiveConnection()));
	}
	
	private static ApplicationContext staticApplicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		staticApplicationContext = applicationContext;
	}

	private class SpringHiveConnection implements Connection {
		
		private HiveClient client = staticApplicationContext.getBean(HiveClient.class);

		
		public SpringHiveConnection() {
//			Statement stmt;
//			try {
//				stmt = createStatement();
//				stmt.execute("set hive.fetch.output.serde = org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe");
//
//			    stmt.close();
//			} catch (SQLException e) {
//				throw new RuntimeException(e);
//			}
		    
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Statement createStatement() throws SQLException {
			
			return new HiveStatement(this.client);
		}

		@Override
		public PreparedStatement prepareStatement(String sql)
				throws SQLException {
			return new HivePreparedStatement(this.client, sql);
		}

		@Override
		public CallableStatement prepareCall(String sql) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public String nativeSQL(String sql) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void setAutoCommit(boolean autoCommit) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public boolean getAutoCommit() throws SQLException {
			return true;
		}

		@Override
		public void commit() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void rollback() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void close() throws SQLException {
			
		}

		@Override
		public boolean isClosed() throws SQLException {
			return false;
		}

		@Override
		public DatabaseMetaData getMetaData() throws SQLException {
			return new HiveDatabaseMetaData(this.client);
		}

		@Override
		public void setReadOnly(boolean readOnly) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public boolean isReadOnly() throws SQLException {
			return false;
		}

		@Override
		public void setCatalog(String catalog) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public String getCatalog() throws SQLException {
			return "";
		}

		@Override
		public void setTransactionIsolation(int level) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public int getTransactionIsolation() throws SQLException {
			return 0;
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			return null;
		}

		@Override
		public void clearWarnings() throws SQLException {
			
		}

		@Override
		public Statement createStatement(int resultSetType,
				int resultSetConcurrency) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int resultSetType, int resultSetConcurrency)
				throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Map<String, Class<?>> getTypeMap() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void setHoldability(int holdability) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public int getHoldability() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Savepoint setSavepoint() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Savepoint setSavepoint(String name) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void rollback(Savepoint savepoint) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void releaseSavepoint(Savepoint savepoint) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Statement createStatement(int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int resultSetType, int resultSetConcurrency,
				int resultSetHoldability) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int autoGeneratedKeys) throws SQLException {
			return new HivePreparedStatement(this.client, sql);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int[] columnIndexes) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				String[] columnNames) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Clob createClob() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Blob createBlob() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public NClob createNClob() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public SQLXML createSQLXML() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public boolean isValid(int timeout) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public void setClientInfo(String name, String value)
				throws SQLClientInfoException {
			throw new SQLClientInfoException("Method not supported", null);
		}

		@Override
		public void setClientInfo(Properties properties)
				throws SQLClientInfoException {
			throw new SQLClientInfoException("Method not supported", null);
		}

		@Override
		public String getClientInfo(String name) throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Properties getClientInfo() throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Array createArrayOf(String typeName, Object[] elements)
				throws SQLException {
			throw new SQLException("Method not supported");
		}

		@Override
		public Struct createStruct(String typeName, Object[] attributes)
				throws SQLException {
			throw new SQLException("Method not supported");
		}
		
	}
	
	public HiveDriver() {
		
		super();
		try {
			DriverManager.deregisterDriver(DriverManager.getDriver("jdbc:hive://"));
			DriverManager.registerDriver(this);
		} catch (SQLException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
	
	private class DBMIgnoreMethodNotSupportedHandler implements InvocationHandler {
		
		private final DatabaseMetaData target;

		public DBMIgnoreMethodNotSupportedHandler(DatabaseMetaData target) {
			super();
			this.target = target;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on Connection interface coming in...
			// Invoke method on target Connection.
			try {
				Object retVal = method.invoke(this.target, args);
				return retVal;
			}
			catch (InvocationTargetException ex) {
				Throwable t = ex.getTargetException();
				if (t.getMessage().equals("Method not supported") && t instanceof SQLException) {
					LOGGER.warn("Ignore hive DatabaseMetaData not supported exception when call method: " + method.getName());
				}
				if (method.getName().equals("isReadOnly")) {
					throw t;
				}
			}
			return null;
		}
	}
	
	private class ConnectionIgnoreMethodNotSupportedHandler implements InvocationHandler {
		
		private final Connection target;

		public ConnectionIgnoreMethodNotSupportedHandler(Connection target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on Connection interface coming in...
			// Invoke method on target Connection.
			try {
				Object retVal = method.invoke(this.target, args);
				if (method.getName().equals("getMetaData")) {
					return (DatabaseMetaData) Proxy.newProxyInstance(
							ClassUtils.getDefaultClassLoader(),
							new Class[] {DatabaseMetaData.class},
							new DBMIgnoreMethodNotSupportedHandler((DatabaseMetaData) retVal));
				}
				return retVal;
			}
			catch (InvocationTargetException ex) {
				Throwable t = ex.getTargetException();
				if (t.getMessage().equals("Method not supported") && t instanceof SQLException) {
					LOGGER.warn("Ignore hive connection not supported exception when call method: " + method.getName());
				}
			}
			return null;
		}
	}
	
}
