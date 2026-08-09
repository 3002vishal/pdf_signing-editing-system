package com.example.demo.service;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class CertificateGenerationService {

    public void generateCertificate(
            InputStream templatePdf,
            OutputStream outputPdf,
            String name,
            float x,
            float y,
            float fontSize,
            String fontName,
            boolean centerText
    ) throws Exception {

        PdfReader reader = new PdfReader(templatePdf);
        PdfWriter writer = new PdfWriter(outputPdf);

        try (PdfDocument pdfDocument =
                     new PdfDocument(reader, writer)) {

            var page = pdfDocument.getPage(1);

            PdfCanvas canvas = new PdfCanvas(page);

            PdfFont font =
                    PdfFontFactory.createFont(fontName);

            float finalX = x;

            if (centerText) {

                float pageWidth =
                        page.getPageSize().getWidth();

                float textWidth =
                        font.getWidth(name, fontSize);

                finalX =
                        (pageWidth - textWidth) / 2;
            }

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