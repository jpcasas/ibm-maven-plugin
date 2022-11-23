# ibm-maven-plugin

# Quick Start

## Install IBM Libraries

```
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar

Example:

mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar

```
**don't change the version  in property "-Dversion=12.0.6"**

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

## Create an ACE Application

Create any ace application / library and inside the project run the command

```
mvn io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.2:generate-pom

```

## List of goals

| Goal                      | Properties                           | Description                                                                     | Example                       |
| ------------------------- | ------------------------------------ | ------------------------------------------------------------------------------- | ----------------------------- |
| ibm:generate-pom          | [here](doc/generate-pom.md)          | Creates a pom file for a project                                                | mvn ibm:generate-pom          |
| ibm:ace-clean             | [here](doc/ace-clean.md)             | clean all target folder except the bar files                                    | mvn ibm:ace-clean             |
| ibm:ace-deploy            | [here](doc/ace-deploy.md)            | deploys all bar files from a folder                                             | mvn ibm:ace-deploy            |
| ibm:ace-override          | [here](doc/ace-override.md)          | override the bar file with the properties inside the resource/properties folder | mvn ibm:ace-override          |
| ibm:ace-bar               | [here](doc/ace-bar.md)               | creates a bar file from the application                                         | mvn ibm:ace-bar               |
| ibm:ace-policy-bar        | [here](doc/ace-policy-bar.md)        | creates a bar from the ace policy                                               | mvn ibm:ace-policy-bar        |
| ibm:ace-properties        | [here](doc/ace-properties.md)        | reads/create the overrides from the application                                 | mvn ibm:ace-properties        |
| ibm:ace-doc               | [here](doc/ace-doc.md)               | creates a markdown documentation of the application                             | mvn ibm:ace-doc               |
| ibm:ace-tests             | [here](doc/ace-tests.md)             | creates the tests skeleton for postman                                          | mvn ibm:ace-tests             |
| ibm:ace-policy-properties | [here](doc/ace-policy-properties.md) | creates the properties for replacement on deployment                            | mvn ibm:ace-policy-properties |
| ibm:ace-policy-package    | [here](doc/ace-policy-package.md)    | creates a bar from the policy                                                   | mvn ibm:ace-policy-package    |
| ibm:ace-policy-replace    | [here](doc/ace-policy-replace.md)    | replace the values on the policy for the values in the properties               | mvn ibm:ace-policy-replace    |
| ibm:ace-keywords          | [here](doc/ace-keywords.md)          | create a keywords file and sets the file into the bar                           | mvn ibm:ace-keywords          |
| ibm:ace-mqs               | [here](doc/ace-mqs.md)               | creates a MQ Script (mqs) reading the nodes from the flows of the application   | mvn ibm:ace-mqs               |
| ibm:mq-deploy             | [here](doc/mq-deploy.md)             | connects to a MQ and execute the MQ scripts                                     | mvn ibm:mq-deploy             |









