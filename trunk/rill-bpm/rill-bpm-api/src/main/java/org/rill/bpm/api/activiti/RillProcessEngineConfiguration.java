package org.rill.bpm.api.activiti;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.SpringTransactionInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.ThreadLocalResourceHolder;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * @author mengran
 * <p>
 *  Add transaction propagation obtain strategy that use thread-binding it first.
 * <p>
 *  Add schema check listener mechanism, use Observer and Proxy Pattern.
 *
 */
public class RillProcessEngineConfiguration extends SpringProcessEngineConfiguration implements BeanFactoryAware {

	protected final Log log = LogFactory.getLog(getClass().getName());
	
	private ApplicationEventPublisher heldApplicationEventPublisher;
	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory; 
	}
	
	@Override
	public ProcessEngine buildProcessEngine() {
		
		// Generate application context as event publisher
        if (heldApplicationEventPublisher == null) {
            // Means aware method have not been call.
            // FIXME: Adapt spring v2.0 implementation, we use new SimpleApplicationEventMulticaster() but not new SimpleApplicationEventMulticaster(BeanFactory)
            this.heldApplicationEventPublisher = new SchemaOperationEventListenerAdapter(new SimpleApplicationEventMulticaster());
        }
        
		ProcessEngine processEngine = super.buildProcessEngine();
        
        return processEngine;
	}
	
	
	
    private class SchemaOperationEventListenerAdapter implements ApplicationEventPublisher {

        private SimpleApplicationEventMulticaster saemc;

        public SchemaOperationEventListenerAdapter(SimpleApplicationEventMulticaster aemc) {
            this.saemc = aemc;
            String[] listenerNames = ((ListableBeanFactory) beanFactory).getBeanNamesForType(SchemaOperationEventListener.class);
            if (listenerNames != null && listenerNames.length > 0) {
                for (String listenerName : listenerNames) {
                    log.info("Add application listener named " + listenerName);
                    aemc.addApplicationListener((SchemaOperationEventListener) beanFactory.getBean(listenerName));
                }
            }
        }

        public void publishEvent(ApplicationEvent event) {

            // Delegate operation
            this.saemc.multicastEvent(event);
        }
    }
	
	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
		if (transactionManager == null) {
			throw new ActivitiException(
					"transactionManager is required property for SpringProcessEngineConfiguration, use "
							+ StandaloneProcessEngineConfiguration.class
									.getName() + " otherwise");
		}

		List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired
				.add(new ControlPropagationSpringTransactionInterceptor(transactionManager,
						TransactionTemplate.PROPAGATION_REQUIRED));
		CommandContextInterceptor commandContextInterceptor = new CommandContextInterceptor(
				commandContextFactory, this);
		defaultCommandInterceptorsTxRequired.add(commandContextInterceptor);
		return defaultCommandInterceptorsTxRequired;
	}

	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequiresNew.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequiresNew
				.add(new ControlPropagationSpringTransactionInterceptor(transactionManager,
						TransactionTemplate.PROPAGATION_REQUIRES_NEW));
		CommandContextInterceptor commandContextInterceptor = new CommandContextInterceptor(
				commandContextFactory, this);
		defaultCommandInterceptorsTxRequiresNew.add(commandContextInterceptor);
		return defaultCommandInterceptorsTxRequiresNew;
	}
	
	  protected void initSessionFactories() {
		super.initSessionFactories();
		
		// Proxy it for apply Observer Pattern, so we can get notice of activiti's schema operation.
		proxyDbSqlSessionFactory();
	  }
	
	private void proxyDbSqlSessionFactory() {
		
		final SessionFactory sessionFactory = getSessionFactories().get(DbSqlSession.class);
		if (sessionFactory instanceof SpringProxy) {
			log.warn("DbSqlSessionFactory has been proxied. so we directly return." + sessionFactory);
			return;
		}
		
		final DbSqlSessionFactory dbSqlSessionFactory = (DbSqlSessionFactory) sessionFactory;
		ProxyFactory factory = new ProxyFactory(dbSqlSessionFactory);
		NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor(new MethodInterceptor() {
			
			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				
				return new ObservedDbSqlSession(dbSqlSessionFactory);
			}
		});
		advisor.addMethodName("openSession");
		factory.addAdvisor(advisor);
		
		SessionFactory proxied = (SessionFactory) factory.getProxy();
		log.info("Replace original DbSqlSessionFactory with it [" + proxied + "] that will return " + ObservedDbSqlSession.class.getName());
		addSessionFactory(proxied);
		
	}
	
	private class ObservedDbSqlSession extends DbSqlSession {

		public ObservedDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
			super(dbSqlSessionFactory);
		}

		public ObservedDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory,
				Connection connection, String catalog, String schema) {
			super(dbSqlSessionFactory, connection, catalog, schema);
		}

//		@Override
//		public void dbSchemaCheckVersion() {
//			super.dbSchemaCheckVersion();
//		}

		@Override
		public void dbSchemaCreate() {
			super.dbSchemaCreate();
			
			RillProcessEngineConfiguration.this.heldApplicationEventPublisher
				.publishEvent(new SchemaOperationEvent(RillProcessEngineConfiguration.this.getCommandExecutorTxRequired(), this, SCHEMA_OPERATION.CREATE));
		}

		@Override
		public void dbSchemaDrop() {
			super.dbSchemaDrop();
			
			RillProcessEngineConfiguration.this.heldApplicationEventPublisher
			.publishEvent(new SchemaOperationEvent(RillProcessEngineConfiguration.this.getCommandExecutorTxRequired(), this, SCHEMA_OPERATION.DROP));
		}

		@Override
		public void dbSchemaPrune() {
			super.dbSchemaPrune();
			
			RillProcessEngineConfiguration.this.heldApplicationEventPublisher
			.publishEvent(new SchemaOperationEvent(RillProcessEngineConfiguration.this.getCommandExecutorTxRequired(), this, SCHEMA_OPERATION.PRUNE));
		}

		@Override
		public String dbSchemaUpdate() {
			String superReturn = super.dbSchemaUpdate();
			
			if (superReturn == null) {
				// Means create schema
				RillProcessEngineConfiguration.this.heldApplicationEventPublisher
				.publishEvent(new SchemaOperationEvent(RillProcessEngineConfiguration.this.getCommandExecutorTxRequired(), this,
						SCHEMA_OPERATION.CREATE));
			} else {
				RillProcessEngineConfiguration.this.heldApplicationEventPublisher
						.publishEvent(new SchemaOperationEvent(RillProcessEngineConfiguration.this.getCommandExecutorTxRequired(), this,
								SCHEMA_OPERATION.UPDATE));
			}
			return superReturn;
		}
		
	}
	
	private enum SCHEMA_OPERATION {
		
		CREATE, UPDATE, DROP, PRUNE;
	}
	
	private class SchemaOperationEvent extends ApplicationEvent {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private DbSqlSession heldDbSqlSession;
		private SCHEMA_OPERATION schemaOperation;
		
		public SchemaOperationEvent(Object source) {
			super(source);
		}

		public SchemaOperationEvent(Object source, DbSqlSession dbSqlSession, SCHEMA_OPERATION so) {
			this(source);
			
			this.heldDbSqlSession = dbSqlSession;
			this.schemaOperation = so;
		}

		public final DbSqlSession getHeldDbSqlSession() {
			return heldDbSqlSession;
		}

		public final SCHEMA_OPERATION getSchemaOperation() {
			return schemaOperation;
		}
		
	}
	
	public static class SchemaOperationEventListener implements ApplicationListener<SchemaOperationEvent> {

		@Override
		public final void onApplicationEvent(SchemaOperationEvent event) {
			
			if (event.getSchemaOperation().equals(SCHEMA_OPERATION.CREATE)) {
				onSchemaCreate((CommandExecutor) event.getSource(),  event.getHeldDbSqlSession());
			} else if (event.getSchemaOperation().equals(SCHEMA_OPERATION.UPDATE)) {
				onSchemaUpdate((CommandExecutor) event.getSource(),  event.getHeldDbSqlSession());
			} else if (event.getSchemaOperation().equals(SCHEMA_OPERATION.DROP)) {
				onSchemaDrop((CommandExecutor) event.getSource(),  event.getHeldDbSqlSession());
			} else if (event.getSchemaOperation().equals(SCHEMA_OPERATION.PRUNE)) {
				onSchemaPrune((CommandExecutor) event.getSource(),  event.getHeldDbSqlSession());
			} else {
				throw new IllegalArgumentException("Unknown schema operation " + event.getSchemaOperation());
			}
		}
		
		public void onSchemaCreate(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
		}
		
		public void onSchemaUpdate(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
		}
		
		public void onSchemaPrune(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
		}
		
		public void onSchemaDrop(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
		}
		
	}
	
	private class ControlPropagationSpringTransactionInterceptor extends SpringTransactionInterceptor {

		public ControlPropagationSpringTransactionInterceptor(
				PlatformTransactionManager transactionManager,
				int transactionPropagation) {
			super(transactionManager, transactionPropagation);
		}

		  @SuppressWarnings({ "unchecked", "rawtypes" })
		  public <T> T execute(final Command<T> command) {
		    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		    
		    // Use transaction propagation that on thread-binding first
		    Integer propagation = (Integer) ThreadLocalResourceHolder.getProperty(ActivitiAccessor.ENGINE_BUILDING_TRANSACTION_PROPAGATION_EXPOSE);
		    if (propagation != null) {
		    	log.info("Replace original transaction propagation with it [" + propagation + "] obtain propagation thread-binding.");
		    	transactionTemplate.setPropagationBehavior(propagation);
		    } else {
		    	transactionTemplate.setPropagationBehavior(transactionPropagation);
		    }
		    
		    T result = (T) transactionTemplate.execute(new TransactionCallback() {
		      public Object doInTransaction(TransactionStatus status) {
		        return next.execute(command);
		      }
		    });
		    return result;
		  }
		
	}


	
}
