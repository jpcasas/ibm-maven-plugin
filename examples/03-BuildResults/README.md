# DEV / BUILD RESULT OF GOALS

## build a bar file
mvn -f MyApp/pom.xml package ibm:ace-bar

## clean all but bar files
mvn -f MyApp/pom.xml ibm:ace-clean

## create override properties for project
Go inside MyApp Folder
cd MyApp
mvn ibm:ace-properties

## create Markdown documentation
Go inside MyApp Folder
mvn ibm:ace-doc

## create mqs scripts
mvn -f MyApp/pom.xml ibm:ace-mqs

## create a bar for policy
mvn -f MyPolicy/pom.xml ibm:ace-policy-bar

## create properties for policy
mvn -f MyPolicy/pom.xml ibm:ace-policy-properties

