# Overview

A library to identify file formats and to extract meta-data from those files.

## Features

- recognize common formats based on an XML-based matching description
- extract meta-data from ceratin formats using specialized matchers and extractors
- allow adding new recognized formats

## Usage

### Maven dependency

    <dependency>
        <groupId>org.jadice.filetype</groupId>
        <artifactId>filetype-analyzer</artifactId>
        <version>1.2.0</version>
    </dependency>

### Minimal usage

    Map<String, Object> results =
        Analyzer.getInstance("/magic.xml")
            .analyze(new File("my-example-file.pdf"));

    System.out.println("Extension: " + results.get(ExtensionAction.KEY));
    System.out.println("Mime-Type: " + results.get(MimeTypeAction.KEY));

    // some types have specialized matchers providing extra info
    System.out.println("Details: " + results.get(PDFMatcher.DETAILS_KEY));
