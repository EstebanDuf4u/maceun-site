package fr.maceun.api.content;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Ligne unique (id = 1) contenant le JSON de {@link SiteContent}. */
@Entity
@Table(name = "site_content")
class SiteContentEntity {

    static final int ID = 1;

    @Id
    private Integer id;
    private String data;
    private LocalDateTime updatedAt;

    String getData() { return data; }
    void setData(String data) { this.data = data; }
    LocalDateTime getUpdatedAt() { return updatedAt; }
    void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
