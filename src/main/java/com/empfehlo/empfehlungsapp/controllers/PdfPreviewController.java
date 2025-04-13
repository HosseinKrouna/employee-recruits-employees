//package com.empfehlo.empfehlungsapp.controllers;
//
//import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
//import com.empfehlo.empfehlungsapp.services.PdfGeneratorService; // Service wird noch für TemplateEngine gebraucht
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.thymeleaf.context.Context;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*; // Importiere java.util.*
//import java.util.stream.Collectors; // Importiere Collectors
//
//@RestController
//public class PdfPreviewController {
//
//    // Behalte den Service, um Zugriff auf die TemplateEngine zu haben
//    private final PdfGeneratorService pdfGeneratorService;
//    // DateFormatter hier definieren oder aus Service holen, falls er public wäre
//    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//
//    // --- Kopierte Skill-Definitionen aus PdfGeneratorService ---
//    //      (Oder lagere sie in eine gemeinsame Klasse aus!)
//    private static final List<String> PREDEFINED_BACKEND_SKILLS = List.of("Java", "Spring");
//    private static final List<String> PREDEFINED_FRONTEND_SKILLS = List.of("Angular", "React", "Vue.js");
//    private static final List<String> PREDEFINED_DATABASE_SKILLS = List.of("SQL", "MongoDB");
//    private static final List<String> PREDEFINED_BUILD_SKILLS = List.of("Maven", "Gradle");
//    private static final List<String> PREDEFINED_CICD_SKILLS = List.of("Jenkins", "Azure", "Bamboo");
//
//    private static final Map<String, List<String>> PREDEFINED_SKILLS_BY_CATEGORY = new LinkedHashMap<>();
//    static {
//        PREDEFINED_SKILLS_BY_CATEGORY.put("Backend", PREDEFINED_BACKEND_SKILLS);
//        PREDEFINED_SKILLS_BY_CATEGORY.put("Frontend", PREDEFINED_FRONTEND_SKILLS);
//        PREDEFINED_SKILLS_BY_CATEGORY.put("Datenbanken", PREDEFINED_DATABASE_SKILLS);
//        PREDEFINED_SKILLS_BY_CATEGORY.put("Buildsysteme", PREDEFINED_BUILD_SKILLS);
//        PREDEFINED_SKILLS_BY_CATEGORY.put("CI/CD-Tools", PREDEFINED_CICD_SKILLS);
//    }
//    // --- Ende Skill-Definitionen ---
//
//
//    @Autowired
//    public PdfPreviewController(PdfGeneratorService pdfGeneratorService) {
//        this.pdfGeneratorService = pdfGeneratorService;
//    }
//
//    @GetMapping(value = "/preview", produces = MediaType.TEXT_HTML_VALUE)
//    public ResponseEntity<String> previewPdfTemplate() {
//        // 1. Test-DTO erstellen (Dein bisheriger Code)
//        RecommendationRequestDTO testDto = createTestDto(); // In eigene Methode ausgelagert
//
//        // 2. Thymeleaf-Kontext erstellen
//        Context context = new Context();
//
//        // --- Logik aus PdfGeneratorService hierher kopieren ---
//
//        // Einfache Variablen setzen
//        context.setVariable("recommendation", testDto);
//
//        // Datumsformatierung
//        context.setVariable("contactDateFormatted", testDto.getContactDate() != null ? testDto.getContactDate().format(dateFormatter) : "");
//        context.setVariable("convincedDateFormatted", testDto.getConvincedCandidateDate() != null ? testDto.getConvincedCandidateDate().format(dateFormatter) : "");
//        context.setVariable("noticePeriodFormatted", testDto.getNoticePeriod() != null ? testDto.getNoticePeriod().format(dateFormatter) : "");
//        context.setVariable("startDateFormatted", testDto.getStartDate() != null ? testDto.getStartDate().format(dateFormatter) : "");
//
//        // "Informed" Map
//        Map<String, Boolean> informed = new HashMap<>();
//        informed.put("position", testDto.isInformedPosition());
//        informed.put("tasks", testDto.isInformedTasks());
//        informed.put("requirements", testDto.isInformedRequirements());
//        informed.put("clientsProjects", testDto.isInformedClientsProjects());
//        informed.put("benefits", testDto.isInformedBenefits());
//        informed.put("training", testDto.isInformedTraining());
//        informed.put("coach", testDto.isInformedCoach());
//        informed.put("roles", testDto.isInformedRoles());
//        context.setVariable("informed", informed);
//
//        // Vollständige SkillsMap erstellen
//        Map<String, Map<String, Integer>> finalSkillsMap = new LinkedHashMap<>();
//        PREDEFINED_SKILLS_BY_CATEGORY.forEach((categoryName, predefinedSkillsInCategory) -> {
//            Map<String, Integer> categoryMapForThymeleaf = new LinkedHashMap<>();
//            Map<String, Integer> submittedSkillsInCategory = getSubmittedSkillsForCategory(testDto, categoryName);
//
//            for (String skillName : predefinedSkillsInCategory) {
//                categoryMapForThymeleaf.put(skillName, submittedSkillsInCategory.getOrDefault(skillName, 0));
//            }
//            Optional<RecommendationRequestDTO.SkillEntry> otherSkill = findOtherSkillEntry(testDto, categoryName);
//            otherSkill.ifPresent(entry -> {
//                if(entry.getName() != null && !entry.getName().isBlank()) {
//                    categoryMapForThymeleaf.put(entry.getName(), entry.getPercentage());
//                }
//            });
//            if (!categoryMapForThymeleaf.isEmpty()) {
//                finalSkillsMap.put(categoryName, categoryMapForThymeleaf);
//            }
//        });
//        if (testDto.getCustomSkills() != null && !testDto.getCustomSkills().isEmpty()) {
//            Map<String, Integer> customSkillsMap = new LinkedHashMap<>();
//            for (RecommendationRequestDTO.SkillEntry entry : testDto.getCustomSkills()) {
//                String skillLabel = (entry.getTechnology() != null && !entry.getTechnology().isBlank())
//                        ? entry.getTechnology() + ": " + entry.getName()
//                        : entry.getName();
//                customSkillsMap.put(skillLabel, entry.getPercentage());
//            }
//            if (!customSkillsMap.isEmpty()) {
//                finalSkillsMap.put("Weitere individuelle Skills", customSkillsMap);
//            }
//        }
//        context.setVariable("skillsMap", finalSkillsMap);
//
//        // --- Ende der kopierten Logik ---
//
//        // 3. HTML mit der TemplateEngine aus dem Service generieren
//        try {
//            String html = pdfGeneratorService.getTemplateEngine().process("recommendation-template", context);
//            return ResponseEntity.ok(html);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("<html><body><h1>Fehler bei der HTML-Generierung</h1><pre>" + e.getMessage() + "</pre></body></html>");
//        }
//    }
//
//    // --- Kopierte Hilfsmethoden aus PdfGeneratorService ---
//    //      (Oder lagere sie in eine gemeinsame Utility-Klasse aus!)
//    private Map<String, Integer> getSubmittedSkillsForCategory(RecommendationRequestDTO dto, String categoryName) {
//        List<RecommendationRequestDTO.SkillEntry> submittedList = getSkillListByCategory(dto, categoryName);
//        if (submittedList == null) return Collections.emptyMap();
//        return submittedList.stream()
//                .filter(entry -> !isOtherSkillEntry(entry, categoryName))
//                .collect(Collectors.toMap(
//                        RecommendationRequestDTO.SkillEntry::getName,
//                        RecommendationRequestDTO.SkillEntry::getPercentage,
//                        (existing, replacement) -> existing,
//                        LinkedHashMap::new
//                ));
//    }
//
//    private Optional<RecommendationRequestDTO.SkillEntry> findOtherSkillEntry(RecommendationRequestDTO dto, String categoryName) {
//        List<RecommendationRequestDTO.SkillEntry> submittedList = getSkillListByCategory(dto, categoryName);
//        if (submittedList == null) return Optional.empty();
//        return submittedList.stream()
//                .filter(entry -> isOtherSkillEntry(entry, categoryName))
//                .findFirst();
//    }
//
//    private boolean isOtherSkillEntry(RecommendationRequestDTO.SkillEntry entry, String categoryName) {
//        // Passe diese Logik an!
//        if (entry == null || entry.getName() == null) return false;
//        List<String> predefined = PREDEFINED_SKILLS_BY_CATEGORY.getOrDefault(categoryName, Collections.emptyList());
//        return !predefined.stream().anyMatch(predef -> predef.equalsIgnoreCase(entry.getName()));
//    }
//
//    private List<RecommendationRequestDTO.SkillEntry> getSkillListByCategory(RecommendationRequestDTO dto, String categoryName) {
//        if (dto == null) return null;
//        switch (categoryName) {
//            case "Backend": return dto.getBackendSkills();
//            case "Frontend": return dto.getFrontendSkills();
//            case "Datenbanken": return dto.getDatabaseSkills();
//            case "Buildsysteme": return dto.getBuildSkills();
//            case "CI/CD-Tools": return dto.getCicdSkills();
//            default: return null;
//        }
//    }
//    // --- Ende kopierte Hilfsmethoden ---
//
//
//    // --- Methode zum Erstellen des Test-DTOs ---
//    private RecommendationRequestDTO createTestDto() {
//        RecommendationRequestDTO testDto = new RecommendationRequestDTO();
//        testDto.setCandidateFirstname("Max");
//        testDto.setCandidateLastname("Mustermann");
//        testDto.setPosition("Softwareentwickler");
//        testDto.setEmploymentStatus("In Anstellung");
//        testDto.setCurrentPosition("Fullstack Developer");
//        testDto.setCurrentCareerLevel("Senior-Level");
//        testDto.setContactDate(LocalDate.now().minusWeeks(1));
//        testDto.setConvincedCandidateDate(LocalDate.now());
//        testDto.setNoticePeriod(LocalDate.now().plusMonths(3));
//        testDto.setStartDate(LocalDate.now().plusMonths(4));
//        testDto.setEmail("max.mustermann@example.com");
//        testDto.setPhone("0123-456789");
//        testDto.setKnownFrom("Altes Projekt");
//        testDto.setExperienceYears(7.5);
//        testDto.setSalaryExpectation(80000);
//        testDto.setWorkHours("Vollzeit (40h)");
//        testDto.setTravelWillingness(15);
//        testDto.setCvChoice("CV hochladen"); // Beispiel
//
//        // Kontakt ist informiert zu:
//        testDto.setInformedPosition(true);
//        testDto.setInformedTasks(true);
//        testDto.setInformedRequirements(false);
//        testDto.setInformedClientsProjects(true);
//        testDto.setInformedBenefits(false);
//        testDto.setInformedTraining(true);
//        testDto.setInformedCoach(false);
//        testDto.setInformedRoles(true);
//
//        // Hobbys, Projekte, Sonstiges
//        testDto.setHobbies("Fotografie, Wandern, Kochen");
//        testDto.setProjectExperience("Leitung eines Angular/Spring-Projekts im Finanzbereich.\nMigration von Legacy-System auf moderne Microservices.");
//        testDto.setMiscellaneous("Sehr zuverlässiger Teamplayer mit hoher Eigenmotivation.");
//        testDto.setPersonalityType("Dominant (D), Gewissenhaft (G)");
//
//        // --- Skills befüllen (wichtig für die Skill-Map-Logik) ---
//        List<RecommendationRequestDTO.SkillEntry> backendSkills = new ArrayList<>();
//        backendSkills.add(new RecommendationRequestDTO.SkillEntry("Java", 90));
//        backendSkills.add(new RecommendationRequestDTO.SkillEntry("Spring", 85));
//        // Beispiel für "Anderen" Skill:
//        backendSkills.add(new RecommendationRequestDTO.SkillEntry("Kotlin", 60)); // Wird als "Anderer" erkannt
//        testDto.setBackendSkills(backendSkills);
//
//        List<RecommendationRequestDTO.SkillEntry> frontendSkills = new ArrayList<>();
//        frontendSkills.add(new RecommendationRequestDTO.SkillEntry("Angular", 75));
//        // React wurde nicht ausgewählt, wird aber mit 0% angezeigt
//        testDto.setFrontendSkills(frontendSkills);
//
//        // Datenbank (SQL nicht ausgewählt)
//        List<RecommendationRequestDTO.SkillEntry> dbSkills = new ArrayList<>();
//        dbSkills.add(new RecommendationRequestDTO.SkillEntry("MongoDB", 70));
//        testDto.setDatabaseSkills(dbSkills);
//
//        // Build (Maven ausgewählt, Gradle nicht)
//        List<RecommendationRequestDTO.SkillEntry> buildSkills = new ArrayList<>();
//        buildSkills.add(new RecommendationRequestDTO.SkillEntry("Maven", 95));
//        testDto.setBuildSkills(buildSkills);
//
//        // CI/CD (Nur Jenkins)
//        List<RecommendationRequestDTO.SkillEntry> cicdSkills = new ArrayList<>();
//        cicdSkills.add(new RecommendationRequestDTO.SkillEntry("Jenkins", 80));
//        testDto.setCicdSkills(cicdSkills);
//
//        // Custom Skills
//        List<RecommendationRequestDTO.SkillEntry> customSkills = new ArrayList<>();
//        customSkills.add(new RecommendationRequestDTO.SkillEntry("Projektmanagement", 85, "Agile Methoden"));
//        customSkills.add(new RecommendationRequestDTO.SkillEntry("Cloud", 70, "AWS"));
//        testDto.setCustomSkills(customSkills); // Setter verwenden (umbenannt von setOtherSkills)
//
//        return testDto;
//    }
//}