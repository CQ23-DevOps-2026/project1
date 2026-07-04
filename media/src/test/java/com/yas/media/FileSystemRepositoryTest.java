package com.yas.media;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

import com.yas.media.config.FilesystemConfig;
import com.yas.media.repository.FileSystemRepository;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Slf4j
class FileSystemRepositoryTest {

    private static final String TEST_URL = "src/test/resources/test-directory";

    @Mock
    private FilesystemConfig filesystemConfig;

    @Mock
    private File file;

    @InjectMocks
    private FileSystemRepository fileSystemRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws IOException {
        Path testDir = Paths.get(TEST_URL);
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                .sorted((p1, p2) -> p2.compareTo(p1))
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException e) { e.printStackTrace(); }
                });
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  persistFile — error paths
    // ──────────────────────────────────────────────────────────────

    @Test
    void testPersistFile_whenDirectoryNotExist_thenThrowsException() {
        when(filesystemConfig.getDirectory()).thenReturn("non-exist-directory");
        assertThrows(IllegalStateException.class,
            () -> fileSystemRepository.persistFile("test-file.png", "test-content".getBytes()));
    }

    @Test
    void testPersistFile_filePathNotContainsDirectory() {
        new File(TEST_URL).mkdirs();
        when(filesystemConfig.getDirectory()).thenReturn(TEST_URL);
        // relative directory → absolute filePath won't start with it → IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
            () -> fileSystemRepository.persistFile("test-file.png", "test-content".getBytes()));
    }

    /** Covers TRUE branch of filename.contains("..") in buildFilePath(). */
    @Test
    void testPersistFile_whenFilenameContainsDotDot_thenThrowsIllegalArgumentException() {
        new File(TEST_URL).mkdirs();
        when(filesystemConfig.getDirectory()).thenReturn(TEST_URL);
        assertThrows(IllegalArgumentException.class,
            () -> fileSystemRepository.persistFile("../evil.png", "content".getBytes()));
    }

    /** Covers TRUE branch of filename.contains("/") — reached when ".." is false. */
    @Test
    void testPersistFile_whenFilenameContainsSlash_thenThrowsIllegalArgumentException() {
        new File(TEST_URL).mkdirs();
        when(filesystemConfig.getDirectory()).thenReturn(TEST_URL);
        assertThrows(IllegalArgumentException.class,
            () -> fileSystemRepository.persistFile("sub/evil.png", "content".getBytes()));
    }

    /** Covers TRUE branch of filename.contains("\\") — reached when ".." and "/" are both false. */
    @Test
    void testPersistFile_whenFilenameContainsBackslash_thenThrowsIllegalArgumentException() {
        new File(TEST_URL).mkdirs();
        when(filesystemConfig.getDirectory()).thenReturn(TEST_URL);
        assertThrows(IllegalArgumentException.class,
            () -> fileSystemRepository.persistFile("sub\\evil.png", "content".getBytes()));
    }

    /**
     * Covers the checkPermissions() failure branch (!canRead() || !canWrite() → true).
     * Uses assumeTrue so the test is skipped gracefully if the OS/user cannot change permissions.
     */
    @Test
    void testPersistFile_whenDirectoryNotWritable_thenThrowsIllegalStateException() throws IOException {
        Path tempDir = Files.createTempDirectory("media-test-perm");
        File dir = tempDir.toFile();
        boolean changed = dir.setWritable(false);
        assumeTrue(changed, "Cannot change directory write permission on this system — skipping");
        try {
            when(filesystemConfig.getDirectory()).thenReturn(tempDir.toAbsolutePath().toString());
            assertThrows(IllegalStateException.class,
                () -> fileSystemRepository.persistFile("test.png", "data".getBytes()));
        } finally {
            dir.setWritable(true);
            Files.deleteIfExists(tempDir);
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  persistFile — HAPPY PATH (most important missing branch!)
    //
    //  All previous tests either fail at checkExistingDirectory,
    //  checkPermissions, or buildFilePath. This test is the ONLY
    //  one that reaches Files.write() and the successful return.
    //  It covers:
    //    • FALSE branch of !filePath.startsWith(directory)
    //    • Files.write() statement
    //    • return filePath.toString()
    // ──────────────────────────────────────────────────────────────
    @Test
    void testPersistFile_whenValidAbsoluteDirectoryAndFilename_thenSaveFile() throws IOException {
        Path tempDir = Files.createTempDirectory("media-test-happy");
        try {
            String filename = "test-image.png";
            byte[] content = "file-content".getBytes();
            when(filesystemConfig.getDirectory()).thenReturn(tempDir.toAbsolutePath().toString());

            String result = fileSystemRepository.persistFile(filename, content);

            assertNotNull(result);
            Path savedFile = Paths.get(result);
            assertTrue(Files.exists(savedFile));
            assertArrayEquals(content, Files.readAllBytes(savedFile));
        } finally {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (IOException e) { /* ignore */ } });
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  getFile — paths
    // ──────────────────────────────────────────────────────────────

    @Test
    void testGetFile_whenDirectIsExist_thenReturnFile() throws IOException {
        String filename = "test-file.png";
        String filePathStr = Paths.get(TEST_URL, filename).toString();
        byte[] content = "test-content".getBytes();
        when(filesystemConfig.getDirectory()).thenReturn(TEST_URL);

        Path filePath = Paths.get(filePathStr);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, content);

        InputStream inputStream = fileSystemRepository.getFile(filePathStr);
        assertArrayEquals(content, inputStream.readAllBytes());
    }

    @Test
    void testGetFileDirectoryDoesNotExist_thenThrowsException() {
        String filePathStr = Paths.get("non-exist-directory", "test-file.png").toString();
        when(filesystemConfig.getDirectory()).thenReturn("non-exist-directory");
        assertThrows(IllegalStateException.class, () -> fileSystemRepository.getFile(filePathStr));
    }

    /**
     * Covers the catch(IOException) branch in getFile() by making the file unreadable
     * after it is created (AccessDeniedException extends IOException).
     */
    @Test
    void testGetFile_whenFileExistsButNotReadable_thenThrowsRuntimeException() throws IOException {
        Path tempDir = Files.createTempDirectory("media-test-ioex");
        Path tempFile = tempDir.resolve("secret.png");
        Files.write(tempFile, "content".getBytes());
        boolean changed = tempFile.toFile().setReadable(false);
        assumeTrue(changed, "Cannot change file read permission on this system — skipping");
        try {
            when(filesystemConfig.getDirectory()).thenReturn(tempDir.toAbsolutePath().toString());
            assertThrows(RuntimeException.class, () -> fileSystemRepository.getFile(tempFile.toString()));
        } finally {
            tempFile.toFile().setReadable(true);
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }
}
