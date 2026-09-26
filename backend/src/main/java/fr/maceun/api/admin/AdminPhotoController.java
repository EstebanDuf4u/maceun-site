package fr.maceun.api.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.maceun.api.photo.PhotoService;
import fr.maceun.api.photo.PhotoView;

@RestController
@RequestMapping("/api/admin/photos")
public class AdminPhotoController {

    private final PhotoService photos;

    public AdminPhotoController(PhotoService photos) {
        this.photos = photos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PhotoView add(@RequestPart("file") MultipartFile file, @RequestParam(required = false) String shape) {
        return photos.add(file, shape);
    }

    @PatchMapping("/{id}")
    PhotoView setShape(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return photos.setShape(id, body.get("shape"));
    }

    @PutMapping("/order")
    List<PhotoView> reorder(@RequestBody List<Long> ids) {
        return photos.reorder(ids);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        photos.delete(id);
    }
}
