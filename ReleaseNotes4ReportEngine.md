## Deployment Details ##
### Open $TOMCAT\_HOME/conf/server.xml ###
  * Add URIEncoding="utf-8" for http Connector
  * Add jvmRoute="re1" for Engine.(re1,re2...ren to make sure unique in all nodes)
### Open $TOMCAT\_HOME/bin/catalina.sh ###
  * Add JAVA\_OPTS="$JAVA\_OPTS -Dorg.apache.catalina.SESSION\_COOKIE\_NAME=REJSESSIONID -Dorg.apache.catalina.SESSION\_PARAMETER\_NAME=rejsessionid"