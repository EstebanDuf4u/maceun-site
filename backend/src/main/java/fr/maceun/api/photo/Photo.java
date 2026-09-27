package fr.maceun.api.photo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String src;
    /** "wide" (paysage) ou "tall" (portrait) : format d'affichage dans la galerie. */
    private String shape;
    private int position;

    public Long getId() { return id; }
    public String getSrc() { return src; }
    public void setSrc(String src) { this.src = src; }
    public String getShape() { return shape; }
    public void setShape(String shape) { this.shape = shape; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
