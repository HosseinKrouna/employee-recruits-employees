package com.empfehlo.empfehlungsapp.controllers;

import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
import com.empfehlo.empfehlungsapp.services.PdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class PdfPreviewController {

    private final PdfGeneratorService pdfGeneratorService;

    @Autowired
    public PdfPreviewController(PdfGeneratorService pdfGeneratorService) {
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @GetMapping(value = "/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewPdfTemplate() {
        RecommendationRequestDTO testDto = new RecommendationRequestDTO();
        testDto.setCandidateFirstname("Max");
        testDto.setCandidateLastname("Mustermann");
        testDto.setPosition("Softwareentwickler");
        testDto.setEmploymentStatus("In Anstellung");
        testDto.setCurrentPosition("Fullstack Developer");
        testDto.setCurrentCareerLevel("Senior-Level");
        testDto.setContactDate(LocalDate.now().minusWeeks(1));
        testDto.setConvincedCandidateDate(LocalDate.now());
        testDto.setNoticePeriod(LocalDate.now().plusMonths(3));
        testDto.setStartDate(LocalDate.now().plusMonths(4));

        testDto.setInformedPosition(true);
        testDto.setInformedTasks(true);
        testDto.setInformedRequirements(false);
        testDto.setInformedClientsProjects(true);
        testDto.setInformedBenefits(false);
        testDto.setInformedTraining(true);
        testDto.setInformedCoach(false);
        testDto.setInformedRoles(true);

        testDto.setHobbies("Fotografie, Wandern, Kochen");
        testDto.setProjectExperience("Leitung eines Angular/Spring-Projekts im Finanzbereich.\nMigration von Legacy-System auf moderne Microservices.");
        testDto.setMiscellaneous("Sehr zuverlässiger Teamplayer mit hoher Eigenmotivation.");

        List<RecommendationRequestDTO.SkillEntry> otherSkills = new ArrayList<>();
        otherSkills.add(new RecommendationRequestDTO.SkillEntry("Data Engineering", 90, "Apache Spark"));
        otherSkills.add(new RecommendationRequestDTO.SkillEntry("Projektmanagement", 85, "Scrum"));
        otherSkills.add(new RecommendationRequestDTO.SkillEntry("Cloud Computing", 80, "AWS"));

        testDto.setOtherSkills(otherSkills);


        Context context = new Context();
        Map<String, Object> variables = pdfGeneratorService.createVariablesFromDto(testDto);
        variables.forEach(context::setVariable);

        String html = pdfGeneratorService.getTemplateEngine().process("recommendation-template", context);
        return ResponseEntity.ok(html);
    }
}

