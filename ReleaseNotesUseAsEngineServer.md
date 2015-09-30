#Use as a BPM server.

# Software & Version #
  * [tomcat 6.0.28](http://archive.apache.org/dist/tomcat/tomcat-6/v6.0.28/bin/apache-tomcat-6.0.28.tar.gz)
  * [jdk6u30](http://download.oracle.com/otn-pub/java/jdk/6u30-b12/jdk-6u30-linux-x64.bin)

# Configure #

Add your content here.  Format your content with:
  * add -Dhttp.keepAlive=false to JVM(catalina.sh/bat) start command-line.
  * add C:\Windows\Fonts\simsun.ttc to dictionary JAVA\_HOME/jre/lib/fonts/
  * Configure DB for engine，add to context.xml
```
<Environment name="jdbc.username" value="<username>" type="java.lang.String"/>
<Environment name="jdbc.password" value="<password>" type="java.lang.String"/>
<Environment name="jdbc.maxPoolSize" value="<maxPoolSize>" type="java.lang.String"/>
<Environment name="jdbc.minPoolSize" value="<minPoolSize>" type="java.lang.String"/>
<Environment name="jdbc.maxIdleTime" value="<maxIdleTime>" type="java.lang.String"/>
<Environment name="jdbcpool.property" value="portNumber=<portNumber>:serverName=<serverName>:databaseName=<databaseName>" type="java.lang.String"/>
```
  * **start tomcat server(need if first time only) to execute engine table creation sql**
  * execute DML at your's DB. MySQL for example
```
drop index ACT_UNIQ_HI_BUS_KEY on ACT_HI_PROCINST;
alter table ACT_RU_IDENTITYLINK drop FOREIGN KEY ACT_FK_TSKASS_TASK;

alter table ACT_RU_EXECUTION drop FOREIGN KEY ACT_FK_EXE_SUPER;
alter table ACT_RU_EXECUTION drop FOREIGN KEY ACT_FK_EXE_PARENT;
alter table ACT_RU_EXECUTION drop FOREIGN KEY ACT_FK_EXE_PROCINST;

alter table ACT_RU_VARIABLE drop FOREIGN KEY ACT_FK_VAR_BYTEARRAY;
alter table ACT_RU_VARIABLE drop FOREIGN KEY ACT_FK_VAR_EXE;
alter table ACT_RU_VARIABLE drop FOREIGN KEY ACT_FK_VAR_PROCINST;

alter table ACT_RU_TASK drop FOREIGN KEY ACT_FK_TASK_PROCDEF;
alter table ACT_RU_TASK drop FOREIGN KEY ACT_FK_TASK_EXE;
alter table ACT_RU_TASK drop FOREIGN KEY ACT_FK_TASK_PROCINST;
```

# Confirm #
Start up tomcat server, and access `http://<host>:<port>/<contextPath>/BPMWebService?wsdl`

# Security #
  * Web console use HTTP BASIC AUTH mechanism, default role is **rill-bpm-web-console**(see web.xml). You need add it to $CATALINA\_HOME/conf/tomcat-users.xml.
  * BPM Web service use HTTP BASIC AUTH mechanism too, by different, it configure at Spring3.1 Environment abstraction.
```
metro.auth.username=rill-bpm-web
metro.auth.password=web-bpm-rill
```

# Cluster #
  * **Client**: Provide load balance invoking feature in rill-bpm-ws stub.jar, and it's strategy is transaction-binding.
  * **Server**:
    1. Add `-Djgroups.tcpping.initial_hosts=server1.ip[port1],server2.ip[port2] -Djgroups.bind_addr=serverN.ip` to JVM(catalina.sh/bat) start command-line.
    1. Configure jgroups.bing\_addr only if jgroups.tcpping.initial\_hosts is not **localhost**

# Monitoring #
  * **!!!KEEP IT AS false WHEN DEBUG MODE!!!** Add `glassfish.enableBtrace=true` in your context.xml
  * Append log configurations(see below) to $CATALINA\_BASE/conf/logging.properties
```
com.sun.enterprise.server.logging.GFFileHandler.file=${com.sun.aas.instanceRoot}/logs/server.log
com.sun.enterprise.server.logging.GFFileHandler.rotationTimelimitInMinutes=0
com.sun.enterprise.server.logging.GFFileHandler.flushFrequency=1
com.sun.enterprise.server.logging.GFFileHandler.formatter=com.sun.enterprise.server.logging.UniformLogFormatter
com.sun.enterprise.server.logging.GFFileHandler.logtoConsole=false
com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=2000000
com.sun.enterprise.server.logging.GFFileHandler.alarms=false
com.sun.enterprise.server.logging.GFFileHandler.retainErrorsStasticsForHours=0
com.sun.enterprise.server.logging.SyslogHandler.useSystemLogging=false

javax.enterprise.resource.sqltrace.level=FINE
```
`*`_add `-Djava.util.logging.config.file="<your's project directory>\logging.properties"` if you use Eclipse/WTP_

# Congratulations #
Just enjoy it.