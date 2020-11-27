package com.pmease.commons.xmt.bean;

import java.util.Stack;

import com.pmease.commons.xmt.VersionedDocument;

public class Bean3 extends Bean1 {
	
	private int age;
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
	}
	
	@SuppressWarnings("unused")
	private void migrate3(VersionedDocument dom, Stack<Integer> versions) {
	}
}
