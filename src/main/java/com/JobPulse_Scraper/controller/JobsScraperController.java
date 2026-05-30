package com.JobPulse_Scraper.controller;



import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/v1")
@RequiredArgsConstructor
public class JobsScraperController {



    @GetMapping("/welcome")
    public String JobsScraperDoneWelcomePage(){

        return "Welcome to Jobs Scraper !";
    }

}
