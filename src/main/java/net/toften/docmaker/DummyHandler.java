package net.toften.docmaker;

import java.util.List;

import net.toften.docmaker.handler.AssemblyHandlerAdapter;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;

public class DummyHandler extends AssemblyHandlerAdapter {

	@Override
	public String getCurrentFragmentName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Repo getCurrentRepo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeneratedSection> getHeaderSections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Section> getSections() {
		// TODO Auto-generated method stub
		return null;
	}

}
