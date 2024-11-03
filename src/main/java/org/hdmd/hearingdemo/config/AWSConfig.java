package org.hdmd.hearingdemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;

@Configuration
public class AWSConfig {

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;


    @Bean
    public S3Client s3Client() {
        System.out.println("Access Key ID: " + accessKeyId);
        System.out.println("Secret Access Key: " + secretAccessKey);
        System.out.println("Region: " + region);

        if (accessKeyId.isBlank() || secretAccessKey.isBlank()) {
            throw new IllegalArgumentException("액세스키와 시크릿키는 blank일 수 없습니다.");
        }
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of(region))
                .build();
    }
}
