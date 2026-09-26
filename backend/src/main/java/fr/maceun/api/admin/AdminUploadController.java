package fr.maceun.api.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.maceun.api.storage.StorageService;
import fr.maceun.api.storage.StorageService.StoredImage;

/** Envoi d'une image (pochette de sortie). Renvoie son chemin public à mettre dans le formulaire. */
@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final StorageService storage;

    public AdminUploadController(StorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    StoredImage upload(@RequestPart("file") MultipartFile file) {
        return storage.store(file);
    }
}
