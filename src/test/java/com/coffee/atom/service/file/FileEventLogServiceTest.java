package com.coffee.atom.service.file;

import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.file.FileEventLog;
import com.coffee.atom.domain.file.FileEventLogRepository;
import com.coffee.atom.domain.file.FileEventLogType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static com.coffee.atom.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileEventLogServiceTest {

    @Mock
    FileEventLogRepository fileEventLogRepository;

    @InjectMocks
    FileEventLogService fileEventLogService;

    @Test
    void saveLog_savesWithFileNameAndSize() {
        AppUser u = user(1L, com.coffee.atom.domain.appuser.Role.ADMIN);
        MockMultipartFile file = new MockMultipartFile("f", "hello.txt", "text/plain", "hi".getBytes());

        fileEventLogService.saveLog(u, FileEventLogType.UPLOAD, file, "url", true);

        ArgumentCaptor<FileEventLog> captor = ArgumentCaptor.forClass(FileEventLog.class);
        verify(fileEventLogRepository).save(captor.capture());
        FileEventLog saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("hello.txt");
        assertThat(saved.getSize()).isEqualTo(String.valueOf(file.getSize()));
        assertThat(saved.getFile()).isEqualTo("url");
        assertThat(saved.getType()).isEqualTo(FileEventLogType.UPLOAD);
        assertThat(saved.getNumber()).isEqualTo(1L);
        assertThat(saved.getIsSuccess()).isTrue();
    }

    @Test
    void saveDeleteLogs_extractsOriginalNameBeforeDoubleDash() {
        AppUser u = user(1L, com.coffee.atom.domain.appuser.Role.ADMIN);
        String url = "https://storage/x/myfile--uuid-1234.pdf";

        fileEventLogService.saveDeleteLogs(List.of(url), u);

        ArgumentCaptor<List<FileEventLog>> captor = ArgumentCaptor.forClass(List.class);
        verify(fileEventLogRepository).saveAll(captor.capture());

        List<FileEventLog> logs = captor.getValue();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getName()).isEqualTo("myfile.pdf");
        assertThat(logs.get(0).getType()).isEqualTo(FileEventLogType.DELETE);
    }
}


