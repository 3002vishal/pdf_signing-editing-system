package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateAndSignRequest {

    private String name;
    private String date;
    private String certificateId;

    private int pageNumber;

    private float textX;
    private float textY;
    private float textFontSize;
    private String textFontType;
    private boolean centerText;

    private float signatureLlx;
    private float signatureLly;
    private float signatureUrx;
    private float signatureUry;

    private String reason;
    private String location;
}