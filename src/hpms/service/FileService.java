package hpms.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileService {
    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    static {
        // Create upload directory if it doesn't exist
        new File(UPLOAD_DIR).mkdirs();
    }

    public static String saveProfilePicture(File file, String staffId) throws IOException {
        // Validate file
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist");
        }

        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed size of 5MB");
        }

        // Check file extension
        String fileName = file.getName().toLowerCase();
        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileName.endsWith("." + ext)) {
                validExtension = true;
                break;
            }
        }
        if (!validExtension) {
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG, and GIF are allowed.");
        }

        // Generate unique filename
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = "profile_" + staffId + "_" + UUID.randomUUID().toString() + fileExtension;
        Path targetPath = Paths.get(UPLOAD_DIR, newFileName);

        // Create uploads directory if it doesn't exist
        Files.createDirectories(Paths.get(UPLOAD_DIR));

        // Save file
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }
}
