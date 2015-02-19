package net.toften.docmaker.markup.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.markup.MarkupProcessor;
import au.com.bytecode.opencsv.CSVReader;

/**
 * The {@link MarkupProcessor} will process CSV (comma-separated-value) files.
 * <p>
 * The processor can convert the contents of the CSV files into:
 * <ul>
 * <li>Tables</li>
 * <li>Hierarchy of headers</li>
 * </ul>
 * 
 * It will also allow for showing only certain columns and for providing filters to hide specific rows.
 * 
 * <h2>Configuration</h2>
 * 
 * The CSV processor is configured through the <code>config</code> attribute on the <code>chapter</code> element
 * in the TOC.
 * 
 * The config elements has a number of elements separated by <code>;</code>, and each element might be further
 * separated by a <code>:</code>, for example into key/value pairs.
 * If an element contains multiple values, they must be separated by <code>,</code>
 * The elements must be specified in order.
 * <p>
 * <p>
 * The following elements can be provided:
 * <ol>
 * <li>Output type: <b>t</b>: tables; <b>h</b>: hierarchy of headers. Defaults to <b>t</b></li>
 * <li>Top row skip. Specifies the number of rows to ignore in the beginning of the file</li>
 * <li>Bottom row skip. Specifies the number of rows to ignore at the end of the file</li>
 * <li>List of headings to include (in the order they should appear). For <code>h</code> output type, the 
 * heading level can be provided as the second element, for example <code>Heading:2</code>. 
 * If it is not provided, the contents will be included
 * in a <code>p</code> section</li>
 * <li>List of filters. See below</li>
 *  
 * <h3>Filters</h3>
 * Filters are declared as a Regex, which will be applied to a cell specified by a heading name.
 * Filters will be applied in an <i>and</i> manner, i.e. all filters must be true for a row to
 * be included.
 * <p>
 * The syntax is: <code>heading:filter</code>, for example:
 * <p>
 * The filter <code>Heading:.*Platform.*</code> will apply the Regex <code>.*Platform.*</code> to the contents
 * of the column <code>Heading</code>, and only include the row if the pattern matches.
 *
 * <h3>Example</h3>
 * <code>h;3;2;Summary:2,Description;Component/s:.*Platform.*</code>
 * <ul>
 * <li>Output type is a hierachy of <b>headings</b></li>
 * <li>The first 3 rows will be skipped</li>
 * <li>The last 2 rows will be ignored</li>
 * <li>The contents of the <i>Summary</i> column will be inserted as a level 2 heading</li>
 * <li>The contents of the <i>Description</i> column will be inserted as text</li>
 * <li>Only rows where the contents of the <i>Component/s</i> column matches the 
 * <code>.*Platform.*</code> Regex will be included</li>
 * </ul>
 */
public class CsvProcessor implements MarkupProcessor {
	@Override
	public String process(File inFile, String config, AssemblyHandler handler) throws IOException {
		return process(new CSVReader(new FileReader(inFile)), config, handler);
	}
	
	@Override
	public String process(InputStream is, String config, AssemblyHandler handler) throws IOException {
		return process(new CSVReader(new InputStreamReader(is)), config, handler);
	}

	@Override
	public String process(String inString, String config, AssemblyHandler handler) throws IOException {
		return process(new CSVReader(new StringReader(inString)), config, handler);
	}
	
	public String process(CSVReader reader, String config, AssemblyHandler handler) throws IOException {
		
		/*
		 * Extract config
		 * 
		 * Example:
		 * 0: ,,
		 * 1: ,,
		 * 2: Key,Heading,Value
		 * 3: 1,Stuff,Gold
		 * 4: 2,Piles,Silver
		 * 5: ,,
		 * 
		 * Size: 6
		 */
		int startSkip = 0;
		int endSkip = 0;
		String format = "t";
		List<String> columns = null;
		Map<String, Pattern> filters = null;
		String textProcessor = null;
		
		if (config != null) {
			String[] settings = config.split(";");
			
			if (settings.length > 0) {
				format = settings[0];
				if (settings.length > 1) {
					startSkip = Integer.parseInt(settings[1]);
					if (settings.length > 2) {
						endSkip = Integer.parseInt(settings[2]);
						if (settings.length > 3) {
							textProcessor = settings[3];
							if (settings.length > 4) {
								columns = new ArrayList<String>(Arrays.asList(settings[4].split("\\s*,\\s*")));
								if (settings.length > 5) {
									// Filters
									filters = new HashMap<String, Pattern>();
									for (String f : settings[5].split("\\s*,\\s*")) {
										String[] colRegex = f.split("\\s*:\\s*");
										filters.put(colRegex[0], Pattern.compile(colRegex[1]));
									}
								}
							}
						}
					}
				}
			}
		}
		
		MarkupProcessor mp = null;
		if (textProcessor != null && !textProcessor.equals("")) {
			mp = handler.getMarkupProcessor(textProcessor);
		}
		
        List<String[]> contents = reader.readAll();

		StringBuffer asHtml = new StringBuffer().append(format.equals("t") ? "<table>" : "");
		List<Integer> headerColumnIndex = new LinkedList<Integer>();
		List<Integer> headerLevelIndex = new LinkedList<Integer>();
		Map<Integer, Pattern> filterColumnIndex = new HashMap<Integer, Pattern>();
		
	    int lineCount = startSkip;
		while (lineCount <= contents.size() - endSkip) {
			String[] currentLine = contents.get(lineCount);

	        if (lineCount == startSkip) { // We are at the header line
		        List<String> headersAsList = Arrays.asList(currentLine);
		        // Find the header columns to include
	        	if (columns != null) {
		        	for (String columnName : columns) {
		        		String[] headerInfo = columnName.split("\\s*:\\s*");
		        		int index = headersAsList.indexOf(headerInfo[0]);
		        		if (index > 0) {
		        			headerColumnIndex.add(index);
		        			headerLevelIndex.add(headerInfo.length < 2 ? -1 : Integer.parseInt(headerInfo[1]));
		        		}
					}
	        	} else {
	        		// If we haven't provided a list of columns, add them all
	        		for (int i = 0; i < currentLine.length; i++) {
	        			headerColumnIndex.add(i);
		        		headerLevelIndex.add(-1);
	        		}
	        	}
	        	
	        	// Find the filter header columns
	        	if (filters != null) {
		        	for (String columnName : filters.keySet()) {
		        		int index = headersAsList.indexOf(columnName);
		        		if (index > 0)
		        			filterColumnIndex.put(index, filters.get(columnName));
					}
	        	}
	        	
	        	if (format.equals("t")) {
		        	// Write column headers
			        asHtml.append("<thead>");
			        asHtml.append("<tr>");
			        
		        	for (Integer c : headerColumnIndex) {
						asHtml.append("<th>" + currentLine[c] + "</th>");
					}
		        	
			        asHtml.append("</tr>");
			        asHtml.append("</thead>");
			        asHtml.append("<tbody>");
	        	}
	        } else {
	        	boolean include = true;
	        	// Check filters
	        	for (Entry<Integer, Pattern> f : filterColumnIndex.entrySet()) {
					if (!f.getValue().matcher(currentLine[f.getKey()]).matches()) {
						include = false;
						break;
					}
				}
	        	
	        	if (include) {
		        	if (format.equals("t")) {
		        		asHtml.append("<tr>");
				        
			        	for (Integer c : headerColumnIndex) {
			        		if (c < currentLine.length)
			        			asHtml.append("<td>" + processMarkup(currentLine[c], mp, config, handler) + "</td>");
			        		else
			        			asHtml.append("<td></td>");
						}
			        	
				        asHtml.append("</tr>");
		        	} else if (format.equals("h")) {
		        		for (int i = 0; i < headerColumnIndex.size(); i++) {
		        			if (headerLevelIndex.get(i) > 0) {
		        				asHtml.append("<h").append(headerLevelIndex.get(i)).append(">");
		        				asHtml.append(currentLine[headerColumnIndex.get(i)]);
		        				asHtml.append("</h").append(headerLevelIndex.get(i)).append(">");
		        			} else {
		        				asHtml.append("<p>");
		        				asHtml.append(processMarkup(currentLine[headerColumnIndex.get(i)], mp, config, handler));
		        				asHtml.append("</p>");
		        			}
		        		}
		        	}
	        	}
	        }
	        
	        lineCount++;
	    }
		asHtml.append(format.equals("t") ? "</tbody></table>" : "");
	    reader.close();
	    
	    return asHtml.toString();
	}
	
	private String processMarkup(String markup, MarkupProcessor mp, String config, AssemblyHandler handler) throws IOException {
		return mp == null ? markup : mp.process(markup, config, handler);
	}

	@Override
	public String getFileExtension() {
		return "cvs";
	}

	@Override
	public void setEncoding(final String encodingString) {
	}
}
