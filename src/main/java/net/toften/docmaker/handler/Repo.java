package net.toften.docmaker.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.xml.sax.SAXException;

public class Repo {
	private URI repoURI;
	private String id;

	public Repo(String id, URI baseURI, String repoURIPath) throws Exception {
		this.id = id;
		this.repoURI = new URI(repoURIPath);
		
		// If the path to the repo is not absolute,
		// append it to the baseURI
		if (!repoURI.isAbsolute()) {
			if (baseURI != null) {
				repoURI = baseURI.resolve(repoURI);
			} else {
				throw new SAXException("Repo URI " + repoURI.toString() + " is not absolute, given " + repoURIPath + " AND baseURI is null");
			}
		}

		if (!repoURI.isAbsolute()) {
			throw new SAXException("Repo URI " + repoURI.toString() + " is not absolute, given " + repoURIPath);
		}

		if (repoURI.getAuthority() != null) {
			throw new SAXException("Repo URI " + repoURI.toString() + " has an authority (" + repoURI.getAuthority() + "), given " + repoURIPath);
		}
	}
	
	public InputStream getFragmentInputStream(String fragmentName) throws IOException, URISyntaxException {
		URI markupFilenameURI = new URI(fragmentName);
		URL fileURL = repoURI.resolve(markupFilenameURI).toURL();
		
		return fileURL.openStream();
	}

	public String getId() {
		return id;
	}

	public URI getURI() {
		return repoURI;
	}
}
