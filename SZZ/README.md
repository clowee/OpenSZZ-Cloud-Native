# SZZ
SZZ implements the algorithm proposed by Sliseris et al. [2] to map the  Fault-Inducing Commits. 


The current version can tag commits in github with the faults reported in Jira. 


## Download
Release 0.1

## Usage: 
1. Clone the GitHub Repository 

2. Download the faults from Jira: 
     
     szz.jar -d jiraKey
     
     eg: szz.jar jiraKey
     
     The script saves the file faults.csv containing the issues reported in Jira  

3. Save gitLogsMap 
     
     szz.jar -l gitRepositoryPath
   
     e.g. szz.jar -l ./projects/ambari 
      
     This script saves the file gitlog.csv containing the parsed gitlog with all the information needed to execute szz

4. Map Faults to commits
      
      szz.jar -m jiraKey
     
     the script takes in input the files generated before (faults.csv and gitlog.csv) and generate the final result in the file FaultInducingCommits.csv
      
      
## Usage [alternative]:
This command executes all the steps at once: 
* szz.jar -all githubUrl, jiraKey
 
The script first clones the gitHub repository, then download the Jira faults, and finally maps faults to commits. 

 

# Notes and References

This algorithm has been implemented in [1]. 

If you use this algorithm, please cite as 

[1] Valentina Lenarduzzi, Davide Taibi, Francesco Lomio, Luca Pellegrini, Heikki Huttunen. "On the Fault Proneness of SonarQube Technical Debt Violations: A comparison of seven Machine Learning Techniques" Journal of Software: Evolution and Process 2019 (in press) 



[2] Jacek Śliwerski , Thomas Zimmermann , Andreas Zeller, When do changes induce fixes?, ACM SIGSOFT Software Engineering Notes, v.30 n.4, July 2005  [doi>10.1145/1082983.1083147]
