package nu.com.rill.analysis.mondrian;

import java.util.List;

import mondrian.rolap.agg.SegmentCacheWorker;
import mondrian.spi.SegmentCache;

import org.junit.Assert;
import org.junit.Test;


public class InfinispanSegmentCacheTest {

//	@Test
//	public void test() {
//		InfinispanSegmentCache cache = new InfinispanSegmentCache();
//		Assert.assertNotNull(cache);
//	}

	@Test
	public void cacheService() {
		
		final List<SegmentCache> externalCache = SegmentCacheWorker.initCache();
		Assert.assertNotNull(externalCache);
		Assert.assertTrue(externalCache.size() == 1);
	}

}
