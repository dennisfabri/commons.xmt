package com.pmease.commons.xmt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class Util {
	public static String readXML(String fileName) {
		InputStream is = null;
		StringBuffer buffer = new StringBuffer();
		try {
			is = Util.class.getResourceAsStream("/com/pmease/commons/xmt/xml/" + fileName);
			Reader in = new InputStreamReader(is, "UTF8");
			int c;
			while ((c = in.read()) != -1)
				buffer.append((char)c);
			return buffer.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}
}
