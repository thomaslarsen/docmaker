package net.toften.docmaker;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

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
	public void testAssembledURI() throws URISyntaxException {
		String scheme = "file";
		String absURI = "/docs/designfaq.html";
		
		URI uri = new URI(scheme, absURI, null);
		assertTrue(uri.isAbsolute());
		assertFalse(uri.isOpaque());
		assertEquals(scheme, uri.getScheme());
		assertNull(uri.getAuthority());
		assertEquals(absURI, uri.getPath());
	}
	
	@Test
	public void testWindowsAbsoluteURI() throws URISyntaxException, UnsupportedEncodingException {
		String winURI = "/c:\\Documents\\File\\";
		
		String absURI = URLEncoder.encode(winURI, "UTF-8");
		
		URI uri = new URI("file:///" + absURI);
		assertTrue(uri.isAbsolute());
		assertFalse(uri.isOpaque());
		assertEquals("file", uri.getScheme());
		assertNull(uri.getAuthority());
		assertEquals("/" + winURI, uri.getPath());
	}

	@Test
	public void testWindowsAssembledURI() throws URISyntaxException {
		String scheme = "file";
		String winURI = "/c:\\Documents\\File\\";
		
		URI uri = new URI(scheme, winURI, null);
		assertTrue(uri.isAbsolute());
		assertFalse(uri.isOpaque());
		assertEquals(scheme, uri.getScheme());
		assertNull(uri.getAuthority());
		assertEquals(winURI, uri.getPath());
	}

	@Test
	public void testCompoundWindowsURI() throws URISyntaxException, UnsupportedEncodingException {
		String scheme = "file";
		String winURI = "/c:\\Documents\\File\\";
		String relURI = "collections/designfaq.html";

		// Convert "\" in URI to "/" to support Windows paths
		winURI = winURI.replace('\\', '/');
		
		URI aUri = new URI(scheme, winURI, null);
		//URI aUri = new URI(scheme, URLEncoder.encode(winURI, "UTF-8"), null);
		URI rUri = new URI(relURI);
		//URI rUri = new URI(URLEncoder.encode(relURI, "UTF-8"));
		assertEquals(winURI, aUri.getPath());
		assertEquals(relURI, rUri.getPath());

		URI cUri = checkWindowsCompound(scheme, aUri, rUri);
		assertEquals(winURI + relURI, cUri.getPath());
	}

	@Test
	public void testCompoundWindowsURI2() throws URISyntaxException, UnsupportedEncodingException {
		String scheme = "file";
		String winURI = "/c:\\Documents\\File\\";
		String relURI = "collections/designfaq.html";

		// Convert "\" in URI to "/" to support Windows paths
		winURI = winURI.replace('\\', '/');
		
		URI aUri = new URI(scheme + "://" + winURI);
		//URI aUri = new URI(scheme, URLEncoder.encode(winURI, "UTF-8"), null);
		URI rUri = new URI(relURI);
		//URI rUri = new URI(URLEncoder.encode(relURI, "UTF-8"));
		assertEquals(winURI, aUri.getPath());
		assertEquals(relURI, rUri.getPath());

		URI cUri = checkWindowsCompound(scheme, aUri, rUri);
		
		assertEquals(winURI + relURI, cUri.getPath());
	}

	private URI checkWindowsCompound(String scheme, URI aUri, URI rUri) {
		assertTrue(aUri.isAbsolute());
		assertFalse(aUri.isOpaque());
		assertNull(aUri.getAuthority());
		assertEquals(scheme, aUri.getScheme());
		assertNull(aUri.getFragment());
		
		assertFalse(rUri.isAbsolute());
		assertFalse(rUri.isOpaque());
		assertNull(rUri.getAuthority());
		assertNull(rUri.getScheme());
		assertNull(rUri.getFragment());
		
		/*
		 * 	Otherwise this method constructs a new hierarchical URI in a manner consistent with RFC 2396, section 5.2; that is: 
		 * 
			*	A new URI is constructed with this URI's scheme and the given URI's query and fragment components. 
			*	If the given URI has an authority component then the new URI's authority and path are taken from the given URI. 

			*	Otherwise the new URI's authority component is copied from this URI, and its path is computed as follows: 
					* 	If the given URI's path is absolute then the new URI's path is taken from the given URI. 
					*	Otherwise the given URI's path is relative, and so the new URI's path is computed by resolving 
						the path of the given URI against the path of this URI. 
						This is done by concatenating all but the last segment of this URI's path, if any, with 
						the given URI's path and then normalizing the result as if by invoking the normalize method. 
		 */
		URI cUri = aUri.resolve(rUri);
		assertTrue(cUri.isAbsolute());
		assertFalse(cUri.isOpaque());
		assertEquals(scheme, cUri.getScheme());
		assertNull(cUri.getFragment());
		assertNull(cUri.getAuthority());
		return cUri;
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

	@Test
	public void testRegex() {
		String regexString = "[.][^.]+$";
		
		assertEquals("test", "test.xml".replaceFirst(regexString, ""));
		assertEquals("test", "test".replaceFirst(regexString, ""));
		assertEquals("test.2", "test.2.xml".replaceFirst(regexString, ""));
	}
	
	@Test
	public void testLocalDir() {
		URI hd = new File("").toURI();
		System.out.println(hd.toString());
		assertTrue(hd.isAbsolute());
	}

	@Test
	public void testLocalDirFromProperty() throws URISyntaxException {
		String hd = System.getProperty("user.dir");
		System.out.println(hd);
		URI hdu = new URI("file", hd, null);
		System.out.println(hdu.toString());
		assertTrue(hdu.isAbsolute());
	}
}
