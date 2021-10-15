pipeline {
    parameters {
        string defaultValue: '', description: 'Versionsnummer (pushed ins Release statt Snapshot-repository)', name: 'PLUGIN_VERSION'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
    }
    agent {
        kubernetes {
            label "ozghub-prozesspipeline-${UUID.randomUUID().toString()}"
            idleMinutes 1
            yaml """
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: gradle
            image: gradle:6.7-jdk11
            command:
            - cat
            tty: true
            resources:
              limits:
                memory: 1536Mi
                cpu: 1500m
              requests:
                memory: 1024Mi
                cpu: 1000m
            volumeMounts:
            - name: gradle-settings
              mountPath: /root/.gradle/gradle.properties
          volumes:
          - name: gradle-settings
            hostPath:
              path: /home/jenkins/.gradle/gradle.properties
        """
        }
    }

    // Generates "BITBUCKET_AUTHENTICATION_USR" and "BITBUCKET_AUTHENTICATION_PSW"
    // Generates "GRADLE_AUTHENTICATION_USR" and "GRADLE_AUTHENTICATION_PSW"
    environment {
        BITBUCKET_AUTHENTICATION = credentials('9026a4c0-38cf-4fb8-b487-efddde85a6be')
        GRADLE_AUTHENTICATION = credentials('61e7b4b8-f75b-11ea-adc1-0242ac120002')
    }

    stages {
        stage('Build und Test') {
            steps {
                script {
                    catchError {
                        container('gradle') {
                            sh 'gradle clean build test --no-daemon -PpluginVersion=' + getPluginVersion()
                        }
                    }
                    rocketChatSendMessage()
                }
            }
        }
        stage('Publish Artifact to SB-Maven-repository') {
            steps {
                script {
                    def pluginVersion = getPluginVersion()
                    def repositoryKey = versionManuallySet() ? "ci-releases" : "ci-snapshots"

                    if (versionManuallySet()) {
                        println "Setze neues Tag " + pluginVersion
                        gitCommitAndTagRelease(pluginVersion)
                    }

                    println "Pushe Version " + pluginVersion + " nach " + repositoryKey
                    container('gradle') {
                        sh('gradle --info artifactoryPublish -PpluginVersion=' + pluginVersion + ' -PrepositoryKey=' + repositoryKey)
                    }
                }
            }
        }
    }
    post {
        always {
            junit 'build/test-results/**/*.xml'
            recordIssues(tools: [
                    java(),
                    spotBugs(pattern: 'build/**/spotbugs/**/*.xml'),
                    checkStyle(pattern: 'build/**/checkstyle/**/*.xml', reportEncoding: 'UTF-8')])
            jacoco()
        }
    }
}

def versionManuallySet() {
    return params.PLUGIN_VERSION != null && params.PLUGIN_VERSION != ""
}

def searchForPostfix(branch, regex) {
    try {
        def m = branch =~ regex
        return m[0][1]
    }
    catch (Throwable t) {
        return "";
    }
}

def getPluginVersion() {
    if (versionManuallySet()) {
        return params.PLUGIN_VERSION
    }

    def branch = env.GIT_BRANCH
    println "Branchname ist: " + branch

    if (branch == "master") {
        return "99.0.0-" + "SNAPSHOT"
    }

    def postfix = searchForPostfix(branch, /.*(SBW-\d*).*/) + searchForPostfix(branch, /.*noticket-(.*)/)
    if (postfix == "")
        postfix = branch.replace("/", "-")

    return postfix + "-SNAPSHOT";
}

def gitCommitAndTagRelease(newVersion) {
    echo "[JENKINSFILE] Erstelle neuen Git Tag"
    String tagName = "release/${newVersion}"
    sh "git config user.email \"jenkins@localhost\""
    sh "git config user.name \"Jenkins\""
    sh "git tag \"${tagName}\""
    def gitUrl = sh(returnStdout: true, script: "git config remote.origin.url").trim()
    withCredentials([[$class: "UsernamePasswordMultiBinding", credentialsId: "9026a4c0-38cf-4fb8-b487-efddde85a6be", usernameVariable: "GIT_AUTHOR_NAME", passwordVariable: "GIT_PASSWORD"]]) {
        gitUrl = gitUrl.replaceAll(/\/\/(.*@)?/, "//${GIT_AUTHOR_NAME}:${GIT_PASSWORD}@")
        sh "git push ${gitUrl} \"${tagName}\""
    }
    return true
}

def getCurrentChangelogAuthor() {
    return currentBuild.changeSets?.collect { it.items?.collect { it.author.toString() } }.flatten().unique()[0]
}

def getRocketChatUserName(fullName) {
    def mapFullNameToRocketChatName = [
            "Johannes Kern"    : "@jkern",
            "Matt Bond"        : "@mbond",
            "Thomas Fox"       : "@fischer",
            "Sebastian Messner": "@smessner",
            "Leonard Wicke"    : "@lwicke"
    ]
    return mapFullNameToRocketChatName[fullName] ?: "#sbw-schildkroeten"
}

String getEmojiForBuildResult(buildResult) {
    def mapResultToEmoji = [
            "NOT_BUILT": ":white_circle:",
            "ABORTED"  : ":white_circle:",
            "SUCCESS"  : ":green_apple:",
            "UNSTABLE" : ":yellow_heart:",
            "FAILED"   : ":jenkinsbad:",
            "FAILURE"  : ":jenkinsbad:"
    ]
    return mapResultToEmoji[buildResult] ?: ":jenkins:"
}

def rocketChatSendMessage() {
    try {
        def buildResult = currentBuild.result ?: "SUCCESS"
        println "[JENKINSFILE] Build & Test sind abgeschlossen. Status: " + buildResult

        def author = getRocketChatUserName(getCurrentChangelogAuthor())
        def emoji = getEmojiForBuildResult(buildResult)
        def message = "${emoji} <${env.BUILD_URL}|${currentBuild.displayName}>\n"
        message += "*Branch:* ${env.GIT_BRANCH}\n"

        if (buildResult == "SUCCESS") {
            message += "*Grund:* :checkered_flag: Der Jenkinsjob ist nun beendet."
        } else {
            message += "*Grund:* Der Build von ozghub-prozess-pipeline ist fehlgeschlagen."
        }

        rocketSend(
                avatar: "https://ci.dev.seitenbau.net/static/e9ab2610/images/headshot.png",
                channel: "${author}",
                message: message,
                rawMessage: true)
    } catch (err) {
        println "[JENKINSFILE] [WARN] Konnte Rocketchat Nachricht nicht versenden.\nFehlermeldung: ${err.getMessage()}"
    }
}
