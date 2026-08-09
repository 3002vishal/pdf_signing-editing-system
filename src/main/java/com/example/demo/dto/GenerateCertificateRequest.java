package com.example.demo.dto;

import lombok.Data;

@Data
public class GenerateCertificateRequest {
    private String name;
    private float x;
    private float y;
    private float fontSize;
    private String fontName;
    private boolean centerText;
}
