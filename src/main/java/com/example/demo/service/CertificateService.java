package com.example.demo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@AllArgsConstructor
public class CertificateService {

    private final CertificateGenerationService certificateGenerationService;
    private final PdfSigningService pdfSigningService;


    public byte[] generateAndSignCertificate(

            String templatePath,

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
        // 1. Create temporary unsigned PDF
        // -----------------------------------------

        Path unsignedPdf =
                Files.createTempFile(
                        "certificate-unsigned-",
                        ".pdf"
                );

        try {

            // -----------------------------------------
            // 2. Generate certificate
            // -----------------------------------------

            certificateGenerationService.generateCertificate(

                    templatePath,

                    unsignedPdf.toString(),

                    name,

                    textX,
                    textY,
                    textFontSize,
                    textFontType,
                    centerText
            );


            // -----------------------------------------
            // 3. Read generated PDF
            // -----------------------------------------

            byte[] unsignedPdfBytes =
                    Files.readAllBytes(unsignedPdf);


            // -----------------------------------------
            // 4. Prepare signature position
            // -----------------------------------------

            // -----------------------------------------
            // 5. Sign PDF
            // -----------------------------------------

            ByteArrayOutputStream signedPdf =
                    new ByteArrayOutputStream();

            try (ByteArrayInputStream input =
                         new ByteArrayInputStream(
                                 unsignedPdfBytes
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
            // 6. Return final signed certificate
            // -----------------------------------------

            return signedPdf.toByteArray();

        } finally {

            // -----------------------------------------
            // 7. Delete temporary unsigned PDF
            // -----------------------------------------

            Files.deleteIfExists(unsignedPdf);
        }
    }
}