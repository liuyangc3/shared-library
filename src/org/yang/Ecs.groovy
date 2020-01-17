package org.yang


@Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-core', version = '4.4.9')
@Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-ecs', version = '4.18.0')
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.IAcsClient
import com.aliyuncs.exceptions.ServerException
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.ecs.model.v20140526.*
import groovy.json.JsonOutput

class Ecs implements Serializable {

    // reference pipeline
    def steps

    // alicloud ecs properties
    def region
    def client

    Ecs(steps) {
        this.steps = steps
    }

    def initClient(region, accessKey, accessSecret) {
        def profile = DefaultProfile.getProfile(region, accessKey, accessSecret)
        this.client = new DefaultAcsClient(profile)
    }

    def describeInstance(instanceId, Closure closure) {
        def instanceIds = JsonOutput.toJson([instanceId])
        def request = new DescribeInstancesRequest();
        request.setRegionId(region)
        request.setInstanceIds(instanceIds)

        try {
            def response = client.getAcsResponse(request)
            def instance = response.instances[0]
            closure(instance)
        } catch (ServerException e) {
            e.printStackTrace()
        }
    }

    def stopInstance(instanceId) {
        def request = new StopInstanceRequest()
        request.setRegionId(region)
        request.setInstanceId(instanceId)

        try {
            def response = client.getAcsResponse(request)
            return response
        } catch (ServerException e) {
            e.printStackTrace()
        }
    }

    def startInstance(instanceId) {
        def request = new StartInstanceRequest();
        request.setRegionId(region)
        request.setInstanceId(instanceId)

        try {
            def response = client.getAcsResponse(request)
            return response
        } catch (ServerException e) {
            e.printStackTrace()
        }
    }

    def describeDisk(instanceId, diskType, Closure closure) {
        def request = new DescribeDisksRequest();
        request.setRegionId(region)
        request.setInstanceId(instanceId)
        request.setDiskType(diskType)

        try {
            def response = client.getAcsResponse(request)
            def disk = response.disks[0]
            closure(disk)
        } catch (ServerException e) {
            e.printStackTrace()
        }
    }

    def createSnapshot(diskId, retentionDays = 0) {
        def request = new CreateSnapshotRequest();
        request.setRegionId(region)
        request.setDiskId(diskId)
        request.setRetentionDays(retentionDays)

        try {
            def response = client.getAcsResponse(request)
            return response.snapshotId
        } catch (ServerException e) {
            e.printStackTrace()
        }
    }
}
