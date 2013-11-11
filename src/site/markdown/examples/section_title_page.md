# Section title page

I want to add a section title page before the chapters of the section.

# Solution

The solution is to extract the title `id` injected in the `<div>` tag before the chapters.

Given the following TOC:

	<document>
		..
		<sections>
			<section level="1" title="Introduction">
				<chapters>
					<chapter level="2" repo="common" fragment="intro" />
					..
				</chapters>
			</section>
			..
		</sections>
	</document>

the following HTML will be generated:

	<div class="section">
		<div class="section-header" name="Introduction" id="adminguide-introduction">
			<div class="chapters">
				<div class="chapter" id="adminguide-introduction-intro">
				..

We will use the `name` attribute of the `section-header` `<div>` tag.

## CSS

The following CSS will extract and insert the `id` attribute into the document:

	div.section-header:before { 
		content: attr(name);

		color: navy;
		text-align: right;
		font-size: xx-large;
		font-weight: bold;
		text-transform: uppercase;
	}

This will in effect insert the contents of the `id` attribute into the HTML document, right align the text, made it bold, xx-large and upper case.

If we want to move the text down a bit, we can further add a margin:

	div.section-header {
		margin-top: 8cm;
	}

Finally, we want to make sure the chapters of the section start on a new page:

	div.chapters {
		/* 
		 * Ensure we always start the main part of the section on a new page
		 */
		page-break-before: always;
	}

and make sure we break the page before the title page:

	div.section {
		page-break-after: always;
	}

