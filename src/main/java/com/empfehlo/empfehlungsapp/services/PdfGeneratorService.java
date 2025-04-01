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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
        Map<String, Object> variables = createVariablesFromDto(dto);
        variables.forEach(context::setVariable);

        String html = templateEngine.process("recommendation-template", context);
        return generatePdfFromHtml(html);
    }

    private byte[] generatePdfFromHtml(String html) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);
        return outputStream.toByteArray();
    }

    private Map<String, Object> createVariablesFromDto(RecommendationRequestDTO dto) {
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

    /**
     * Erzeugt das PDF, speichert es im Ordner "generatedPdf" und gibt den Dateinamen zurück.
     */
    public String generateAndStorePdf(RecommendationRequestDTO dto) throws DocumentException, IOException {
        byte[] pdfContent = generateRecommendationPdf(dto);

        Path generatedDir = Paths.get(System.getProperty("user.dir"), "generatedPdf");
        if (Files.notExists(generatedDir)) {
            Files.createDirectories(generatedDir);
        }

        String candidateName = dto.getCandidateFirstname() + "_" + dto.getCandidateLastname();
        String filename = "Empfehlung_" + candidateName + ".pdf";
        Path pdfPath = generatedDir.resolve(filename);

        Files.write(pdfPath, pdfContent);
        return filename;
    }
}
