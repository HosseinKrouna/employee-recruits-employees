package com.empfehlo.empfehlungsapp.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateFirstname;
    private String candidateLastname;
    private String position;
    private String status;

    @ManyToOne
    private Employee recommendedBy;

    private String documentCvPath;
    private String documentCoverLetterPath;

    private LocalDateTime submittedAt;

    public Recommendation() {
    }

    public Recommendation(Long id, String candidateFirstname, String candidateLastname, String position, String status, Employee recommendedBy, String documentCvPath, String documentCoverLetterPath, LocalDateTime submittedAt) {
        this.id = id;
        this.candidateFirstname = candidateFirstname;
        this.candidateLastname = candidateLastname;
        this.position = position;
        this.status = status;
        this.recommendedBy = recommendedBy;
        this.documentCvPath = documentCvPath;
        this.documentCoverLetterPath = documentCoverLetterPath;
        this.submittedAt = submittedAt;
    }

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

    public Employee getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(Employee recommendedBy) {
        this.recommendedBy = recommendedBy;
    }

    public String getDocumentCvPath() {
        return documentCvPath;
    }

    public void setDocumentCvPath(String documentCvPath) {
        this.documentCvPath = documentCvPath;
    }

    public String getDocumentCoverLetterPath() {
        return documentCoverLetterPath;
    }

    public void setDocumentCoverLetterPath(String documentCoverLetterPath) {
        this.documentCoverLetterPath = documentCoverLetterPath;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
