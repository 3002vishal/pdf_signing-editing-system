package com.example.demo.service;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
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
    private String keystorePath;       // e.g. classpath:certificates/signer.p12

    @Value("${pdf.signing.keystore-password}")
    private String keystorePassword;

    @Value("${pdf.signing.key-alias}")
    private String keyAlias;

    @Value("${pdf.signing.key-password}")
    private String keyPassword;

    public PdfSigningService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        // Register BouncyCastle once
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Signs a PDF at a specific rectangle on a specific page.
     *
     * @param sourcePdf    the unsigned PDF (e.g. template.pdf)
     * @param outputStream where the signed PDF is written
     * @param pageNumber   1-based page number to place the signature on
     * @param llx, lly     lower-left x/y of the signature box (PDF points, origin bottom-left)
     * @param urx, ury     upper-right x/y of the signature box
     * @param reason       shown in the signature ("Approved", "Contract signed", etc.)
     * @param location     shown in the signature ("Bangalore, India")
     */
    public void signPdf(InputStream sourcePdf,
                        OutputStream outputStream,
                        int pageNumber,
                        float llx, float lly, float urx, float ury,
                        String reason,
                        String location) throws Exception {

        // 1. Load the PKCS#12 keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        Resource keystoreResource = resourceLoader.getResource(keystorePath);
        try (InputStream ksStream = keystoreResource.getInputStream()) {
            ks.load(ksStream, keystorePassword.toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) ks.getKey(keyAlias, keyPassword.toCharArray());
        Certificate[] chain = ks.getCertificateChain(keyAlias);

        if (privateKey == null || chain == null) {
            throw new IllegalStateException(
                    "No private key / certificate chain found for alias '" + keyAlias + "'. " +
                            "Check keystore alias and passwords.");
        }

        // 2. Build the signer properties (page, rect, reason, location, field name)
        SignerProperties signerProperties = new SignerProperties()
                .setFieldName("signature-" + System.currentTimeMillis()) // unique field name
                .setPageNumber(pageNumber)
                .setPageRect(new Rectangle(llx, lly, urx - llx, ury - lly))
                .setReason(reason)
                .setLocation(location)
                .setSignatureCreator("CDAC-PdfSigningService");

        // 3. Create the signer — SignerProperties is passed via the constructor in iText 8.0.4,
        //    there is no setSignerProperties() setter on this version.
        PdfReader reader = new PdfReader(sourcePdf);
        PdfSigner signer = new PdfSigner(
                reader,
                outputStream,
                null,                     // temp file path; null = buffer in memory
                new StampingProperties(),
                signerProperties
        );

        // 4. Sign
        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature pks = new PrivateKeySignature(
                privateKey, DigestAlgorithms.SHA256, BouncyCastleProvider.PROVIDER_NAME);

        signer.signDetached(
                digest,
                pks,
                chain,
                null,               // CRL list
                null,               // OCSP client
                null,               // TSA client
                0,                  // estimated size (0 = auto)
                PdfSigner.CryptoStandard.CMS
        );
    }
}