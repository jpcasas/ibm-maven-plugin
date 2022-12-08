# List of ACE Goals

| Goal                          | Properties                                 | Description                                                                     | Examples                               | stage      |
| ----------------------------- | ------------------------------------------ | ------------------------------------------------------------------------------- | -------------------------------------- | ---------- |
| mvn ibm:generate-pom          | [properties](doc/generate-pom.md)          | Creates a pom file for a project                                                | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-clean             | [properties](doc/ace-clean.md)             | clean all target folder except the bar files                                    | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-properties        | [properties](doc/ace-properties.md)        | reads/create the overrides from the application                                 | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-doc               | [properties](doc/ace-doc.md)               | creates a markdown documentation of the application                             | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-policy-properties | [properties](doc/ace-policy-properties.md) | creates the properties for replacement on deployment                            | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-mqs               | [properties](doc/ace-mqs.md)               | creates a MQ Script (mqs) reading the nodes from the flows of the application   | [example](examples/02-BuildGoals)      | dev        |
| mvn ibm:ace-override          | [properties](doc/ace-override.md)          | override the bar file with the properties inside the resource/properties folder | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-bar               | [properties](doc/ace-bar.md)               | creates a bar file from the application                                         | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-bar        | [properties](doc/ace-policy-bar.md)        | creates a bar from the ace policy                                               | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-policy-replace    | [properties](doc/ace-policy-replace.md)    | replace the values on the policy for the values in the properties               | [example](examples/02-BuildGoals)      | build      |
| mvn ibm:ace-keywords          | [properties](doc/ace-keywords.md)          | create a keywords file and sets the file into the bar                           | [example](examples/04-DeploymentGoals) | deployment |
| mvn ibm:ace-deploy            | [properties](doc/ace-deploy.md)            | deploys all bar files from a folder                                             | [example](examples/04-DeploymentGoals) | deployment |
| mvn ibm:mq-deploy             | [properties](doc/mq-deploy.md)             | connects to a MQ and execute the MQ scripts                                     | [example](examples/04-DeploymentGoals) | deployment |
