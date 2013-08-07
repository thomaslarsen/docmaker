# Page footer

I want to add the document title to the centre of every page footer.

# Solution

We extract the title from the properties and add it to the footer using a CSS `@page` directive.

## The TOC

Given the following TOC:

	<document>
		..
		<!-- the title will automatically be defined as a property -->
		<header title="User manual">
			..
		</header>
	
		..
	</document>

this will generate this HTML:

	<div class="metadata">
		<div class="meta" key="title">User manual</div>
	</div>

## Extract the title

Using the CSS `running` directive, we extract the title:

	div.meta[key=title] {
		position: running(title);
	}

This will in effect put the contents of the title into a variable called `title`, which we can reference in other parts of the CSS.

## Adding the title to the `@page` directive

In the CSS, we create the following `@page` directive:

	@page {
	  margin: 1.5cm;
	  margin-left: 3.5cm;
	  size: A4;
	    
	  @bottom-center {
		  content: element(title);
	  }
	}

When we print the document (or generate the PDF), each page will now show the title in the centre of the bottom of the page.
This will in addition set the page margins and define the page size as *A4*.

### Hiding the title on the first page

If we have a document title page, we might want to hide the title on the first page. This is done with this CSS directive:

	@page:first {
	  
	  @bottom-center {
		  content: "";
	  }
	}
