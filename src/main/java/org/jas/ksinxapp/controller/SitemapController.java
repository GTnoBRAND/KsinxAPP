package org.jas.ksinxapp.controller;

import org.jas.ksinxapp.service.CourseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SitemapController {

    private final CourseService service;
    private final String baseUrl;

    public SitemapController(CourseService service, @Value("${app.base-url}") String baseUrl){
        this.service = service;
        this.baseUrl = baseUrl;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public String sitemap(Model model){
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("courses", service.findActiveCourseForSitemap());
        return "sitemap";
    }

}
