package com.example.demo.controller;

import com.example.demo.dto.SignPdfRequest;
import com.example.demo.service.PdfSigningService; // Update with your actual service package
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final ResourceLoader resourceLoader;
    private final PdfSigningService pdfSigningService;

    // Constructor Injection
    public PdfController(ResourceLoader resourceLoader, PdfSigningService pdfSigningService) {
        this.resourceLoader = resourceLoader;
        this.pdfSigningService = pdfSigningService;
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
}