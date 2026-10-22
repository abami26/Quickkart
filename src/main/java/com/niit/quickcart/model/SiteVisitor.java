package com.niit.quickcart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_visitors")
public class SiteVisitor {

    @Id
    @Column(length = 36)
    private String visitorId;

    @Column(nullable = false)
    private LocalDateTime firstSeenAt = LocalDateTime.now();

    public SiteVisitor() {
    }

    public SiteVisitor(String visitorId) {
        this.visitorId = visitorId;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public LocalDateTime getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(LocalDateTime firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }
}