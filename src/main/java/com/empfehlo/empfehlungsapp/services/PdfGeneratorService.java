package com.empfehlo.empfehlungsapp.services;

import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    public PdfGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateRecommendationPdf(RecommendationRequestDTO dto) throws DocumentException, IOException {
        Context context = new Context();
        context.setVariable("recommendation", dto);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        context.setVariable("contactDateFormatted",
                dto.getContactDate() != null ? dto.getContactDate().format(dateFormatter) : "");
        context.setVariable("convincedDateFormatted",
                dto.getConvincedCandidateDate() != null ? dto.getConvincedCandidateDate().format(dateFormatter) : "");
        context.setVariable("noticePeriodFormatted",
                dto.getNoticePeriod() != null ? dto.getNoticePeriod().format(dateFormatter) : "");
        context.setVariable("startDateFormatted",
                dto.getStartDate() != null ? dto.getStartDate().format(dateFormatter) : "");


        Map<String, Boolean> informed = new HashMap<>();
        informed.put("position", dto.isInformedPosition());
        System.out.println("position"+ dto.isInformedPosition());
        informed.put("tasks", dto.isInformedTasks());
        informed.put("requirements", dto.isInformedRequirements());
        informed.put("clientsProjects", dto.isInformedClientsProjects());
        informed.put("benefits", dto.isInformedBenefits());
        informed.put("training", dto.isInformedTraining());
        informed.put("coach", dto.isInformedCoach());
        informed.put("roles", dto.isInformedRoles());
        context.setVariable("informed", informed);


        // Neue Map für alle Skill-Kategorien
        Map<String, Map<String, Integer>> skillsMap = new LinkedHashMap<>();

        skillsMap.put("Backend", createSkillMap(dto.getBackendSkills()));
        skillsMap.put("Frontend", createSkillMap(dto.getFrontendSkills()));
        skillsMap.put("Database", createSkillMap(dto.getDatabaseSkills()));
        skillsMap.put("Build", createSkillMap(dto.getBuildSkills()));
        skillsMap.put("CI/CD", createSkillMap(dto.getCicdSkills()));
        if (dto.getCustomSkills() != null) {
            for (RecommendationRequestDTO.SkillEntry skill : dto.getCustomSkills()) {
                String category = skill.getTechnology() != null && !skill.getTechnology().isBlank()
                        ? skill.getTechnology()
                        : "Sonstige";

                skillsMap.computeIfAbsent(category, k -> new LinkedHashMap<>())
                        .put(skill.getName(), skill.getPercentage());
            }
        }


        System.out.println("Custom-Skills:");
        dto.getCustomSkills().forEach(skill ->
                System.out.println(skill.getName() + " - " + skill.getPercentage()));


// Diese eine Map ins Template geben
        context.setVariable("skillsMap", skillsMap);


        String html = templateEngine.process("recommendation-template", context);

        // TEST-Ausgabe des HTMLs:
        System.out.println("Generiertes HTML:\n" + html);

        return generatePdfFromHtml(html);


    }

    private Map<String, Integer> createSkillMap(List<RecommendationRequestDTO.SkillEntry> skills) {
        Map<String, Integer> skillMap = new LinkedHashMap<>();
        if (skills != null) {
            for (RecommendationRequestDTO.SkillEntry skill : skills) {
                String label = skill.getTechnology() != null && !skill.getTechnology().isBlank()
                        ? skill.getTechnology() + " – " + skill.getName()
                        : skill.getName();

                skillMap.put(label, skill.getPercentage());


            }
        }
        return skillMap;
    }


    private byte[] generatePdfFromHtml(String html) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        URL baseResource = getClass().getResource("/static/");
        if (baseResource == null) {
            throw new RuntimeException("Kein Basisverzeichnis gefunden!");
        }
        String baseUrl = baseResource.toExternalForm();
        renderer.setDocumentFromString(html, baseUrl);
        renderer.layout();
        renderer.createPDF(outputStream);
        return outputStream.toByteArray();
    }

    public Map<String, Object> createVariablesFromDto(RecommendationRequestDTO dto) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recommendation", dto);

        if (dto.getContactDate() != null) {
            variables.put("contactDateFormatted", dto.getContactDate().format(dateFormatter));
        }
        if (dto.getConvincedCandidateDate() != null) {
            variables.put("convincedDateFormatted", dto.getConvincedCandidateDate().format(dateFormatter));
        }
        if (dto.getNoticePeriod() != null) {
            variables.put("noticePeriodFormatted", dto.getNoticePeriod().format(dateFormatter));
        }
        if (dto.getStartDate() != null) {
            variables.put("startDateFormatted", dto.getStartDate().format(dateFormatter));
        }

        Map<String, Boolean> informed = new HashMap<>();
        informed.put("position", dto.isInformedPosition());
        informed.put("tasks", dto.isInformedTasks());
        informed.put("requirements", dto.isInformedRequirements());
        informed.put("clientsProjects", dto.isInformedClientsProjects());
        informed.put("benefits", dto.isInformedBenefits());
        informed.put("training", dto.isInformedTraining());
        informed.put("coach", dto.isInformedCoach());
        informed.put("roles", dto.isInformedRoles());
        variables.put("informed", informed);

        return variables;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
     * Erzeugt das PDF, speichert es im Ordner "generatedPdf" und gibt den Dateinamen zurück.
     */
    public String generateAndStorePdf(RecommendationRequestDTO dto) throws DocumentException, IOException {
        byte[] pdfContent = generateRecommendationPdf(dto);

        // Definiere den Speicherort (Ordner "generatedPdf" im aktuellen Verzeichnis)
        Path generatedDir = Paths.get(System.getProperty("user.dir"), "generatedPdf");
        if (Files.notExists(generatedDir)) {
            Files.createDirectories(generatedDir);
        }

        // Erzeuge einen Dateinamen, z. B. "Empfehlung_Vorname_Nachname.pdf"
        String candidateName = dto.getCandidateFirstname() + "_" + dto.getCandidateLastname();
        String filename = "Empfehlung_" + candidateName + ".pdf";
        Path pdfPath = generatedDir.resolve(filename);

        Files.write(pdfPath, pdfContent);
        return filename;
    }

}
