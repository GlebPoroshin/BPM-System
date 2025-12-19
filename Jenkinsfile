pipeline {
    agent any
    options {
        timestamps()
        ansiColor('xterm')
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Publish contracts') {
            steps {
                sh '''
                  set -e
                  cd bpm-events-contracts && ./gradlew publishToMavenLocal --no-daemon
                  cd ../bpm-grpc-contracts && ./gradlew publishToMavenLocal --no-daemon
                  cd ../bpm-api && ./gradlew publishToMavenLocal --no-daemon
                '''
            }
        }
        stage('Build services') {
            steps {
                sh '''
                  set -e
                  cd bpm-main-service && ./gradlew clean build --no-daemon -x test
                  cd ../bpm-audit-service && ./gradlew clean build --no-daemon -x test
                  cd ../bpm-statistics-service && ./gradlew clean build --no-daemon -x test
                  cd ../bpm-compliance-service && ./gradlew clean build --no-daemon -x test
                  cd ../bpm-onboarding-service && ./gradlew clean build --no-daemon -x test
                  cd ../notification-service && ./gradlew clean build --no-daemon -x test
                '''
            }
        }
        stage('Docker build') {
            steps {
                sh '''
                  docker-compose build
                '''
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true
        }
    }
}
