# Goal ibm:ace-keywords

Add keywords file inside the barfile. This allows to add information on build or deployment time.

Search in the environment 
Search in the system properties (passed by pom, commmand line, settings.xml)

Enviromental Variables added (if found):

* BUILD_USER
* BUILD_USER_ID
* BUILD_URL
* BUILD_USER_EMAIL
* MQSI_*


## Optional Parameters

| Name        | Type   | Default | Since | Description       |
| ----------- | ------ | ------- | ----- | ----------------- |
| ibm.ace.bar | String |         | 1.0.0 | bar file location |