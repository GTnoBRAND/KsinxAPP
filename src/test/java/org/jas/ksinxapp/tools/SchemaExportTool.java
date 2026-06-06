package org.jas.ksinxapp.tools;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.DelayedDropRegistryNotAvailableImpl;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Enrollment;
import org.jas.ksinxapp.model.Modules;
import org.jas.ksinxapp.model.PaymentTransaction;
import org.jas.ksinxapp.model.SubscriptionModel;
import org.jas.ksinxapp.model.Task;
import org.jas.ksinxapp.model.TaskSubmission;
import org.jas.ksinxapp.model.User;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Offline generator for the Flyway baseline. Boots only Hibernate metadata (no DB, no Docker,
 * no Spring context) using the exact dialect + naming strategy the running app uses, then writes
 * the CREATE script to target/generated-baseline.sql.
 *
 * Run with:  ./mvnw test -Dtest=SchemaExportTool
 * This is a build-time tool, not an assertion-bearing test.
 */
class SchemaExportTool {

    @Test
    void exportSchema() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                // matches Spring Boot's default physical naming (camelCase -> snake_case)
                .applySetting("hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .applySetting("hibernate.format_sql", "true")
                .applySetting("hibernate.hbm2ddl.delimiter", ";")
                .build();

        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Course.class)
                    .addAnnotatedClass(Enrollment.class)
                    .addAnnotatedClass(Modules.class)
                    .addAnnotatedClass(Task.class)
                    .addAnnotatedClass(TaskSubmission.class)
                    .addAnnotatedClass(PaymentTransaction.class)
                    .addAnnotatedClass(SubscriptionModel.class)
                    .buildMetadata();

            Map<String, Object> config = new HashMap<>();
            config.put("jakarta.persistence.schema-generation.scripts.action", "create");
            config.put("jakarta.persistence.schema-generation.scripts.create-target", "target/generated-baseline.sql");
            config.put("jakarta.persistence.schema-generation.create-source", "metadata");

            SchemaManagementToolCoordinator.process(
                    metadata, registry, config, DelayedDropRegistryNotAvailableImpl.INSTANCE);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
