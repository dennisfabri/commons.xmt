package com.pmease.commons.xmt.bean;

import java.util.Stack;

import org.dom4j.Element;

import com.pmease.commons.xmt.VersionedDocument;

public class Bean6 extends Bean3 {
	private int priority;

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
		Element element = dom.getRootElement().element("prioritized");
		element.setName("priority");
		if (element.getText().equals("true"))
			element.setText("10");
		else
			element.setText("1");
	}
}
