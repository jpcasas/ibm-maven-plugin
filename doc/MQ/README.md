# IBM MQ

Features:
 * Each script can be used as a Mustache template.
 * Can use an SSH key or SSH user/password to connect to the MQ server
 * Supports multi-instance topology (the script runs on the node where the queue manager is running)
 * Can use PCF (Programmable Command Formats) to execute scripts (independent of the OS: AIX/Windows/Linux/Docker/K8S)
 * Credentials can be read (encrypted) from `settings.xml`

## Common Parameters

| Name                 | Type   | Default        | Since | Description                                                                                                       |
| -------------------- | ------ | -------------- | ----- | ----------------------------------------------------------------------------------------------------------------- |
| mq.serverId          | String |                | 1.1.0 | Id of a `<server>` in `settings.xml` with the user/password (recommended, see below)                             |
| mq.user              | String |                | 1.0.0 | SSH/PCF user name                                                                                                 |
| mq.password          | String |                | 1.0.0 | SSH/PCF password. Avoid it on the command line (shell history, `ps`): use `mq.serverId`                          |
| mq.pextension        | String | properties     | 1.0.0 | Extension of the file with the key=value properties to replace in the script file (`<script>.<pextension>`)       |
| mq.separator         | String | ,              | 1.0.0 | Separator of the list of servers                                                                                  |
| mq.servers           | String |                | 1.0.0 | List of servers separated by the configured separator                                                             |
| mq.queueManager      | String |                | 1.0.0 | Queue manager name (validated: `A-Z a-z 0-9 . _ % /`, max 48)                                                     |
| mq.scripts.install   | File   |                | 1.0.0 | Folder with the MQ scripts (`*.mqs`, `*.mqsc`) to run when `mq.operation` is `install`                            |
| mq.scripts.uninstall | File   |                | 1.0.0 | Folder with the MQ scripts to run when `mq.operation` is `uninstall`                                              |
| mq.operation         | String | install        | 1.0.0 | `install` or `uninstall`                                                                                          |
| mq.env.properties    | File   | env.properties | 1.0.0 | Properties file with the environment variables used in every script                                               |

Scripts run in alphabetical order. The goal fails if any script fails.

### Credentials in settings.xml

```xml
<settings>
  <servers>
    <server>
      <id>mq-dev</id>
      <username>mqm</username>
      <!-- mvn --encrypt-password -->
      <password>{COQLCE6DU6GtcS5P=}</password>
      <!-- optional, SSH only -->
      <privateKey>${user.home}/.ssh/id_ed25519</privateKey>
      <passphrase>{...}</passphrase>
    </server>
  </servers>
</settings>
```

```bash
mvn ibm:mq-deploy-ssh -Dmq.serverId=mq-dev -Dmq.servers=mq1,mq2 -Dmq.queueManager=QM1 -Dmq.scripts.install=install
```

# Goal ibm:mq-deploy-ssh

Runs the MQ scripts on a queue manager through SSH.

| Name                          | Type    | Default                | Since | Description                                                                                                                           |
| ----------------------------- | ------- | ---------------------- | ----- | ------------------------------------------------------------------------------------------------------------------------------------- |
| mq.ssh.exec                   | String  | runmqsc %s             | 1.0.0 | Remote command. `%s` is the queue manager; the script is sent through **stdin**. Legacy `echo "%s" \| runmqsc %s` is still accepted (deprecated) |
| mq.ssh.runc                   | String  | bash --login -c "%s"   | 1.0.0 | Wrapper used to run `dspmq -m <QM>` to find the active instance in a multi-instance topology                                          |
| mq.ssh.privatekeylocation     | String  | ~/.ssh/id_rsa          | 1.0.0 | Private key. If it does not exist `~/.ssh/id_ed25519`, `~/.ssh/id_ecdsa` and `~/.ssh/id_rsa` are tried                                 |
| mq.key.auth                   | boolean | true                   | 1.0.0 | Use the private key to authenticate                                                                                                   |
| mq.ssh.port                   | int     | 22                     | 1.0.0 | SSH port                                                                                                                              |
| mq.ssh.strictHostKeyChecking  | String  | yes                    | 1.1.0 | `yes`: the host must be in known_hosts; `accept-new`: unknown hosts are added, changed keys are rejected; `no`: no check (**insecure**) |
| mq.ssh.knownHosts             | String  | ~/.ssh/known_hosts     | 1.1.0 | known_hosts file                                                                                                                      |
| mq.ssh.timeout                | int     | 30000                  | 1.1.0 | Connection timeout (ms)                                                                                                               |

Add the MQ servers to `known_hosts` before the first deployment (e.g. in the CI agent):

```bash
ssh-keyscan -H mq1.example.com mq2.example.com >> ~/.ssh/known_hosts
```

# Goal ibm:mq-deploy-pcf

Runs the MQ scripts through a client connection (PCF escape command).

| Name                | Type   | Default | Since | Description                                                                   |
| ------------------- | ------ | ------- | ----- | ----------------------------------------------------------------------------- |
| mq.port             | int    | 1414    | 1.0.0 | MQ listener port of the queue manager                                         |
| mq.channel          | String |         | 1.0.0 | SVRCONN channel name                                                          |
| mq.keystore         | String |         | 1.0.0 | Keystore (JKS/PKCS12) with the client certificate and the trusted CAs (TLS)   |
| mq.keystorePassword | String |         | 1.0.0 | Password of the keystore                                                      |
| mq.cipherSuite      | String |         | 1.0.0 | Cipher suite of the channel (enables TLS)                                     |
| mq.timeout          | int    | 1000    | 1.0.0 | Timeout (ms) used to test if the listener port is open                        |

Passwords are sent with MQCSP authentication (no 12 character limit).
