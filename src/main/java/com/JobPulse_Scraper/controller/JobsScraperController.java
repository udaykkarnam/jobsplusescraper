package com.JobPulse_Scraper.controller;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/v1")
@RequiredArgsConstructor
@Slf4j
public class JobsScraperController {



    @GetMapping("/welcome")
    public String JobsScraperDoneWelcomePage(){

        log.info("Welcome to JobsScraperController");

        return "Welcome to Jobs Scraper !";
    }

}
