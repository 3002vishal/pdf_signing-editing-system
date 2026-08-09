package com.example.demo.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import org.springframework.stereotype.Service;

@Service
public class CertificateGenerationService {

    public void generateCertificate(
            String templatePath,
            String outputPath,
            String name,
            float x,
            float y,
            float fontSize,
            String fontName,
            boolean centerText

    ) throws Exception {

        PdfReader reader = new PdfReader(templatePath);
        PdfWriter writer = new PdfWriter(outputPath);

        try (PdfDocument pdfDocument =
                     new PdfDocument(reader, writer)) {

            // Get page
            var page = pdfDocument.getPage(1);

            PdfCanvas canvas =
                    new PdfCanvas(page);

            // -----------------------------------------
            // Create font
            // -----------------------------------------

            PdfFont font =
                    PdfFontFactory.createFont(fontName);

            // -----------------------------------------
            // Calculate X position
            // -----------------------------------------

            float finalX = x;

            if (centerText) {

                float pageWidth =
                        page.getPageSize().getWidth();

                float textWidth =
                        font.getWidth(name, fontSize);

                finalX =
                        (pageWidth - textWidth) / 2;
            }

            // -----------------------------------------
            // Write name
            // -----------------------------------------

            canvas.beginText();

            canvas.setFontAndSize(
                    font,
                    fontSize
            );

            canvas.moveText(
                    finalX,
                    y
            );

            canvas.showText(name);

            canvas.endText();
        }
    }
}