# Overview

The **TOC** is an XML document that describes:
* The list of fragments that makes up the generated document
* A list of fragment repositories
* A set of properties, which can be injected into the generated document

The TOC structure is:

	<document>
		<repos>
			<repo .. />
		</repos>
		
		<header title="..">
			<meta .. />
		</header>
		
		<properties>
			<property key=".." value=".." />
		</properties>
		
		<sections>
			<section title=".." level="..">
				<chapters>
					<chapter level=".." repo=".." fragment=".." />
					<chapter .. />
				</chapters>
				
				<element key=".." />
			</section>
			<section>
				..
			</section>
		</sections>
	</document>
	
## Fragment repositories

A repository is a pointer to a collection of fragments. Repositories are references from the `chapter` elements in the TOC.

A repository reference contains the following attributes:

* **id** which is the reference used in the `chapter` element
* **uri** which is the *absolute* URI to the repository

## Properties

A property is a key/value pair that can be injected into the generated document using an `element` element inside the `chapters` or `chapter` elements.

## Sections

A sections is used for logically separating a document into a number of parts. Each section will contain a number of chapters which defines the content of the section (included fragments). It might also contain a number of elements, which references a previously defined property.

A `section` has the following attributes:

* **title** which is injected into the generated document
* **level** which is used to rebase all `<h>` tags in the included fragments

In the generated document, a section will be preceded with the following HTML:

	<div class="section">
		<div class="section-header id="*title*">
		
## Chapters



## Elements

Injecting a property will result in the following HTML being generated:

	<div class="element">
		<div key="*key*">
			*value*
		</div>
	</div>
	
Elements are useful in connection with document metadata or document styling.