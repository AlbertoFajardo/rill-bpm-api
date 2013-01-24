package nu.com.rill.analysis.data.hive;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import mondrian.spi.impl.JdbcDialectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HiveDialect extends mondrian.spi.impl.HiveDialect {

	private static final Log LOG = LogFactory.getLog(HiveDialect.class);
	
	public HiveDialect(Connection connection) throws SQLException {
		super(connection);
	}

	public static final JdbcDialectFactory FACTORY = new JdbcDialectFactory(
			HiveDialect.class, DatabaseProduct.HIVE) {
		protected boolean acceptsConnection(Connection connection) {

			try {
				final String productName = connection.getMetaData()
						.getDatabaseProductName();
				if (productName.toLowerCase().equals(DatabaseProduct.HIVE.name().toLowerCase())) {
					return true;
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}

			return super.acceptsConnection(connection);
		}

	};
	
	private Map<String, String> columnAliases;

	public final void setColumnAliases(Map<String, String> columnAliases) {
		this.columnAliases = columnAliases;
	}

	@Override
	public String generateOrderItem(String expr, boolean nullable,
			boolean ascending, boolean collateNullsLast) {
		
		if (columnAliases != null && columnAliases.containsKey(expr)) {
			LOG.info("Change order by item: " + expr + " to alias: " + columnAliases.get(expr));
			expr = columnAliases.get(expr);
		}
		
		return super.generateOrderItem(expr, nullable, ascending, collateNullsLast);
	}

	

}
