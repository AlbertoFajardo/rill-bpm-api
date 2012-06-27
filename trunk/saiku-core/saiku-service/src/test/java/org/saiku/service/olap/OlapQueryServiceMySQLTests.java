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
import org.saiku.AbstractServiceUtils;
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
		
		final String mdx = "SELECT {Hierarchize({[Measures].[Show Cnt]})} ON COLUMNS, " + 
				"CrossJoin([Time].[Year].Members, " +
				"CrossJoin([Regions].[Region].Members, " +
				"[WInfo].[Account].Members)) ON ROWS " + 
				"FROM [TF_CUBE]";
		
		CellDataSet cds = olapQueryService.executeMdx(query.toString(), mdx);
		LOGGER.info("Execute MDX[" + mdx + "] result : " + ToStringBuilder.reflectionToString(cds));
		
	}

}
