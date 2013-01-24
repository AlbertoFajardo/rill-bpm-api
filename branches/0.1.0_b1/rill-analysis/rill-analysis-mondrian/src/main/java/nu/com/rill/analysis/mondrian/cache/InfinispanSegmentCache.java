package nu.com.rill.analysis.mondrian.cache;

import java.util.List;

import mondrian.rolap.cache.MemorySegmentCache;
import mondrian.spi.SegmentBody;
import mondrian.spi.SegmentCache;
import mondrian.spi.SegmentHeader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;

public class InfinispanSegmentCache extends MemorySegmentCache implements SegmentCache {
	
	private static final Log LOGGER = LogFactory.getLog(InfinispanSegmentCache.class);
	
	private SpringEmbeddedCacheManager springEmbeddedCacheManager;
	public static final String DEFAULT_CACHE_NAME = "default";

	public InfinispanSegmentCache() {
		super();
		
		SpringEmbeddedCacheManagerFactoryBean factoryBean = new SpringEmbeddedCacheManagerFactoryBean();
		factoryBean.setConfigurationFileLocation(new ClassPathResource("mondrian-infinispan-config.xml"));
		try {
			factoryBean.afterPropertiesSet();
			springEmbeddedCacheManager = factoryBean.getObject();
			
			LOGGER.info("Infinispan initializing...");
			// Initialize cache
			springEmbeddedCacheManager.getCache(DEFAULT_CACHE_NAME);
			LOGGER.info("Infinispan initialized.");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public SegmentBody get(SegmentHeader header) {
		
		ValueWrapper vw = springEmbeddedCacheManager.getCache(DEFAULT_CACHE_NAME).get(header);
		if (vw == null || vw.get() == null) {
			LOGGER.debug("Evict and return null for not hit cache by segment header: " + header);
			springEmbeddedCacheManager.getCache(DEFAULT_CACHE_NAME).evict(header);
			return null;
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Hit cache and return " + vw.get() + " by segment header: " + header);
		}
		return (SegmentBody) vw.get();
	}

	@Override
	public boolean contains(SegmentHeader header) {
		
		return get(header) != null;
	}

//	@SuppressWarnings("unchecked")
	@Override
	public List<SegmentHeader> getSegmentHeaders() {
		
//		Set<SegmentHeader> headerSet = (Set<SegmentHeader>) springEmbeddedCacheManager.getCache(DEFAULT_CACHE_NAME).getNativeCache().keySet();
//		return new ArrayList<SegmentHeader>(headerSet);
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean put(final SegmentHeader header, SegmentBody body) {
		
		springEmbeddedCacheManager.getCache(DEFAULT_CACHE_NAME).put(header, body);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Put into cache " + body + " by segment header: " + header);
		}
		
		fireSegmentCacheEvent(new SegmentCache.SegmentCacheListener.SegmentCacheEvent() {
			public boolean isLocal() {
				return true;
			}

			public SegmentHeader getSource() {
				return header;
			}

			public EventType getEventType() {
				return SegmentCacheListener.SegmentCacheEvent.EventType.ENTRY_CREATED;
			}
		});
		return true;
	}

	@Override
	public boolean remove(final SegmentHeader header) {

		SegmentBody sb = get(header);
		final boolean result = (sb == null);
		springEmbeddedCacheManager.getCache(DEFAULT_CACHE_NAME).evict(header);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Evict from cache " + ObjectUtils.getDisplayString(sb) + " by segment header: " + header);
		}
		if (result) {
			fireSegmentCacheEvent(new SegmentCache.SegmentCacheListener.SegmentCacheEvent() {
				public boolean isLocal() {
					return true;
				}

				public SegmentHeader getSource() {
					return header;
				}

				public EventType getEventType() {
					return SegmentCacheListener.SegmentCacheEvent.EventType.ENTRY_DELETED;
				}
			});
		}
		return result;
	}

	@Override
	public void tearDown() {
		
		super.tearDown();
		
		springEmbeddedCacheManager.stop();
	}

}
