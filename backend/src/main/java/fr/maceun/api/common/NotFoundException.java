package fr.maceun.api.common;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String what) {
        super(what + " introuvable");
    }
}
