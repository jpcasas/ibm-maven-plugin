# DEV / BUILD GOALS

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

## create citrus tests
Go inside MyApp Folder
mvn ibm:ace-tests 

## create a bar for policy
mvn -f MyPolicy/pom.xml ibm:ace-policy-bar

## create properties for policy
Go inside MyPolicy Folder
mvn ibm:ace-policy-properties


git filter-branch --index-filter 'git rm -rf --cached --ignore-unmatch examples/02-BuildGoals/MyApp/README.md' HEAD

