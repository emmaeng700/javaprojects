package com.example.uploadingfiles.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.uploadingfiles.config.StorageProperties;

@Service
public class StorageService {

	private final Path rootLocation;

	@Autowired
	public StorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	public void init() {
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize storage location", e);
		}
	}

	public void deleteAll() {
		try {
			if (Files.exists(rootLocation)) {
				Files.walk(rootLocation)
					.sorted(java.util.Comparator.reverseOrder())
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
							throw new RuntimeException("Could not delete " + path, e);
						}
					});
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not delete storage location", e);
		}
	}
}
