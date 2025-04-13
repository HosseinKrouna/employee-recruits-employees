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
import java.util.*; // Importiere java.util.*
import java.util.stream.Collectors; // Importiere Collectors

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // --- Definition der vordefinierten Skills ---
    private static final List<String> PREDEFINED_BACKEND_SKILLS = List.of("Java", "Spring");
    private static final List<String> PREDEFINED_FRONTEND_SKILLS = List.of("Angular", "React", "Vue.js");
    private static final List<String> PREDEFINED_DATABASE_SKILLS = List.of("SQL", "MongoDB");
    private static final List<String> PREDEFINED_BUILD_SKILLS = List.of("Maven", "Gradle");
    private static final List<String> PREDEFINED_CICD_SKILLS = List.of("Jenkins", "Azure", "Bamboo");

    // Map zur Zuordnung von Kategorie zu vordefinierten Skills (LinkedHashMap für Reihenfolge)
    private static final Map<String, List<String>> PREDEFINED_SKILLS_BY_CATEGORY = new LinkedHashMap<>();
    static {
        PREDEFINED_SKILLS_BY_CATEGORY.put("Backend", PREDEFINED_BACKEND_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("Frontend", PREDEFINED_FRONTEND_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("Datenbanken", PREDEFINED_DATABASE_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("Buildsysteme", PREDEFINED_BUILD_SKILLS);
        PREDEFINED_SKILLS_BY_CATEGORY.put("CI/CD-Tools", PREDEFINED_CICD_SKILLS);
    }
    // --- Ende der Skill-Definitionen ---

    @Autowired
    public PdfGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generiert das PDF für eine Empfehlung, inklusive aufbereiteter Skill-Map.
     */
    public byte[] generateRecommendationPdf(RecommendationRequestDTO dto) throws DocumentException, IOException {
        Context context = new Context();

        // 1. Setze einfache Variablen und formatierte Daten
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

        // 2. Erstelle die aufbereitete SkillsMap für Thymeleaf
        Map<String, Map<String, Integer>> finalSkillsMap = createFinalSkillsMap(dto);
        context.setVariable("skillsMap", finalSkillsMap);

        // 3. Verarbeite das Template
        String html = templateEngine.process("recommendation-template", context);

        // 4. Generiere PDF aus HTML
        return generatePdfFromHtml(html);
    }

    /**
     * Erstellt die finale Map für die Skills, die an Thymeleaf übergeben wird.
     * Enthält alle vordefinierten Skills (mit 0%, falls nicht angegeben),
     * die "Anderen" Skills und gruppiert Custom Skills nach Technologie.
     */
    private Map<String, Map<String, Integer>> createFinalSkillsMap(RecommendationRequestDTO dto) {
        Map<String, Map<String, Integer>> finalSkillsMap = new LinkedHashMap<>();

        // --- Schritt 1 & 2: Sammle alle Standard- und "Andere" Skills ---
        Map<String, Integer> allSubmittedStandardSkills = new HashMap<>();
        Map<String, RecommendationRequestDTO.SkillEntry> otherSkillsMap = new HashMap<>();

        processSkillCategory(dto.getBackendSkills(), "Backend", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getFrontendSkills(), "Frontend", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getDatabaseSkills(), "Datenbanken", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getBuildSkills(), "Buildsysteme", allSubmittedStandardSkills, otherSkillsMap);
        processSkillCategory(dto.getCicdSkills(), "CI/CD-Tools", allSubmittedStandardSkills, otherSkillsMap);


        // --- Schritt 3: Füge vordefinierte Kategorien zur finalen Map hinzu ---
        PREDEFINED_SKILLS_BY_CATEGORY.forEach((categoryName, predefinedSkillsInCategory) -> {
            Map<String, Integer> skillsForCategory = new LinkedHashMap<>();
            // Füge alle vordefinierten Skills mit ihrem Wert (oder 0) hinzu
            for (String skillName : predefinedSkillsInCategory) {
                skillsForCategory.put(skillName, allSubmittedStandardSkills.getOrDefault(skillName, 0));
            }
            // Füge den "Anderen" Skill für diese Kategorie hinzu, falls vorhanden
            otherSkillsMap.values().stream()
                    .filter(s -> categoryName.equals(determineCategoryForOtherSkill(s))) // Finde passenden "Anderen"
                    .findFirst()
                    .ifPresent(otherSkill -> {
                        if(otherSkill.getName() != null && !otherSkill.getName().isBlank()) {
                            skillsForCategory.put(otherSkill.getName(), otherSkill.getPercentage());
                        }
                    });

            // Füge Kategorie nur hinzu, wenn sie vordefinierte Skills hat (oder einen "Anderen")
            if (!skillsForCategory.isEmpty()) {
                finalSkillsMap.put(categoryName, skillsForCategory);
            }
        });


        // --- Schritt 4: Füge Custom Skills hinzu (gruppiert nach Technologie) ---
        if (dto.getCustomSkills() != null && !dto.getCustomSkills().isEmpty()) {
            for (RecommendationRequestDTO.SkillEntry customSkill : dto.getCustomSkills()) {
                if (customSkill.getName() == null || customSkill.getName().isBlank()) continue; // Überspringe leere Custom Skills

                // Bestimme die Kategorie: Technologie oder Fallback
                String categoryKey = (customSkill.getTechnology() != null && !customSkill.getTechnology().isBlank())
                        ? customSkill.getTechnology().trim() // Technologie als Kategorie
                        : "Sonstige individuelle Skills"; // Fallback-Kategorie

                // Hole oder erstelle die Map für diese Kategorie
                Map<String, Integer> categoryMap = finalSkillsMap.computeIfAbsent(categoryKey, k -> new LinkedHashMap<>());

                // Füge den Skill hinzu
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
            if (skill == null || skill.getName() == null) continue; // Ungültige Einträge überspringen

            if (isOtherSkillEntry(skill, categoryName)) {
                // Es ist der "Andere"-Skill für diese Kategorie
                if (!skill.getName().isBlank()){ // Nur hinzufügen, wenn ein Name angegeben wurde
                    otherSkillsTargetMap.put(skill.getName(), skill); // Speichere ganzen Eintrag
                }
            } else {
                // Es ist ein Standard-Skill (oder sollte einer sein)
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
        // Einfache Annahme: Finde die Kategorie, wo dieser Skill NICHT vordefiniert ist.
        // Das ist nicht sehr robust. Besser wäre es, wenn die Kategorie im DTO gespeichert würde.
        for (Map.Entry<String, List<String>> categoryEntry : PREDEFINED_SKILLS_BY_CATEGORY.entrySet()) {
            if (isOtherSkillEntry(otherSkill, categoryEntry.getKey())) {
                // Prüfe zusätzlich, ob der Skill vielleicht doch in einer ANDEREN Liste vorkommt
                boolean inAnyOtherList = PREDEFINED_SKILLS_BY_CATEGORY.entrySet().stream()
                        .filter(e -> !e.getKey().equals(categoryEntry.getKey())) // Andere Kategorien
                        .anyMatch(e -> e.getValue().stream().anyMatch(s -> s.equalsIgnoreCase(otherSkill.getName())));
                if (!inAnyOtherList) {
                    return categoryEntry.getKey(); // Wahrscheinlich die richtige Kategorie
                }
            }
        }
        return null; // Konnte Kategorie nicht bestimmen
    }


    /**
     * Generiert das PDF aus dem gegebenen HTML-String.
     */
    private byte[] generatePdfFromHtml(String html) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        // Basis-URL für Ressourcen (CSS, Bilder) finden
        URL baseResource = getClass().getResource("/static/");
        if (baseResource == null) {
            baseResource = PdfGeneratorService.class.getClassLoader().getResource("static/");
            if (baseResource == null) {
                System.err.println("WARNUNG: Basisverzeichnis /static/ fuer PDF-Ressourcen nicht gefunden!");
                renderer.setDocumentFromString(html); // Ohne Basis-URL versuchen
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
        byte[] pdfContent = generateRecommendationPdf(dto); // Ruft die Hauptmethode auf

        Path generatedDir = Paths.get(System.getProperty("user.dir"), "generatedPdf");
        if (Files.notExists(generatedDir)) {
            Files.createDirectories(generatedDir); // Erstellt Verzeichnis, falls nicht vorhanden
        }

        String candidateName = (dto.getCandidateFirstname() != null ? dto.getCandidateFirstname() : "")
                + "_"
                + (dto.getCandidateLastname() != null ? dto.getCandidateLastname() : "");
        // Ersetze ungültige Zeichen im Dateinamen
        candidateName = candidateName.replaceAll("[^a-zA-Z0-9_\\-]", "_").replaceAll("_+", "_"); // Ersetzt auch mehrere _ durch einen
        if (candidateName.equals("_")) candidateName = "Unbekannt"; // Fallback, falls beide Namen fehlen/ungültig sind

        String filename = "Empfehlung_" + candidateName + ".pdf";
        Path pdfPath = generatedDir.resolve(filename);

        Files.write(pdfPath, pdfContent);
        System.out.println("PDF gespeichert unter: " + pdfPath.toAbsolutePath()); // Logge den Pfad
        return filename; // Nur den Dateinamen zurückgeben
    }
}