package com.empfehlo.empfehlungsapp.services;

import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final List<String> PREDEFINED_BACKEND_SKILLS = List.of("Java", "Spring");
    private static final List<String> PREDEFINED_FRONTEND_SKILLS = List.of("Angular", "React", "Vue.js");
    private static final List<String> PREDEFINED_DATABASE_SKILLS = List.of("SQL", "MongoDB");
    private static final List<String> PREDEFINED_BUILD_SKILLS = List.of("Maven", "Gradle");
    private static final List<String> PREDEFINED_CICD_SKILLS = List.of("Jenkins", "Azure", "Bamboo");

    private static final Map<String, List<String>> PREDEFINED_SKILLS_BY_CATEGORY = new LinkedHashMap<>();
    static {
        PREDEFINED_SKILLS_BY_CATEGORY.put("Backend", PREDEFINED_BACKEND_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("Frontend", PREDEFINED_FRONTEND_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("Datenbanken", PREDEFINED_DATABASE_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("Buildsysteme", PREDEFINED_BUILD_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("CI/CD-Tools", PREDEFINED_CICD_SKILLS);
    }

    @Autowired
    public PdfGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generiert das PDF für eine Empfehlung, inklusive aufbereiteter Skill-Map.
     */
    public byte[] generateRecommendationPdf(RecommendationRequestDTO dto) throws DocumentException, IOException {
        Context context = new Context();

        context.setVariable("recommendation", dto);
        context.setVariable("contactDateFormatted", dto.getContactDate() != null ? dto.getContactDate().format(dateFormatter) : "");
        context.setVariable("convincedDateFormatted", dto.getConvincedCandidateDate() != null ? dto.getConvincedCandidateDate().format(dateFormatter) : "");
        context.setVariable("noticePeriodFormatted", dto.getNoticePeriod() != null ? dto.getNoticePeriod().format(dateFormatter) : "");
        context.setVariable("startDateFormatted", dto.getStartDate() != null ? dto.getStartDate().format(dateFormatter) : "");

        Map<String, Boolean> informed = new HashMap<>();
        informed.put("position", dto.isInformedPosition());
        informed.put("tasks", dto.isInformedTasks());
        informed.put("requirements", dto.isInformedRequirements());
        informed.put("clientsProjects", dto.isInformedClientsProjects());
        informed.put("benefits", dto.isInformedBenefits());
        informed.put("training", dto.isInformedTraining());
        informed.put("coach", dto.isInformedCoach());
        informed.put("roles", dto.isInformedRoles());
        context.setVariable("informed", informed);

        Map<String, Map<String, Integer>> finalSkillsMap = createFinalSkillsMap(dto);
        context.setVariable("skillsMap", finalSkillsMap);

        String html = templateEngine.process("recommendation-template", context);

        return generatePdfFromHtml(html);
    }

    /**
     * Erstellt die finale Map für die Skills, die an Thymeleaf übergeben wird.
     * Enthält alle vordefinierten Skills (mit 0%, falls nicht angegeben),
     * die "Anderen" Skills und gruppiert Custom Skills nach Technologie.
     */
    private Map<String, Map<String, Integer>> createFinalSkillsMap(RecommendationRequestDTO dto) {
        Map<String, Map<String, Integer>> finalSkillsMap = new LinkedHashMap<>();

        Map<String, Integer> allSubmittedStandardSkills = new HashMap<>();
        Map<String, RecommendationRequestDTO.SkillEntry> otherSkillsMap = new HashMap<>();

        processSkillCategory(dto.getBackendSkills(), "Backend", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getFrontendSkills(), "Frontend", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getDatabaseSkills(), "Datenbanken", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getBuildSkills(), "Buildsysteme", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getCicdSkills(), "CI/CD-Tools", allSubmittedStandardSkills, otherSkillsMap);


        PREDEFINED_SKILLS_BY_CATEGORY.forEach((categoryName, predefinedSkillsInCategory) -> {
            Map<String, Integer> skillsForCategory = new LinkedHashMap<>();
            for (String skillName : predefinedSkillsInCategory) {
                skillsForCategory.put(skillName, allSubmittedStandardSkills.getOrDefault(skillName, 0));
            }
            otherSkillsMap.values().stream()
                    .findFirst()
                    .ifPresent(otherSkill -> {
                        if(otherSkill.getName() != null && !otherSkill.getName().isBlank()) {
                            skillsForCategory.put(otherSkill.getName(), otherSkill.getPercentage());
                        }
                    });

            if (!skillsForCategory.isEmpty()) {
                finalSkillsMap.put(categoryName, skillsForCategory);
            }
        });


        if (dto.getCustomSkills() != null && !dto.getCustomSkills().isEmpty()) {
            for (RecommendationRequestDTO.SkillEntry customSkill : dto.getCustomSkills()) {

                String categoryKey = (customSkill.getTechnology() != null && !customSkill.getTechnology().isBlank())

                Map<String, Integer> categoryMap = finalSkillsMap.computeIfAbsent(categoryKey, k -> new LinkedHashMap<>());

                categoryMap.put(customSkill.getName(), customSkill.getPercentage());
            }
        }

        return finalSkillsMap;
    }

    /**
     * Verarbeitet eine Liste von Skill-Einträgen für eine bestimmte Kategorie,
     * trennt Standard-Skills von "Anderen" Skills.
     */
    private void processSkillCategory(List<RecommendationRequestDTO.SkillEntry> skills,
                                      String categoryName,
                                      Map<String, Integer> standardSkillsTargetMap,
                                      Map<String, RecommendationRequestDTO.SkillEntry> otherSkillsTargetMap) {
        if (skills == null) return;

        for (RecommendationRequestDTO.SkillEntry skill : skills) {

            if (isOtherSkillEntry(skill, categoryName)) {
                }
            } else {
                standardSkillsTargetMap.put(skill.getName(), skill.getPercentage());
            }
        }
    }

    /**
     * Hilfsmethode, um zu prüfen, ob ein SkillEntry der "Andere"-Eintrag für eine Kategorie ist.
     * Aktuelle Annahme: Ein Skill ist "Andere", wenn sein Name NICHT in der vordefinierten Liste ist.
     * Passe diese Logik bei Bedarf an!
     */
    private boolean isOtherSkillEntry(RecommendationRequestDTO.SkillEntry entry, String categoryName) {
        if (entry == null || entry.getName() == null) return false;
        List<String> predefined = PREDEFINED_SKILLS_BY_CATEGORY.getOrDefault(categoryName, Collections.emptyList());
        return !predefined.stream().anyMatch(predef -> predef.equalsIgnoreCase(entry.getName()));
    }

    /**
     * Versucht, die ursprüngliche vordefinierte Kategorie für einen "Anderen"-Skill zu bestimmen.
     * Diese Methode ist nötig, um "Andere"-Skills korrekt zuzuordnen.
     * Sie ist hier nur eine einfache Annahme - sie muss evtl. verbessert werden,
     * z.B. indem der "Technology"-String im DTO die Kategorie enthält.
     */
    private String determineCategoryForOtherSkill(RecommendationRequestDTO.SkillEntry otherSkill) {
        for (Map.Entry<String, List<String>> categoryEntry : PREDEFINED_SKILLS_BY_CATEGORY.entrySet()) {
            if (isOtherSkillEntry(otherSkill, categoryEntry.getKey())) {
                boolean inAnyOtherList = PREDEFINED_SKILLS_BY_CATEGORY.entrySet().stream()
                        .anyMatch(e -> e.getValue().stream().anyMatch(s -> s.equalsIgnoreCase(otherSkill.getName())));
                if (!inAnyOtherList) {
                }
            }
        }
    }


    /**
     * Generiert das PDF aus dem gegebenen HTML-String.
     */
    private byte[] generatePdfFromHtml(String html) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        URL baseResource = getClass().getResource("/static/");
        if (baseResource == null) {
            baseResource = PdfGeneratorService.class.getClassLoader().getResource("static/");
            if (baseResource == null) {
                System.err.println("WARNUNG: Basisverzeichnis /static/ fuer PDF-Ressourcen nicht gefunden!");
            } else {
                renderer.setDocumentFromString(html, baseResource.toExternalForm());
            }
        } else {
            renderer.setDocumentFromString(html, baseResource.toExternalForm());
        }
        renderer.layout();
        renderer.createPDF(outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Gibt die Thymeleaf TemplateEngine zurück (nützlich für den PreviewController).
     */
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
     * Generiert das PDF und speichert es im Dateisystem.
     * @param dto Die Empfehlungsdaten.
     * @return Den Dateinamen (nicht den Pfad) der gespeicherten Datei.
     * @throws DocumentException Bei Fehlern während der PDF-Erstellung.
     * @throws IOException Bei Fehlern beim Speichern der Datei.
     */
    public String generateAndStorePdf(RecommendationRequestDTO dto) throws DocumentException, IOException {

        Path generatedDir = Paths.get(System.getProperty("user.dir"), "generatedPdf");
        if (Files.notExists(generatedDir)) {
        }

        String candidateName = (dto.getCandidateFirstname() != null ? dto.getCandidateFirstname() : "")
                + "_"
                + (dto.getCandidateLastname() != null ? dto.getCandidateLastname() : "");

        String filename = "Empfehlung_" + candidateName + ".pdf";
        Path pdfPath = generatedDir.resolve(filename);

        Files.write(pdfPath, pdfContent);
    }
}