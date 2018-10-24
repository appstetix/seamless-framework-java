# Seamless Framework

## Introduction
Seamless is a provider agnostic framework allowing you to write once and deploy almost anywhere. The framework does this by abstracting your business logic from the vendor specific interface, allowing you to seamlessly switch vendors/providers without have to rewrite your application specific logic. So whether your applications run on a VM, container or serverlessly, the Seamless Framework can adapt to almost any environment with only minor configuration changes.

## Why Seamless?

  - Write once, deploy on-premesis, in the cloud or both
  - Well defined separation of provider and application logic prevents vendor-lockin
  - Easily plugin API validation and filters
  - Custom error handlers allow you to respond to errors in a predictive manner
  - Built-in URL versioning
  - Easy to use and quick to get started

## Technologies

Seamless uses a number of projects to work properly:

* [Vertx](https://vertx.io) - The framework is built using it as a base
* [Java 8](https://www.java.com/en/download) - Written in Java 8
* [maven](https://maven.apache.org) - Used for dependency management

## Installation

Currently this project hasn't been added to any dependency repository like maven central so the project will have to be cloned and built manually. Before you clone the repo make sure you have Java 8 + maven install on your machine. The steps to build the project are as follows:

```sh
$ git clone https://appstetix@bitbucket.org/appstetixappstract/seamless-framework.git
$ cd seamless-framework
$ mvn clean install
```

## Quick start

**Step 1 - Create a new maven project:**
**Step 2 - Include Seamless dependencies:**
For the purposes of this tutorial we will use the web module of the seamless framework as it contains a built-in web server, allowing us to quickly test our functionality through REST calls.
```xml
<dependency>
    <groupId>com.appstetix.appstract</groupId>
    <artifactId>seamless-web</artifactId>
    <version>1.0</version>
</dependency>
```

**Step 3 - Create API handler:**
API handlers are where your business/application logic will reside. This allows you to keep it consisting regardless of the provider/vendor you choose at the time.
```java
import com.appstetix.appstract.seamless.core.api.SeamlessHandler;
import com.appstetix.appstract.seamless.core.annotation.APIHandler;
import com.appstetix.appstract.seamless.core.annotation.Endpoint;

@APIHandler()
public class MyHandler extends SeamlessHandler {
    @Endpoint()
    public void myEndpoint(Message message) {
        successful(message, "Hello World");
    }
}
```
Here is a brief explanation of the code above:

- Extending the **SeamlessHandler** gives you basic functionality to communicate with the framework
- Annotating your class with **@APIHandler** allows the framework to register your class as an destination for incoming events
- Annotating your method with **@Endpoint** allows this method to be exposed as a REST resource
- Using the **successful()** method is a quick and easy way to tell the framework that you have executed the code successfully

**Step 4 - Create an API class:**

The API class acts as the entry point to your application. Seamless has provided a number of vendor/providers for you to extend, allowing you to quickly switch between them without having to change code in your handler class (created above).

```java
import com.appstetix.appstract.seamless.web.SeamlessWeb;
import com.appstetix.appstract.seamless.core.annotation.API;

@API
public class Application extends SeamlessWeb {
    public static void main(String[] args) {
        new Application();
    }
}
```

Here is a brief explanation of the code above:

- Extending the **SeamlessWeb** class tells the framework that you would like the application to be accessed through the web (the application is deployed on a web server)
- Annotating your class with **@API** allows the framework to register your class as an entry point for incoming events (like http requests)

**Step 5 - Test application:**
The only thing left to do is to run your application's main method. You can do this using your IDE or by running a **mvn clean install** and then running the jar in the target directory.

Once the application is running you can enter the following URL in your web browser: http://localhost:8888/. You should now see **"hello world"** as a result.

## Basic Usage
Now that you have a running app, we are going to really use some of the features of the framework.

##### @APIHandler
Update your @APIHandler with the following code:
```java
@APIHandler(baseURL="test")
```
The base url allows you to map all http requests with a particular url prefix to this API handler. In this case any URL with the path **/test** will be mapped to this handler. It's a good way to group functionality, especially when they share similar resources like data sources and/or services. 

A table with the **@APIHandler** features is listed below. 

| Property  | Required  | Description |
|---|---|---|
| baseURL  | false  | Prefix all @Endpoint paths with the given value  |
| access  | false  | Allows only events of a certain type to be registered for this handler. The values are either **ALL**, **WEB_ONLY** or **TASKS_ONLY**. **WEB_ONLY** will only register methods with the **@Endpoint** annotation, while **TASKS_ONLY** only register methods with the **@Task** annotation. **ALL** registers both **@Endpoint** and **@Task** annotations and is the default  |

##### @Endpoint
As explained in the quick start section, the @Endpoint annotation exposes that particular method as web resource and makes it accessible via a URL. For more control over the URL and its behaviour, update your endpoint as follows:
```java
@Endpoint(path = "demo", version = 1, method = HttpMethod.GET)
```
The path url is the unique address to that particular method. In this case the url path will be suffixed with the value of the path property. Furthermore the framework also support built in versioning. By specifying the version, the framework will automatically append the version number specified along with a "v". This means, with the base path mentioned above, that full path for this particular resource will be **/test/v1/demo**. Restart your app and see if the changes worked. Depending on your application needs, you can mix and match the baseURL in the @APIHandler and the path/version in the @Endpoint to make the desired URL pattern. 

Here are a full list of features for the **@Endpoint** annotation:
| Property  | Required  | Description |
|---|---|---|
| path  | false  | suffix the resource endpoint with the given value  |
| version  | false  | Specify the version of this resource. Added to the url path |
| method  | false  | Specify the HTTP method for this resource. The default is a **GET** method  |
| secure  | false  | If true (default) all http requests to this URL will pass through any specified validators at the **@API** level. If there aren't any declared validators, this property is ignored  |

##### SeamlessHandler

The SeamlessHandler class forms the base from which all API handlers are built. It also provides functionality to make it easier to interact with the rest of the framework. Let's discuss the various components of the SeamlessHandler:

###### Message
As the name suggests, messages are the glue that links your handler with the API which invoked it. It's VERY important that you respond to messages in every handler otherwise the framework will through a timeout exception. The best way to do so is to use specialized methods in the SeamlessHandler that tell the framework whether your method was successful or not. The **successful()** method allows for several parameters to be passed to is with the message parameter being the only required parameter. 

Equally if something goes wrong, whether it's a process violation or an unexpected exception, you can use the **failed()** method to let the framework know. This method also allows several parameters to be passed to it with the message parameter being the only one that's required. 

##### @API
The @API annotation represents the entry point of your application. Depending on whether you want to run your application on a server or as a function, you my need 1 or many API entry points. Typically in a tranditional application, like the one in the Quick Start section, your would have a single entry point (or API). However with serverless functions you my have an entry point (or API) for each of your functions. 

We will demonstrate the usage of the @API annotation with the following example. Change the class you annotated with @API with the following code:

```java
@API(handlers = MyHandler.class)
```
By doing this you tell this API that it should only route events to the specified handlers (MyHandler in this case). We recommend this approach if you want to deploy your application to a serverless provider (like AWS lambda) for the a few reasons:

1. This method is quicker because it doesn't have to scan the classpath for handlers which can save you money
2. It also prevents the framework from starting up/setting up handlers that will never be used which will improve your cold-start time

The default behaviour of the framework is to scan your project for any class annotated with the @APIHandler annotation, which is why the example in the quick start still worked despite us not explicitly specifying the handler. If you plan to deploy your application on server or container, not specifying the handlers will work fine. 

##### API Validators
Most applications require some level of security or validation before the application proceeds with processing any request/event. The Seamless Framework allows you to plugin these validators at the API level, allowing the events to be validated before proceeding to the relevant API handler. The steps to register a validator is detailed below:

###### Step 1
Create a validator class as follows
```java
import com.appstetix.appstract.seamless.core.generic.APIValidator;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.exception.APIViolationException;

public class MyRequestValidator implements APIValidator {
    @Override
    public void validate(SeamlessRequest request) throws APIViolationException {
        System.out.println("write your validation code here...");
    }
}
```
Here is a brief explanation of the code above:

- Implementing the **APIValidator** will require you to implement the **validate()** method
- A **SeamlessRequest** object is passed through which contains all the details of the event
- If there is a violation/exception, you can throw a **APIViolationException** or extension of this class

## Todos

 -[ ] Add support for path parameters
 -[X]  Added exception handling sub framework
 -[ ] Add URL pattern regex to override default patterns for @Endpoint resources
 -[ ] Add life-cycle hooks for plugins
 -[ ] Add MORE vendors/providers

License
----

MIT


**Free Software, Hell Yeah!**

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [dill]: <https://github.com/joemccann/dillinger>
   [git-repo-url]: <https://github.com/joemccann/dillinger.git>
   [john gruber]: <http://daringfireball.net>
   [df1]: <http://daringfireball.net/projects/markdown/>
   [markdown-it]: <https://github.com/markdown-it/markdown-it>
   [Ace Editor]: <http://ace.ajax.org>
   [node.js]: <http://nodejs.org>
   [Twitter Bootstrap]: <http://twitter.github.com/bootstrap/>
   [jQuery]: <http://jquery.com>
   [@tjholowaychuk]: <http://twitter.com/tjholowaychuk>
   [express]: <http://expressjs.com>
   [AngularJS]: <http://angularjs.org>
   [Gulp]: <http://gulpjs.com>

   [PlDb]: <https://github.com/joemccann/dillinger/tree/master/plugins/dropbox/README.md>
   [PlGh]: <https://github.com/joemccann/dillinger/tree/master/plugins/github/README.md>
   [PlGd]: <https://github.com/joemccann/dillinger/tree/master/plugins/googledrive/README.md>
   [PlOd]: <https://github.com/joemccann/dillinger/tree/master/plugins/onedrive/README.md>
   [PlMe]: <https://github.com/joemccann/dillinger/tree/master/plugins/medium/README.md>
   [PlGa]: <https://github.com/RahulHP/dillinger/blob/master/plugins/googleanalytics/README.md>
