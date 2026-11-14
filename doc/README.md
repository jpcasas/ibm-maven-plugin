# List of ACE Goals

| Goal                          | Properties                                 | Description                                                                     | Examples                               | stage      |
| ----------------------------- | ------------------------------------------ | ------------------------------------------------------------------------------- | -------------------------------------- | ---------- |
| mvn ibm:generate-pom          | [properties](generate-pom.md)          | Creates a pom file for a project                                                | [example](../examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-clean             | [properties](ace-clean.md)             | clean all target folder except the bar files                                    | [example](../examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-properties        | [properties](ace-properties.md)        | reads/create the overrides from the application                                 | [example](../examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-doc               | [properties](ace-doc.md)               | creates a markdown documentation of the application                             | [example](../examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-policy-properties | [properties](ace-policy-properties.md) | creates the properties for replacement on deployment                            | [example](../examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-mqs               | [properties](ace-mqs.md)               | creates a MQ Script (mqs) reading the nodes from the flows of the application   | [example](../examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-override          | [properties](ace-override.md)          | override the bar file with the properties inside the resource/properties folder | [example](../examples/02-BuildGoals)      | build      |
| mvn ibm:ace-bar               | [properties](ace-bar.md)               | creates a bar file from the application                                         | [example](../examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-bar        | [properties](ace-policy-bar.md)        | creates a bar from the ace policy                                               | [example](../examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-replace    | [properties](ace-policy-replace.md)    | replace the values on the policy for the values in the properties               | [example](../examples/02-BuildGoals)      | build      |
| mvn ibm:ace-keywords          | [properties](ace-keywords.md)          | create a keywords file and sets the file into the bar                           | [example](../examples/04-DeploymentGoals) | deployment |
| mvn ibm:ace-deploy            | [properties](ace-deploy.md)            | deploys all bar files from a folder                                             | [example](../examples/04-DeploymentGoals) | deployment |
| mvn ibm:mq-deploy-ssh         | [properties](MQ/README.md)                 | connects to the MQ server through SSH and runs the MQ scripts                   | [example](../examples/06-MQDeployment) | deployment |
| mvn ibm:mq-deploy-pcf         | [properties](MQ/README.md)                 | connects to the queue manager (client channel) and runs the MQ scripts with PCF | [example](../examples/06-MQDeployment) | deployment |
| mvn ibm:ace-create-tests      |                                            | generates an ACE test project from recorded messages (`ibmint`, beta)           |                                        | test       |
| mvn ibm:ace-compile-tests     |                                            | compiles the generated ACE test project into a jar (beta)                       |                                        | test       |
| mvn ibm:ace-verify            |                                            | packages the application with its test project and runs the tests (beta)        |                                        | test       |
| mvn ibm:help                  |                                            | lists the goals; `mvn ibm:help -Ddetail -Dgoal=<goal>` shows all parameters     |                                        |            |
