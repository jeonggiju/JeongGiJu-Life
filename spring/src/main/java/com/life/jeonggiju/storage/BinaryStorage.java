package com.life.jeonggiju.storage;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

public interface BinaryStorage {
	void put(UUID userId, byte[] data);

	InputStream get(UUID userId);

	Boolean exists(UUID userId);
}