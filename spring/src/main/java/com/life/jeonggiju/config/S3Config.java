package com.life.jeonggiju.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Getter
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3Config {
	@Value("${aws.accessKeyId}")
	private String accessKey;

	@Value("${aws.secretKey}")
	private String secretKey;

	@Value("${aws.region}")
	private String region;

	@Value("${aws.bucket}")
	private String bucket;

	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

		return S3Client.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
			.build();
	}
}
