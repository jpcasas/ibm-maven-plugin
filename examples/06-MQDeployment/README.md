# Examples deployment of MQ Scripts

## Run Container

```bash

docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 --publish 1414:1414 --publish 9443:9443 --detach --volume qm1data:/mnt/mqm ibmcom/mq

```

## deployment using Host / port / channel 

```bash

mvn -Dmq.channel=DEV.ADMIN.SVRCONN -Dmq.servers=localhost -Dmq.user=admin -Dmq.password=passw0rd -Dmq.queueManager=QM1 -Dmq.scripts.install=install io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.4-SNAPSHOT:mq-deploy-pcf

```

# Configure MQ group & user 

in order to connect and create/read/update/delete MQ Objects we need:
- create a user / group
- Give the corret rights

## MQ Scripts
```bash

sudo useradd --system mqadmin
sudo passwd mqadmin

```

```

DEFINE CHANNEL('DEV.ADMIN.SVRCONN') CHLTYPE(SVRCONN) REPLACE
SET CHLAUTH('DEV.ADMIN.SVRCONN') TYPE(BLOCKUSER) USERLIST('nobody') DESCR('Allows admins on ADMIN channel') ACTION(REPLACE)
SET CHLAUTH('DEV.ADMIN.SVRCONN') TYPE(USERMAP) CLNTUSER('mqadmin') USERSRC(CHANNEL) DESCR('Allows admin user to connect via ADMIN channel') ACTION(REPLACE)
REFRESH SECURITY(*)

```


## deployment using ssh user / password

mvn -Dmq.ssh.exec="echo \"%s\" | runmqsc %s" -Dmq.key.auth=false -Dmq.ssh.port=2222 -Dmq.servers=localhost -Dmq.user=jpcasas -Dmq.password=casasc -Dmq.queueManager=QM1 -Dmq.scripts.install=install io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.4-SNAPSHOT:mq-deploy-ssh

## deployment using ssh key