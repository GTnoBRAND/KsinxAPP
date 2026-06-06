package org.jas.ksinxapp;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(IntegrationTestContainers.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
}
