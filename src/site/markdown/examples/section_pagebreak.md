# Page break between section

I want to add a page break between all the sections in the document.

# Solution

We will use the fact that a `<div class="section">` tag is automatically injected into all the sections. We will reference this from the CSS:

	div.section {
		page-break-after: always;
	}
