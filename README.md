mud-server
==========

A MUD server written in Java. Elements of the D&D/d20 system are used to implement the 'game' aspect of the
server code with regards to things like player and item stats, etc.

## Notes

Some of the following directories need to exist before the game starts. '\' is the top directory
for all the files. The code makes an attempt to find these folders and create them while it is setting
up the game, and generates some basic data like an empty database and configuration files. That means
that if you don't download the data from here (effectively test data at some level), then you will need
to get find suitable pre-existing data or you'll need to add some manually.

**Only the IMPORTANT! directories are absolutely essential, although the others may be important in the future.**

Obviously you need the program itself, so this is just excepting source and compiled .class files

```
\data          IMPORTANT! the folder inside of which all game data is stored, server will likely crash without data
\data\accounts for account files, not really used or important
\data\backup   database backups: currently no system for backups besides copying main text db file
\data\config   IMPORTANT! config files
\data\help     IMPORTANT! server/game command help files: if absent, the game may crash when trying to find them
\data\maps     not really important at all, except for map command which was just a tangential test
\data\motd     IMPORTANT! message  of the day, ets shown on connect before login, non-existence may cause crash?
\data\spells   spell data, totally unused
\data\theme    IMPORTANT! sets mud name as well as day/month/year for time, month names etc
```

This code is very much in an unfinished state, there may be radical shifts in the inner workings
in the future. Try not to make too many assumptions about stability and backup the database if you
play with this as future code may corrupt the data and/or utilize an alternate approach for data storage.

This code requires at least Java 7 (1.7)

## Usage

```
Usage: java -jar MUDServer.jar

    --port  <port number> specifiy port for the server to listen on
    --debug               enable debugging messages
```

## Help

For help using the code and miscellaneous documentation, see the [Wiki](https://github.com/jnharton/mud-server/wiki) for this project.

## Updates

December 2012
The code is up for this, although my code depends on some LGPL'd network (server/client) code. My apologies
for the weird structure, which is due to this project being worked on in eclipse. 

January 27, 2013
I've started pulling together some basic documentation and info on the wiki section of the repository. I have
more information on my computer, but it's somewhat fragmented and not all of it is up to date with the current
state of the code and some is more ideas and notions than concrete details about the inner workings.

February 3, 2013
Seeing lots of contribution from [joshgit](https://github.com/joshgit), and dealt with ~3 pull requests already. Made
switch from Java 6 (1.6) to Java 1.7 (1.7) in project settings today in part to make it not complain about empty generic
usage in Java 7, and there were hardly any problems. So from now on I'll be working under the assumption of Java 7 as
the required minimum.

## Copyright
Copyright (c) 2012 Jeremy Harton. See LICENSE.txt for further details.

The license given applies to all files in the source directory, excepting those under src/mud/net which are
licensed under the GNU LGPL (Lesser GNU Public License) For the specific details of that license refer to the
headers in the respective files or the included full, unpersonalized version of LGPL 2.1 in LICENSE2.TXT.
