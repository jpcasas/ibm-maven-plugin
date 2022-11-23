# DEPLOYMENT GOALS

## Requirements 

* **(Not Mandatory)** Configure private or public repositories https://maven.apache.org/guides/mini/guide-configuring-maven.html
* **(Not Mandatory)** Configure the profiles for each enviroment in the settings.xml file

For testing proposes we will start a docker container of ACE 11

```

docker run --name aceserver -p 7600:7600 -p 7800:7800 -p 7843:7843 --env LICENSE=accept --env ACE_SERVER_NAME=ACESERVER ibmcom/ace:latest

```

# Deploy Bar without settings.xml

## First we want to override the 
```

mvn -Dibm.ace.overrideFile=resources/overrides/DEV.properties ibm:ace-override

```

## Add some information to the bar file
set some properties with the prefix MQSI_

```
mvn -DMQSI_DEV=Juan -DMQSI_VERSION=1.0.0 ibm:ace-keywords

```

## deploy the bar into a NODE (old way)

```
mvn -Dibm.ace.is=IS -Dibm.ace.host=localhost ibm:ace-deploy

```

# Deploy Bar with settings.xml

## First we want to override the 
```

mvn -s configuration/settings.xml -PDEV ibm:ace-override

```

## Add some information to the bar file
set some properties with the prefix MQSI_

```
mvn -DMQSI_DEV=Juan -DMQSI_VERSION=1.0.0 ibm:ace-keywords

```

## deploy the bar into a NODE (old way)

```
mvn -s configuration/settings.xml -Dibm.ace.is=IS -PDEV ibm:ace-deploy

```


