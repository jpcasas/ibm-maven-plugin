# ibm-maven-plugin

# About
This plugin aims to help the developpers with goals like:

* Creates mq scripts from flows (search in all flows MQ Objects to create mq scripts)
* Creates a basic documentation
* Easy integration and installation into the toolkit
* Goal to mavenize the project

**Also provides**

* Maven Dependency features. include your libraries in
* Works with packagebar. no toolkit need it for build bars (headless builds)
* MQ SSH Deployment multi-instance topology supported (API & PCF Deployment soon) 
* Buld & deploy bars.

**Coming soon.**

* Creates a jenkins/gitlab pipelines (soon more)
* APIC Goals
* Remove IBM Libs dependencies
* Docker File and Kubernetes deployment descriptors

**Not supported**

* Build bar of applications that uses java projects (java node) 


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
build and deploy in 3 steps

## Create an ACE Application

Create any ace application / library and inside the project run the command

### **Mavenize**

```
mvn io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.3:generate-pom

```
### **build bar**

```
mvn package ibm:ace-bar ibm:ace-clean

```

### **deploy bar**

Integration server Topology
```
 mvn -Dibm.ace.host=<host> -Dibm.ace.port=<port> ibm:ace-deploy

```

Integration Node Topology

```
mvn -Dibm.ace.is=IS -Dibm.ace.host=localhost -Dibm.ace.port=<port> ibm:ace-deploy

```

# Others Examples 

* generate documentation
* generate overrides
* overrides bar
* add keywords to bar
* generate mq scripts
* generate Integration Tests (Beta)
* Policies build and deployments
  
[here the examples](examples/README.md) 

## List of goals

| Goal                          | Properties                                 | Description                                                                     | Examples                               | stage      |
| ----------------------------- | ------------------------------------------ | ------------------------------------------------------------------------------- | -------------------------------------- | ---------- |
| mvn ibm:generate-pom          | [properties](doc/generate-pom.md)          | Creates a pom file for a project                                                | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-clean             | [properties](doc/ace-clean.md)             | clean all target folder except the bar files                                    | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-properties        | [properties](doc/ace-properties.md)        | reads/create the overrides from the application                                 | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-doc               | [properties](doc/ace-doc.md)               | creates a markdown documentation of the application                             | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-policy-properties | [properties](doc/ace-policy-properties.md) | creates the properties for replacement on deployment                            | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-mqs               | [properties](doc/ace-mqs.md)               | creates a MQ Script (mqs) reading the nodes from the flows of the application   | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-override          | [properties](doc/ace-override.md)          | override the bar file with the properties inside the resource/properties folder | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-bar               | [properties](doc/ace-bar.md)               | creates a bar file from the application                                         | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-bar        | [properties](doc/ace-policy-bar.md)        | creates a bar from the ace policy                                               | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-replace    | [properties](doc/ace-policy-replace.md)    | replace the values on the policy for the values in the properties               | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-keywords          | [properties](doc/ace-keywords.md)          | create a keywords file and sets the file into the bar                           | [example](examples/04-DeploymentGoals) | deployment |
| mvn ibm:ace-deploy            | [properties](doc/ace-deploy.md)            | deploys all bar files from a folder                                             | [example](examples/04-DeploymentGoals) | deployment |
| mvn ibm:mq-deploy             | [properties](doc/mq-deploy.md)             | connects to a MQ and execute the MQ scripts                                     | [example](examples/04-DeploymentGoals) | deployment |




# Work in progress documentation

* Deployment of MQ Objects By SSH or PCF commands
* Dependecy Management
* CI Servers templates






