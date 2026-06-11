package org.jas.ksinxapp.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.model.UserRegisteredEvent;
import org.jas.ksinxapp.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final EmailService emailService;

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event){
        log.info("Sending verification to {} email after commit ", event.email());
        emailService.sendVerificationEmail(event.email(), event.recipientName(), event.token());
    }
}
