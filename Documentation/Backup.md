**Alarm Clock Feature to schedule the backup**

There is a feature in Android called alarm clock to do repetitive timed tasks. We would use this feature to do backup on daily, weekly or monthly basis. The backup would run in background and even when app is closed. It would be a background service. We would open a task for it.

**Restore**

We would only restore the backup on initial installation of application just like whatsapp do. Also at that time we would get user permission for accessing Google Drive. Just like Whatsapp, we would ask the setup of backup at installation as well. We would open a task for it.

**Wrapper for accessing Google Drive**

Instead of writing code which directly accesses the Google Drive. We would write wrapper classes. These would be used by our application. This is because we want to make our logic generic and in future if we want to use Dropbox instead of Google Drive, we would not require much work. We would open a task for it.

**Data Structure**

Now, in order to store the sqlite table entries in files, we would create file for each table that we have in our schema. Each line in the file will represented data entry in that table. The file format would be called csv so that we easily separate the columns and make them in tabular format. We would also store display pictures of groups and for this we would maintain a separate file which would indicate which image belongs to which group.

For the chat attachments, we would create folders for each type of attachment and would also maintain file for indexing which attachment belongs to which chat. Attachments would be audio files, documents, pictures and video files. We would open number of tasks for this.

**Background Service for doing backup**

We would code the background service which would be responsible for doing backup. This would run at scheduled times and user won’t notice when it is running. It would silently do its work. We would open task for it.

**Settings**

We would create a settings page for backup and restore feature where user would be able to select his/her preferences regarding backup and restore. User would also be able to stop the backup or remove the old backup. We would open two tasks for it.

**Current Syncing**

In current sync during installation, we are doing lot of work which also slows down the installation. We would be removing chat sync, groups sync, and other syncs which happen during installation and instead we would restore data from Google Drive after asking user. This would also help us to not store chat and group information on server. There would be less http requests to server. I would open task for this.

**Setup Google Drive app in Google App console and gain user permission**

We would setup the Google Drive app in app console to get API Key and App secret so that we are able to integrate it in our application. Also, we would code screens to gain user permission to access the Google Drive. We would open two tasks for this.
