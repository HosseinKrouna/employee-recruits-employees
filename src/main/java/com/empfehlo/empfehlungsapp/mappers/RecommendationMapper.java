package com.empfehlo.empfehlungsapp.mappers;

import com.empfehlo.empfehlungsapp.dtos.RecommendationRequestDTO;
import com.empfehlo.empfehlungsapp.dtos.RecommendationResponseDTO;
import com.empfehlo.empfehlungsapp.models.Recommendation;
import com.empfehlo.empfehlungsapp.models.User;

import java.time.LocalDateTime;

public class RecommendationMapper {

    public static RecommendationResponseDTO toDTO(Recommendation entity) {
        RecommendationResponseDTO dto = new RecommendationResponseDTO();
        dto.setId(entity.getId());
        dto.setCandidateFirstname(entity.getCandidateFirstname());
        dto.setCandidateLastname(entity.getCandidateLastname());
        dto.setPosition(entity.getPosition());
        dto.setStatus(entity.getStatus());
        dto.setDocumentCvPath(entity.getDocumentCvPath());
        dto.setBusinessLink(entity.getBusinessLink());
        dto.setDocumentPdfPath(entity.getDocumentPdfPath());
        dto.setSubmittedAt(entity.getSubmittedAt() != null
                ? entity.getSubmittedAt().toString()
                : null);

        if (entity.getRecommendedBy() != null) {
            User user = entity.getRecommendedBy();
            dto.setUserId(user.getId());
            dto.setRecommendedByUsername(user.getUsername());
        }

        return dto;
    }

    public static Recommendation toEntity(RecommendationRequestDTO dto, User user) {
        Recommendation entity = new Recommendation();
        entity.setCandidateFirstname(dto.getCandidateFirstname());
        entity.setCandidateLastname(dto.getCandidateLastname());
        entity.setPosition(dto.getPosition());
        entity.setDocumentCvPath(dto.getDocumentCvPath());
        entity.setBusinessLink(dto.getBusinessLink());
        entity.setDocumentPdfPath(dto.getDocumentPdfPath());
        entity.setStatus("Eingereicht"); // Defaultstatus
        entity.setSubmittedAt(LocalDateTime.now()); // Zeitpunkt setzen
        entity.setRecommendedBy(user);
        return entity;
    }

}
