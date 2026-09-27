package fr.maceun.api.photo;

public record PhotoView(Long id, String src, String shape) {

    static PhotoView of(Photo p) {
        return new PhotoView(p.getId(), p.getSrc(), p.getShape());
    }
}
