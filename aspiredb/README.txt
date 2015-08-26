ASPIREdb installation instructions:

http://aspiredb.chibi.ubc.ca/data-loads-and-admin-tools-setup/

-----------------------------------------------------------------------------
This distribution contains:


aspiredb.war (the web application archive containing all the program files)
aspiredb.sql (an sql script defining an empty aspiredb schema)
aspiredbCli.jar (a JAR file with the libraries required to run the CLI )
aspiredbCli.sh (a script to make it easier to run the command line tools)
README.txt (this file)

----------------------------------------------------------------------------
Aspiredb is a Java based web application that requires a Java web application 
server(servlet container) like Tomcat, and a MySQL database server.

Information for downloading and installing the Tomcat and deploying an 
application to it can be found here:

http://tomcat.apache.org/download-70.cgi
http://tomcat.apache.org/tomcat-7.0-doc/setup.html
http://tomcat.apache.org/tomcat-7.0-doc/appdev/deployment.html

Information for downloading and installing MySQL can be found here:

http://dev.mysql.com/downloads/

------------------------------------------------------------------------------
Configuring ASPIREdb

aspiredb.properties is a text file required by the Aspiredb web application. 
It must be placed in the home directory of the web server (in Tomcat's case 
whatever directory CATALINA_HOME is set to).

the contents of aspiredb.properties should be as follows:

jdbc.driverClassName=com.mysql.jdbc.Driver

# change this value to whatever the path is to your database with the aspiredb schema
jdbc.url=jdbc:mysql://localhost:3306/aspiredb  

# change this value to a user that has access to the aspiredb schema
jdbc.username=usernameXXX   

# change this value to the user's password of the user that has access to the aspiredb schema
jdbc.password=XXXXXXX       

See "default.properties" for more details.

-----------------------------------------------------------------------------------
Deploying ASPIREdb

After you have installed MySQL and a web server(e.g Tomcat), you must complete 
the following steps for Aspiredb to run correctly:

1. Create a database(lets call it "aspiredb" for the purposes of these instructions) 
and run the aspiredb.sql script on the aspiredb database. Add the path to this 
database to aspiredb.properties as the value for the jdbc.url property.

2. Create a database user for the aspiredb database that has read and write 
permissions.  Place the username and password for this user in aspiredb.properties 
as the values for the jdbc.username and jdbc.password properties. This user will 
be used by the web application to access the database.

3. Deploy the aspiredb.war file the appropriate directory of your web server 
(e.g. for Tomcat: CATALINA_HOME/webapps)

4. Place the aspiredb.properties text file in the home directory of the web server

5. Start your web server 

6. In your browser go to the appropriate url to the web app 
(e.g. http://path_to_webapp/aspiredb/home.html) and sign in using 
username/passwords:  user/changeme, or administrator/changemeadmin

7. The database will currently be empty, follow the instructions on 
http://aspiredb.chibi.ubc.ca/data-loaders-2/ to upload data to your project

8. Change the default users(user, administrator) passwords and create new users 
by following the instructions on http://aspiredb.chibi.ubc.ca/admin-tools/adding-usersgroups/

-----------------------------------------------------------------------------------
Building ASPIREdb from source code

Some instructions for developers.

- Assuming you have installed maven, compile and install it just running:
$ mvn clean install

- Import and run in Eclipse:

 Then you can import the project in your eclipse workspace:

    * File -> Import -> Existing Projects into Workspace 

 Finally you should be able to run the project in development mode

    * Right click on the project -> Run as -> Web Application
    
- Generating DDL
To create the SQL schema from the current entities listed in hibernate.cfg.xml, 
run a Maven build with the goal hibernate3:hbm2ddl.
This produces a schema in /aspiredb/target/hibernate3/sql/schema.sql.
If new entities have been added, they have to also be added to
/aspiredb/src/main/resources/hibernate.cfg.xml using <mapping class = "classname" /> .
Unfortunately, it is not possible to use wildcards to specify mapped classes.

Hibernate configuration:
http://docs.jboss.org/hibernate/annotations/3.5/reference/en/html/ch01.html

Maven-hibernate3 plugin:
http://mojo.codehaus.org/maven-hibernate3/hibernate3-maven-plugin/hbm2ddl-mojo.html    

 - Generating UML
To generate a UML diagram from the entity classes, use ObjectAid:
http://www.objectaid.com
Install Object Aid to Eclipse following these instructions:
http://www.objectaid.com/installation
Inside your project, create an empty class diagram.  New > Other > ObjectAid
UML Diagram > Class Diagram
Then drag classes from Package Explorer onto the the diagram.

