pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }

    environment {
        // Чтобы кеши были изолированы в пределах workspace и не конфликтовали между разными билдами на одном агенте
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
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
                  ./bpm-events-contracts/gradlew -p bpm-events-contracts publishToMavenLocal --no-daemon -Porg.gradle.java.installations.auto-detect=false
                  ./bpm-grpc-contracts/gradlew -p bpm-grpc-contracts publishToMavenLocal --no-daemon -Porg.gradle.java.installations.auto-detect=false
                  ./bpm-api/gradlew -p bpm-api publishToMavenLocal --no-daemon -Porg.gradle.java.installations.auto-detect=false
                '''
            }
        }

        stage("Build Services (Verification)") {
            failFast true
            parallel {
                stage("bpm-main-service") {
                    steps {
                        sh '''
                          set -e
                          ./bpm-main-service/gradlew -p bpm-main-service clean bootJar --no-daemon -x test -Porg.gradle.java.installations.auto-detect=false
                        '''
                    }
                }

                stage("bpm-onboarding-service") {
                    steps {
                        sh '''
                          set -e
                          ./bpm-onboarding-service/gradlew -p bpm-onboarding-service clean bootJar --no-daemon -x test -Porg.gradle.java.installations.auto-detect=false
                        '''
                    }
                }

                stage("bpm-audit-service") {
                    steps {
                        sh '''
                          set -e
                          ./bpm-audit-service/gradlew -p bpm-audit-service clean bootJar --no-daemon -x test -Porg.gradle.java.installations.auto-detect=false
                        '''
                    }
                }

                stage("bpm-compliance-service") {
                    steps {
                        sh '''
                          set -e
                          ./bpm-compliance-service/gradlew -p bpm-compliance-service clean bootJar --no-daemon -x test -Porg.gradle.java.installations.auto-detect=false
                        '''
                    }
                }
            }
        }

        stage("Archive Results") {
            steps {
                archiveArtifacts artifacts: '**/build/libs/*.jar', allowEmptyArchive: true
            }
        }
    }
}
