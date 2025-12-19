def contractModules = [
    'bpm-events-contracts',
    'bpm-grpc-contracts',
    'bpm-api'
]

def services = [
    'bpm-main-service',
    'bpm-audit-service',
    'bpm-statistics-service',
    'bpm-compliance-service',
    'bpm-onboarding-service',
    'notification-service'
]

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

        stage('Detect changes') {
            steps {
                script {
                    sh 'git fetch --all --prune'
                    def diff = sh(
                        script: """
                          set -e
                          git diff --name-only origin/${env.BRANCH_NAME}..HEAD 2>/dev/null || git diff --name-only HEAD~1..HEAD || true
                        """.stripIndent(),
                        returnStdout: true
                    ).trim()

                    def changedDirs = diff ? diff.split("\\n")*.tokenize('/')*.getAt(0).unique().findAll { it } : []
                    // Если diff пустой (первый билд или нет origin/BRANCH), соберём всё
                    if (changedDirs.isEmpty()) {
                        currentBuild.description = "No diff detected; building all"
                        env.REBUILD_ALL = 'true'
                    } else {
                        currentBuild.description = "Changed: ${changedDirs.join(', ')}"
                        env.CHANGED_DIRS = changedDirs.join(' ')
                    }

                    // Вычисляем флаги
                    def needContracts = (env.REBUILD_ALL == 'true') || changedDirs.any { it in contractModules }
                    env.NEED_CONTRACTS = needContracts ? 'true' : 'false'

                    def servicesToBuild = (env.REBUILD_ALL == 'true') ? services : changedDirs.findAll { it in services }
                    env.SERVICES_TO_BUILD = servicesToBuild.join(' ')
                }
            }
        }

        stage('Publish contracts') {
            when { expression { env.NEED_CONTRACTS == 'true' } }
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
            when { expression { env.SERVICES_TO_BUILD?.trim() } }
            steps {
                script {
                    env.SERVICES_TO_BUILD.split().each { svc ->
                        sh """
                          set -e
                          cd ${svc} && ./gradlew clean build --no-daemon -x test
                        """
                    }
                }
            }
        }

        stage('Docker build') {
            when { expression { env.SERVICES_TO_BUILD?.trim() } }
            steps {
                sh "docker-compose build ${env.SERVICES_TO_BUILD}"
            }
        }

        stage('Docker up') {
            when { expression { env.SERVICES_TO_BUILD?.trim() } }
            steps {
                sh "docker-compose up -d ${env.SERVICES_TO_BUILD}"
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true
        }
    }
}
