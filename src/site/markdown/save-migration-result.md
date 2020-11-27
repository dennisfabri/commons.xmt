You may save back the migrated XML to avoid migration when it is loaded next time. This is optional but can improve the performance. This can be achieved with migration listener as demonstrated below:

```
package example;

import com.pmease.commons.xmt.MigrationListener;
import com.pmease.commons.xmt.VersionedDocument;

public class Test {
	public static void main(String args[]) {
		String xml = readXMLFromFileOrDatabase();
		Task task = (Task) VersionedDocument.fromXML(xml).toBean(new MigrationListener() {

			public void migrated(Object bean) {
				saveXMLToFileOrDatabase(VersionedDocument.fromBean(bean).toXML());
			}
			
		});
	}
	
	private static String readXMLFromFileOrDatabase() {
		// read xml from file or database here
	}
	
	private static void saveXMLToFileOrDatabase(String xml) {
		// save xml to file or database here
	}
	
}
```
