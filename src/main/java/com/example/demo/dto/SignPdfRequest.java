package com.example.demo.dto;

import lombok.Data;

@Data
public class SignPdfRequest {
    private int pageNumber;
    private float llx;
    private float lly;
    private float urx;
    private float ury;
    private String reason;
    private String location;
}
