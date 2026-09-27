package fr.maceun.api.release;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Un son. Maceun ne saisit que le lien (et éventuellement une date pour un son pas encore sorti) ;
 * le reste est rempli par {@link ReleaseSync} depuis Fanlink et Deezer.
 */
@Entity
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // saisi dans l'admin
    private String url;
    private LocalDate manualReleaseDate;
    private boolean visible = true;

    // récupéré automatiquement (dernière valeur connue)
    private String title;
    private String cover;
    private String genre;
    private LocalDate releaseDate;
    private String isrc;
    private String platforms;          // JSON [{name, url}]
    private LocalDateTime syncedAt;
    private String syncError;

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public LocalDate getManualReleaseDate() { return manualReleaseDate; }
    public void setManualReleaseDate(LocalDate manualReleaseDate) { this.manualReleaseDate = manualReleaseDate; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getIsrc() { return isrc; }
    public void setIsrc(String isrc) { this.isrc = isrc; }
    public String getPlatforms() { return platforms; }
    public void setPlatforms(String platforms) { this.platforms = platforms; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
    public String getSyncError() { return syncError; }
    public void setSyncError(String syncError) { this.syncError = syncError; }
}
