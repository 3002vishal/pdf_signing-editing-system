package com.example.demo.controller;

import com.example.demo.dto.GenerateAndSignRequest;
import com.example.demo.dto.GenerateCertificateRequest;
import com.example.demo.dto.SignPdfRequest;
import com.example.demo.service.CertificateGenerationService;
import com.example.demo.service.CertificateService;
import com.example.demo.service.PdfSigningService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final ResourceLoader resourceLoader;
    private final PdfSigningService pdfSigningService;
    private final CertificateGenerationService certificateGenerationService;
    private final CertificateService certificateService;

    public PdfController(
            ResourceLoader resourceLoader,
            PdfSigningService pdfSigningService,
            CertificateGenerationService certificateGenerationService,
            CertificateService certificateService) {

        this.resourceLoader = resourceLoader;
        this.pdfSigningService = pdfSigningService;
        this.certificateGenerationService = certificateGenerationService;
        this.certificateService = certificateService;
    }


    // =========================================================
    // 1. SIGN EXISTING PDF
    // =========================================================

    @PostMapping("/sign")
    public ResponseEntity<byte[]> signPdf(
            @RequestBody SignPdfRequest request) throws Exception {

        Resource template =
                resourceLoader.getResource(
                        "classpath:templates/template.pdf"
                );

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        try (InputStream templateInputStream =
                     template.getInputStream()) {

            pdfSigningService.signPdf(

                    templateInputStream,

                    out,

                    request.getPageNumber(),

                    request.getLlx(),
                    request.getLly(),
                    request.getUrx(),
                    request.getUry(),

                    request.getReason(),
                    request.getLocation()
            );
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=signed.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }


    // =========================================================
    // 2. GENERATE CERTIFICATE
    // =========================================================

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateCertificate(
            @RequestBody GenerateCertificateRequest request)
            throws Exception {

        Resource template =
                resourceLoader.getResource(
                        "classpath:templates/template.pdf"
                );

        ByteArrayOutputStream outputPdf =
                new ByteArrayOutputStream();

        try (InputStream templateInputStream =
                     template.getInputStream()) {

            certificateGenerationService.generateCertificate(

                    templateInputStream,

                    outputPdf,

                    request.getName(),

                    request.getX(),
                    request.getY(),

                    request.getFontSize(),
                    request.getFontName(),

                    request.isCenterText()
            );
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificate.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(outputPdf.toByteArray());
    }


    // =========================================================
    // 3. GENERATE + SIGN CERTIFICATE
    // =========================================================
    @PostMapping("/generate-and-sign")
    public ResponseEntity<byte[]> generateAndSignCertificate(
            @RequestBody GenerateAndSignRequest request) throws Exception {

        Resource template =
                resourceLoader.getResource(
                        "classpath:templates/template.pdf"
                );

        byte[] signedPdf;

        try (InputStream templateInputStream =
                     template.getInputStream()) {

            signedPdf = certificateService.generateAndSignCertificate(

                    templateInputStream,

                    request.getName(),
                    request.getDate(),
                    request.getCertificateId(),

                    request.getPageNumber(),

                    request.getTextX(),
                    request.getTextY(),

                    request.getTextFontSize(),
                    request.getTextFontType(),

                    request.isCenterText(),

                    request.getSignatureLlx(),
                    request.getSignatureLly(),
                    request.getSignatureUrx(),
                    request.getSignatureUry(),

                    request.getReason(),
                    request.getLocation()
            );
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificate.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(signedPdf);
    }
}