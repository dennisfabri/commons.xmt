#Deserialization problem when evolve classes

Assume a Task class below with a prioritized field indicating whether it is a prioritized task:

```java
package example;

public class Task {
        public boolean prioritized;
}
```

With XStream, we can serialize object of this class to XML like below:

```java
import com.thoughtworks.xstream.XStream;

public class Test {
        public static void main(String args[]) {        
                Task task = new Task();
                task.prioritized = true;
                String xml = new XStream().toXML(task);
                saveXMLToFileOrDatabase(xml);
        }

        private static void saveXMLToFileOrDatabase(String xml) {
                // save XML to file or database here
        }
}
```

The resulting XML will be:

``` xml
<example.Task>
  <prioritized>true</prioritized>
</example.Task>
```

And you can deserialize the XML to get back task object:

```
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Test {
        public static void main(String args[]) {                
                String xml = readXMLFromFileOrDatabase();
                Task task = (Task) new XStream(new DomDriver()).fromXML(xml);
        }

        private static String readXMLFromFileOrDatabase() {
                // read XML from file or database here
        }
}
```

Everything is fine. Now we find that a prioritized flag is not enough, so we enhance the Task class to be able to distinguish between high priority, medium priority and low priority:

```java
package example;

public class Task {
	enum Priority {HIGH, MEDIUM, LOW}
	
	public Priority priority;
}
```

However deserialization of previously saved xml is no longer possible since the new Task class is not compatible with previous version.
How XMT address the problem

XMT comes to rescue: it introduces class VersionedDocument to version serialized XMLs and handle the migration. With XMT, serialization of task object can be written as:

```java
package example;
import com.pmease.commons.xmt.VersionedDocument;

public class Test {
        public static void main(String args[]) {
                Task task = new Task();
                task.prioritized = true;
                String xml = VersionedDocument.fromBean(task).toXML();
                saveXMLToFileOrDatabase(xml);
        }

        private static void saveXMLToFileOrDatabase(String xml) {
                // save XML to file or database here
        }

}
```

For task class of the old version, the resulting XML will be:

```xml
<example.Task version="0">
  <prioritized>true</prioritized>
</example.Task>
```

Compared with the XML generated previously with XStream, an additional attribute version is added to the root element indicating version of the XML. The value is set to 0 unless there are migration methods defined in the class as we will introduce below.

When Task class is evolved to use enum based priority field, we add a migrate method like below:

```java
package example;

import java.util.Stack;
import org.dom4j.Element;
import com.pmease.commons.xmt.VersionedDocument;

public class Task {
	enum Priority {HIGH, MEDIUM, LOW}
	
	public Priority priority;

	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
		Element element = dom.getRootElement().element("prioritized");
		element.setName("priority");
		if (element.getText().equals("true"))
			element.setText("HIGH");
		else
			element.setText("LOW");
	}
}
```

Migration methods need to be declared as a private method with name in form of migrateXXX, where XXX is a number indicating current version of the class. Here method migrate1 indicates that current version of the task class is of "1", and the method migrates the XML from version 0 to 1. The XML to be migrated is passed as a VersionedDocument object which implements dom4j Document interface and you may use dom4j to migrate it to be compatible with current version of the class. The versions parameter is used to handle the migration if class inheritance hierarchy is changed and will be introduced in [class hierarchy migration chapter](multitier-migration.html).
In this migration method, we read back the "prioritized" element of version 0, rename it as "priority", and set the value as "HIGH" if the task is originally a prioritized task; otherwise, set the value as "LOW".

With this migration method defined, you can now safely deserialize the task object from XML:

```java
package example;

import com.pmease.commons.xmt.VersionedDocument;

public class Test {
	public static void main(String args[]) {
		String xml = readXMLFromFileOrDatabase();
		Task task = (Task) VersionedDocument.fromXML(xml).toBean();
	}

        private static String readXMLFromFileOrDatabase() {
                // read XML from file or database here
        }

}
```

The deserialization works not only for XML of the old version, but also for XML of the new version. At deserialization time, XMT compares version of the XML (recorded in the version attribute as we mentioned earlier) with current version of the class (maximum suffix number of various migrate methods), and run applicable migrate methods one by one. In this case, if a XML of version "0" is read, method migrate1 will be called; if a XML of version "1" is read, no migration methods will be called since it is already up to date.

As class keeps evolving, more migration methods can be added to the class by increasing suffix number of latest migration method. For example, let's further enhance our task class so that the priority field is taking a numeric value ranging from 1 to 10. We add another migrate method to the Task class to embrace the change:

```java
@SuppressWarnings("unused")
private void migrate2(VersionedDocument dom, Stack<Integer> versions) {
	Element element = dom.getRootElement().element("priority");
	if (element.getText().equals("HIGH"))
		element.setText("10");
	else if (element.getText().equals("MEDIUM"))
		element.setText("5");
        else 
                element.setText("1");
}
```

This method only handles the migration from version "1" to version "2", and we do not need to care about version "0" any more, since XML of version "0" will first be migrated to version "1" by calling method migrate1 before running this method.
With this change, you will be able to deserialize the task object from XML of current version and any previous versions.

This simple tutorial gives you the idea of how to migrate field change of classes. XMT can handle much complicated scenarios, such as migrating data defined in multiple tiers of class hierarchy, addressing class hierarchy change, etc. These topics are covered in other chapters.

***When write migration methods, it will be convenient to examine XML of previous version and current version at the same time. To get XML of current version, just define an object of your current class, filling necessary fields, and call VersionedDocument.fromBean(obj).toXML()***