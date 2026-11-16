# Goal ibm:ace-deploy

Search and deploy bar files

## Required Parameters

| Name         | Type   | Default | Since | Description                                                 |
| ------------ | ------ | ------- | ----- | ----------------------------------------------------------- |
| ibm.ace.host | String |         | 1.0.0 | Integration Node/Server host name or ip                     |
| ibm.ace.port | int    | 4414    | 1.0.0 | Administration Port of the Integration Node/Server          |
| ibm.ace.is   | String |         | 1.0.0 | Integration Server Name where the bar file will be deployed |


## Optional Parameters

| Name                           | Type    | Default | Since | Description                                  |
| ------------------------------ | ------- | ------- | ----- | -------------------------------------------- |
| ibm.ace.sec.truststore         | String  |         | 1.0.0 | location for a java truststore               |
| ibm.ace.sec.truststorePassword | String  |         | 1.0.0 | password for truststore                      |
| ibm.ace.sec.keystore           | String  |         | 1.0.0 | location for the java keystore               |
| ibm.ace.sec.keystorePassword   | String  |         | 1.0.0 | password for keystore                        |
| ibm.ace.serverId               | String  |         | 1.1.0 | Id of a `<server>` in settings.xml with the user/password (can be encrypted with `mvn --encrypt-password`). Recommended over `ibm.ace.sec.password` |
| ibm.ace.sec.user               | String  |         | 1.0.0 | User Name for deployment                     |
| ibm.ace.sec.password           | String  |         | 1.0.0 | Password of the user (avoid it on the command line: use `ibm.ace.serverId`) |
| ibm.ace.timeout                | int     | 600     | 1.0.0 | timeout for response of Node / IS in seconds |
| ibm.ace.useSSL                 | boolean | false   | 1.0.0 | enable ssl connection                        |
| ibm.ace.ssl.insecure           | boolean | false   | 1.1.0 | disable certificate/host name verification (test environments only; prefer `ibm.ace.sec.truststore`) |
| ibm.ace.bar                    | String  |         | 1.0.0 | Location of bar file to deploy               |
Since 1.1.0 the TLS certificate of the server is verified (with the JVM default trust store, or
`ibm.ace.sec.truststore` if set) and the goal fails if any bar file is not deployed.
HTTP redirects are not followed, so credentials are only sent to `ibm.ace.host`. Without
`ibm.ace.useSSL=true` credentials travel in clear text (a warning is logged).
