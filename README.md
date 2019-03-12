# OpenSZZ
SZZ Algorithm To Detect Fault-Inducing Commits

#How to: 

sudo mvn install
sudo docker run -p 8080:8080 localhost/openszz:0.0.1-SNAPSHOT
localhost:8080/SZZ?git=GITURL&jiraUrl=JIRA_URL&email=MYEMAIL

example:
localhost:8080/SZZ?git=https://github.com/apache/commons-bcel.git&jiraUrl=https://issues.apache.org/jira/projects/BCEL/&email=test@test.com
