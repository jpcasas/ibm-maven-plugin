mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=<VERSION> -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=<VERSION> -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/jplugin2.jar" -DgroupId=com.ibm.etools.mft -DartifactId=jplugin2 -Dversion=<VERSION> -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/javacompute.jar" -DgroupId=com.ibm.etools.mft -DartifactId=javacompute -Dversion=<VERSION> -Dpackaging=jar


