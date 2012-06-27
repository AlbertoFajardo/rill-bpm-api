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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.saiku.TConnectionManager;
import org.saiku.datasources.connection.IConnectionManager;
import org.saiku.datasources.datasource.SaikuDatasource;
import org.saiku.olap.discover.OlapMetaExplorer;
import org.saiku.olap.dto.SaikuCube;
import org.saiku.olap.dto.resultset.CellDataSet;
import org.saiku.service.datasource.ClassPathResourceDatasourceManager;
import org.saiku.service.datasource.DatasourceService;
import org.saiku.service.datasource.IDatasourceManager;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class OlapQueryServiceHiveTests {
	
	private static final Log LOGGER = LogFactory.getLog(OlapQueryServiceHiveTests.class);
	
	private static final String[] CONFIGS = new String[] { "classpath:org/saiku/service/olap/applicationContext-hive.xml" };
	
	private static OlapMetaExplorer olapMetaExplorer;
	private static OlapQueryService olapQueryService;
    private static Properties testProps = new Properties();
    
	@BeforeClass
	public static void setup() throws IOException {
		
		LOGGER.info("Log test");
		
		// Start hive server
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(CONFIGS);
		// shutdown the context along with the VM
		ctx.registerShutdownHook();
		
		IConnectionManager ic = new TConnectionManager();
		File f = new File(System.getProperty("java.io.tmpdir") + "/files/");
		f.mkdir();
		IDatasourceManager ds = new ClassPathResourceDatasourceManager(
				System.getProperty("java.io.tmpdir") + "/files/");
		InputStream inputStream = OlapQueryServiceHiveTests.class
				.getResourceAsStream("hadoop.properties");
		try {
			testProps.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ds.setDatasource(new SaikuDatasource("hive-ds", SaikuDatasource.Type.OLAP,
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
		SaikuCube tfCube = null;
		for (SaikuCube cube : cubes) {
			if (cube.getCubeName().equals("[TF_CUBE]")) {
				tfCube = cube;
			}
		}
		Assert.assertNotNull(tfCube);
		final UUID query = UUID.randomUUID();
		olapQueryService.createNewOlapQuery(query.toString(), tfCube);
		
		final String mdx = "SELECT {Hierarchize({[Measures].[Show Cnt]})} ON COLUMNS, " + 
						"CrossJoin([Time].[Year].Members, " +
						"CrossJoin([Regions].[Region].Members, " +
						"[WInfo].[Account].Members)) ON ROWS " + 
						"FROM [TF_CUBE]";
		
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
