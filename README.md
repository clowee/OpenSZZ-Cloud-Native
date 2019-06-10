# OpenSZZ

<a href="https://ibb.co/SdtJLnG"><img src="https://i.ibb.co/b2s7fBD/Ohne-Titel.png" alt="Ohne-Titel" border="0"></a>

Web Application that apply SZZ Algorithm to calculate <i>BugInducingCommits</i> of any project. 

The web application needs as input:
- Git repository URL of the project to be analysed
- Jira repository URL

For example for Apache BCEL import shoud have the following Format:
<p>Git URL = https://github.com/apache/commons-bcel.git
<p>Jira URL = https://issues.apache.org/jira/projects/BCEL/ 

The output is a csv file containing for each identified <i>BugInducingCommit</i> the corresponding
<i>BugFixingCommit</i>, the <i>issueType</i> and the involved changed file.

 

# Pre-requisites
Docker 
<p>Docker Compose

# Setup
Configure the ports to your liking modifing the .env file.

The application is scalable and the core part can be replicated in n different containers with random ports.
Here it is possible to configure the range of ports. 
<p>PORTRANGE_FROM=1000
<p>PORTRANGE_TO=2000

DISPATCHER_PORT=8888 => It is where the dispatcher container it is running
APP_PORT=8081 => It is where the GUI Application is running. 

SERVER=http://localhost => URL where the dispatcher server it is reachable without port.

# How to Run
Docker compose should be modified in the following way:

SERVER=http://localhost
It should contains the ip address of the server where the container is running. 

cd gui
mvn install

cd ..

cd core 
mvn install

docker-compose up --scale web=#containers
(docker-compose up --scale web=5 will create 5 containers 5 connected to 5 random ports)
