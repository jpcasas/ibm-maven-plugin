# Goal ibm:ace-deploy

Search and deploy bar files

## Required Parameters

| Name         | Type   | Default | Since | Description                                                 |
| ------------ | ------ | ------- | ----- | ----------------------------------------------------------- |
| ibm.ace.host | String |         | 1.0.0 | Intgration Node/Server host name or ip                      |
| ibm.ace.port | int    | 4414    | 1.0.0 | Administration Port of the Integration Node/Server          |
| ibm.ace.is   | String |         | 1.0.0 | Integration Server Name where the bar file will be deployed |


## Optional Parameters

| Name                           | Type    | Default | Since | Description                                  |
| ------------------------------ | ------- | ------- | ----- | -------------------------------------------- |
| ibm.ace.sec.truststore         | String  |         | 1.0.0 | location for a java truststore               |
| ibm.ace.sec.truststorePassword | String  |         | 1.0.0 | password for truststore                      |
| ibm.ace.sec.keystore           | String  |         | 1.0.0 | location for the java keystore               |
| ibm.ace.sec.keystorePassword   | String  |         | 1.0.0 | password for keystore                        |
| ibm.ace.sec.user               | String  |         | 1.0.0 | User Name for deployment                     |
| ibm.ace.sec.password           | String  |         | 1.0.0 | password if the username                     |
| ibm.ace.timeout                | int     | 600     | 1.0.0 | timeout for response of Node / IS in seconds |
| ibm.ace.useSSL                 | boolean | false   | 1.0.0 | enable ssl connection                        |
| ibm.ace.bar                    | String  |         | 1.0.0 | Location of bar file to deploy               |