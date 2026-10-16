package org.edu.pet.cloud_file_storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.service.s3.DirectorySpecificService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredListener {

    private final DirectorySpecificService directoryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {

        try {

            directoryService.createDirectory(SanitizedPath.ROOT, event.userId());
            log.info("Root directory for user with id {} successfully created", event.userId());
        } catch (Exception e) {
            log.error("Failed to create directory for user with id {}", event.userId(), e);
        }
    }
}