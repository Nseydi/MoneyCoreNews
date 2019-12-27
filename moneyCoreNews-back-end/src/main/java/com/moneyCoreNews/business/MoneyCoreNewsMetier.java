package com.moneyCoreNews.business;

import com.moneyCoreNews.util.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class MoneyCoreNewsMetier {
    @Value("${upload.photo.extensions}")
    private String validPhotoExtensions;

    public boolean isPhotoValidExtension(String fileExtension)
            throws InvalidFileException {


        if (fileExtension == null) {
            throw new InvalidFileException("No File Extension");
        }

        fileExtension = fileExtension.toLowerCase();

        for (String validExtension : validPhotoExtensions.split(",")) {
            if (fileExtension.equals(validExtension)) {
                return true;
            }
        }
        return false;
    }

}
