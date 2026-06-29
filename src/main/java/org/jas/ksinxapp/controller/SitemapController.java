package org.jas.ksinxapp.controller;

import org.jas.ksinxapp.service.CourseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Controller
public class SitemapController {

    private final CourseService service;
    private final SpringTemplateEngine templateEngine;
    private final String baseUrl;

    public SitemapController(CourseService service, SpringTemplateEngine templateEngine,@Value("${app.base-url}") String baseUrl){
        this.service = service;
        this.templateEngine = templateEngine;
        this.baseUrl = baseUrl;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap(){
        Context context = new Context();
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("courses", service.findActiveCourseForSitemap());
        return templateEngine.process("sitemap",context);
    }

}
