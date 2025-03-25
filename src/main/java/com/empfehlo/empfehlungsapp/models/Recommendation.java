//package com.empfehlo.empfehlungsapp.models;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "recommendations")
//public class Recommendation {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String candidateFirstname;
//    private String candidateLastname;
//    private String position;
//    private String status;
//
//    // Statt einer Beziehung speichern wir direkt den Username als String
//    @Column(name = "recommended_by_username")
//    private String recommendedByUsername;
//
//    private String documentCvPath;
//    private String documentCoverLetterPath;
//
//    private LocalDateTime submittedAt;
//
//    // Default-Konstruktor
//    public Recommendation() {
//    }
//
//    // Konstruktor mit allen Attributen
//    public Recommendation(Long id, String candidateFirstname, String candidateLastname, String position, String status, String recommendedByUsername, String documentCvPath, String documentCoverLetterPath, LocalDateTime submittedAt) {
//        this.id = id;
//        this.candidateFirstname = candidateFirstname;
//        this.candidateLastname = candidateLastname;
//        this.position = position;
//        this.status = status;
//        this.recommendedByUsername = recommendedByUsername;
//        this.documentCvPath = documentCvPath;
//        this.documentCoverLetterPath = documentCoverLetterPath;
//        this.submittedAt = submittedAt;
//    }
//
//    // Getter & Setter für alle Felder
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getCandidateFirstname() {
//        return candidateFirstname;
//    }
//
//    public void setCandidateFirstname(String candidateFirstname) {
//        this.candidateFirstname = candidateFirstname;
//    }
//
//    public String getCandidateLastname() {
//        return candidateLastname;
//    }
//
//    public void setCandidateLastname(String candidateLastname) {
//        this.candidateLastname = candidateLastname;
//    }
//
//    public String getPosition() {
//        return position;
//    }
//
//    public void setPosition(String position) {
//        this.position = position;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getRecommendedByUsername() {
//        return recommendedByUsername;
//    }
//
//    public void setRecommendedByUsername(String recommendedByUsername) {
//        this.recommendedByUsername = recommendedByUsername;
//    }
//
//    public String getDocumentCvPath() {
//        return documentCvPath;
//    }
//
//    public void setDocumentCvPath(String documentCvPath) {
//        this.documentCvPath = documentCvPath;
//    }
//
//    public String getDocumentCoverLetterPath() {
//        return documentCoverLetterPath;
//    }
//
//    public void setDocumentCoverLetterPath(String documentCoverLetterPath) {
//        this.documentCoverLetterPath = documentCoverLetterPath;
//    }
//
//    public LocalDateTime getSubmittedAt() {
//        return submittedAt;
//    }
//
//    public void setSubmittedAt(LocalDateTime submittedAt) {
//        this.submittedAt = submittedAt;
//    }
//}


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
    private String documentCoverLetterPath;

    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.EAGER) // Wir wollen die User-Info direkt mitladen
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "role"}) // verhindert, dass Passwörter im JSON auftauchen
    private User recommendedBy;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCandidateFirstname() { return candidateFirstname; }
    public void setCandidateFirstname(String candidateFirstname) { this.candidateFirstname = candidateFirstname; }

    public String getCandidateLastname() { return candidateLastname; }
    public void setCandidateLastname(String candidateLastname) { this.candidateLastname = candidateLastname; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDocumentCvPath() { return documentCvPath; }
    public void setDocumentCvPath(String documentCvPath) { this.documentCvPath = documentCvPath; }

    public String getDocumentCoverLetterPath() { return documentCoverLetterPath; }
    public void setDocumentCoverLetterPath(String documentCoverLetterPath) { this.documentCoverLetterPath = documentCoverLetterPath; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public User getRecommendedBy() { return recommendedBy; }
    public void setRecommendedBy(User recommendedBy) { this.recommendedBy = recommendedBy; }
}

