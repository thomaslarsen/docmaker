package net.toften.docmaker.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import net.toften.docmaker.DocMakerException;

/**
 * This class represents a repository of fragment files.
 * <p>
 * It is created from the <repo> element in the TOC XML file.
 * 
 * @author thomaslarsen
 *
 */
public class Repo {
	private URI repoURI;
	private String id;

	/**
	 * If the path to the repo is releative, it will be added to the baseURI
	 * 
	 * @param id the id attribute of the repo element
	 * @param baseURI the base URI specified to the {@link AssemblyHandler#parse(net.toften.docmaker.LogWrapper, InputStream, String, String, URI, java.util.Map, java.util.Properties, java.util.List) parser}
	 * @param repoURIPath the path to the repo as specified in the repo attribute. Can be absolute or releative
	 * @throws URISyntaxException 
	 * @throws DocMakerException
	 */
	public Repo(String id, URI baseURI, String repoURIPath) throws DocMakerException, URISyntaxException {
		this.id = id;
		this.repoURI = new URI(repoURIPath);
		
		// If the path to the repo is not absolute, append it to the baseURI
		if (!repoURI.isAbsolute()) {
			if (baseURI != null) {
				repoURI = baseURI.resolve(repoURI);
			} else {
				throw new DocMakerException("Repo URI " + repoURI.toString() + " is not absolute, given " + repoURIPath + " AND baseURI is null");
			}
		}

		if (!repoURI.isAbsolute()) {
			throw new DocMakerException("Repo URI " + repoURI.toString() + " is not absolute, given " + repoURIPath);
		}
	}

	public String getId() {
		return id;
	}

	public URI getURI() {
		return repoURI;
	}
	
	/**
	 * Get the {@link URI} of a fragment.
	 * 
	 * @param fragmentName the relative path of the fragment file
	 * @return
	 * @throws URISyntaxException
	 */
	public URI getFragmentURI(String fragmentName) throws URISyntaxException {
		return getURI().resolve(fragmentName).normalize();
	}
	
	/**
	 * Get an {@link InputStream} to a fragment.
	 * 
	 * @param fragmentName the relative path of the fragment file
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public InputStream getFragmentInputStream(String fragmentName) throws IOException, URISyntaxException {
		return getFragmentURI(fragmentName).toURL().openStream();
	}
}
