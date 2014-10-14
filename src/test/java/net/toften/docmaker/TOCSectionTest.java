package net.toften.docmaker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import static org.junit.Assert.*;

public class TOCSectionTest {
	private DefaultAssemblyHandler ah;
	
	private String currentTocName = "toc";
	private String currentFragment = "<h1>Heading</h1><p>Hello</p>";
	
	private static final String END_DIV = "</div>";
	
	String[][][] testAttributesData = {
			{	// 0
				{ "title", "test" },
				{ "level", "2" }
			},
			{	// 1
				{ "title", "test" }
			},
			{	// 2
				{ "id", "main-css" },
				{ "url", "http://homepage" }
			},
			{	// 3
				{ "key", "author" },
				{ "value", "james" }
			},
			{	// 4
				{ "fragment", "intro" },
				{ "repo", "main" },
				{ "level", String.valueOf(2 + DefaultAssemblyHandler.EFFECTIVE_LEVEL_ADJUSTMENT) }
			},
			{	// 5 - relative repo URI
				{ "id", "main" },
				{ "uri", "./src" }
			},
			{	// 6 - absolute repo URI
				{ "id", "main" },
				{ "uri", "file:///src" }
			},
			{	// 7
				{ "key", "author" }
			}
	};
	
	String [][] expectedResultData = {
			{	// 0
				"<div class=\"section-header\" id=\"toc-test\" title=\"test\">" 
			},
			{	// 1
				"<div class=\"metadata\">",
				END_DIV
			},
			{	// 2 
				"<div class=\"meta-section\" id=\"toc-test\" title=\"test\">" 
			},
			{	// 3
				"<link",
				" id=\"main-css\"",
				" url=\"http://homepage\"",
				"/>"
			},
			{	// 4
				"<div class=\"metadata\">",
				"<div class=\"meta\" key=\"author\">",
				"james",
				END_DIV,
				END_DIV
			},
			{	// 5
				"<title>test</title>"
			},
			{	// 6
//				"<div class=\"chapter\" id=\"toc-test-intro\" title=\"intro\">",
				"<h5 id=\"toc-test-intro-heading\">Heading</h5><p>Hello</p>",
				END_DIV
			},
			{	// 7
				"<div key=\"author\">",
				"james",
				END_DIV
			}
	};

	@Before
	public void setupAssemblyHandler() {
		ah = new DefaultAssemblyHandler() {
			
			@Override
			public String getTocFileName() {
				return currentTocName;
			}
			
			@Override
			protected String getFragmentAsHTML(String repoName, 
					String fragmentName, int chapterLevelOffset, String config)
					throws IOException, URISyntaxException {
				return currentFragment;
			}
		};
	}
	
	private Attributes runTest(final Integer dataSet, final Integer resultSet) {
		
		ah.setCurrentFileHandler(new DefaultAssemblyHandler.GenericFileHandler() {
			int resultIndex = 0;
			
			@Override
			public void writeToOutputFile(String text) throws IOException {
				if (resultSet != null && resultIndex < expectedResultData[resultSet].length)
					assertEquals(expectedResultData[resultSet][resultIndex++], text);
				else
					fail("Unexpected file write: " + text);
			}
		});
		
		return new AttributesImpl() {
			@Override
			public String getValue(String qName) {
				if (dataSet != null && dataSet >= 0) {
					for (int i = 0; i < getLength(); i++) {
						if (testAttributesData[dataSet][i][0].equals(qName))
							return testAttributesData[dataSet][i][1];
					}
				}
				
				return null;
			}
			
			@Override
			public String getValue(int index) {
				if (dataSet != null && dataSet >= 0) {
					return testAttributesData[dataSet][index][1];
				}
				
				return null;
			}
			
			@Override
			public String getQName(int index) {

				if (dataSet != null && dataSet >= 0) {
					return testAttributesData[dataSet][index][0];
				}
				
				return null;
			}
			
			@Override
			public int getLength() {
				return testAttributesData[dataSet].length;
			}
		};
	}
	
	@Test
	public void testSectionWithLevel() throws IOException, SAXException {
		ah.handleSectionElement(runTest(0, 0));
		
		assertEquals(2, ah.getCurrentSectionLevel().intValue());
		assertEquals("test", ah.getCurrentSectionName());
	}
	
	@Test (expected = SAXException.class)
	public void testSectionWithoutLevel() throws IOException, SAXException {
		ah.handleSectionElement(runTest(1, 2));
		
		assertEquals(null, ah.getCurrentSectionLevel());
		assertEquals("test", ah.getCurrentSectionName());
	}

	@Test
	public void testMetaSectionWithoutLevel() throws IOException, SAXException {
		ah.handleMetaSectionElement(runTest(1, 2));
		
		assertEquals(null, ah.getCurrentSectionLevel());
		assertEquals("test", ah.getCurrentSectionName());
	}

	@Test
	public void testMetaSectionWithLevel() throws IOException, SAXException {
		ah.handleMetaSectionElement(runTest(0, 2));
		
		assertEquals(null, ah.getCurrentSectionLevel());
		assertEquals("test", ah.getCurrentSectionName());
	}
	
	@Test
	public void testMetaElement() throws IOException, SAXException {
		String metaName = "link";
		
		ah.handleMetaElement(metaName, runTest(2, 3));
	}
	
	@Test
	public void testPropertyElement() throws IOException, SAXException {
		ah.handlePropertyElement(runTest(3, null));
		
		assertTrue(ah.metaData.containsKey("author"));
		assertEquals("james", ah.metaData.get("author"));
		assertEquals(1, ah.metaData.size());
	}

	@Test
	public void testElementElement() throws IOException, SAXException {
		testPropertyElement(); // Inject some metadata
		
		ah.handleElementElement(runTest(7, 7));
	}
	
	@Test
	public void testSectionsWithNoProperties() throws IOException, SAXException {
		ah.handleSectionsElement(runTest(null, 1));
		
		assertEquals(null, ah.getCurrentSectionLevel());
		assertEquals(null, ah.getCurrentSectionName());
	}

	@Test
	public void testSectionsWithProperties() throws IOException, SAXException {
		testPropertyElement(); // Inject a property
		
		ah.handleSectionsElement(runTest(null, 4));
		
		assertEquals(null, ah.getCurrentSectionLevel());
		assertEquals(null, ah.getCurrentSectionName());
	}
	
	@Test
	public void testHeaderElementNoCSS() throws IOException, SAXException {
		ah.handleHeaderElement(runTest(1, 5));
		
		assertTrue(ah.metaData.containsKey("title"));
		assertEquals("test", ah.metaData.get("title"));
		assertEquals(1, ah.metaData.size());
		
		assertEquals("test", ah.getDocumentTitle());
		
		assertEquals(null, ah.getCurrentSectionLevel());
		assertEquals(null, ah.getCurrentSectionName());
	}
	
	@Test ( expected = SAXException.class )
	public void testRelativeRepoWithNoBase() throws URISyntaxException, SAXException {
		ah.handleRepoElement(runTest(5, null));
	}
	
	@Test
	public void testAbsoluteRepoWithNoBase() throws URISyntaxException, SAXException {
		ah.handleRepoElement(runTest(6, null));
		
		assertTrue(ah.repos.containsKey("main"));
		assertEquals(new URI("file:///src"), ah.repos.get("main"));

		assertEquals(1, ah.repos.size());
	}
	
	@Test ( expected = SAXException.class )
	public void testChapterWithWrongRepo() throws URISyntaxException, SAXException, IOException {
		ah.handleChapterElement(runTest(4, null));
	}
	
	@Test
	public void testChapterWithLevel() throws URISyntaxException, SAXException, IOException {
		testAbsoluteRepoWithNoBase();
		testSectionsWithNoProperties();
		testSectionWithLevel(); // current section: test / level: 2
		
		ah.handleChapterElement(runTest(4, 6)); // current repo: main / level: 3
		assertEquals("main", ah.getCurrentRepoName());
		assertEquals("intro", ah.getCurrentFragmentName());
	}
}
