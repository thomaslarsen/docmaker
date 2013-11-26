# What is docmaker?

Docmaker is a [Maven](http://maven.apache.org) plugin that can convert a set of markup documents into a single output file, defined by a table of contents XML file and styled using CSS.

Docmaker is made up of a number of elements:

* The **document** it the generated output, usually a PDF but can be other formats
* A **fragment** contains the actual contents and is a part of one or more *documents*. Usually written in [Markdown](http://daringfireball.net/projects/markdown/syntax), but can be other formats
* The [**TOC**](toc.html) is an XML file that describes the *fragments* in the *document*
* A **transient HTML document** comprising all the *fragments* will be styled using a **CSS** document

## Why

The motivation for creating docmaker was a need to create a number of PDF documents as part of a deliverables for a product, where some sections were reused across a number of the documents.
It also needed to be easy to maintain the various parts of the final PDF documents.

I needed to fulfil the following requirements:

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

1. Convert each fragment into a HTML
2. Assemble the HTML fragments as defined in the TOC into a single transient HTML document
3. Convert the transient HTML document into PDF

## Converting fragments to HTML

Each fragment will be written in a markup language. Docmaker will use a [`MarkupProcessor`](extensions/markupprocessor.html) to convert the fragment into an HTML file.

> **Note**
>
> The fragment HTML file is not a complete *HTML document*, i.e. it will not contain any `<html>` or `<body>` tags for example.

## Assembling the transient HTML document

The [TOC](toc.html) describes the structure of the document. This is organised into a hierarchy of **sections** and **chapters**, where a *section* can contain multiple *chapters* and a chapter refers to a single *fragment*.

The fragments will appear in the document in the order they are specified in the TOC. Each section and chapter will be surrounded by a `<div>` tag in the transient HTML document.
Each of these `<div>` tags will have a generated `id` attribute allowing cross references and links to target them.

### Normalising header tags

Any `<h>` sections in the fragment HTML file will be normalised, i.e. they will all start from `<h1>`.

### Injecting header IDs

Headers will get an `id` attribute injected allowing cross references and links to target them.

## Converting transient HTML document into PDF

The transient HTML document will be styled using a *CSS document* and then converted into PDF. Docmaker will use a [`PostProcessor`](extensions/postprocessor.html) to do the HTML to PDF conversion.

# Extending docmaker

Docmaker can be extended in many ways. Internally, docmaker uses the same extension points as listed below:
