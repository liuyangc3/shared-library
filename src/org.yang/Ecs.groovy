package org.yang

/**
 Instance ecs API

 When Saving pipeline state on class
 must implement the Serializable interface
 */
@Grapes([
        @Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-core', version = '4.4.9'),
        @Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-ecs', version = '4.18.0')
])
class Ecs

    private IAcsClient client
    private String accessKey
    private String accessSecret
    def steps
    def instance
    def region

    Instance(steps) {
        this.steps = steps
    }

    def newClient(region, accessKey, accessSecret) {
        this.region = region
        this.accessKey = accessKey
        this.accessSecret = accessSecret
        def profile = DefaultProfile.getProfile(region, accessKey, accessSecret);
        this.client = new DefaultAcsClient(profile);
    }

    def describeInstance(instanceId) {
        request = new DescribeInstancesRequest();
        request.setRegionId(region);
        request.setInstanceIds(instanceId);

        try {
            DescribeInstancesResponse response = client.getAcsResponse(request);

            System.out.println(new Gson().toJson(response));
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
    }


}
