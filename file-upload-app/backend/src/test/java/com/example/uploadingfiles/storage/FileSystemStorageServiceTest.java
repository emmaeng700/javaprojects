package com.example.uploadingfiles.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSystemStorageServiceTest {

    private StorageProperties properties = new StorageProperties();
    private FileSystemStorageService service;
    private Path testDir;

    @BeforeEach
    void setUp() {
        properties.setLocation("test-upload");
        testDir = Paths.get(properties.getLocation());
        service = new FileSystemStorageService(properties);
        service.deleteAll();
        service.init();
    }

    @Test
    void init_shouldCreateDirectory() {
        assertThat(Files.exists(testDir)).isTrue();
        assertThat(Files.isDirectory(testDir)).isTrue();
    }

    @Test
    void store_withValidFile_shouldSaveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Spring Framework Test".getBytes()
        );

        service.store(file);

        Path storedFile = testDir.resolve("test.txt");
        assertThat(Files.exists(storedFile)).isTrue();
    }

    @Test
    void store_withEmptyFile_shouldThrowException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThatThrownBy(() -> service.store(emptyFile))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to store empty file");
    }

    @Test
    void store_withRelativePath_shouldThrowException() {
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "text/plain",
                "malicious content".getBytes()
        );

        assertThatThrownBy(() -> service.store(maliciousFile))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("relative path");
    }

    @Test
    void loadAll_shouldReturnAllFiles() {
        MockMultipartFile file1 = new MockMultipartFile("file", "file1.txt", "text/plain", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "file2.txt", "text/plain", "content2".getBytes());

        service.store(file1);
        service.store(file2);

        List<Path> files = service.loadAll().collect(Collectors.toList());

        assertThat(files).hasSize(2);
        assertThat(files).extracting(path -> path.getFileName().toString())
                .containsExactlyInAnyOrder("file1.txt", "file2.txt");
    }

    @Test
    void loadAll_withEmptyDirectory_shouldReturnEmptyStream() {
        List<Path> files = service.loadAll().collect(Collectors.toList());
        assertThat(files).isEmpty();
    }

    @Test
    void load_shouldReturnPath() {
        Path path = service.load("test.txt");
        assertThat(path.getFileName().toString()).isEqualTo("test.txt");
    }

    @Test
    void loadAsResource_withExistingFile_shouldReturnResource() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resource.txt",
                "text/plain",
                "Resource content".getBytes()
        );

        service.store(file);

        Resource resource = service.loadAsResource("resource.txt");

        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
        assertThat(resource.getFilename()).isEqualTo("resource.txt");
    }

    @Test
    void loadAsResource_withNonExistingFile_shouldThrowException() {
        assertThatThrownBy(() -> service.loadAsResource("nonexistent.txt"))
                .isInstanceOf(StorageFileNotFoundException.class)
                .hasMessageContaining("Could not read file: nonexistent.txt");
    }

    @Test
    void deleteAll_shouldRemoveAllFiles() {
        MockMultipartFile file = new MockMultipartFile("file", "delete-me.txt", "text/plain", "content".getBytes());
        service.store(file);

        assertThat(Files.exists(testDir.resolve("delete-me.txt"))).isTrue();

        service.deleteAll();

        assertThat(Files.exists(testDir)).isFalse();
    }

    @Test
    void constructor_withEmptyLocation_shouldThrowException() {
        StorageProperties emptyProps = new StorageProperties();
        emptyProps.setLocation("");

        assertThatThrownBy(() -> new FileSystemStorageService(emptyProps))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("cannot be Empty");
    }

    @Test
    void constructor_withBlankLocation_shouldThrowException() {
        StorageProperties blankProps = new StorageProperties();
        blankProps.setLocation("   ");

        assertThatThrownBy(() -> new FileSystemStorageService(blankProps))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("cannot be Empty");
    }
}