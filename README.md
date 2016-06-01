# booth-portals
BoothPortals Bukkit Plugin

This is a simple portal-based teleportation plugin for the Bukkit/Spigot Minecraft Servers. I wrote it using version 1.9, but it might as well work with earlier or later versions, I haven't checked yet.

My aim was to provide a simple, safe and command-free alternative to existing portal plugins. Everything from creating, destroying, choosing destinations and teleportation is done by placing, removing, or clicking blocks.

The plugin has support for permissions, although they are  not fully implemented yet.

Finally, I took some care with respect to internationalization. You can add your own laguage to it by adding a corresponding Messages_ll.properties or Messages_ll_CC.properties file, where ll is the two-character language code (e.g., en, es, it, pt) and CC is the two-character country code (US,UY,BR, etc.).

The main bulding mechanism is through Apache Maven. Thus, the sources are organized following the standard project structure as defined by Maven (sources go under src, created files go under target, see their documentation for more details) and the project is specified in the pom.xml file (this is the Maven equivalent of a Makefile).

Maven is marvelous at handling project dependencies, so you just need to have a working Java development kit (1.7 or above), Maven, and that's it.

Type

mvn package

under the root folder, and Maven should take care of the rest. Maven downloads resources on the fly, so be sure to be connected to the internet, and wait for a while because it takes a while to download all the requirements.

USAGE

After a succesfull build, copy the jar found under target/ to the plugins folder of your minecraft server. Also copy the config.yml file there, in case you want to fiddle with the default configuration (notoriously, the language of the plugin)

CONTACT

You can reach me at nacho@fing.edu.uy

Please keep in mind that I did this as a hobby. I have taught Java for over 15 years, so I know about Java, but I am quite new to Minecraft and Bukkit so I may not know a lot about Bukkit and its intricacies. I welcome any suggestions for improvements!

Thank you,
Ignacio Ramirez.

