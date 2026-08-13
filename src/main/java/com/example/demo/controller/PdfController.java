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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
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
   @PostMapping(
           value = "/sign",
           consumes = MediaType.MULTIPART_FORM_DATA_VALUE
   )
   public ResponseEntity<byte[]> signPdf(

           @RequestPart("file")
           MultipartFile pdfFile,

           @RequestPart("signatureImage")
           MultipartFile signatureImage,

           @RequestPart("request")
           SignPdfRequest request

   ) throws Exception
   {
       ByteArrayOutputStream output = new ByteArrayOutputStream();

       pdfSigningService.signPdf(
               pdfFile.getInputStream(),
               signatureImage.getInputStream(),
               output,
               request.getPageNumber(),
               request.getLlx(),
               request.getLly(),
               request.getUrx(),
               request.getUry(),
               request.getName(),
               request.getDesignation(),
               request.getOrganization()

       );

       return ResponseEntity.ok()
               .contentType(MediaType.APPLICATION_PDF)
               .header(
                       HttpHeaders.CONTENT_DISPOSITION,
                       "attachement: filename= signed.pdf"

               ).body(output.toByteArray());


   }


    // =========================================================
    // 2. GENERATE CERTIFICATE
    // =========================================================

    @PostMapping(
            value = "/generate",

            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> generateCertificate(
            @RequestPart("file")
            MultipartFile pdfFile,
            @RequestPart("request")
            GenerateCertificateRequest request)
            throws Exception {



        ByteArrayOutputStream outputPdf =
                new ByteArrayOutputStream();



            certificateGenerationService.generateCertificate(

                    pdfFile.getInputStream(),

                    outputPdf,

                    request.getName(),

                    request.getX(),
                    request.getY(),

                    request.getFontSize(),
                    request.getFontName(),

                    request.isCenterText()
            );


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
    @PostMapping(
            value = "/generate-and-sign",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> generateAndSignCertificate(

            @RequestPart("file")
            MultipartFile pdfFile,

            @RequestPart("signatureImage")
            MultipartFile signatureImage,

            @RequestPart("request")
            GenerateAndSignRequest request

    ) throws Exception {



        byte[] signedPdf;


            signedPdf =
                    certificateService.generateAndSignCertificate(

                            pdfFile.getInputStream(),

                            signatureImage.getInputStream(),

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