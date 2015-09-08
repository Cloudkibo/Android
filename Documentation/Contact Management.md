![Design Diagram](https://github.com/Cloudkibo/Android/blob/master/Documentation/images/contact%20diagram.PNG)

# Contacts Management in Android

Each android device has to maintain local copy of data of user in sqlite database. We do this so that data is shown to user even when offline. We run sync adapter to synchronize local database with server database periodically subjecting to availability of Internet.

## Mongodb

On server side, our data is stored in mongodb database. For contacts, we have a mongodb collection called contactslist. Fields are : 

1. userid
2. contactid
3. unreadMessage
4. detailsshared

Unread message from any contact is reflected in contactlist table with column unreadMessage. In this way, we know that person has unread message by which friend.
Details Shared columns is by default 'No' when add request is made. We change its value to 'Yes' when the other person approves the request.

## Android Application

Android application should exponse functions like : 

1. Add contact
2. Remove contact
3. Approve add request
4. Reject add request
5. Load contacts from server
6. Store/load contacts to and from sqlite database
7. Do proper synchronization on intervals to update data on both sides

## SQlite database

In sqlite database, we must have the replica table of contactslist table of server. One other table that we should have on android local database is to store contacts from user's address book (only those who are invited by user to cloudkibo). This is not yet done and would be given more details later.
