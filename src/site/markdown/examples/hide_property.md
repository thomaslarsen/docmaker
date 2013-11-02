# Hide properties

Properties show up in the document, but I want to hide it.

# Solution

We need to identify the property, and use CSS to hide the element.

For example I want to hide the `author` property defined in the TOC:

	<document>
		..
		<properties>
			<property key="author" value="John Doe" />
			..
		</properties>
		..

This property will generated the following HTML:

	<div class="metadata">
		<div class="meta" key="author">John Doe</div>
		..
	</div>

which will show up in the result document.

## Hiding the property

The following CSS will hide the property:

	div.meta[key=author] {
		display: none;
	}
