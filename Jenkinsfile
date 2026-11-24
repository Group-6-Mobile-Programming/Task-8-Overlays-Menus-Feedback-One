// Runs Gradle on any OS: ./gradlew on Linux/Mac, gradlew.bat on Windows
def gradle(String args) {
    if (isUnix()) {
        sh "./gradlew ${args}"
    } else {
        bat "gradlew.bat ${args}"
    }
}

pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout(true)   // the Checkout stage below does it once
    }

    triggers {
        githubPush()   // fired by the GitHub webhook through ngrok
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false'
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
                        bat 'java -version'
                    }
                    gradle('--version')
                }
            }
        }

        stage('Inject Firebase config') {
            steps {
                // Keeps google-services.json out of the public repo.
                // Single quotes on purpose: the secret path must not be interpolated by Groovy.
                withCredentials([file(credentialsId: 'google-services-json', variable: 'GS_JSON')]) {
                    script {
                        if (isUnix()) {
                            sh 'cp "$GS_JSON" app/google-services.json'
                        } else {
                            bat 'copy /Y "%GS_JSON%" "app\\google-services.json"'
                        }
                    }
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