# ibm-maven-plugin

[![CI](https://github.com/jpcasas/ibm-maven-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/jpcasas/ibm-maven-plugin/actions/workflows/ci.yml)

Maven plugin for:

* IBM App Connect Enterprise (ACE) 11 / 12 / IBM Integration Bus (IIB) 10
* IBM MQ (Linux installations through SSH, any platform through PCF)

# About
This plugin aims to help developers with goals like:

* Creates mq scripts from flows (search in all flows MQ Objects to create mq scripts)
* Creates a basic documentation
* Easy integration and installation into the toolkit
* Goal to mavenize the project

**Also provides**

* Maven dependency management: include your libraries as Maven dependencies
* Works with packagebar: no toolkit needed to build bars (headless builds)
* MQ deployment through SSH (multi-instance topology supported) or PCF
* Build & deploy bars

**Roadmap** (not implemented yet, see the [issues](https://github.com/jpcasas/ibm-maven-plugin/issues))

* Jenkins/GitLab pipeline templates
* IBM API Connect goals
* Dockerfile and Kubernetes deployment descriptors

**Not supported**

* Build bar of applications that uses java projects (java node) 


# Requirements

**The IBM libraries must be installed in your local Maven repository for the following goals:**

- **Build bars**

- **Override bars**

- **Generates MQ Scripts**

- **Read Bar Properties & Creates overrides**

## Install IBM Libraries

```bash
mvn install:install-file -Dfile="<ACE_HOME>/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="<ACE_HOME>/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar
```

Example (`<ACE_HOME>` is the ACE installation folder, e.g. `/opt/IBM/ace-12.0.6.0`; from an ACE
console it is `$MQSI_BASE_FILEPATH`, see [Install-ibm-jars.md](Install-ibm-jars.md)):

```bash
mvn install:install-file -Dfile="/opt/IBM/ace-12.0.6.0/common/classes/IntegrationAPI.jar" -DgroupId=com.ibm -DartifactId=IntegrationAPI -Dversion=12.0.6 -Dpackaging=jar
mvn install:install-file -Dfile="/opt/IBM/ace-12.0.6.0/server/classes/brokerutil.jar" -DgroupId=com.ibm -DartifactId=brokerutil -Dversion=12.0.6 -Dpackaging=jar
```

**The other goals can be run without installing the libraries**

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
mvn io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.8:generate-pom

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

# Security notes (1.1.0)

* SSH host keys are verified against `~/.ssh/known_hosts` (`mq.ssh.strictHostKeyChecking`).
* TLS certificates of the ACE administration API are verified (`ibm.ace.ssl.insecure=true` only for tests).
* Read credentials from `settings.xml` (`ibm.ace.serverId`, `mq.serverId`) instead of `-D...password=`.

See the [CHANGELOG](CHANGELOG.md) for the upgrade notes.

# Building from source

The IBM jars are proprietary. To build and run the unit tests without an ACE installation, install
the compile-only stubs (CI does the same). JDK 11+ is required to build; the plugin runs on Java 8+.

```bash
./ci/install-ibm-stubs.sh
mvn verify
```

To use the built plugin with ACE, install the real IBM jars instead (see above).

Release: `mvn -P release deploy` (needs a `central` server token for the Central Portal and GPG).
Dependency vulnerability scan: `mvn -P owasp verify`.
