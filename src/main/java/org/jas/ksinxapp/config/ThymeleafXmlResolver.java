package org.jas.ksinxapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Set;

@Configuration
@Slf4j
public class ThymeleafXmlResolver {

    @Bean
    public SpringResourceTemplateResolver xmlTemplateResolver(ApplicationContext applicationContext){
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".xml");
        resolver.setTemplateMode(TemplateMode.XML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setResolvablePatterns(Set.of("sitemap"));
        resolver.setOrder(1);
        System.out.println("XML TEMPLATE RESOLVER REGISTERED: prefix=" + resolver.getPrefix()
                + ", suffix=" + resolver.getSuffix()
                + ", patterns=" + resolver.getResolvablePatterns());
        return resolver;
    }
}
