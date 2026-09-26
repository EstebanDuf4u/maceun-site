package fr.maceun.api.release.source;

/** Lien refusé ou illisible. Le message est affiché tel quel dans l'admin. */
public class LinkException extends RuntimeException {

    public LinkException(String message) {
        super(message);
    }
}
