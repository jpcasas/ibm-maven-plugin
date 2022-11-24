# ibm-maven-plugin

# Requirements

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

# Quick Start

## Create an ACE Application

Create any ace application / library and inside the project run the command

1. **Mavenize**

```
mvn io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.3:generate-pom

```
2. **build bar**

```
mvn package ibm:ace-bar ibm:ace-clean

```

3. **deploy bar**

In a stand alone instegration server
```
 mvn -Dibm.ace.host=<host> -Dibm.ace.port=<port> ibm:ace-deploy

```

Integration Node Topology

```
mvn -Dibm.ace.is=IS -Dibm.ace.host=localhost -Dibm.ace.port=<port> ibm:ace-deploy

```

## List of goals

| Goal                          | Properties                                 | Description                                                                     | Examples                               | stage      |
| ----------------------------- | ------------------------------------------ | ------------------------------------------------------------------------------- | -------------------------------------- | ---------- |
| mvn ibm:generate-pom          | [properties](doc/generate-pom.md)          | Creates a pom file for a project                                                | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-clean             | [properties](doc/ace-clean.md)             | clean all target folder except the bar files                                    | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-properties        | [properties](doc/ace-properties.md)        | reads/create the overrides from the application                                 | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-doc               | [properties](doc/ace-doc.md)               | creates a markdown documentation of the application                             | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-tests             | [properties](doc/ace-tests.md)             | creates the tests skeleton for postman                                          | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-policy-properties | [properties](doc/ace-policy-properties.md) | creates the properties for replacement on deployment                            | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-mqs               | [properties](doc/ace-mqs.md)               | creates a MQ Script (mqs) reading the nodes from the flows of the application   | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-override          | [properties](doc/ace-override.md)          | override the bar file with the properties inside the resource/properties folder | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-bar               | [properties](doc/ace-bar.md)               | creates a bar file from the application                                         | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-bar        | [properties](doc/ace-policy-bar.md)        | creates a bar from the ace policy                                               | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-package    | [properties](doc/ace-policy-package.md)    | creates a bar from the policy                                                   | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-replace    | [properties](doc/ace-policy-replace.md)    | replace the values on the policy for the values in the properties               | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-keywords          | [properties](doc/ace-keywords.md)          | create a keywords file and sets the file into the bar                           | [example](examples/04-DeploymentGoals) | deployment |
| mvn ibm:ace-deploy            | [properties](doc/ace-deploy.md)            | deploys all bar files from a folder                                             | [example](examples/04-DeploymentGoals) | deployment |
| mvn ibm:mq-deploy             | [properties](doc/mq-deploy.md)             | connects to a MQ and execute the MQ scripts                                     | [example](examples/04-DeploymentGoals) | deployment |


# Examples

[here the examples](examples/README.md) 






