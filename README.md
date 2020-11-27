# commons.xmt
XStream Migration Tool

XStream is a popular tool to serialize Java objects to XML and back again. However in a production environment, 
user needs to deal with migration of serialized XML if fields of associated Java classes have been changed. 
XMT (XStream Migration Tool) makes this task much easier with the help of Java reflection and dom4j: when you 
change fields of the class, you just provide a migration method in your class to handle migration of dom4j 
document (DOM representation of serialized XML) from previous version to current version, and XMT will take 
care of the rest.

## Origin
This documentation is based on the original documentation from <https://wiki.pmease.com/display/xmt/Documentation+Home>. 
The original source is based on work from <pmease.com>.
