@Library('github.com/bonitasoft-presales/bonita-jenkins-library@1.0.1') _

node('bcd-790') {

    def scenarioFile = "/home/bonita/bonita-continuous-delivery/scenarios/scenario-7.9.0-ec2.yml"
    def bonitaConfiguration = params.environment ?: "Development"

    // set to true/false to switch verbose mode
    def debugMode = params.debug ?:	false;

	
	// start settings
	// not supposed to be modified
	
	// used to archive artifacts
    def jobBaseName = "${env.JOB_NAME}".split('/').last()

    // used to set build description and bcd_stack_id
    def gitRepoName = "${env.JOB_NAME}".split('/')[1]
    def normalizedGitRepoName = gitRepoName.toLowerCase().replaceAll('-','_')

    // used to set bcd_stack_id
    def branchName = env.BRANCH_NAME
    def normalizedBranchName = branchName.toLowerCase().replaceAll('-','_')

    //bcd_stack_id overrides scenario value
    def stackName = "${normalizedGitRepoName}_${normalizedBranchName}" 
	 
    def debug_flag = ''
    def verbose_mode = ''
    if ("${debugMode}".toBoolean()) {
        debug_flag = '-X'
    	verbose_mode = '-v'
    
    } // end of settings

  ansiColor('xterm') {
    timestamps {
        stage("Checkout") {
            checkout scm
            echo "jobBaseName: $jobBaseName"
            echo "gitRepoName: $gitRepoName"
        }

        stage("Build LAs") {
            bcd scenario:scenarioFile, args: "--extra-vars bcd_stack_id=${stackName} livingapp build ${debug_flag} -p ${WORKSPACE} --environment ${bonitaConfiguration}"
            //Generate a .bos archive from the repository
            sh 'mvn assembly:single'
            sh '''
	    			timestamp=$(date +"%Y%m%d%H%M");
				for f in target/credit-card-dispute-resolution-*.zip; do 
				    mv -- "$f" "${f%.zip}-${timestamp}.bos"
				done
            '''
        }

        stage("Create stack") {
            bcd scenario:scenarioFile, args: "--extra-vars bcd_stack_id=${stackName} ${verbose_mode} stack create", ignore_errors: false
        }

        stage("Undeploy server") {
            bcd scenario:scenarioFile, args: "--extra-vars bcd_stack_id=${stackName} ${verbose_mode} stack undeploy", ignore_errors: true
        }
              
        stage("Deploy server") {    
            def json_path = pwd(tmp: true) + '/bcd_stack.json'
            bcd scenario:scenarioFile, args: "--extra-vars bcd_stack_id=${stackName} ${verbose_mode} -e bcd_stack_json=${json_path} stack deploy"
            // set build description using bcd_stack.json file
            def props = readJSON file: json_path
            currentBuild.description = "<a href='${props.bonita_url}'>${props.bcd_stack_id}</a>"
        }

        def zip_files = findFiles(glob: "target/*_${jobBaseName}-${bonitaConfiguration}-*.zip")
        def bconf_files = findFiles(glob: "target/*_${jobBaseName}-${bonitaConfiguration}-*.bconf")
        def bConfArg = bconf_files && bconf_files[0].length > 0 ? "-c ${WORKSPACE}/${bconf_files[0].path}" : ""

        stage('Deploy LAs') {
            bcd scenario:scenarioFile, args: "--extra-vars bcd_stack_id=${stackName} livingapp deploy ${debug_flag} -p ${WORKSPACE}/${zip_files[0].path} ${bConfArg}"
        }

        stage('Archive') {
            archiveArtifacts artifacts: "target/*.zip, target/*.bconf, target/*.xml, target/*.bar, target/*.bos", fingerprint: true, flatten:true
        }
  	} // timestamps
  } // ansiColor
} // node
