# MoneyCoreNews
# MoneyCoreNews Web site
Installation, Configuration& Deployment

####
Outils partie Back-End:

Java 8
Spring Boot 1.5

Outils partie Front-End

####


Recovering of the code: Back-End and Front-End (from Git Repository: Github)


The steps to do in local development server is:

Create a new folder that will contain the two projects of moneyCoreNews (front & back ends)

C:\>mkdir project

Initialize local git repository
C:\project>git init


Initialized empty Git repository in C:/project/.git

Recover and Copy the code of the back-end project in the local 

C:\project>git clone https://github.com/Nseydi/MoneyCoreNews.git

Change your login github

Cloning into 'moneyCoreNews-back-end'...


Update the code in your local repo with the changes from other members of your team using the following commands:

fetch , which downloads the changes from your remote repo but does not apply them to your code.

merge , which applies changes taken from fetch to a branch on your local repo.

pull , which is a combined command that does a fetch and then a merge.

Configuration of the back-end application (API REST)


Configure the back-end app (API REST) 


Change the configuration of your Database (the user, password, port ...) in the same configuration file "src/main/resources/application.properties"

You will not have to create any DB,it will be created automatically with name:moneycore


Change this property 'angular.url=http://...' in the file "application.properties", specify the URL where the Front End Application is running (The goal is to allow the requests from this server : from front side to back side)

 Change the path of the logging files: put a path where you to want to store your logs, change wherever there is a path expression in the xml file src/main/resources/logback.xml.
 

To configuretheSecurity aspects (filters : to filter allowed requests), you can edit (add, remove, edit filters) in the java class C:\project\MoneyCoreNews\moneyCoreNews-back-end\src\main\java\com\moneyCoreNews\security\SecurityConfig.java by adding the line ….antMatchers(…URLS...).permitAll()


Deploy & Run the back-end application (API REST)


You can then run these two commands using terminal in the root of your project:

C:\>cd C:\project\MoneyCoreNews\moneyCoreNews-back-end

C:\>mvn spring-boot:run

 
 If you prefer Eclipse you can easily run the api by clicking on the project then choose Run as Spring boot App. AND CHECK THE URL http://localhost:8080/ in your favorite Browser.
 
To show the Web Services exposed by this End Point, a generated web page will be available by adding to the URL this part /swagger-ui.html/

Installation & Configuration of the front-end application (Web Application)

