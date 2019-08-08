# OpenSZZ

OpenSZZ is our open source implementation of the SZZ Algorithm to calculate <i>BugInducingCommits</i> of any project using Git as versioning system and Jira as issue tracker. 

OpenSZZ can be used as standalone Java application, or as a web application. 
We recommend to use the web application for analyzing several projects in parallel.

Both versions need as input:
- Git repository URL of the project to be analysed
- Jira repository URL

As example,  for Apache BCEL import shoud have the following Format:
<p>Git URL = https://github.com/apache/commons-bcel.git
<p>Jira URL = https://issues.apache.org/jira/projects/BCEL/ 

The output is a csv file containing for each identified <i>BugInducingCommit</i> the corresponding
<i>BugFixingCommit</i>, the <i>issueType</i> and the involved changed file.


# Standalone Application

## Pre-requisites
Java 1.8 or higher 

## How to run it

# Web-Based Application

The web-based is a cloud-native application, based on three microservices, deployed as docker containers.  

<a href="https://ibb.co/SdtJLnG"><img src="https://i.ibb.co/b2s7fBD/Ohne-Titel.png" alt="Ohne-Titel" border="0"></a>



## Pre-requisites
Docker 
<p>Docker Compose

## Setup

Clone the Repository 
```
git clone https://github.com/clowee/OpenSZZ.git
```

Configure .env file.

The application is scalable and the core part can be replicated in n different containers with random ports.
<p>Here it is possible to configure the range of ports. 

PORTRANGE_FROM=1000
PORTRANGE_TO=2000

DISPATCHER_PORT=8888 => Port where the Dispatcher Container is supposed to run
<p>APP_PORT=8081 => Port where the Application Container (Frontend) is supposed to run

SERVER=http://localhost => URL where the dispatcher server it is reachable.

<p>Configure the email address from which you want to send the confirmation email (tested with gmail accounts)
EMAIL=
PASS=


## How to Run
<b>Just run the following command</b>
```
sudo docker-compose build

sudo docker-compose up -d --scale web=#replicates
```
This will build both of the docker images and will launch them in the background with the database container. 
<p>The scale option indicates how many times the container web should be replicated. 
<p><i>sudo docker-compose up -d --scale web=5</i> will create in total 5 containers web.

You can show the services down with
```
docker-compose down
```

<b>Checking status</b>
```
docker-compose ps
```
<b>Updating</b> 
<i>If the Webservice or Webapp projects are updated, you need to update the submodules and rebuild the source and the docker-images</i>. Just rerun
```
sudo docker-compose up --build -d --scale web=#replicates
```
