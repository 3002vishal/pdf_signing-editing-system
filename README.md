# PDF Signing & Editing System

A **Java 17 / Spring Boot** service for generating, editing, and digitally signing PDF documents using certificate-based cryptography.

## Features

- Generate and sign PDF documents through REST endpoints
- Digital PDF signatures using X.509 certificates
- Certificate generation and certificate-template services
- PDF processing with iText
- Cryptographic operations through Bouncy Castle
- Request validation with Spring Boot validation

## Tech Stack

- Java 17
- Spring Boot
- iText 8
- Bouncy Castle
- Maven
- REST APIs

## Project Structure

```text
src/main/java/com/example/demo/
├── controller/
│   └── PdfController.java
├── dto/
│   ├── GenerateAndSignRequest.java
│   ├── GenerateCertificateRequest.java
│   └── SignPdfRequest.java
└── service/
    ├── CertificateGenerationService.java
    ├── CertificateService.java
    ├── CertificateTemplateService.java
    └── PdfSigningService.java
```

## Running Locally

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

## Security

Do not commit PKCS#12 files, private keys, passwords, or production certificates. The repository is configured to ignore common key and keystore formats. Generate local development credentials separately.

## Purpose

This project demonstrates backend engineering around **PDF processing, digital signatures, PKI, certificate handling, and REST API design**.
