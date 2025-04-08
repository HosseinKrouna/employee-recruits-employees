package com.empfehlo.empfehlungsapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateFirstname;
    private String candidateLastname;
    private String position;
    private String status;
    private String documentCvPath;
    private String documentPdfPath;
    private LocalDateTime submittedAt;
    private String cvChoice;
    private String cvLink;

    @JoinColumn(name = "user_id", nullable = false)
    private User recommendedBy;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCandidateFirstname() {
        return candidateFirstname;
    }

    public void setCandidateFirstname(String candidateFirstname) {
        this.candidateFirstname = candidateFirstname;
    }

    public String getCandidateLastname() {
        return candidateLastname;
    }

    public void setCandidateLastname(String candidateLastname) {
        this.candidateLastname = candidateLastname;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentCvPath() {
        return documentCvPath;
    }

    public void setDocumentCvPath(String documentCvPath) {
        this.documentCvPath = documentCvPath;
    }

    public String getDocumentPdfPath() {
        return documentPdfPath;
    }

    public void setDocumentPdfPath(String documentPdfPath) {
        this.documentPdfPath = documentPdfPath;
    }

    public String getCvChoice() { return cvChoice; }
    public void setCvChoice(String cvChoice) { this.cvChoice = cvChoice; }

    public String getCvLink() { return cvLink; }
    public void setCvLink(String cvLink) { this.cvLink = cvLink; }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public User getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(User recommendedBy) {
        this.recommendedBy = recommendedBy;
    }
}

