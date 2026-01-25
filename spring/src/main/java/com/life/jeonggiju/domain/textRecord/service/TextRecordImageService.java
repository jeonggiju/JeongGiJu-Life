package com.life.jeonggiju.domain.textRecord.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.life.jeonggiju.config.S3Config;
import com.life.jeonggiju.domain.textRecord.entity.TextRecord;
import com.life.jeonggiju.domain.textRecord.entity.TextRecordImage;
import com.life.jeonggiju.domain.textRecord.exception.TextRecordImageException;
import com.life.jeonggiju.domain.textRecord.exception.TextRecordNotFoundException;
import com.life.jeonggiju.domain.textRecord.repository.TextRecordImageRepository;
import com.life.jeonggiju.domain.textRecord.repository.TextRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class TextRecordImageService{

	private static final String PATH_PREFIX = "text-record";
	private final S3Config s3Config;
	private final S3Client s3Client;
	private final TextRepository textRepository;
	private final TextRecordImageRepository imageRepository;

	@Transactional
	public List<String> uploadImages(UUID textRecordId, List<MultipartFile> files) {
		TextRecord textRecord = textRepository.findById(textRecordId)
			.orElseThrow(() -> TextRecordNotFoundException.withId(textRecordId));

		int currentOrder = imageRepository.countByTextRecordId(textRecordId);
		List<String> uploadedUrls = new ArrayList<>();

		for (MultipartFile file : files) {
			String imageUrl = uploadToS3(textRecordId, file);
			TextRecordImage image = TextRecordImage.of(textRecord, imageUrl, currentOrder++);
			imageRepository.save(image);
			uploadedUrls.add(imageUrl);
		}

		return uploadedUrls;
	}

	public void deleteAllImages(UUID textRecordId) {
		List<TextRecordImage> images = imageRepository.findByTextRecordIdOrderByDisplayOrderAsc(textRecordId);
		for (TextRecordImage image : images) {
			deleteFromS3(image.getImageUrl());
		}
	}

	@Transactional
	public void deleteImageByUrl(String imageUrl) {
		TextRecordImage image = imageRepository.findByImageUrl(imageUrl)
			.orElseThrow(() -> TextRecordImageException.deleteFailed(imageUrl, "이미지를 찾을 수 없습니다."));
		deleteFromS3(imageUrl);
		imageRepository.delete(image);
	}

	public List<String> getImageUrls(UUID textRecordId) {
		return imageRepository.findByTextRecordIdOrderByDisplayOrderAsc(textRecordId)
			.stream()
			.map(TextRecordImage::getImageUrl)
			.toList();
	}

	private String uploadToS3(UUID textRecordId, MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		String extension = originalFilename != null && originalFilename.contains(".")
			? originalFilename.substring(originalFilename.lastIndexOf("."))
			: "";
		String key = PATH_PREFIX + "/" + textRecordId + "/" + UUID.randomUUID() + extension;

		try {
			s3Client.putObject(
				b -> b.bucket(s3Config.getBucket())
					.key(key)
					.contentType(file.getContentType()),
				RequestBody.fromBytes(file.getBytes())
			);
		} catch (Exception e) {
			throw TextRecordImageException.uploadFailed(textRecordId, e.getMessage());
		}

		return String.format("https://%s.s3.%s.amazonaws.com/%s",
			s3Config.getBucket(), s3Config.getRegion(), key);
	}

	private void deleteFromS3(String imageUrl) {
		String key = extractKeyFromUrl(imageUrl);

		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(s3Config.getBucket())
				.key(key)
				.build());
		} catch (Exception e) {
			throw TextRecordImageException.deleteFailed(imageUrl, e.getMessage());
		}
	}

	private String extractKeyFromUrl(String imageUrl) {
		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
			s3Config.getBucket(), s3Config.getRegion());
		return imageUrl.replace(prefix, "");
	}
}
