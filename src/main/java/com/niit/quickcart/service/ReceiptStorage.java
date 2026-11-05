package com.niit.quickcart.service;

import com.niit.quickcart.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Validates and saves customer payment receipts (image or PDF) to disk.
 * Only a generated file name is stored on the order, never the customer's own file name.
 */
@Service
public class ReceiptStorage {

    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private final Path directory;

    @Autowired
    public ReceiptStorage(@Value("${quickcart.receipts.dir:./data/receipts}") String directory) {
        this(Path.of(directory));
    }

    public ReceiptStorage(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
    }

    /** Checks the upload and returns the extension to save it with. Throws BadRequestException if invalid. */
    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please upload your payment receipt.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("Receipt is too large. Maximum size is 5MB.");
        }
        byte[] head = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded receipt.");
        }
        String ext = detectExtension(head, read);
        if (ext == null) {
            throw new BadRequestException("Receipt must be a JPG, PNG, WEBP or PDF file.");
        }
        return ext;
    }

    /** Saves the receipt and returns the stored file name. */
    public String save(MultipartFile file, String extension) {
        try {
            Files.createDirectories(directory);
            String name = UUID.randomUUID() + "." + extension;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, directory.resolve(name));
            }
            return name;
        } catch (IOException e) {
            throw new RuntimeException("Could not save payment receipt.", e);
        }
    }

    public void delete(String storedName) {
        if (storedName == null) return;
        try {
            Files.deleteIfExists(directory.resolve(storedName));
        } catch (IOException ignored) {
            // best effort cleanup
        }
    }

    // Check the real file signature, not just the browser-supplied content type.
    static String detectExtension(byte[] b, int n) {
        if (n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (n >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
        if (n >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') return "pdf";
        if (n >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }
}
