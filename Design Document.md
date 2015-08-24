#Main Application with components and background services

This is the main application code which is responsible for showing views and carrying required user tasks. This application is connected to background services and other components written by us.

##Socket Service

Our socket.io code is running in this background service. As socket.io is technology to support real-time communication, it is required that we should keep it running on the android device. In this manner, no matter application is running or not, we will always be connected to our server using socket.io. If we have any call or message while application is closed, socket service has responsibility to check application open status and then if application is closed or in background then show the notification or alert.
When we start our application, each of its activity gets bound to this service. In this way, our application gets connected to this service which runs in the background. Each activity uses this socket service to send any real-time message to server i.e. chat message or signaling messages. We do activity-service communication, so that activity listens to specifics events or messages which occur in socket service. 

##Sync Adapter

This background service is used to synchronize our application database with our server side database. It fetches the data from server on defined intervals and then updates the application’s local database accordingly. This runs in the background and is completely controlled by android OS. E.g. if battery is low then android OS can close the sync adapter, or if Internet is available then android OS can start this sync adapter so that our app can synchronize data. For testing purposes, it does synchronize on very small intervals for now i.e. after every 1 minute. We can set the intervals using Calendar API on android. We have kept it simple for now i.e. sync adapter fetches the data from the server and replaces the entire data on client side. We have checks in android code that change should not occur on client side data until it occurs on server side data at the same time.

##Content Provider

Content Provider is an application component which is used by sync adapter to read and write data from local SQLite database. It has a structured way to make queries to database. It has one database contract which tells the sync adapter that which tables are exposed by our application and what is their structure. Content provider can also be used to expose our data to other android applications if they know our database contract. As sync adapter is separate entity and not part of our application therefore it needs content provider to talk to our database.

##Account Manager

This API is provided by Android and we need to inherit to use it. Using this, we can make our CloudKibo account on android device. Account manager is responsible for storing authentication token on android device and give it to application when required. It also fetches new token in background if the current token is expired. All standard applications on Android have their accounts made using account manager. In settings of Android, we can see which applications are maintaining accounts using account manager.

##Account Authenticator

This basically talks to Account Manager during the login. Also, if the password was changed on web and android could not get the new token then Account Manager shows this in notification manager to ask the new password from user.

##Database Handler

We have this utility class which we use in all our application to store and retrieve data from local SQLite database of our application. It has separate functions to fetch data from each table. We also use functions to store data in table. We don’t deal with the database instance in all our application. We just create one database instance in this utility class and rest of the application invokes functions of this class to talk to database.

##File Chooser

This component was written for File Transfer. It provides the user with user interface to choose file from folders. It shows different options to the user when user tries to transfer file. User can choose file from gallery or built-in file explorer in file chooser.

##Connection Manager

We have this utility class which we use in all our application to make network requests. Whenever application needs to send or receive data over the network, it uses this class. It has some functions to fetch data from server using HTTP GET or send data to server using HTTP POST. It then converts the data given by server to the format which is required by application.

##File Transfer Service
File transfer logic is now sent to service which runs in background during file transfer. It does not force the user to stay on the application until file transfer is complete. File gets transferred in the background and progress is updated on the notification manager.
