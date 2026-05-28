package org.jas.ksinxapp.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleConverter implements AttributeConverter<User.Role, String> {

    @Override
    public String convertToDatabaseColumn(User.Role role) {
        if (role == null) return null;
        return role.name();
    }

    @Override
    public User.Role convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return User.Role.valueOf(dbValue.toUpperCase());
    }
}
