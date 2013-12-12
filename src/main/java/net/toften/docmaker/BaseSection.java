package net.toften.docmaker;

public abstract class BaseSection {
	private String sectionName;
	private boolean isRotated;

	public BaseSection(String sectionName, boolean isRotated) {
		this.sectionName = sectionName;
	}
	
	public String getSectionName() {
		return sectionName;
	}

	public boolean isRotated() {
		return isRotated;
	}
	
	public String getDivOpenTag(AssemblyHandler handler) {
		String classAttr = getDivClassName() + (isRotated() ? " rotate" : "");
		
		return constructDivOpenTag(classAttr, getIdAttr(handler), getSectionName());
	}
	
	public String getIdAttr(AssemblyHandler handler) {
		return (handler.getTocFileName() + "-" + getSectionName()).trim().toLowerCase().replaceAll("[ _]",  "-").replaceAll("[^\\dA-Za-z\\-]", "");
	}
	
	protected abstract String getDivClassName();

	protected String constructDivOpenTag(String divClass, String divId, String divTitle) {
		return "<div class=\"" + divClass + "\" id=\"" + divId + "\" title=\"" + divTitle + "\">";
	}
}
