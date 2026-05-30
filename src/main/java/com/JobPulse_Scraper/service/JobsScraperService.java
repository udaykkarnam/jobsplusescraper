package com.JobPulse_Scraper.service;


import com.JobPulse_Scraper.entity.Job;
import com.JobPulse_Scraper.repository.JobRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobsScraperService {
    private final JobRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void scrapeJobs() {


        log.info("SCRAPING STARTED");

        int savedCount = 0;

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions()
                            .setHeadless(false));

            Page page = browser.newPage();
            for (int pageNumber = 1; pageNumber <= 100; pageNumber++) {

                String url =
                        "https://www.naukri.com/spring-boot-developer-jobs-" + pageNumber;


                System.out.println("OPENING PAGE : " + pageNumber);

                page.navigate(url);


                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.waitForTimeout(2000);

                Locator jobs =
                        page.locator(".srp-jobtuple-wrapper");

                int count = jobs.count();

                log.info("TOTAL JOBS FOUND IN PAGE "
                        + pageNumber + " : " + count);

                for (int i = 0; i < count; i++) {

                    try {

                        Locator job = jobs.nth(i);

                        String title =
                                getTextSafely(job.locator("a.title"));


                        boolean skipJob =
                                title != null
                                        && title.toLowerCase()
                                        .contains("java full stack developer");

                        String company =
                                getTextSafely(job.locator(".comp-name"));



                        String location = "";

                        Locator locationLocator =
                                job.locator(".locWdth");

                        String jobUrl =
                                job.locator("a.title")
                                        .getAttribute("href");
                        if (jobUrl == null || jobUrl.isBlank()) {

                            log.warn("JOB URL NOT FOUND : {}", title);

                            continue;
                        }


                        String experience = "";

                        Locator experienceLocator =
                                job.locator(".expwdth");

                        if (experienceLocator.count() > 0) {

                            experience =
                                    experienceLocator.textContent();

                        } else {


                            System.out.println("URL = " + jobUrl);
                            log.warn("EXPERIENCE NOT FOUND FOR JOB : {}", title);

                            continue;
                        }

                        if (locationLocator.count() > 0) {

                            location =
                                    locationLocator.textContent();

                        } else {

                            System.out.println("TITLE = " + title);
                            System.out.println("COMPANYNAME = " + company);
                            System.out.println("EXPERIENCE = " + experience);
                            System.out.println("LOCATION = NOT FOUND");
                            System.out.println("URL = " + jobUrl);

                            log.warn("LOCATION NOT FOUND FOR JOB : {}", title);

                            continue;
                        }

                        String posted = "";

                        Locator postedLocator =
                                job.locator(".job-post-day");

                        if (postedLocator.count() > 0) {

                            posted = postedLocator.textContent();

                        } else {

                            log.warn("POSTED DATE NOT FOUND : {}", title);

                            continue;
                        }

                        String skills =
                                job.locator(".tags-gt li")
                                        .allTextContents()
                                        .toString();



                        boolean earlyApplicant =
                                job.textContent()
                                        .contains("Early Applicant");


                        if (!experience.contains("-")) {

                            log.warn("INVALID EXPERIENCE FORMAT : {}", experience);

                            continue;
                        }

                        String[] expArray =
                                experience.replace("Yrs", "")
                                        .trim()
                                        .split("-");

                        if (expArray.length < 2) {

                            log.warn("INVALID EXPERIENCE SPLIT : {}", experience);

                            continue;
                        }

                        int minExperience;
                        int maxExperience;

                        try {

                            minExperience =
                                    Integer.parseInt(expArray[0].trim());

                            maxExperience =
                                    Integer.parseInt(expArray[1].trim());

                        } catch (NumberFormatException e) {

                            log.warn("UNABLE TO PARSE EXPERIENCE : {}", experience);

                            continue;
                        }



                        boolean matchesMyExperience =
                                5 >= minExperience
                                        && 5 <= maxExperience;



                        String postedText =
                                posted.toLowerCase();

                        boolean postedWithin2Days =
                                postedText.contains("today")
                                        || postedText.contains("1 day")
                                        || postedText.contains("2 days")
                                        || postedText.contains("few hours ago")
                                        || postedText.contains("walk-in");


                        if (matchesMyExperience
                                && postedWithin2Days
                                && !skipJob) {

                            log.info("------------------------------------------------------------");



                            log.info("""
        TITLE = {}
        COMPANYNAME = {}
        EXPERIENCE = {}
        LOCATION = {}
        POSTED = {}
        URL = {}
        """,
                                    title,
                                    company,
                                    experience,
                                    location,
                                    posted,
                                    jobUrl
                            );

                            Job entity = Job.builder()
                                    .title(title)
                                    .company(company)
                                    .location(location)
                                    .experience(experience)
                                    .skills(skills)
                                    .earlyApplicant(earlyApplicant)
                                    .jobUrl(jobUrl)
                                    .createdAt(LocalDateTime.now())

                                    .build();

                            boolean alreadyExists =
                                    repository.existsByJobUrl(jobUrl);

                            if (!alreadyExists) {

                                repository.save(entity);

                                savedCount++;

                                log.info("JOB SAVED : " + title);

                            } else {

                                log.info("DUPLICATE JOB SKIPPED : " + title);
                                log.info("#######################################################################");

                            }


                        }

                    } catch (Exception e) {

                        System.out.println("ERROR IN JOB");

                        try {
                            System.out.println("FAILED JOB TITLE : "
                                    + jobs.nth(i).locator("a.title").textContent());
                        } catch (Exception ignored) {
                        }

                        log.error(
                                "ERROR IN JOB | PAGE : {} | INDEX : {}",
                                pageNumber,
                                i,
                                e
                        );
                    }
                }
            }

            browser.close();

            System.out.println();
            System.out.println("=================================");
            System.out.println("TOTAL URLS SAVED = " + savedCount);
            System.out.println("=================================");

            log.info("SCRAPING COMPLETED");

        } catch (Exception e) {

            log.error("ERROR OCCURRED DURING SCRAPING", e);
        }
    }


    private String getTextSafely(Locator locator) {

        try {

            if (locator.count() > 0) {

                return locator.textContent().trim();
            }

        } catch (Exception e) {

            log.warn("UNABLE TO FETCH TEXT");
        }

        return "";
    }
}
