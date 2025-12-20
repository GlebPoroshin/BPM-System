pipeline {
    agent any
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    stages {
        stage("Checkout") {
            steps {
                checkout scm
            }
        }
        
        stage("Build Contracts") {
            steps {
                sh '''
                  set -e
                  ./bpm-events-contracts/gradlew -p bpm-events-contracts publishToMavenLocal --no-daemon
                  ./bpm-grpc-contracts/gradlew -p bpm-grpc-contracts publishToMavenLocal --no-daemon
                  ./bpm-api/gradlew -p bpm-api publishToMavenLocal --no-daemon
                '''
            }
        }
        
        stage("Build Services") {
            steps {
                sh '''
                  set -e
                  # Build only what is needed
                  ./bpm-main-service/gradlew -p bpm-main-service clean bootJar --no-daemon -x test
                  ./bpm-onboarding-service/gradlew -p bpm-onboarding-service clean bootJar --no-daemon -x test
                  ./bpm-audit-service/gradlew -p bpm-audit-service clean bootJar --no-daemon -x test
                  ./bpm-compliance-service/gradlew -p bpm-compliance-service clean bootJar --no-daemon -x test
                  ./notification-service/gradlew -p notification-service clean bootJar --no-daemon -x test
                  # ./bpm-statistics-service/gradlew -p bpm-statistics-service clean bootJar --no-daemon -x test
                '''
            }
        }
        
        stage("Docker Deploy") {
            steps {
                sh '''
                  set -e
                  # We only build and restart the services we just built
                  docker-compose build bpm-main-service bpm-onboarding-service bpm-audit-service bpm-compliance-service notification-service
                  docker-compose up -d bpm-main-service bpm-onboarding-service bpm-audit-service bpm-compliance-service notification-service
                '''
            }
        }
    }
}

