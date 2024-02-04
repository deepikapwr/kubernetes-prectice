pipeline {
    agent {
        docker {
            image 'abhishekf5/maven-abhishek-docker-agent:v1'
            args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
  
    stages {
        stage('Checkout') {
            steps {
                sh 'pwd'
                sh 'echo passed!!!!'
                git branch: 'main', url: 'git@github.com:deepikapwr/Simplified-CICD-GitOps-with-Jenkins-and-ArgoCD.git' # replace with your github repository 
                sh 'pwd'
                sh 'ls'
            }
        }
        stage('Build and Test') {
            steps {
                sh 'ls -ltr'
                // build the project and create a JAR files
                sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn clean package'
            }
        }
        stage('Static Code Analysis') {
            environment {
                SONAR_URL = "http://13.127.183.32:9000/"  ## replace the address once the sonarqube server is live 
            }
            steps {
                withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
                    sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}'
                }
            }
        }
        stage('Build and Push Docker Image') {
            environment {
                DOCKER_IMAGE = "kiran63/ultimate-cicd:${BUILD_NUMBER}" ## replace the DockerHub account name with yours 
                DOCKERFILE_LOCATION = "java-maven-sonar-argocd-helm-k8s/spring-boot-app/Dockerfile"
                REGISTRY_CREDENTIALS = credentials('docker-cred')
            }
            steps {
                script {
                    sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && docker build -t ${DOCKER_IMAGE} .'
                    def dockerImage = docker.image("${DOCKER_IMAGE}")
                    docker.withRegistry('https://index.docker.io/v1/', "docker-cred") {
                        dockerImage.push()
                    }
                }
            }
        }
        stage('Update Deployment File') {
            environment {
                GIT_REPO_NAME = "complete-CI/CD" ## replace with your github repository name
                GIT_USER_NAME = "deepikapwr"   ## replace with your github account username
            }
            steps {
                withCredentials([string(credentialsId: 'github', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "dpka531@gmail.com" ## replace with your github useremail
                        git config user.name "deepikapwr"            ## replace with your github usernamename
                        OLD_BUILD_NUMBER=$((${BUILD_NUMBER}-1))
                        sed -i "s/${OLD_BUILD_NUMBER}/${BUILD_NUMBER}/g" java-maven-sonar-argocd-helm-k8s/spring-boot-app-manifests/deployment.yml
                        git add java-maven-sonar-argocd-helm-k8s/spring-boot-app-manifests/deployment.yml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push @github.com/${GIT_USER_NAME}/${GIT_REPO_NAME">https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}



pipeline {
    agent {
        docker {
            image 'abhishekf5/maven-abhishek-docker-agent:v1'
            args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
  
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
        stage('Checkout') {
            steps {
                sh 'pwd'
                sh 'echo passed!!!!'
                git branch: 'main', url: 'git@github.com:deepikapwr/Simplified-CICD-GitOps-with-Jenkins-and-ArgoCD.git'
                sh 'pwd'
                sh 'ls'
            }
        }
        stage('Build and Test') {
            steps {
                sh 'ls -ltr'
                // build the project and create a JAR files
                sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn clean package'
            }
        }
        stage('Static Code Analysis') {
            environment {
                SONAR_URL = "http://13.127.183.32:9000/"  
            }
            steps {
                withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
                    sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}'
                }
            }
        }
        stage('Build and Push Docker Image') {
            environment {
                DOCKER_IMAGE = "dpka531/completecicd:${BUILD_NUMBER}" 
                DOCKERFILE_LOCATION = "java-maven-sonar-argocd-helm-k8s/spring-boot-app/Dockerfile"
                REGISTRY_CREDENTIALS = credentials('docker-hub')
            }
            steps {
                script {
                    sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && docker build -t ${DOCKER_IMAGE} .'
                    def dockerImage = docker.image("${DOCKER_IMAGE}")
                    docker.withRegistry('https://index.docker.io/v1/', "docker-hub") {
                        dockerImage.push()
                    }
                }
            }
        }
        stage('Update Deployment File') {
            environment {
                GIT_REPO_NAME = "complete-CI/CD"
                GIT_USER_NAME = "deepikapwr"  
            }
            steps {
                withCredentials([string(credentialsId: 'github', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "dpka531@gmail.com" 
                        git config user.name "deepikapwr"            
                        OLD_BUILD_NUMBER=$((${BUILD_NUMBER}-1))
                        sed -i "s/${OLD_BUILD_NUMBER}/${BUILD_NUMBER}/g" java-maven-sonar-argocd-helm-k8s/spring-boot-app-manifests/deployment.yml
                        cat script.sh
                        bash -x script.sh
                        git add .
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push @github.com/${GIT_USER_NAME}/${GIT_REPO_NAME">https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}