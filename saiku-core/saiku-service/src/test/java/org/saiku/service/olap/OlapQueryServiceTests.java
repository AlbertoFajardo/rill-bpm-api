package org.saiku.service.olap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.saiku.AbstractServiceUtils;
import org.saiku.TConnectionManager;
import org.saiku.datasources.connection.IConnectionManager;
import org.saiku.datasources.datasource.SaikuDatasource;
import org.saiku.olap.discover.OlapMetaExplorer;
import org.saiku.olap.discover.OlapMetaExplorerTest;
import org.saiku.olap.dto.SaikuCube;
import org.saiku.olap.dto.resultset.CellDataSet;
import org.saiku.service.datasource.ClassPathResourceDatasourceManager;
import org.saiku.service.datasource.DatasourceService;
import org.saiku.service.datasource.IDatasourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlapQueryServiceTests {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OlapQueryServiceTests.class);
	
	private static OlapMetaExplorer olapMetaExplorer;
	private static OlapQueryService olapQueryService;
    private static Properties testProps = new Properties();
    
	@BeforeClass
	public static void setup() throws IOException {
		AbstractServiceUtils ast = new AbstractServiceUtils();
		ast.initTestContext();
		IConnectionManager ic = new TConnectionManager();
		String returned = OlapMetaExplorerTest.computeTestDataRoot(OlapMetaExplorerTest.class);
		File f = new File(System.getProperty("java.io.tmpdir") + "/files/");
		f.mkdir();
		IDatasourceManager ds = new ClassPathResourceDatasourceManager(
				System.getProperty("java.io.tmpdir") + "/files/");
		InputStream inputStream = OlapMetaExplorerTest.class
				.getResourceAsStream("connection.properties");
		try {
			testProps.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ds.setDatasource(new SaikuDatasource("test", SaikuDatasource.Type.OLAP,
				testProps));
		ic.setDataSourceManager(ds);
		olapMetaExplorer = new OlapMetaExplorer(ic);

		olapQueryService = new OlapQueryService();
		OlapDiscoverService olapDiscoverService = new OlapDiscoverService();
		DatasourceService dsService = new DatasourceService();
		dsService.setConnectionManager(ic);
		olapDiscoverService.setDatasourceService(dsService);
		olapQueryService.setOlapDiscoverService(olapDiscoverService);
	}
	
	private void sleep(long sleep) {
		
		try {
			LOGGER.info("Sleep " + sleep);
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testQuery() {
		
		List<SaikuCube> cubes = olapMetaExplorer.getAllCubes();
		SaikuCube salesCube = null;
		for (SaikuCube cube : cubes) {
			if (cube.getCubeName().equals("[Sales]")) {
				salesCube = cube;
			}
		}
		Assert.assertNotNull(salesCube);
		final UUID query = UUID.randomUUID();
		olapQueryService.createNewOlapQuery(query.toString(), salesCube);
		
		final String mdx = "SELECT NON EMPTY {Hierarchize({[Measures].[Sales Count]})} ON COLUMNS, " +
				"NON EMPTY {Hierarchize({{[Customers].[Country].Members}, {[Customers].[State Province].Members}})} ON ROWS" +
				" FROM [Sales]";
		
		CellDataSet cds = olapQueryService.executeMdx(query.toString(), mdx);
		LOGGER.info("Execute MDX[" + mdx + "] result : " + ToStringBuilder.reflectionToString(cds));
		
		sleep(2000L);
		
		long beforeExecuteMdx = System.currentTimeMillis();
		cds = olapQueryService.executeMdx(query.toString(), mdx);
		long executeCostTime = System.currentTimeMillis() - beforeExecuteMdx;
		LOGGER.info("Execute MDX[" + mdx + "] result : " + ToStringBuilder.reflectionToString(cds));
		
		sleep(2000L);
		
		// Concurrent execute
		int threadPoolSize = 10;
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(threadPoolSize);
		for (int i = 0; i < threadPoolSize; i++) {
			ses.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					CellDataSet scheduleCds = olapQueryService.executeMdx(query.toString(), mdx);
					LOGGER.info("Schedule execute MDX[" + mdx + "] result : " + ToStringBuilder.reflectionToString(scheduleCds));
				}

			}, 1000, executeCostTime + 1000, TimeUnit.MILLISECONDS);
		}
		
		sleep(10000L);
		ses.shutdown();
		
	}

}
