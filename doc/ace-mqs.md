# Goal ibm:ace-mqs

Reads the project searching MQ nodes or related in order to get the name and generate the MQ script with the names


## Optional Parameters

| Name                      | Type    | Default                | Since | Description                                                              |
| ------------------------- | ------- | ---------------------- | ----- | ------------------------------------------------------------------------ |
| outputDir                 | boolean | target                 | 1.0.0 | working directory (target) **User property is:** project.build.directory |
| ibm.mq.install.folder     | File    | resources/mq/install   | 1.0.0 | Location where the installations scripts are located                     |
| ibm.mq.uninstall.folder   | File    | resources/mq/uninstall | 1.0.0 | Location where the uninstall scripts are located                         |
| ibm.ace.dryRun            | String  | false                  | 1.0.0 | Print the scripts - do not create any files                              |
| ibm.mq.install.fileName   | String  | 01-install.mqs         | 1.0.0 | name of the script file created for installation                         |
| ibm.mq.uninstall.fileName | String  | 01-uninstall.mqs       | 1.0.0 | name of the script file created for uninstallation                       |


## Install template
```
DEFINE QLOCAL('L.{{QUEUE_NAME}}.BO') +
DEFPSIST(YES) +
DESCR('Backout queue for failed transactions') +
REPLACE

DEFINE QALIAS('A.{{QUEUE_NAME}}.BO') +
TARGET('L.{{QUEUE_NAME}}.BO') +
TARGTYPE(QUEUE) +
DEFPSIST(YES) +
DESCR('Backout queue alias for failed transactions') +
REPLACE

DEFINE QLOCAL ('L.{{QUEUE_NAME}}') +
BOQNAME('A.{{QUEUE_NAME}}.BO') +
BOTHRESH(3) +
DEFPSIST(YES) +
DESCR('Queue Local Generated') +
REPLACE

DEFINE QALIAS('{{QUEUE_NAME}}') +
TARGET( L.{{QUEUE_NAME}}) +
TARGTYPE(QUEUE) +
DEFPSIST(YES) +
REPLACE


```

## Uninstall template
```

DELETE QLOCAL('L.{{QUEUE_NAME}}.BO')
DELETE QALIAS('A.{{QUEUE_NAME}}.BO')
DELETE QLOCAL ('L.{{QUEUE_NAME}}')
DELETE QALIAS('{{QUEUE_NAME}}')


```