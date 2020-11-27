You may evolve your sub class to take a new name. In this case, you will need to tell XMT of the new class when doing the deserialization:

```
package example;

import com.pmease.commons.xmt.VersionedDocument;

public class Test {
	public static void main(String args[]) {
		String xml = readXMLFromFileOrDatabase();
		NewTask task = (NewTask) VersionedDocument.fromXML(xml).toBean(NewTask.class);
	}

        private static String readXMLFromFileOrDatabase() {
                // read XML from file or database here
        }

}
```

XMT will explore hierarchy of specified class and call various migration methods as necessary to do the migration.