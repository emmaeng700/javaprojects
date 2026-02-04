package com.example.uploadingfiles;

import com.example.uploadingfiles.controller.FileUploadRestController;
import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileUploadRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void listFiles_shouldReturnFileNames() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("file1.txt"), Paths.get("file2.pdf")));

        this.mvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("file1.txt")))
                .andExpect(jsonPath("$[1]", is("file2.pdf")));
    }

    @Test
    public void uploadFile_shouldReturnCreatedStatus() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "upload.txt",
                "text/plain",
                "Test Content".getBytes()
        );

        this.mvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("uploaded successfully")))
                .andExpect(jsonPath("$.filename", is("upload.txt")));

        then(this.storageService).should().store(file);
    }

    @Test
    public void downloadFile_withExistingFile_shouldReturnFile() throws Exception {
        org.springframework.core.io.Resource resource = 
            org.mockito.Mockito.mock(org.springframework.core.io.Resource.class);
        
        given(resource.getFilename()).willReturn("download.txt");
        given(this.storageService.loadAsResource("download.txt")).willReturn(resource);

        this.mvc.perform(get("/api/download/download.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    containsString("attachment; filename=\"download.txt\"")));
    }

    @Test
    public void downloadFile_withMissingFile_shouldReturn404() throws Exception {
        given(this.storageService.loadAsResource("missing.txt"))
                .willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("/api/download/missing.txt"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getFileCount_shouldReturnCount() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("a.txt"), Paths.get("b.txt"), Paths.get("c.txt")));

        this.mvc.perform(get("/api/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)));
    }
}