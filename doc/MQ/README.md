# IBM MQ

Features:
 * Each script can be use as a Mustache Template.
 * Can use SSH key in order to connect to the MQ Server
 * Can use SSH user/password to login and execute the scripts
 * Supports Multi-instance Topology 
 * Can use PCF (Programmable Command Formats) to execute scripts (Idependent of the OS AIX/Windows/Linux/Docker/K8S)
 * **(Soon)** Batch execution -  executes the same script in multiples QMs

# Goal ibm:mq-deploy

Run MQ Script on a queue manager

## Parameters

| Name                      | Type   | Default                                       | Since | Description                                                                                                            |
| ------------------------- | ------ | --------------------------------------------- | ----- | ---------------------------------------------------------------------------------------------------------------------- |
| mq.user                   | String |                                               | 1.0.0 | SSH User name if using SSH user/password mode                                                                          |
| mq.password               | String |                                               | 1.0.0 | SSH Password if using SSH user/password mode                                                                           |
| mq.ssh.exec               | String | bash --login -c \"echo \"%s\" \| runmqsc %s\" | 1.0.0 | bash command to run mq script. First parameter is the script, second parameter is the name of the Queue Manager        |
| mq.pextension             | String | properties                                    | 1.0.0 | extension of the file where are the key value properties to replace in the script file                                 |
| mq.env.properties         | File   | env.properties                                | 1.0.0 | File where the properties are readed                                                                                   |
| mq.separator              | String | ,                                             | 1.0.0 | separator use to separe the list of queue managers                                                                     |
| mq.servers                | String |                                               | 1.0.0 | List of queue managers separeted by the configured separator (default: ,)                                              |
| mq.ssh.privatekeylocation | String | ~/.ssh/id_rsa                                 | 1.0.0 | location of the private key used to connect to the MQ Server in SSH mode                                               |
| mq.scripts.install        | File   |                                               | 1.0.0 | Folder that contains the MQ Scripts to replace and run when the parameter mq.operation is install                      |
| mq.scripts.uninstall      | File   |                                               | 1.0.0 | Folder that contains the MQ Scripts to replace and run when the parameter mq.operation is other different than install |
| mq.key.auth               | String | true                                          | 1.0.0 | true if you want to use the private key to authenticate in ssh mode                                                    |
| mq.ssh.port               | String | 22                                            | 1.0.0 | SSH port in the MQ Server                                                                                              |
| mq.operation              | String | install                                       | 1.0.0 | select the operation in order to select the install or uninstall folder                                                |

# Examples




