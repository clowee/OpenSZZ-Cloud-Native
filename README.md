# OpenSZZ
SZZ Algorithm To Detect Fault-Inducing Commits


# How to Run
Docker compose should be modified in the following way:

SERVER=http://localhost
It should contains the ip or dress of the server where the container is running. 

cd gui
mvn install

cd ..

cd core 
mvn install

docker-compose up --scale web=#containers
(docker-compose up --scale web=5 will create 5 containers 5 connected to 5 random ports)
