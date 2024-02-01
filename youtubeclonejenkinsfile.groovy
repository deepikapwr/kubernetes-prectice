def COLOR_MAP = [
    'FAILURE' : 'danger',
    'SUCCESS' : 'good'
]
pipeline{
    agent any
    tools{
        jdk 'jdk17'
        nodejs 'node16'
    }
    environment {
        SCANNER_HOME=tool 'sonar-scanner'
    }
    stages{
        stage('gitcheckout'){
            steps{
                git branch: 'main', credentialsId: 'git', url: 'git@github.com:deepikapwr/Youtube-clone-app.git'
            }
        }
        stage("Sonarqube Analysis "){
            steps{
                withSonarQubeEnv('sonar-server') {
                    sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=youtube \
                    -Dsonar.projectKey=youtube '''
                }
            }    
        }
        stage("qualitygate"){
            when { expression { params.action == 'create'}}    
            steps{
                script{
                    def credentialsId = 'sonarqube'
                    qualityGate(credentialsId)
                }
            }
        }
        stage('Install Dependencies') {
            steps {
                sh "npm install"
            }
        }
        stage('docker build & push') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker', toolName: 'docker') {
                      sh "docker build --build-arg REACT_APP_RAPID_API_KEY=3bb3b5a716msh0d06e282c5c6b66p1d0530jsn03b7c6469679 -t youtube ."
                      sh "docker tag youtube dpka531/youtube:latest"
                      sh "docker push dpka531/youtube:latest"
                    }
                }
            }
        }
        stage('deploy to eks') {
            steps {
                sh 'kubectl apply -f deployment.yml'
            }
        }
    }
    post {
        always {
            echo 'slack Notifications'
            slackSend (
                channel: '#jenkins',
            color: COLOR_MAP[currentBuild.currentResult],
            message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} \n build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}"
            )
        }

    }
}



def COLOR_MAP = [
    'FAILURE' : 'danger',
    'SUCCESS' : 'good'
]
pipeline{
    agent any
    tools{
        jdk 'jdk17'
        nodejs 'node16'
    }
    environment {
        SCANNER_HOME=tool 'sonar-scanner'
    }
    stages{
        stage('gitcheckout'){
            steps{
                git branch: 'main', credentialsId: 'git', url: 'git@github.com:deepikapwr/Youtube-clone-app.git'
            }
        }
        stage("Sonarqube Analysis "){
            steps{
                withSonarQubeEnv('sonar-server') {
                    sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=youtube \
                    -Dsonar.projectKey=youtube '''
                }
            }    
        }
        stage("qualitygate"){
            when { expression { params.action == 'create'}}    
            steps{
                script{
                    def credentialsId = 'sonarqube'
                    qualityGate(credentialsId)
                }
            }
        }
        stage('Install Dependencies') {
            steps {
                sh "npm install"
            }
        }
        stage('docker build & push') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker', toolName: 'docker') {
                      sh "docker build --build-arg REACT_APP_RAPID_API_KEY=3bb3b5a716msh0d06e282c5c6b66p1d0530jsn03b7c6469679 -t youtube ."
                      sh "docker tag youtube dpka531/youtube:latest"
                      sh "docker push dpka531/youtube:latest"
                    }
                }
            }
        }
    }
    post {
        always {
            echo 'slack Notifications'
            slackSend (
                channel: '#jenkins',
            color: COLOR_MAP[currentBuild.currentResult],
            message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} \n build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}"
            )
        }

    }
} 