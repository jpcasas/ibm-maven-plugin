# {{PROJECTNAME}}
Description of project

## Information

| | |  
| -----------|---------|
| Git Url          | {{GITURL}} |
| Nexus url        | {{NEXUSURL}} |
| Group Id         | {{GROUPID}} |
| Current Version  | {{VERSION}} |


## Properties
| Key | Value |  
| ----------- |---------|
{{#properties}}
| {{key}}     |  {{value}}    |
{{/properties}}

## Content
{{#content}}
### {{type}}
{{#files}}
   - {{schema}}
{{/files}}
{{/content}}