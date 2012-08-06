package org.saiku.service.olap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlapQueryServiceMySQLTests {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OlapQueryServiceMySQLTests.class);
	
	private static OlapMetaExplorer olapMetaExplorer;
	private static OlapQueryService olapQueryService;
    private static Properties testProps = new Properties();
    
	@BeforeClass
	public static void setup() throws IOException {
//		AbstractServiceUtils ast = new AbstractServiceUtils();
//		ast.initTestContext();
		IConnectionManager ic = new TConnectionManager();
//		String returned = OlapMetaExplorerTest.computeTestDataRoot(OlapQueryServiceMySQLTests.class);
		File f = new File(System.getProperty("java.io.tmpdir") + "/files/");
		f.mkdir();
		IDatasourceManager ds = new ClassPathResourceDatasourceManager(
				System.getProperty("java.io.tmpdir") + "/files/");
		InputStream inputStream = OlapQueryServiceMySQLTests.class
				.getResourceAsStream("connection-mysql.properties");
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
			if (cube.getCubeName().equals("[TF_CUBE]")) {
				salesCube = cube;
			}
		}
		Assert.assertNotNull(salesCube);
		final UUID query = UUID.randomUUID();
		olapQueryService.createNewOlapQuery(query.toString(), salesCube);
		
//		final String mdx = "SELECT {Hierarchize({[Measures].[Show Cnt]})} ON COLUMNS, " + 
//				"CrossJoin([Time].[Year].Members, " +
//				"CrossJoin([Regions].[Region].Members, " +
//				"[WInfo].[Account].Members)) ON ROWS " + 
//				"FROM [TF_CUBE]";
		
		final String mdx = "SELECT {Hierarchize({[Measures].[Show Cnt]})} ON COLUMNS," +
				"Hierarchize(Union(CrossJoin({[Time].[2011]}, CrossJoin([Regions].[Region].Members, " +
				"{[WInfo].[张三的帐户].[张三的计划A].[张三的单元A].[张三买的关键词A2]})), CrossJoin({[Time].[2011]}, " +
				"CrossJoin([Regions].[Region].Members, {[WInfo].[张三的帐户].[张三的计划A].[张三的单元B].[张三买的关键词B1]})))) ON ROWS " +
				"FROM [TF_CUBE]";
		
		CellDataSet cds = olapQueryService.executeMdx(query.toString(), mdx);
		LOGGER.info("Execute MDX[" + mdx + "] result : " + ToStringBuilder.reflectionToString(cds));
		
		sleep(2000L);
		
		cds = olapQueryService.executeMdx(query.toString(), mdx);
		LOGGER.info("Execute MDX[" + mdx + "] result : " + ToStringBuilder.reflectionToString(cds));
		
		final String mdx2 = "SELECT {Hierarchize({[Measures].[Show Cnt]})} ON COLUMNS," +
				"Hierarchize(CrossJoin({[Time].[2011]}, CrossJoin([Regions].[东北], " +
				"{[WInfo].[张三的帐户].[张三的计划A].[张三的单元A].[张三买的关键词A2]}))) ON ROWS " +
				"FROM [TF_CUBE]";
		
		cds = olapQueryService.executeMdx(query.toString(), mdx2);
		LOGGER.info("Execute MDX[" + mdx2 + "] result : " + ToStringBuilder.reflectionToString(cds));
		
		sleep(2000L);
		
		cds = olapQueryService.executeMdx(query.toString(), mdx2);
		LOGGER.info("Execute MDX[" + mdx2 + "] result : " + ToStringBuilder.reflectionToString(cds));
		
//		sleep(2000L);
//		
//		// Concurrent execute
//		int threadPoolSize = 10;
//		ScheduledExecutorService ses = Executors.newScheduledThreadPool(threadPoolSize);
//		for (int i = 0; i < threadPoolSize; i++) {
//			ses.scheduleAtFixedRate(new Runnable() {
//
//				@Override
//				public void run() {
//					CellDataSet scheduleCds = olapQueryService.executeMdx(query.toString(), mdx);
//					LOGGER.info("Schedule execute MDX[" + mdx + "] result : " + ToStringBuilder.reflectionToString(scheduleCds));
//				}
//
//			}, 1000, executeCostTime + 1000, TimeUnit.MILLISECONDS);
//		}
//		
//		sleep(10000L);
//		ses.shutdown();
		
	}

}
