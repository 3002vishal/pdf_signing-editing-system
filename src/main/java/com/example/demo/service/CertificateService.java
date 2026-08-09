package com.example.demo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@AllArgsConstructor
public class CertificateService {

    private final CertificateGenerationService certificateGenerationService;
    private final PdfSigningService pdfSigningService;

    public byte[] generateAndSignCertificate(

            InputStream templatePdf,

            String name,
            String date,
            String certificateId,

            int pageNumber,

            float textX,
            float textY,
            float textFontSize,
            String textFontType,
            boolean centerText,

            float signatureLlx,
            float signatureLly,
            float signatureUrx,
            float signatureUry,

            String reason,
            String location

    ) throws Exception {

        // -----------------------------------------
        // 1. Generate unsigned PDF IN MEMORY
        // -----------------------------------------

        ByteArrayOutputStream unsignedPdf =
                new ByteArrayOutputStream();

        certificateGenerationService.generateCertificate(

                templatePdf,

                unsignedPdf,

                name,

                textX,
                textY,
                textFontSize,
                textFontType,
                centerText
        );


        // -----------------------------------------
        // 2. Sign unsigned PDF IN MEMORY
        // -----------------------------------------

        ByteArrayOutputStream signedPdf =
                new ByteArrayOutputStream();

        try (ByteArrayInputStream input =
                     new ByteArrayInputStream(
                             unsignedPdf.toByteArray()
                     )) {

            pdfSigningService.signPdf(

                    input,

                    signedPdf,

                    pageNumber,

                    signatureLlx,
                    signatureLly,
                    signatureUrx,
                    signatureUry,

                    reason,
                    location
            );
        }


        // -----------------------------------------
        // 3. Return final signed PDF
        // -----------------------------------------

        return signedPdf.toByteArray();
    }
}