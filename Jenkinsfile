@Library('github.com/bonitasoft-presales/bonita-jenkins-library@1.0.1') _

//Global variable shared between nodes
def props //Valuated during Deploy Server stage

node('bcd-790') {

    def scenarioFile = "/home/bonita/bonita-continuous-delivery/scenarios/scenario-7.9.0-ACM-ec2.yml"
    def bonitaConfiguration = params.environment ?: "Development"

    // set to true/false to switch verbose mode
    def debugMode = params.debug ?:	false;
    
    // set to true/false to enable required event handler activation state
    // uses a tuned BCD version until BCD OOB support
    def bonita_enable_acm_handler = true
    def applicationToken = 'cases'
    // start settings
    // not supposed to be modified
	
	// used to archive artifacts
    def jobBaseName = "${env.JOB_NAME}".split('/').last()

    // used to set build description and bcd_stack_id
    def gitRepoName = "${env.JOB_NAME}".split('/')[1] 
    
    // bcd_stack_id overrides scenario value
    // unsupported chars must be replaced
    def stackName = "${gitRepoName.toLowerCase()}_${env.BRANCH_NAME.toLowerCase()}" 
    String[ ] excludedChars= [ '-', '\\.', '\\/' ]
    excludedChars.each{ excluded ->
        stackName = stackName.replaceAll(excluded,'_')
    }
	 
    def debug_flag = ''
    def verbose_mode = ''
    if ("${debugMode}".toBoolean()) {
        debug_flag = '-X'
    	verbose_mode = '-v'
    } 
    
    def extraVars = "--extra-vars bcd_stack_id=${stackName} --extra-vars bonita_enable_acm_handler=${bonita_enable_acm_handler}"
    // end of settings

  ansiColor('xterm') {
    timestamps {
        stage("Checkout") { 
            checkout scm
            echo "jobBaseName: $jobBaseName"
            echo "gitRepoName: $gitRepoName"
            stash name: 'tests', includes: 'tests/**'
        }
        
        stage("Build LAs") {
            try{
                bcd scenario:scenarioFile, args: "${extraVars} livingapp build ${debug_flag} -p ${WORKSPACE} --environment ${bonitaConfiguration}"
            }finally{
                junit allowEmptyResults : true, testResults: 'restAPIExtensions/**/target/*-reports/*.xml'
                publishHTML (target: [
                  allowMissing: true,
                  alwaysLinkToLastBuild: false,
                  keepAll: true,
                  reportDir: 'restAPIExtensions/creditCardDisputeResolutionRestAPI/target/spock-reports',
                  reportFiles: 'index.html',
                  reportName: "REST API Extension Report"
                ])
            }
        }
        
         stage("Package BOS Archive") {
            sh 'mvn assembly:single'
            sh '''
                for f in target/credit-card-dispute-resolution-*.zip; do 
                    mv -- "$f" "${f%.zip}.bos"
                done
                timestamp=$(date +"%Y%m%d%H%M");
                for f in target/credit-card-dispute-resolution-*-SNAPSHOT.bos; do 
                    if [ -f "$f" ]; then
                        mv -- "$f" "${f%-SNAPSHOT.bos}-${timestamp}.bos"
                    fi
                done
            '''
        }

        stage("Create stack") {
            bcd scenario:scenarioFile, args: "${extraVars} ${verbose_mode} stack create", ignore_errors: false
        }

        stage("Undeploy server") {
            bcd scenario:scenarioFile, args: "${extraVars} ${verbose_mode} stack undeploy", ignore_errors: true
        }
              
        stage("Deploy server") {    
            def json_path = pwd(tmp: true) + '/bcd_stack.json'
            bcd scenario:scenarioFile, args: "${extraVars} ${verbose_mode} -e bcd_stack_json=${json_path} stack deploy"
            // set build description using bcd_stack.json file
            props = readJSON file: json_path
            currentBuild.description = "<a href='${props.bonita_url}/apps/${applicationToken}'>${props.bcd_stack_id}</a>"
        }

        def zip_files = findFiles(glob: "target/*_${jobBaseName}-${bonitaConfiguration}-*.zip")
        def bconf_files = findFiles(glob: "target/*_${jobBaseName}-${bonitaConfiguration}-*.bconf")
        def bConfArg = bconf_files && bconf_files[0].length > 0 ? "-c ${WORKSPACE}/${bconf_files[0].path}" : ""

        stage('Deploy LAs') {
            bcd scenario:scenarioFile, args: "${extraVars} livingapp deploy ${debug_flag} -p ${WORKSPACE}/${zip_files[0].path} ${bConfArg}"
        }

        stage('Archive') {
            archiveArtifacts artifacts: "target/*.zip, target/*.bconf, target/*.xml, target/*.bar, target/*.bos", fingerprint: true
        }
  	} // timestamps
  } // ansiColor
} // node

node('cypress'){

     stage('E2E Tests') {
     
         unstash 'tests'
         
         def cypressConf = readJSON file: 'tests/cypress.json'
         cypressConf.baseUrl = props.bonita_url
         writeJSON file: 'tests/cypress.json', json: cypressConf, pretty: 4
           
          dir('tests'){
             ansiColor('xterm') {
                 try{
                      sh 'npm install && npm test'
                 }finally{
                      publishHTML (target: [
                          allowMissing: false,
                          alwaysLinkToLastBuild: false,
                          keepAll: true,
                          reportDir: 'tests/mochawesome-report',
                          reportFiles: 'mochawesome.html',
                          reportName: "Cypress Report"
                        ])
                 }
            }
        }
    }

    stage('Archive videos') {
        archiveArtifacts artifacts: "tests/cypress/videos/*.mp4", fingerprint: true
    }
}

