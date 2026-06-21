package org.jas.ksinxapp.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.model.UserRegisteredEvent;
import org.jas.ksinxapp.service.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event){
        log.info("Dispatching verification email to {} after commit", event.email());
        emailService.sendVerificationEmail(event.email(), event.recipientName(), event.token());
    }
}
