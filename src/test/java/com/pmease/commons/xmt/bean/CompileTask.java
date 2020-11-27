package com.pmease.commons.xmt.bean;

import java.util.List;
import java.util.Stack;

import com.pmease.commons.xmt.VersionedDocument;

public class CompileTask extends AbstractCompileTask {
	public List<String> srcFiles;

	public String destDir = "classes";

	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
		dom.getRootElement().addElement("destDir").setText("classes");
	}

	@SuppressWarnings("unused")
	private void migrate2(VersionedDocument dom, Stack<Integer> versions) {
		versions.push(0);
		dom.getRootElement().addElement("options").setText("-debug");
	}
}