// Runs Gradle on any OS: ./gradlew on Linux/Mac, gradlew.bat on Windows
def gradle(String args) {
    if (isUnix()) {
        sh "./gradlew ${args}"
    } else {
        bat """
            set "PATH=${env.JAVA_HOME}\\bin;%PATH%"
            gradlew.bat ${args}
        """
    }
}

pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        githubPush()   // fired by the GitHub webhook through ngrok
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false'
        JAVA_HOME   = 'C:\\Program Files\\Android\\Android Studio\\jbr'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify tools') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'java -version'
                        sh 'chmod +x gradlew'
                    } else {
                        bat '''
                            set "PATH=%JAVA_HOME%\\bin;%PATH%"
                            java -version
                        '''
                    }
                    gradle('--version')
                }
            }
        }

        stage('Build debug APK') {
            steps {
                script { gradle('clean assembleDebug') }
            }
        }

        stage('Unit tests') {
            steps {
                script { gradle('testDebugUnitTest') }
            }
        }

        stage('Lint') {
            steps {
                // Lint problems mark the build UNSTABLE instead of failing it
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script { gradle('lintDebug') }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'app/build/test-results/**/*.xml'
            archiveArtifacts artifacts: 'app/build/reports/lint-results-debug.html', allowEmptyArchive: true
        }
        success {
            archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
        }
        failure {
            echo 'Build failed - check the Console Output for the first red error.'
        }
    }
}