MCDaemon [![Build Status](https://travis-ci.org/Finomnis/MCDaemon.png?branch=master)](https://travis-ci.org/Finomnis/MCDaemon)
========

A Java Daemon for automated Minecraft server maintenance

_-- Beta 0.3 released --_

For details, go to http://www.minecraftforum.net/topic/1645748-mcdaemon-a-java-daemon-for-unixwindows/


Features:
---------

-	Automatic initialization and update
-	Automatic crash detection and restart
-	Scheduled call of an external backup script


Supported Minecraft-Versions:
-----------------------------

-	Vanilla
-	~~Bukkit~~ (Out of date, currently unmaintained)
-	FTB


Supported Modes:
----------------

- Directly run jar as process (Platformindependant)
- Run as Daemon via jsvc (Unix)
- [Upcoming] Run as Service via procrun (Windows)
