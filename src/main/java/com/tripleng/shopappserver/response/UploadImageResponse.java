package com.tripleng.shopappserver.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UploadImageResponse {
    private String message;
    private List<String> imageUrls;
    private List<String> errors;

    public UploadImageResponse() {
        this.message = "";
        this.imageUrls = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public void addImageUrl(String imageUrl) {
        if (!imageUrls.contains(imageUrl)) {
            imageUrls.add(imageUrl);
        }
    }

    public void removeImageUrl(String imageUrl) {
        this.imageUrls.remove(imageUrl);
    }

    public void addError(String s) {
        this.errors.add(s);
    }
}
