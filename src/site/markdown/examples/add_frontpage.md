# Adding a front page

I want to add a front page containing the document title and the author name, but I don't want to put this information in a fragment.

# Solution

The way to do this is to use a *meta section*.

Meta section are sections without any chapters. A meta section will only contain `<div>` elements, which can be referenced from the CSS.

## Adding properties

First we want to define a set of properties in the `TOC` containing the elements we want to display on the front page; in this case the title and the author.

**Note**, the title is automatically defined as a property, so we only need to add the author:

	<document>
		..
		<!-- the title will automatically be defined as a property -->
		<header title="User manual">
			..
		</header>
		
		<properties>
			<property key="author" value="Thomas Larsen" />
		</properties>

		..
	</document>

The property definition will generate the following HTML:

	<div class="metadata">
		<div class="meta" key="author">Thomas Larsen</div>
		<div class="meta" key="title">User manual</div>
	</div>

## Creating a meta section

After we have added the properties, we will add a meta section. The definition of a meta section is *a section with no level attribute*, i.e.

	<document>
		..
	
		<sections>
			<!-- A metasection - NO level attribute -->
			<section title="frontpage">
				<element key="title />
				<element key="author" />
			</section>

		..
	</document>

In the above TOC, we have added a meta section with two `<element>` tags referencing the defined properties.

The above meta section will produce the following HTML:

	<div class="section">
		<div class="meta-section" id="frontpage">
			<div class="element">
				<div key="title">User manual</div>
			</div>
			<div class="element">
				<div key="author">Thomas Larsen</div>
			</div>
		</div>
	</div>

Note, the `<div>` element for the meta section will be generated with the `id` named the defined title in the TOC.

## The CSS

The final step is to create the CSS referencing the generated `<div>` elements:

	/*
	 * Find the meta section with the id "frontpage"
	 * We add a top margin and right align all the text
	 */
	div.meta-section[id=frontpage] {
		margin-top: 6cm;
		text-align: right;
	}
	
	/*
	 * Here we reference the title div
	 * We make the title xx-large, green and all upper case
	 */
	div.meta-section[id=frontpage] div[key=title] {
		color: green;
		font-size: xx-large;
		font-weight: bold;
		text-transform: uppercase;
	}
	
	/*
	 * Next we reference the author div
	 * We put a margin on the top to put a bit of distance to the title, make the colour silver and size x-large
	 */
	div.meta-section[id=frontpage] div[key=author] {
		margin-top: 1cm;
		color: silver;
		font-size: x-large;
	}

The above CSS will generate a front page with the title and author on.

On a final note, if we want to make a page break after each section, we can add this directive:

	div.section {
		page-break-after: always;
	}

or if we only want a break after the front page:

	div.meta-section[id=frontpage] {
		..
	
		page-break-after: always;
	}
