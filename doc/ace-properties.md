# Goal ibm:ace-properties

Read properties of the project / bar file


## Required Parameters

| Name              | Type   | Default                    | Since | Description               |
| ----------------- | ------ | -------------------------- | ----- | ------------------------- |
| outputDir         | File   | ${project.build.directory} | 1.0.0 | working directory. target |
| ibm.ace.extension | String | .bar                       | 1.0.0 | file suffix               |

## Optional Parameters

| Name                     | Type    | Default             | Since | Description                                              |
| ------------------------ | ------- | ------------------- | ----- | -------------------------------------------------------- |
| ibm.ace.pextension       | String  | .properties         | 1.0.0 | Extension of the properties file                         |
| ibm.ace.propertiesFolder | String  | resources/overrides | 1.0.0 | folder where the properties files are located            |
| ibm.ace.dryRun           | boolean | false               | 1.0.0 | Print all overridable properties (don't create any file) |


