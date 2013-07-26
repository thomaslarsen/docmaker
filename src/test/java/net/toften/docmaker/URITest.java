package net.toften.docmaker;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

import org.junit.Test;

public class URITest {

	@Test
	public void testRelURI() throws URISyntaxException {
		String relURI = "docs/guide/collections/designfaq.html";
		
		URI uri = new URI(relURI);
		assertFalse(uri.isAbsolute());
		assertNull(uri.getScheme());
	}
	
	@Test
	public void testAbsURI() throws URISyntaxException {
		String absURI = "file:///docs/designfaq.html";
		
		URI uri = new URI(absURI);
		assertTrue(uri.isAbsolute());
		assertFalse(uri.isOpaque());
		assertEquals("file", uri.getScheme());
		assertNull(uri.getAuthority());
		assertEquals("/docs/designfaq.html", uri.getPath());
	}

	@Test
	public void testCompoundURI() throws URISyntaxException {
		String absURI = "file:///docs/";
		String relURI = "guide/collections/designfaq.html";
		
		URI aUri = new URI(absURI);
		URI rUri = new URI(relURI);
		assertTrue(aUri.isAbsolute());
		assertFalse(rUri.isAbsolute());
		assertFalse(aUri.isOpaque());
		assertFalse(rUri.isOpaque());
		assertEquals("file", aUri.getScheme());
		assertNull(rUri.getScheme());
		assertEquals("/docs/", aUri.getPath());
		assertEquals("guide/collections/designfaq.html", rUri.getPath());
		
		URI cUri = aUri.resolve(rUri);
		assertTrue(cUri.isAbsolute());
		assertFalse(cUri.isOpaque());
		assertEquals("file", cUri.getScheme());
		assertNull(cUri.getAuthority());
		assertEquals("/docs/guide/collections/designfaq.html", cUri.getPath());
	}

	@Test
	public void testCompoundURI2() throws URISyntaxException {
		String absURI = "file:///docs/guide/";
		String relURI = "../collections/designfaq.html";
		
		URI aUri = new URI(absURI);
		URI rUri = new URI(relURI);
		assertTrue(aUri.isAbsolute());
		assertFalse(rUri.isAbsolute());
		assertFalse(aUri.isOpaque());
		assertFalse(rUri.isOpaque());
		assertEquals("file", aUri.getScheme());
		assertNull(rUri.getScheme());
		assertEquals("/docs/guide/", aUri.getPath());
		assertEquals("../collections/designfaq.html", rUri.getPath());
		
		URI cUri = aUri.resolve(rUri);
		assertTrue(cUri.isAbsolute());
		assertFalse(cUri.isOpaque());
		assertEquals("file", cUri.getScheme());
		assertNull(cUri.getAuthority());
		assertEquals("/docs/collections/designfaq.html", cUri.getPath());
	}

	@Test
	public void testCompoundURI3() throws URISyntaxException {
		String absURI = "file:///docs/guide/";
		String relURI = "./collections/designfaq.html";
		
		URI aUri = new URI(absURI);
		URI rUri = new URI(relURI);
		assertTrue(aUri.isAbsolute());
		assertFalse(rUri.isAbsolute());
		assertFalse(aUri.isOpaque());
		assertFalse(rUri.isOpaque());
		assertEquals("file", aUri.getScheme());
		assertNull(rUri.getScheme());
		assertEquals("/docs/guide/", aUri.getPath());
		assertEquals("./collections/designfaq.html", rUri.getPath());
		
		URI cUri = aUri.resolve(rUri);
		assertTrue(cUri.isAbsolute());
		assertFalse(cUri.isOpaque());
		assertEquals("file", cUri.getScheme());
		assertNull(cUri.getAuthority());
		assertEquals("/docs/guide/collections/designfaq.html", cUri.getPath());
	}
}
