# What is docmaker?

Docmaker is a [Maven](http://maven.apache.org) plugin that can convert a set of markup documents into a single output file, defined by a TOC and styled using CSS.

Docmaker is made up of a number of elements:

* The **document** it the generated document, usually a PDF but can be other formats
* A **fragment** is a part of one or more *documents*
* The **TOC** is an XML file that describes the structure of the *document*
* The styling of the *document* is described in a **CSS** document

## Why

The motivation for creating docmaker was a need to create a number of PDF files as part of a deliverables for a product, where some sections were reused across a number of the files.
It also needed to be easy to maintain the various parts of the final PDF documents.

The drivers for creating docmaker is based on the needs of a project I was working on. I needed to fulfil the following requirements:

* Use of a markup language (e.g. Markdown) as the fragment format
* Separation of contents and layout (using CSS)
* Assembly of a document based on a number of fragments
* Ability to create multiple fragments, which can be stored in multiple locations, including the web
* Ability to reuse fragments in multiple documents
* Peer review of fragments using standard code review tools (such as Crucible)
* Integration of document build into the project CI tool (e.g. Jenkins)
* Integration into a rich-text editors for authoring of the contents

# How it works

Docmaker will go through the following steps:

1. Convert each fragment into HTML
2. Assemble the HTML fragments as defined in the TOC into a single HTML document
3. Convert the assembled HTML document into PDF

## Converting fragments to HTML

Each fragment will be written in a markup language. Docmaker will use a `MarkupProcessor` to convert the fragment into an HTML file. Note, the fragment HTML file is not a complete *HTML document* i.e. it will not contain any `<html>` or `<body>` tags for example.

Any `<h>` sections in the fragment HTML file will be normalised, i.e. they will all start from `<h1>`.

## Assembling the fragments

The [TOC](#toc.html) describes which 