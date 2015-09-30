# Our vision #
Build a enterprise-level, light-wight BPM platform base on [Activiti](http://www.activiti.org/),Tomcat,Embedded Glassfish.

## Highlights ##
  * Use JTA/2PC+WS-Coor/AT to resolved the distribution transaction consistency
## Architecture ##

### Server side ###
  * [Spring](http://www.springsource.org/) and [embedded glassifsh](http://glassfish.java.net/) provide transaction, web service(JTA, JAXWS, WS-`*`)
  * Dev rill-bpm-ws module for export rill-bpm-api(see below)
  * Dev rill-bpm-api module for abstract work-flow access API(Such as create process instance, complete task instance)
  * Use [Activiti](http://www.activiti.org/) as work-flow engine.

### Client side ###
  * Use rill-bpm-ws-stub to access web service

# Deploy as BPM server #
  * [deployment wiki](http://code.google.com/p/rill-bpm-api/wiki/ReleaseNotesUseAsEngineServer)
  * Console Url `http://<host>:<port>/<context>`