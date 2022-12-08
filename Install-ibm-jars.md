# Run in IBM App console 

## or import mqsiprofile

```bash

source <installation>/ace-<version>/server/bin/mqsiprofile

```

```bash

mvn install:install-file -Dfile="$MQSI_BASE_FILEPATH/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="$MQSI_BASE_FILEPATH/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar

```


