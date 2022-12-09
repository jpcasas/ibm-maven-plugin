# ibm-maven-plugin

Maven plugin for: 

* IBM App Connect Enterprice (ACE) 11 / 12 / IBM Integration Bus (IIB) 10
* IBM MQ Series (Linux Installations)
* IBM API Connect (**soon**)

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

**Coming soon**

* Creates a jenkins/gitlab pipelines (soon more)
* APIC Goals
* Docker File and Kubernetes deployment descriptors

**Not supported**

* Build bar of applications that uses java projects (java node) 


# Requirements

**Installation of IBM Libraries are used by the following goals:**

- **Build bars**

- **Override bars**

- **Generates MQ Scripts**

- **Read Bar Properties & Creates overrides**

## Install IBM Libraries

```
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="<INSTALL_PATH>/<VERSION>/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar

Example:

mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="/home/jpcasas/data/programs/ace-12.0.6.0/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar

```

**The other goals can be run without Installing the libraries** 

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
# Quick Start IBM ACE Toolkit

[documentation](examples/05-ToolkitConfigurations/README.md)

# Quick Start IBM MQ Series

[documentation](examples/06-MQDeployment/README.md)

# Quick Start Build and Deploy CLI
build and deploy in 3 steps

## Create an ACE Application

Create any ace application / library and inside the project run the command

### **Mavenize**

```
mvn io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.7:generate-pom

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
* Deploying MQ Objects
  
[here the examples](examples/README.md) 

## List of goals

[here](doc/README.md) 


# IBM MQ Series documentation

Documentation

  [here](doc/MQ/README.md) 

Examples how to do to run MQ Scripts in remote Queue Managers

  [here](examples/06-MQDeployment/README.md) 

# Work in progress documentation

* Dependecy Management
* CI Servers templates






