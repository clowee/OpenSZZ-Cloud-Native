# OpenSZZ

OpenSZZ is our open source implementation of the SZZ Algorithm [1] to calculate the <i>BugInducingCommits</i> of any project using Git as versioning system and Jira as issue tracker. 

A dataset including the analysis of 33 projects, has been published in 2019 [2]. 

* [Pre-Requisites](https://github.com/clowee/OpenSZZ/blob/master/README.md#pre-requisites)
* [Setup](https://github.com/clowee/OpenSZZ/blob/master/README.md#setup)
    * [Launch the application server](https://github.com/clowee/OpenSZZ/blob/master/README.md#launch-the-application-server)
* [How to Use it](https://github.com/clowee/OpenSZZ/blob/master/README.md#how-to-use-it)
* [References](https://github.com/clowee/OpenSZZ/blob/master/README.md#references)

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


### Launch the Application Server
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

Access to the OpenSZZ page to http://localhost:8081 (replace localhost and port with the seerver_host and APP_port parameters used in the .env file)

## How to Use it
Enter the following data in the graphical user interface:
- Git repository URL of the project to be analysed
- Jira repository URL

As example,  for Apache BCEL import shoud have the following Format:
<p>Git URL = https://github.com/apache/commons-bcel.git
<p>Jira URL = https://issues.apache.org/jira/projects/BCEL/ 

The output is a csv file containing for each identified <i>BugInducingCommit</i> the corresponding
<i>BugFixingCommit</i>, the <i>issueType</i> and the involved changed file.
<a href="https://ibb.co/SdtJLnG"><img src="https://i.ibb.co/b2s7fBD/Ohne-Titel.png" alt="Ohne-Titel" border="0"></a>



# References

[1] Jacek Śliwerski, Thomas Zimmermann, and Andreas Zeller. 2005. When do changes induce fixes?. In Proceedings of the 2005 international workshop on Mining software repositories (MSR '05). ACM, New York, NY, USA, 1-5. DOI=http://dx.doi.org/10.1145/1082983.1083147

[2] V. Lenarduzzi, N. Saarimäki, and D. Taibi,“The Technical Debt Dataset”, in The Fifteenth International Conference on Predictive Models and Data Analytics in Software Engineering (PROMISE’19), Brazil, 2019.
