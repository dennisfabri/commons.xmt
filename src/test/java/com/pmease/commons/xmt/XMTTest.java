package com.pmease.commons.xmt;

import static com.pmease.commons.xmt.Util.readXML;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.pmease.commons.xmt.bean.Bean1;
import com.pmease.commons.xmt.bean.Bean2;
import com.pmease.commons.xmt.bean.Bean3;
import com.pmease.commons.xmt.bean.Bean4;
import com.pmease.commons.xmt.bean.Bean5;
import com.pmease.commons.xmt.bean.Bean7;
import com.pmease.commons.xmt.bean.CompileTask;

public class XMTTest {
	
	@Ignore
	@Test
	public void testMigration() {
		Bean1 bean = (Bean1) VersionedDocument.fromXML(readXML("bean1.xml")).toBean();
		assertEquals(bean.getPriority(), 10);
	}
	
	@Ignore
	@Test
	public void testClassRenamedMigration() {
		Bean2 bean = (Bean2) VersionedDocument.fromXML(readXML("bean1.xml")).toBean(Bean2.class);
		assertEquals(bean.getPriority(), 10);
	}
	
	@Test
	public void testCompositeVersion() {
		assertEquals(MigrationHelper.getVersion(Bean3.class), "1.3");
	}

	@Test
	public void testNoVersion() {
		assertEquals(MigrationHelper.getVersion(Bean4.class), "0");
	}

	@Ignore
	@Test
	public void testCompositeMigration() {
		Bean5 bean = (Bean5) VersionedDocument.fromXML(readXML("bean2.xml")).toBean();
		assertEquals(bean.getPriority(), 10);
		assertEquals(bean.getFirstName(), "Robin");
	}

	@Ignore
	@Test
	public void testAddClass() {
		Bean7 bean = (Bean7) VersionedDocument.fromXML(readXML("bean2.xml")).toBean(Bean7.class);
		assertEquals(bean.getPriority(), 10);
		assertEquals(bean.getFirstName(), "Robin");
		assertEquals(bean.getAge(), 34);
	}
	
	@Ignore
	@Test
	public void testRemoveClass() {
		CompileTask task = (CompileTask) VersionedDocument.fromXML(readXML("task.xml")).toBean();
		assertEquals(task.priority, 10);
		assertEquals(task.destDir, "classes");
		assertEquals(task.options, "-debug");
		assertEquals(task.srcFiles.size(), 2);
	}
}
