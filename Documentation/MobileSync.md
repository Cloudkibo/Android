# Mobile Data Sync using Azure Mobile Services

Azure Offline sync feature will resolve the conflicts that we often have while doing sync between server and mobile devices. It would allow the app to be responsive by storing the data in the sqlite data store locally. It will also resolve the conflicts when same data is changed by two different devices. However, it is upto the developer to decide when we want to do the sync. Options are 'on start of application' or 'on availability of networ' or both. The function to do sync is provided to developer by the SDK.

In order to get started with Azure offline sync services in our mobile, we have to decide which tables we want to sync with the server. Not all tables which are currently in CloudKibo needs syncing i.e. users, contactlist etc. The main table that we want to sync is for chat. In both iOS and Android, they define the structure of the table in form of a class where the private fields of the class represent the fields of a table and entire class represents a single tupple or row of a table. We would just give this structure to the SDK and it would automatically create the table out of it.

The structure for our chat table will be as following: 

- `to` Receiver of the message. Mobile number is stored here.
- `from` Sender of the message. Mobile number is stored here.
- `fromFullName` Sender of the message. Display name is stored here.
- `msg` Chat message content is stored here.
- `date` Date and time for the message is stored here.
- `owneruser` the owner of this copy of message. Each message is stored twice on server, one copy for sender and one for receiver
- `uniqueid` Unique id of the message. This is used to uniquely identify the message
- `status` Current status of the message. i.e. sent, delivered, seen or pending
- `type` Type of the message. i.e. is it just chat or file attachement
- `file_type` File type of the file. If the message has attachment then type of that attachment would be defined here.

## Time for syncing

As the sync is incremental and won't take much time, I recommend doing sync on both 'start of application' and 'on availability' of Internet. We can also use third option additionally which is the schedule time for sync. This third option is often suitable for news paper applications where there is a defined time to get new articles. So we would start from first two options and then we would decide if we need third option or not.

Each mobile device is responsible to call sync function at 'start of application' or 'at availability of Internet'. The sdk doesn't do scheduling of sync and it is left up to the developer. SDK will just take care of how the sync is performed and conflicts are resolved. The method to start sync is given in sample applications which we had download from the Azure.

## Use existing iOS application and enable it for sync

**Note** Also use the code of sample application in reference with this guide.

Download the Mobile Services iOS SDK

In your Xcode project, add the Mobile Apps iOS libraries to your project. To do this, drag the MicrosoftAzureMobile.framework folder from Finder into your project.

Add the import to your Objective-C bridging header file, typically APPNAME-Bridging-Header.h:

`#import <MicrosoftAzureMobile/MicrosoftAzureMobile.h>`

Add the following public property to your AppDelegate.swift file:

`var client = MSClient?`

Add the following code to your AppDelegate.swift file in the application:didFinishLaunchingWithOptions function

    self.client = MSClient(
        applicationURLString:"https://samplenode1.azurewebsites.net"
    )

Add the following code to your AppDelegate.swift file in the application:didFinishLaunchingWithOptions function

    // todo note: in the following chunk, replace the todoitem table with our own table given above
    let delegate = UIApplication.sharedApplication().delegate as AppDelegate
    let client = delegate.client!
    let item = ["text":"Awesome item"]
    let itemTable = client.tableWithName("TodoItem")
    itemTable.insert(item) {
        (insertedItem, error) in
        if error {
            println("Error" + error.description);
        } else {
            println("Item inserted, id: " + insertedItem["id"])
        }
    }

Run the iOS project to start working with data in your mobile backend.

## Use existing Android application and enable it for sync

For Android Studio, add the following lines to the project’s Gradle.build file:

    buildscript {
        repositories {
            jcenter()
        }
    }

And add the following to the app’s Gradle.build file with your desired SDK version plugged in (you can find the latest versions here ):

    compile 'com.microsoft.azure:azure-mobile-android:{version}'

In your app add the following line to your AndroidManifest.xml file:

    <uses-permission android:name="android.permission.INTERNET" />

Add the following line to the top of the .java file containing your launcher activity:

    import com.microsoft.windowsazure.mobileservices.*;

Inside your activity, add a private variable:

    private MobileServiceClient mClient;

Add the following code to the onCreate method of the activity:

    mClient = new MobileServiceClient(
          "https://samplenode1.azurewebsites.net",      
          this
    );

**Store data in your mobile service**

Add a sample Item class to your project:

    // todo note Use the table defined above, this is sample table
    public class TodoItem {
          public String Id;
          public String Text;
    }

In the same activity where you defined mClient, add the following code:

    TodoItem item = new TodoItem();
    item.Text = "Awesome item";
    mClient.getTable(TodoItem.class).insert(item, new TableOperationCallback<item>() {
          public void onCompleted(TodoItem entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                      // Insert succeeded
                } else {
                      // Insert failed
                }
          }
    });

Run the Android project to start working with data in your mobile backend.

## Server side changes on the node.js

Here instead of removing the previous chat table, we would just create another with the new name 'chat'. In this way, we would silently migrate to new way of syncing without breaking the older code. When new version of table (which is sync-enabled) is stable, we would remove the old one. On server side, we would create dynamic schema so that it would automatically adjust when we add new variables into the table. Server code would be our own however the data would be stored on the azure cloud.

## Data Restriction

UNDER CONSTRUCTION
