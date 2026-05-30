package com.JobPulse_Scraper.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String company;

    private String location;

    private String experience;

    @Column(length = 5000)
    private String skills;

    private boolean earlyApplicant;

    @Column(length = 1000,unique = true)
    private String jobUrl;



    private LocalDateTime createdAt;




}
