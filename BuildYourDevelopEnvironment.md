# Required software #

Base on Eclipse/Maven platform.
  * jdk1.6.0\_30 and apache-tomcat-6.0.28
  * Download eclipse3.7 JEE version, and modify Installed JREs.
  * Install Activiti Designer plugin(http://activiti.org/designer/update/).
  * Install SubVersive plugin(Indigo official update site)
  * Download source code and import into eclipse.


# Details #

Project descriptions:
  * rill-bpm
    * rill-bpm-api
    * rill-bpm-ws
    * rill-bpm-web
    * rill-bpm-webclient
  * rill-bpm-embeddedgf

# !Important! #
  1. add -Dhttp.keepAlive=false to JVM start command (to prevent thread hangs)
  1. import rill-bpm modules as maven project(don't open rill-bpm-embeddedgf project)