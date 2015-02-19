# Overview

The **TOC** is an XML document that describes:

* The list of sections and fragments that makes up the generated document
* A list of fragment repositories
* A set of properties, which can be injected into the generated document

The TOC structure is:

	<document>
		<repos>
			<repo id=".." uri=".." />
		</repos>
		
		<header title="..">
			<meta .. />
			<link .. />
			<base .. />
		</header>
		
		<properties>
			<property key=".." value=".." />
		</properties>
		
		<sections>
			<hsection title=".." classname=".." />
			<section title=".." level="..">
				<chapters>
					<chapter level=".." repo=".." fragment=".." />
					<chapter .. />
				</chapters>
				
				<element key=".." />
			</section>

			<metasection title="..">
				<element key=".." />
				..
			</metasection>

			<psection title=".." classname=".." level=".." />
		</sections>
	</document>
	
## Fragment repositories

A repository is a pointer to a collection of fragments. Repositories are references from the `chapter` elements in the TOC.

A repository reference contains the following attributes:

* **id** which is the reference used in the `chapter` element
* **uri** which is the URI of the repository

### Repository URIs

The specified URI can either be a relative or an absolute URI.
If the URI specified is absolute, it must resolve to a directory where the fragment files can be loaded from.
If the URI is relative, the specified URI will be added to the [`fragmentURI`](docmaker-mojo.html#fragmentURI) specified in the POM. The resulting compound URI will then be treated as an absolute URI.

Fragment repositories will not produce any HTML output.

## Header

This provides a facility to insert elements into the HTML `head` section.

Docmaker will translate the tag name into the head section. the attributes will be copied across as-is, for example:

	<header title="..>
		<meta name="description" content="Free Web tutorials" />

Will insert the following HTML:

	<html>
		<head>
			<title>...</title>
			<meta name="description" content="Free Web tutorials" />
			
I short, sections will essentially be copied across from the TOC to the HTML.

The following HTML `head` tags are supported:

* `meta`
* `link`
* `base`

## Properties

A property is a key/value pair that can be injected into the generated document using an `element` element inside the `chapters` or `chapter` elements.

Properties will not produce any HTML output.

## Sections

A sections is used for logically separating a document into a number of parts. Each section will contain a number of chapters which defines the content of the section (included fragments). It might also contain a number of elements, which references a previously defined property.

There are three types of sections:

* Contents Section - `<section>` element
* Meta Section - `<metasection>` element
* Pseudo Section - `<psection>` element
* Header Section - `<hsection>` element

All sections will have the following attributes:

* **title** which is injected into the generated document

#### Generated section `id` attribute

The *generated id* will be the TOC filename combined with the section title (all in lower case).
For example:

* **TOC** `adminguide.xml`
* **Section title** `Introduction`
* **Generated ID** `adminguide-introduction`

and

* **TOC** `userguide.xml`
* **Section title** `User administration`
* **Generated ID** `userguide-user-administration`

### Contents Section

A contents section has the following additional attributes:

* **level** which is used to rebase all `<h>` tags in the included fragments

In the transient HTML document, a contents section will be preceded with the following HTML:

	<div class="section">
		<div class="section-header name="*title*" id="*generated id*">
		

If a contents section contains any `elements`, they will be written in the end of the section.

### Meta section

A meta section does not contain any additional attributes.

If we have a TOC with the filename `adminguide.xml` and the following meta section:

	<metasection title="frontpage">
		<element key="author" />
		<element key="release" />
	</metasection>

and following properties:

	<properties>
		<property key="author" value="Thomas Larsen" />
		<property key="release" value="GA 1.0" />
	</properties>

In the transient HTML document, a meta section will have the following contents:

	<div class="section">
		<div class="meta-section" id="adminguide-frontpage" name="frontpage">
			<div key="author">
				Thomas Larsen
			</div>
			<div key="release">
				GA 1.0
			</div>
		</div>
	</div>

Example of [how to use a meta section](examples/add_frontpage.html).

### Pseudo section

A pseudo section has the following additional attributes:

* **classname** which specifies the absolute classname of the generating class. This class must implement the [`PseudoSection`](extensions/pseudosection.html) interface
* ***additional values*** as needed by the genrating class

Pseudo sections are used to inject additional contents into the transient HTML document.
Pseudo sections contents will be generated by a class (which must be on the classpath). The generated contents will be preceded with the following HTML:

	<div class="section">
		<div class="pseudo-section name="*title*" id="*generated id*">
		

## Chapters

Chapters are references to the fragments. A chapter reference will have the following attributes:

* **repo** which refers to a fragment repository specified in the `<repos>` section
* **fragment** which is the filename (without extension) of the fragment file
* **level** which is header level the fragment should be normalised to in the transient HTML document

A chapter will be preceded with the following HTML:

	-- section div element, or previous chapter
	<div class="chapters">
		<div class="chapter">
			<div class="chapter" id="adminguide-admin-user-administration-add_user">
				..
		
		<div class="chapter">
			<div class="chapter" id="adminguide-admin-user-administration-permissions_user">
				..

### Generated chapter `id` attribute

The *generated id* will be the TOC filename combined with the section title and the fragment filename (all in lower case).
For example:

* **TOC** `adminguide.xml`
* **Section title** `User administration`
* **fragment filename** `add_user`
* **Generated ID** `adminguide-admin-user-administration-add_user`

and

* **TOC** `adminguide.xml`
* **Section title** `User administration`
* **fragment filename** `permissions_user`
* **Generated ID** `adminguide-admin-user-administration-permissions_user`

## Elements

Injecting a property will result in the following HTML being generated:

	<div class="element">
		<div key="*key*">
			*value*
		</div>
	</div>
	
Elements are useful in connection with document metadata or document styling and are normally found in meta sections, but can also be used in contents sections.