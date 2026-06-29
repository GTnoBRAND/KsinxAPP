package org.jas.ksinxapp.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Set;

@Configuration
public class ThymeleafXmlResolver {

    @Bean
    public SpringResourceTemplateResolver xmlTemplateResolver(ApplicationContext applicationContext){
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/static/");
        resolver.setSuffix(".xml");
        resolver.setTemplateMode(TemplateMode.XML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setResolvablePatterns(Set.of("sitemap"));
        resolver.setOrder(1);
        return resolver;
    }
}
