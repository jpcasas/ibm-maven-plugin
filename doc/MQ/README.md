# IBM MQ

Features:
 * Each script can be use as a Mustache Template.
 * Can use SSH key in order to connect to the MQ Server
 * Can use SSH user/password to login and execute the scripts
 * Supports Multi-instance Topology 
 * Can use PCF (Programmable Command Formats) to execute scripts (Idependent of the OS AIX/Windows/Linux/Docker/K8S)
 * **(Soon)** Batch execution -  executes the same script in multiples QMs



## Common Parameters

| Name                 | Type   | Default        | Since | Description                                                                                                            |
| -------------------- | ------ | -------------- | ----- | ---------------------------------------------------------------------------------------------------------------------- |
| mq.user              | String |                | 1.0.0 | SSH/PCF User name if using SSH user/password mode                                                                          |
| mq.password          | String |                | 1.0.0 | SSH/PCF Password if using SSH user/password mode                                                                           |
| mq.pextension        | String | properties     | 1.0.0 | extension of the file where are the key value properties to replace in the script file                                 |
| mq.separator         | String | ,              | 1.0.0 | separator use to separe the list of queue managers                                                                     |
| mq.servers           | String |                | 1.0.0 | List of queue managers separeted by the configured separator (default: ,)                                              |
| mq.queueManager      | String |                | 1.0.0 | Queue Manager Name                                                                                                     |
| mq.scripts.install   | File   |                | 1.0.0 | Folder that contains the MQ Scripts to replace and run when the parameter mq.operation is install                      |
| mq.scripts.uninstall | File   |                | 1.0.0 | Folder that contains the MQ Scripts to replace and run when the parameter mq.operation is other different than install |
| mq.operation         | String | install        | 1.0.0 | select the operation in order to select the install or uninstall folder                                                |
| mq.env.properties    | File   | env.properties | 1.0.0 | File where the properties are readed                                                                                   |

# Goal ibm:mq-deploy-ssh

Run MQ Script on a queue manager using SSH

| Name                      | Type   | Default                 | Since | Description                                                                                                     |
| ------------------------- | ------ | ----------------------- | ----- | --------------------------------------------------------------------------------------------------------------- |
| mq.ssh.exec               | String | echo "%s" \| runmqsc %s | 1.0.0 | bash command to run mq script. First parameter is the script, second parameter is the name of the Queue Manager |
| mq.ssh.privatekeylocation | String | ~/.ssh/id_rsa           | 1.0.0 | location of the private key used to connect to the MQ Server in SSH mode                                        |
| mq.key.auth               | String | true                    | 1.0.0 | true if you want to use the private key to authenticate in ssh mode                                             |
| mq.ssh.port               | String | 22                      | 1.0.0 | SSH port in the MQ Server                                                                                       |


# Goal ibm:mq-deploy-pcf

Run MQ Script on a queue manager using PCF API

| Name                | Type   | Default | Since | Description                                                                |
| ------------------- | ------ | ------- | ----- | -------------------------------------------------------------------------- |
| mq.port             | int    | 1414    | 1.0.0 | MQ listener port of the Queue Manager                                      |
| mq.channel          | String |         | 1.0.0 | MQ SVRCONN Channel Name                                                    |
| mq.keystore         | String |         | 1.0.0 | MQ Keystore where the certificates are stored (if using SSL)               |
| mq.keystorePassword | String |         | 1.0.0 | Password for the keystore                                                  |
| mq.cipherSuite      | String |         | 1.0.0 | Chpher Suite used in the channel (if using SSL)                            |
| mq.timeout          | int    | 1000    | 1.0.0 | Timeout used to test if the port is Open (for multi-instance installation) |