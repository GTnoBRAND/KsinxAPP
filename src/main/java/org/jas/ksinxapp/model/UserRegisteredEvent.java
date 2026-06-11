package org.jas.ksinxapp.model;


//event holds the data that the email(listener) needs
public record UserRegisteredEvent(
        String email,
        String recipientName,
        String token
) {
}
