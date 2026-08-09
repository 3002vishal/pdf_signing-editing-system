package com.example.demo.controller;

import com.example.demo.dto.GenerateAndSignRequest;
import com.example.demo.dto.GenerateCertificateRequest;
import com.example.demo.dto.SignPdfRequest;
import com.example.demo.service.CertificateGenerationService;
import com.example.demo.service.CertificateService;
import com.example.demo.service.CertificateTemplateService;
import com.example.demo.service.PdfSigningService; // Update with your actual service package
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final ResourceLoader resourceLoader;
    private final PdfSigningService pdfSigningService;
    private final CertificateGenerationService certificateGenerationService;
    private final CertificateService certificateService;

    // Constructor Injection
    public PdfController(ResourceLoader resourceLoader, PdfSigningService pdfSigningService,
                         CertificateGenerationService certificateGenerationService,
                         CertificateService certificateService) {
        this.resourceLoader = resourceLoader;
        this.pdfSigningService = pdfSigningService;
        this.certificateGenerationService = certificateGenerationService;
        this.certificateService = certificateService;
    }

    @PostMapping("/sign")
    public ResponseEntity<byte[]> signPdf(@RequestBody SignPdfRequest request) throws Exception {
        Resource template = resourceLoader.getResource("classpath:templates/template.pdf");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        pdfSigningService.signPdf(
                template.getInputStream(),
                out,
                request.getPageNumber(),                // Page number
                request.getLlx(),
                request.getLly(),
                request.getUrx(),
                request.getUry(), // Coordinates: llx, lly, urx, ury
                request.getReason(),
                request.getLocation()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=signed.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateCertificate(
            @RequestBody GenerateCertificateRequest request)
            throws Exception {

        String template =
                "src/main/resources/templates/template.pdf";

        String output =
                "generated-certificate.pdf";

        certificateGenerationService.generateCertificate(
                template,
                output,
                request.getName(),
                request.getX(),
                request.getY(),
                request.getFontSize(),
                request.getFontName(),
                request.isCenterText()
        );

        byte[] pdf =
                Files.readAllBytes(Path.of(output));

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificate.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }

    @PostMapping("/generate-and-sign")
    public ResponseEntity<byte[]> generateAndSignCertificate(
            @RequestBody GenerateAndSignRequest request
    ) throws Exception {

        Resource template =
                resourceLoader.getResource(
                        "classpath:templates/template.pdf"
                );

        byte[] signedPdf =
                certificateService.generateAndSignCertificate(

                        template.getURL().getPath(),

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

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificate.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(signedPdf);
    }

}