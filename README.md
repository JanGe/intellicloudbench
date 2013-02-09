# Want to find the Cloud Computing service that best suits your needs?
# Check out IntelliCloudBench!

## What is IntelliCloudBench?

### Web application

IntelliCloudBench is a Vaadin web application that supports you in benchmarking multiple Cloud Computing service providers at a time.
Currently it supports a subset of the benchmarks that are used by the Phoronix Test Suite and the Cloud Computing services supported by jclouds.

### libIntelliCloudBench

The part of IntelliCloudBench that does most of the work is encapsulated in a separate library.

## How to use?

IntelliCloudBench can be deployed as a WAR package in any Java Servlet container.

To build the WAR file, follow these instructions:

1.  Add the necessary libraries to your local Maven repository

    ```
    mvn install:install-file -Dfile=lib/scribe-1.3.2-patched.jar -DgroupId=org.scribe -DartifactId=scribe -Dversion=1.3.2-patched -Dpackaging=jar
    mvn install:install-file -Dfile=lib/icepush.jar -DgroupId=org.icepush -DartifactId=icepush -Dversion=2.0.0-alpha3 -Dpackaging=jar
    mvn install:install-file -Dfile=lib/icepush-gwt.jar -DgroupId=org.icepush.gwt -DartifactId=icepush-gwt -Dversion=2.0.0-alpha3 -Dpackaging=jar
    ```

2.  Set up a Google API access key

    To make login with Google work, a project has to be registered at Google's API Console (https://code.google.com/apis/console/) and a "Client ID for web applications" has to be created. Specify your *Redirect URIs* as `HOST:PORT/IntelliCloudBench/` and `HOST:PORT/IntelliCloudBench/1/`. ID and secret must then be setup in `src/main/resources/oauth.properties` like this:

    ```
    googleapikey=YOUR_GOOGLE_CLIENT_ID
    googleapisecret=YOUR_GOOGLE_CLIENT_SECRET
    ```

3. Build IntelliCloudBench and deploy the WAR file to your Servlet Container

    If you are using Jetty, just run:

    ```
    mvn jetty:run
    ```

    *Note*: The Java 7 SDK is required. On Ubuntu, install `openjdk-7-jdk`.
