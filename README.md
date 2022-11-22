# ibm-maven-tools

## build instructions

### Install / Deploy IBM ACE app Libraries

#### Install file command

#### deploy file command

```
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/jplugin2.jar" -DgroupId=com.ibm.etools.mft -DartifactId=jplugin2 -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/javacompute.jar" -DgroupId=com.ibm.etools.mft -DartifactId=javacompute -Dversion=12.0.6 -Dpackaging=jar

Example:

mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/server/classes/jplugin2.jar" -DgroupId=com.ibm.etools.mft -DartifactId=jplugin2 -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/server/classes/javacompute.jar" -DgroupId=com.ibm.etools.mft -DartifactId=javacompute -Dversion=12.0.6 -Dpackaging=jar

```


## Install into local m2 repo

```
mvn install 
```

## Maven configuration

into the settings.xml 

user:   ${user.home}/.m2/settings.xml

global: ${maven.home}/conf/settings.xml

add
```

<settings>
    <pluginGroups>
        <pluginGroup>io.github.jpcasas.ibm.plugin</pluginGroup>
    </pluginGroups>
</settings>


```

## List of goals

| Goal        | Properties  | Description | Example
| ----------- | ----------- | ----------- | ----------- |
| ibm:ace-clean| none | clean all target folder except the bar files | mvn ibm:ace-clean |
| ibm:ace-clean| WIP | deploys all bar files from a folder | mvn ibm:ace-deploy |



