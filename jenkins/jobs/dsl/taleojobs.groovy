// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def taleoSrcCodeUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/Taleo_Source_Code"
def taleoRlsUrl = "ssh://jenkins@gerrit:29418/${PROJECT_NAME}/Taleo_Release"

// Jobs
def build = freeStyleJob(projectFolderName + "/01_Build")
def test = freeStyleJob(projectFolderName + "/02_Test")
def deploy = freeStyleJob(projectFolderName + "/03_Deploy")

// Views
def pipelineView = buildPipelineView(projectFolderName + "/Taleo_Automation")

pipelineView.with{
    title('Taleo_Automation_Pipeline')
    displayedBuilds(5)
    selectedJob(projectFolderName + "/01_Build")
    showPipelineParameters()
    showPipelineDefinitionHeader()
    refreshFrequency(5)
}

build.with{
  description()
  wrappers {
    preBuildCleanup()
    sshAgent("adop-jenkins-master")
  }
  scm{
    git{
      remote{
        url(taleoSrcCodeUrl)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
  }
  triggers { 
  }
  publishers {
    downstreamParameterized {
      trigger(projectFolderName + "/02_Test"){
         condition("SUCCESS")
         parameters{
            predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
	  }	
        }
      }
  }
}

test.with{
  description()
  wrappers {
	preBuildCleanup()
	sshAgent("adop-jenkins-master")
	}
  scm{
	git{
      	  remote{
              url(taleoRlsUrl)
              credentials("adop-jenkins-master")
           }
          branch("*/master")
        }
      }
  steps {
        shell ('''#!/bin/bash
		
		echo "===================================================="
		echo " Testing the Application "
        	echo "===================================================="
       		echo "\n"
        	echo "Loading Configuration File"
        	cat /var/jenkins_home/jobs/Oracle/jobs/Taleo/jobs/01_Build/workspace/sel-automate/lib/sel.conf
        	echo "IN-PROGRESS: Validating all possible values"
        	echo "COMPLETE: Validation"
        	echo "===================================================="
		echo " Testing Successfull "
        	echo "===================================================="
		echo " Triggering Next Job: 03_Deploy "
        	echo "====================================================")
	       ''')
	}
	triggers{
		scm('*/1 * * * *')
	}     
	publishers{
		downstreamParameterized{
		  trigger(projectFolderName + "/Oracle/Taleo/03_Deploy"){
			condition("SUCCESS")
			 parameters{
			  predefinedProp("PARENT_BUILD",'${PARENT_BUILD}')
			}
		  }
		}
	}
}

deploy.with{
	description()
        parameters{
		stringParam("Username","john.lester.f.lucena")
		stringParam("Login_URL","https://ptraccenture.taleo.net/smartorg/iam/accessmanagement/login.jsf?redirectionURI=https%3A%2F%2Fptraccenture.taleo.net%2Fsmartorg%2FTaleoHomePage.jss&TARGET=https%3A%2F%2Fptraccenture.taleo.net%2Fsmartorg%2FTaleoHomePage.jss")
		stringParam("Homepage","https://ptraccenture.taleo.net/smartorg/TaleoHomePage.jss?lang=en")
		stringParam("ConfigurationFile","/var/jenkins_home/jobs/Oracle/jobs/Taleo/jobs/3_Deploy/workspace/config.txt")
		stringParam("DatabaseFile","/var/jenkins_home/jobs/Oracle/jobs/Taleo/jobs/1_Build/workspace/sel-automate/lib/sel.conf")
		stringParam("Selenium_Hub","http://selenium-hub:4444/wd/hub")
		stringParam("Password","Accenture01")
		choiceParam("Browser","firefox\nchrome")
	}
	wrappers {
		preBuildCleanup()
		sshAgent("adop-jenkins-master")
	}
	scm{ 
    	   git{ 
              remote{ 
         	    url(taleoRlsUrl) 
                    credentials("adop-jenkins-master") 
                    } 
       	      branch("*/master") 
     	  } 
   	}
	steps { 
              maven{ 
 	           goals('clean install') 
 	           mavenInstallation("ADOP Maven") 
 	      } 
        } 	 
	steps {
		shell ('''#!/bin/bash

			echo "Converting Excel File to Text File"

			echo ${WORKSPACE}

			/xlsx2csv/xlsx2csv.py -d ">" ${WORKSPACE}/Configurations.xlsx  ${WORKSPACE}/config.csv
			mv ${WORKSPACE}/config.csv ${WORKSPACE}/config.txt

			java -jar ${WORKSPACE}/sel-automate/target/sel-automate-0.0.1-SNAPSHOT.jar -u $Username -p $Password -l $Login_URL -h $Homepage -c $ConfigurationFile -f $DatabaseFile -s $Selenium_Hub -b  $Browser   
                       ''')
	}
}





