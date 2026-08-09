package com.example.demo.service;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.forms.fields.TextFormFieldBuilder;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

public class CertificateTemplateService {

    public void createFields(
            String inputPdf,
            String outputPdf) throws Exception {

        PdfReader reader = new PdfReader(inputPdf);
        PdfWriter writer = new PdfWriter(outputPdf);

        PdfDocument pdfDocument =
                new PdfDocument(reader, writer);

        PdfAcroForm form =
                PdfAcroForm.getAcroForm(pdfDocument, true);

        // =========================================================
        // NAME FIELD
        // =========================================================

        PdfTextFormField nameField =
                new TextFormFieldBuilder(
                        pdfDocument,
                        "certificateName")
                        .setWidgetRectangle(
                                new Rectangle(
                                        150,  // x
                                        350,  // y
                                        300,  // width
                                        50    // height
                                ))
                        .createText();

        nameField.setFontSize(22);

        nameField.getFirstFormAnnotation()
                .setBackgroundColor(null);

        nameField.getFirstFormAnnotation()
                .setBorderWidth(0);

        form.addField(nameField);


        // =========================================================
        // DATE FIELD
        // =========================================================

        PdfTextFormField dateField =
                new TextFormFieldBuilder(
                        pdfDocument,
                        "certificateDate")
                        .setWidgetRectangle(
                                new Rectangle(
                                        150,
                                        280,
                                        150,
                                        30
                                ))
                        .createText();

        dateField.setFontSize(12);

        // Make field transparent
        dateField.getFirstFormAnnotation()
                .setBackgroundColor(null);

        dateField.getFirstFormAnnotation()
                .setBorderWidth(0);
        form.addField(dateField);
        form.getField("certificateName") .setValue("Vishal Kumar");
        form.getField("certificateDate") .setValue("08 August 2026");


        // =========================================================
        // CLOSE
        // =========================================================

        pdfDocument.close();
    }
}