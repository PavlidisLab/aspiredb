

This is the project aspiredb which uses the GwtQuery Library.

- Assuming you have installed maven, compile and install it just running:
$ mvn clean install

- Run it in development mode:
$ mvn gwt:run

- Import and run in Eclipse:

 The archetype generates a project ready to be used in eclipse, 
 but before importing it you have to install the following plugins:

    * Google plugin for eclipse (update-site: http://dl.google.com/eclipse/plugin/3.7 or 3.6 or 3.5)
    * Sonatype Maven plugin (update-site: http://m2eclipse.sonatype.org/site/m2e)

 Then you can import the project in your eclipse workspace:

    * File -> Import -> Existing Projects into Workspace 

 Finally you should be able to run the project in development mode and to run the gwt test unit.

    * Right click on the project -> Run as -> Web Application
    * Right click on the test class -> Run as -> GWT JUnit Test 

    
- Generating DDL
To create the SQL schema from the current entities listed in hibernate.cfg.xml, run a Maven build with the goal hibernate3:hbm2ddl.
This produces a schema in /aspiredb/target/hibernate3/sql/schema.sql.
If new entities have been added, they have to also be added to /aspiredb/src/main/resources/hibernate.cfg.xml 
using <mapping class = "classname" /> .
Unfortunately, it is not possible to use wildcards to specify mapped classes.
Hibernate configuration: http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html/ch01.html
Maven-hibernate3 plugin: http://mojo.codehaus.org/maven-hibernate3/hibernate3-maven-plugin/hbm2ddl-mojo.html    


 - Generating UML
To generate a UML diagram from the entity classes, use ObjectAid: http://www.objectaid.com
Install Object Aid to Eclipse following these instructions: http://www.objectaid.com/installation
Inside your project, create an empty class diagram.  New > Other > ObjectAid UML Diagram > Class Diagram
Then drag classes from Package Explorer onto the the diagram.
