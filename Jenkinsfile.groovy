node {
    def env = "qa2"
    def prod_build
    def prod_branch
    def dev_build
    def dev_branch
    
    stage ('Preparation') {
        git credentialsId: 'git-001', url: 'https://github.com/ansonLx/jenkins-jobs.git'

        prod_build = sh(script: 'cat ./sh/ne-version | grep "^prod_build=" | sed "s/prod_build=//"', returnStdout: true).trim()
        prod_branch = sh(script: 'cat ./sh/ne-version | grep "^prod_branch=" | sed "s/prod_branch=//"', returnStdout: true).trim()
        dev_build = sh(script: 'cat ./sh/ne-version | grep "^dev_build=" | sed "s/dev_build=//"', returnStdout: true).trim()
        dev_branch = sh(script: 'cat ./sh/ne-version | grep "^dev_branch=" | sed "s/dev_branch=//"', returnStdout: true).trim()
    }
    
    stage ("deploy ns_version->${dev_build} ne_version->${prod_build}") {
        echo "deploy ${env} ne-version: ${prod_build} ns-version: ${dev_build}"
        sh(script: "./sh/deploy_test.sh ${prod_build} ${dev_build}")
    }
    stage ('test-01') {
        echo "run test job ne_version is ${prod_build} ns_version is ${dev_build}"
        build job: 'run test job 01', parameters: [string(name: 'ne_version', value: "${prod_build}"), string(name: 'ns_version', value: "${dev_build}")]
    }
    stage ('deploy-02') {
        echo "deploy ${env} ne-version: ${dev_build} ns-version: ${prod_build}"
        sh(script: "./sh/deploy_test.sh ${dev_build} ${prod_build}")
    }
    stage ('test-02') {
        echo "run test job ne_version is ${dev_build} ns_version is ${prod_build}"
        build job: 'run test job 01', parameters: [string(name: 'ne_version', value: "${dev_build}"), string(name: 'ns_version', value: "${prod_build}")]
    }
}
