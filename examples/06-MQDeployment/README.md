# Examples Deploying MQ Scripts

# Deployment using Host / port / channel 

For the example, we're going to use and container of MQ

## Run Container

```bash

docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 --publish 1414:1414 --publish 9443:9443 --detach --volume qm1data:/mnt/mqm ibmcom/mq

```
## Installing scripts of the install folder using the MQ PCF API

Run command: 

```bash

mvn -Dmq.channel=DEV.ADMIN.SVRCONN -Dmq.servers=localhost -Dmq.user=admin -Dmq.password=passw0rd -Dmq.queueManager=QM1 -Dmq.scripts.install=install io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.4:mq-deploy-pcf

```

## Example of SVRCONN Channel Configuration

```

DEFINE CHANNEL('DEV.ADMIN.SVRCONN') CHLTYPE(SVRCONN) REPLACE
SET CHLAUTH('DEV.ADMIN.SVRCONN') TYPE(BLOCKUSER) USERLIST('nobody') DESCR('Allows admins on ADMIN channel') ACTION(REPLACE)
SET CHLAUTH('DEV.ADMIN.SVRCONN') TYPE(USERMAP) CLNTUSER('mqadmin') USERSRC(CHANNEL) DESCR('Allows admin user to connect via ADMIN channel') ACTION(REPLACE)
REFRESH SECURITY(*)

```

# Deployment using SSH

This goal allows to use ssh to connect to the host and run the MQ Scripts

## Requirements

### Configure MQ group & user 

in order to connect and create/read/update/delete MQ Objects we need:
- create a user / group
- Give the corret rights

Create User

```bash

sudo useradd mqadmin
sudo passwd mqadmin
sudo su - mqm

```

Setting Authorization for user

```bash
# These commands give group 'asd' full administrative access on IBM MQ for UNIX and Linux.
setmqaut -m QM1 -t qmgr -g "mqadmin" +connect +inq +alladm
setmqaut -m QM1 -n "**" -t q -g "mqadmin" +alladm +crt
setmqaut -m QM1 -n "**" -t topic -g "mqadmin" +alladm +crt
setmqaut -m QM1 -n "**" -t channel -g "mqadmin" +alladm +crt
setmqaut -m QM1 -n "**" -t process -g "mqadmin" +alladm +crt
setmqaut -m QM1 -n "**" -t namelist -g "mqadmin" +alladm +crt
# The following commands provide administrative access for MQ Explorer.
setmqaut -m QM1 -n SYSTEM.MQEXPLORER.REPLY.MODEL -t q -g "mqadmin" +dsp +inq +get
setmqaut -m QM1 -n SYSTEM.ADMIN.COMMAND.QUEUE -t q -g "mqadmin" +dsp +inq +put

echo "REFRESH SECURITY(*)" | runmqsc <QueueManager>

```


## Deployment using ssh user / password


```bash
mvn -Dmq.key.auth=false -Dmq.ssh.port=2222 -Dmq.servers=localhost -Dmq.user=mqadmin -Dmq.password=casasc -Dmq.queueManager=QM1 -Dmq.scripts.install=install io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.4:mq-deploy-ssh

```

## deployment using ssh key

copying key into remote host

```bash

ssh-copy-id mqadmin@<host_name/ip>

```

Run command using ssh key

```bash

mvn -Dmq.ssh.port=2222 -Dmq.servers=localhost -Dmq.user=mqadmin -Dmq.queueManager=QM1 -Dmq.scripts.install=install io.github.jpcasas.ibm.plugin:ibm-maven-plugin:1.0.4:mq-deploy-ssh

```

# Known Issues

## com.jcraft.jsch.JSchException: invalid privatekey

you need to convert the key to a supported format 

```bash

ssh-keygen -p -f (<privateKeyFile>|~/.ssh/id_rsa) -m pem 

```