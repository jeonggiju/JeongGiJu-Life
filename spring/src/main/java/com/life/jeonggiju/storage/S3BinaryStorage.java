package com.life.jeonggiju.storage;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.life.jeonggiju.config.S3Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3BinaryStorage implements BinaryStorage {

	private final S3Config config;
	private final S3Client s3;
	private static final String PATH = "profile/";

	@Override
	public void put(UUID userId, byte[] data, String contentType) {
		String key = PATH + "/" +  userId;

		s3.putObject(b -> b.bucket(config.getBucket())
			.key(key)
			.contentType(contentType), RequestBody.fromBytes(data));
	}

	@Override
	public InputStream get(UUID userId) {
		String key = PATH + "/" +  userId;

		return s3.getObject(b -> b.bucket(config.getBucket())
			.key(key));
	}

	@Override
	public Boolean exists(UUID userId) {
		String key = PATH + "/" +  userId;

		HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
			.bucket(config.getBucket())
			.key(key)
			.build();

		s3.headObject(headObjectRequest);
		return true;
	}

	@Override
	public String getUrl(UUID userId) {
		String key = PATH + "/" + userId;
		return String.format("https://%s.s3.%s.amazonaws.com/%s",
			config.getBucket(), config.getRegion(), key);
	}
}
