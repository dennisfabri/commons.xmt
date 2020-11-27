#Migrate super class and sub class separately

Continue with version "1" of the Task class defined in Getting Started chapter:

```
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

Now we have class CompileTask subclassing from Task class as below:

```
package example;

import java.util.List;

public class CompileTask extends Task {
        public List<String> srcFiles;
}
```

Instance of CompileTask can be serialized to XML with XMT as below:

```
package example;

import java.util.ArrayList;

import com.pmease.commons.xmt.VersionedDocument;

public class Test {
	public static void main(String args[]) {
		CompileTask task = new CompileTask();
		task.priority = Task.Priority.HIGH;
		task.srcFiles = new ArrayList<String>();
		task.srcFiles.add("Class1.java");
		task.srcFiles.add("Class2.java");
		String xml = VersionedDocument.fromBean(task).toXML();
                saveXMLToFileOrDatabase(xml);
	}

        private static void saveXMLToFileOrDatabase(String xml) {
                // save XML to file or database here
        }
}
```

The resulting XML will be:

```
<example.CompileTask version="1.0">
  <priority>HIGH</priority>
  <srcFiles>
    <string>Class1.java</string>
    <string>Class2.java</string>
  </srcFiles>
</example.CompileTask>
```

Pay attention to version attribute of the root element: XMT examines the class hierarchy (except for class java.lang.Object) to get current version of each class, and concatenates them with period. Since class Task is of version "1" and class CompileTask is of version "0", the resulting version of the hierarchy (or composite version) is "1.0".
When deserializing the compile task object from XML, XMT splits this composite version to get XML version for each class in the hierarchy, and repeats the process described in Getting Started chapter for each of these classes. So if class Task is evolved to use numeric priority field, we simply add migrate methods in Task class, while keep CompileTask class intacted. On the other hand, if we evolve class CompileTask to include a destDir field, we can define the migrate method in CompileTask like below, while keep class Task intacted:

```
package example;

import java.util.List;
import java.util.Stack;
import com.pmease.commons.xmt.VersionedDocument;

public class CompileTask extends Task {
        public List<String> srcFiles;
        
        public String destDir = "classes";
        
	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
		dom.getRootElement().addElement("destDir").setText("classes");
	}
}
```

This separation of concerns is very important since there might exist many sub classes, and you certainly do not want to modify those sub classes if super class is evolved, and vice versa.
Address class hierarchy change problem

Now we introduce class AbstractCompileTask in the middle of Task and CompileTask like below:

```
package example;

public abstract class AbstractCompileTask extends Task {
        public String options = "-debug";
}

package example;

import java.util.List;
import java.util.Stack;

public class CompileTask extends AbstractCompileTask {
        public List<String> srcFiles;
        
        public String destDir = "classes";
        
	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
		dom.getRootElement().addElement("destDir").setText("classes");
	}
}
```

When deserialize from aforementioned "1.0" XML, XMT does the following:

1. Split "1.0" into a versions stack containing element "0" and "1" from top to bottom.
2. Set current class to be CompileTask.
3. Pop up top element "0" from versions stack, compare it with current version of current class and invoke migration methods as necessary. This results in invocation of method migrate1 of class CompileTask, with current versions stack passed as param versions.
4. Set current class to super class of CompileTask, which is now AbstractCompileTask.
5. Pop up top element "1" from versions stack, and compare it with current version of current class. There is a class mis-match at this point: version "1" recorded in XML is derived from class Task, while current class is AbstractCompileTask.

This issue can be solved by introducing another migrate method into class CompileTask:

```
package example;

import java.util.List;
import java.util.Stack;

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
```

This new migrate method pushes version "0" (current version of class AbstractCompileTask) into versions stack. The stack now contains element "0" and "1" from top to bottom, which can be used to handle data migration of class AbstractCompileTask and Task correctly. It also adds "options" element so that compile task objects deserialized from XML of old versions has the default compile options value of "-debug".

Now that we've successfully handled the case of adding new class into the hierarchy, but how about removing an existing class? Let's assume that class Task is removed and class AbstractCompileTask needs to take care of the priority field. In order to deserialize from XML of old versions, we write AbstractCompileTask as below (assume class Task is at version "2" when it is removed):

```
package example;

import java.util.Stack;
import org.dom4j.Element;
import com.pmease.commons.xmt.VersionedDocument;
import com.pmease.commons.xmt.MigrationHelper;

public class AbstractCompileTask {
        public int priority;

        public String options = "-debug";        

	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
                int taskVersion = versions.pop();
                MigrationHelper.migrate(String.valueOf(taskVersion), TaskMigrator.class, dom);
	}
        
        public static class TaskMigrator {

	        @SuppressWarnings("unused")
	        private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
		        Element element = dom.getRootElement().element("prioritized");
		        element.setName("priority");
		        if (element.getText().equals("true"))
			        element.setText("HIGH");
		        else
			        element.setText("LOW");
	        }

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
        }
}
```

The newly added migrate method pop up the top element from versions stack to get the version intended to migrate class Task, and call MigrationHelper.migrate to apply migration methods defined in class TaskMigrator as necessary. Migration methods defined in class TaskMigrator are simply copied from the deleted Task class.

When migrate from aforementioned "1.0" XML with this new class hierarchy, XMT does the following:

1. Split "1.0" into a versions stack containing element "0" and "1" from top to bottom.
2. Set current class to CompileTask.
3. Pop up top element "0" from versions stack, compare it with current version of current class and invoke migration methods as necessary. This results in invocation of method migrate1 and migrate2 defined in class CompileTask:
    - Method migrate1 is invoked to add "destDir" element with value "classes", and the versions stack contains element "1" after invocation.
    - Method migrate2 is invoked to add "options" element with value "-debug", and the versions stack contains element "0" and "1" from top to bottom.
4. Set current class to super class of CompileTask, which is now AbstractCompileTask.
5. Pop up top element "0" from versions stack, compare it with current version of current class and invoke migration methods as necessary. As a result of this, XMT invokes method migrate1 of class AbstractCompileTask, with current versions stack (now contains only one element "1") passed as param versions. This method pops up top element from versions stack, which is "1", and pass it as param fromVersion when invoking method MigrationHelper.migrate. This results in invocation of method migrate2 of class TaskMigrator to migrate enum based priority to be numeric based.
6. Now the versions stack is empty and the migration process is done successfully.
