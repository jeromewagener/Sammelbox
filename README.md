Collector-Desktop
=================

Collector-Desktop is the desktop client of a collection manager suite originally 
developed by Jérôme Wagener and Paul Bicheler. This repository is currently in
private mode until most bugs are fixed and the code is cleaned up!

The project was imported from a local SVN repository. Thus no prior revision history
is available. This also means that the original commit is a collaboration by Paul and Jérôme.

Have fun ;)

## Features

- Manage any kind of collection with no limits to your imagination. The "Album" in 
  which your collection items are stored is fully customizable and can be adapted at
  any time. The user interface will adapt itself automatically to the "Album" that you just created!
- Search & filter using the power of native SQL queries
- Synchronize your collection with a mobile device over your local LAN or WLAN. While synchronizing,
  no data will be sent through the internet, making your collection invisible to other people. (Not fully implemented yet)
- Import & Export features (CSV, HTML) as well as a standardized SQLite database in which all data is stored.
  This means that your data is always available on a local database that you have total control over.

## Technical Information

- Java / HTML / JavaScript / CSS based
- Uses a SQLite database which can be inspected and modified through any standard SQLite viewer 
- Native user interface support through SWT for Windows, Linux and Mac OS
