// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def erpDEVUrl = "http://john.smith@52.48.3.198/gerrit/deploy"

// Jobs
def deploy_to_dev = freeStyleJob(projectFolderName + "/01_Deploy_to_DEV")
def deploy_to_test = freeStyleJob(projectFolderName + "/02_Deploy_to_TEST")

deploy_to_dev.with{
  description()
  scm{
    git{
      remote{
        url(erpDEVUrl)
        credentials("adop-jenkins-master")
      }
      branch("*/master")
    }
  }
  steps {
	batchFile('cd C:\\Oracle\\Middleware\\Oracle_SOA1\\common\\bin\\wlst.cmd C:\\AppDeployer.py -u weblogic -p welcome1 -a t3://localhost:7003 -d "C:\\Program Files (x86)\\Jenkins\\jobs\\Deploy_to_DEV\\workspace" -t http://localhost:8003')
 }	
}

deploy_to_test.with{
  description()
  steps {
        batchFile ('cd C:\\Oracle\\Middleware\\Oracle_SOA1\\common\\bin\\wlst.cmd C:\\AppDeployer.py -u weblogic -p welcome1 -a t3://localhost:7002 -d "C:\\Program Files (x86)\\Jenkins\\jobs\\Deploy_to_DEV\\workspace" -t http://localhost:8002')
	}
}





