package py.com.kuaa.s3_backup_service.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3Client {

    public AmazonS3 getClientAWS(String accessKeyId, String accessSecKey, String region) {

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, accessSecKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();

        //// Regions.US_EAST_1
        /*
         * Regiones posibles
         * us-gov-west-1 = AWS GovCloud (US)
         * us-east-1 = US East (N. Virginia)
         * us-east-2 = US East (Ohio)
         * us-west-1 = US West (N. California)
         * us-west-2 = US West (Oregon)
         * eu-west-1 = EU (Ireland)
         * eu-west-2 = EU (London)
         * eu-west-3 = EU (Paris)
         * eu-central-1 = EU (Frankfurt)
         * ap-south-1 = Asia Pacific (Mumbai)
         * ap-southeast-1 = Asia Pacific (Singapore)
         * ap-southeast-2 = Asia Pacific (Sydney)
         * ap-northeast-1 = Asia Pacific (Tokyo)
         * ap-northeast-2 = Asia Pacific (Seoul)
         * sa-east-1 = South America (Sao Paulo)
         * cn-north-1 = China (Beijing)
         * cn-northwest-1 = China (Ningxia)
         * ca-central-1 = Canada (Central)
         * 
         * 
         */

    }
}
