package com.life.jeonggiju.storage.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class ProfileImageUploadException extends StorageException {
	public ProfileImageUploadException(Throwable cause) {
		super(ErrorCode.PROFILE_IMAGE_UPLOAD_FAILED, cause);
	}
}
