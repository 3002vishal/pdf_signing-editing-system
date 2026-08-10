package com.example.demo.service;

import com.itextpdf.forms.form.element.SignatureFieldAppearance;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.SignerProperties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

@Service
public class PdfSigningService {

    private final ResourceLoader resourceLoader;

    @Value("${pdf.signing.keystore-path}")
    private String keystorePath;

    @Value("${pdf.signing.keystore-password}")
    private String keystorePassword;

    @Value("${pdf.signing.key-alias}")
    private String keyAlias;

    @Value("${pdf.signing.key-password}")
    private String keyPassword;


    public PdfSigningService(ResourceLoader resourceLoader) {

        this.resourceLoader = resourceLoader;

        if (Security.getProvider(
                BouncyCastleProvider.PROVIDER_NAME) == null) {

            Security.addProvider(
                    new BouncyCastleProvider()
            );
        }
    }


    public void signPdf(

            InputStream sourcePdf,

            InputStream signatureImage,

            OutputStream outputStream,

            int pageNumber,

            float llx,
            float lly,
            float urx,
            float ury,

            String name,
            String designation,
            String organization

    ) throws Exception {


        // =========================================================
        // 1. LOAD PKCS12 KEYSTORE
        // =========================================================

        KeyStore ks =
                KeyStore.getInstance("PKCS12");

        Resource keystoreResource =
                resourceLoader.getResource(keystorePath);

        try (InputStream ksStream =
                     keystoreResource.getInputStream()) {

            ks.load(
                    ksStream,
                    keystorePassword.toCharArray()
            );
        }


        // =========================================================
        // 2. GET PRIVATE KEY
        // =========================================================

        PrivateKey privateKey =
                (PrivateKey) ks.getKey(
                        keyAlias,
                        keyPassword.toCharArray()
                );


        // =========================================================
        // 3. GET CERTIFICATE CHAIN
        // =========================================================

        Certificate[] chain =
                ks.getCertificateChain(keyAlias);


        if (privateKey == null || chain == null) {

            throw new IllegalStateException(
                    "No private key / certificate chain found " +
                            "for alias '" + keyAlias + "'."
            );
        }


        // =========================================================
        // 4. VALIDATE RECTANGLE
        // =========================================================

        float signatureWidth =
                urx - llx;

        float signatureHeight =
                ury - lly;


        if (signatureWidth <= 0 ||
                signatureHeight <= 0) {

            throw new IllegalArgumentException(
                    "Invalid signature rectangle."
            );
        }


        // =========================================================
        // 5. LOAD IMAGE
        // =========================================================

        byte[] imageBytes =
                signatureImage.readAllBytes();

        if (imageBytes.length == 0) {

            throw new IllegalArgumentException(
                    "Signature image is empty."
            );
        }


        ImageData imageData =
                ImageDataFactory.create(imageBytes);


        // =========================================================
        // 6. UNIQUE FIELD NAME
        // =========================================================

        String fieldName =
                "signature-" + System.currentTimeMillis();


        // =========================================================
        // 7. SIGNATURE BOX INTERNAL DIMENSIONS
        // =========================================================

        float padding = 5;

        float contentWidth =
                signatureWidth - (2 * padding);

        float contentHeight =
                signatureHeight - (2 * padding);


        // =========================================================
        // 8. RESERVE SPACE FOR TEXT
        // =========================================================

        /*
         * We need enough space for:
         *
         * Name
         * Designation
         * Organization
         *
         * 3 lines × approximately 10-12 points.
         */
        float textHeight = 42;


        float imageAvailableHeight =
                contentHeight - textHeight;


        if (imageAvailableHeight <= 0) {

            throw new IllegalArgumentException(
                    "Signature rectangle is too small. " +
                            "Increase its height."
            );
        }


        // =========================================================
        // 9. CREATE SIGNATURE IMAGE
        // =========================================================

        Image signature =
                new Image(imageData);


        /*
         * IMPORTANT:
         *
         * scaleToFit() preserves the original aspect ratio.
         *
         * The image will use as much width as possible,
         * but will never exceed the image area height.
         */
        signature.scaleToFit(
                contentWidth,
                imageAvailableHeight
        );


        /*
         * Center the image horizontally.
         */
        signature.setHorizontalAlignment(
                com.itextpdf.layout.properties.HorizontalAlignment.CENTER
        );


        /*
         * Remove extra margins.
         */
        signature
                .setMarginTop(0)
                .setMarginBottom(2)
                .setMarginLeft(0)
                .setMarginRight(0);


        // =========================================================
        // 10. CREATE SIGNATURE CONTENT
        // =========================================================

        Div signatureContent =
                new Div();

        signatureContent
                .setWidth(contentWidth)
                .setHeight(contentHeight)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(0)
                .setMargin(0);


        // =========================================================
        // 11. ADD SIGNATURE IMAGE
        // =========================================================

        signatureContent.add(signature);


        // =========================================================
        // 12. ADD NAME
        // =========================================================

        if (name != null &&
                !name.isBlank()) {

            Paragraph nameParagraph =
                    new Paragraph(name)
                            .setFontSize(10)
                            .setBold()
                            .setTextAlignment(
                                    TextAlignment.CENTER
                            )
                            .setMargin(0)
                            .setPadding(0)
                            .setFixedLeading(11);

            signatureContent.add(
                    nameParagraph
            );
        }


        // =========================================================
        // 13. ADD DESIGNATION
        // =========================================================

        if (designation != null &&
                !designation.isBlank()) {

            Paragraph designationParagraph =
                    new Paragraph(designation)
                            .setFontSize(8)
                            .setTextAlignment(
                                    TextAlignment.CENTER
                            )
                            .setMargin(0)
                            .setPadding(0)
                            .setFixedLeading(10);

            signatureContent.add(
                    designationParagraph
            );
        }


        // =========================================================
        // 14. ADD ORGANIZATION
        // =========================================================

        if (organization != null &&
                !organization.isBlank()) {

            Paragraph organizationParagraph =
                    new Paragraph(organization)
                            .setFontSize(8)
                            .setTextAlignment(
                                    TextAlignment.CENTER
                            )
                            .setMargin(0)
                            .setPadding(0)
                            .setFixedLeading(10);

            signatureContent.add(
                    organizationParagraph
            );
        }


        // =========================================================
        // 15. CREATE SIGNATURE APPEARANCE
        // =========================================================

        SignatureFieldAppearance appearance =
                new SignatureFieldAppearance(fieldName)
                        .setContent(signatureContent);


        // =========================================================
        // 16. CREATE SIGNER PROPERTIES
        // =========================================================

        SignerProperties signerProperties =
                new SignerProperties()

                        .setFieldName(fieldName)

                        .setPageNumber(pageNumber)

                        .setPageRect(
                                new Rectangle(
                                        llx,
                                        lly,
                                        signatureWidth,
                                        signatureHeight
                                )
                        )

                        .setReason(
                                "Signed by " + name
                        )

                        .setLocation(
                                organization
                        )

                        .setSignatureCreator(
                                "CDAC-PdfSigningService"
                        )

                        .setSignatureAppearance(
                                appearance
                        );


        // =========================================================
        // 17. CREATE PDF READER
        // =========================================================

        PdfReader reader =
                new PdfReader(sourcePdf);


        // =========================================================
        // 18. CREATE PDF SIGNER
        // =========================================================

        PdfSigner signer =
                new PdfSigner(
                        reader,
                        outputStream,
                        null,
                        new StampingProperties(),
                        signerProperties
                );


        // =========================================================
        // 19. CREATE DIGEST
        // =========================================================

        IExternalDigest digest =
                new BouncyCastleDigest();


        // =========================================================
        // 20. CREATE CRYPTOGRAPHIC SIGNATURE
        // =========================================================

        IExternalSignature pks =
                new PrivateKeySignature(
                        privateKey,
                        DigestAlgorithms.SHA256,
                        BouncyCastleProvider.PROVIDER_NAME
                );


        // =========================================================
        // 21. DIGITALLY SIGN PDF
        // =========================================================

        signer.signDetached(

                digest,

                pks,

                chain,

                null,       // CRL

                null,       // OCSP

                null,       // TSA

                0,

                PdfSigner.CryptoStandard.CMS
        );
    }
}