def alicloud = libraryFromLocalRepo().com.f2pool.alicloud

def instanceName
def instanceStatus
def datadiskId
def snapshotId


pipeline {
  agent any

  parameters {
    string(name: 'instanceId', defaultValue: '', description: 'instance id for backup')
    string(name: 'regionId', defaultValue: '', description: 'region id for backup')
    string(name: 'cron', defaultValue: 'H H/12 * * *', description: 'backup twice each day (every 12 hours)')
  }

  environment {
    ACCESS_KEY = credentials('ALICLOUD_ACCESS_KEY')
    SECRET_KEY = credentials('ALICLOUD_SECRET_KEY')
    SLACK_CHANNEL = '#jenkins'
  }

  triggers {
    cron("${params.cron}")
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Instance information') {
      steps {
        script {
          def ecs = alicloud.Ecs.new(this)
          ecs.initClient(params.regionId, ACCESS_KEY, SECRET_KEY)

          ecs.describeInstance(params.instanceId) {
            instanceName = it.instanceName
            instanceStatus = it.status
          }

          ecs.describeDisk(params.instanceId, "data")
            datadiskId = it.diskId
        }

        sh """
          echo instance name: ${instanceName}
          echo instance status: ${instanceStatus}
          echo data disk id: ${datadiskId}
        """
      }
    }

    stage('Create snapshot') {
      steps { // stop the instance
        script {
          def ecs = alicloud.Ecs.new(this)
          ecs.initClient(params.regionId, ACCESS_KEY, SECRET_KEY)
          if (instanceStatus == "Running") {
            // stop and  watch status, wait 120s
            // if instance still running should
            // force stop and send notice
            ecs.stopInstance(params.instanceId)
            def stopped = false
            def forcestop = false
            def seconds = 0

            while (!stopped) {
              ecs.describeInstance(params.instanceId) {
                stopped = it.status == "Stopped"
              }
              seconds += 3
              sleep(5000)

              // if instance still running we
              // should force stop this instance
              if (seconds > 60) {
                forcestop = true
                break
              }
            }

            if (forcestop) {
              // TODO force stop instance
              slackSend(channel: '${SLACK_CHANNEL}', color: 'red', message: 'Stop instance timeout(120s).')
            }
          }

          // backup data disk
          snapshotId = ecs.createSnapshot(datadiskId, 1)
          ecs.startInstance(params.instanceId)
        }
        sh "echo snapshot id:${snapshotId}"
      }
    }
  }

  post {
    // send result to slack, need to install plugin Slack Notification
    always {
      slackSend(channel: "${SLACK_CHANNEL}", message: "Starting backup instance data disk, instance: ${instanceName}, build: ${BUILD_NUMBER}.")
    }
    success {
      slackSend(channel: "${SLACK_CHANNEL}", color: 'green', message: "Backup success snapshot id: ${snapshotId}.")
    }
    failure {
      slackSend(channel: "${SLACK_CHANNEL}", color: 'red', message: 'Backup failed.')
    }
  }
}

def libraryFromLocalRepo() {
  // Workaround for loading the current repo as shared build lib.
  // Checks out to workspace local folder named like the identifier.
  // We have to pass an identifier with version (use branch name). Otherwise the build fails.
  library(identifier: "t@master", retriever: legacySCM(scm))
}
