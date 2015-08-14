## Comix
Comix is a Load Balancing Reverse Proxy Server build on top of Netty for Minecraft Networks.

![ScreenShot](http://www.ibm.com/developerworks/websphere/library/techarticles/1308_gupta/images/fig01.jpg)

**Comix is at the moment under heavy development, so stay up to date with the latest commit and changes! Dont't forget to checkout the wiki for possible config changes!**

### Website
[Comix Website](http://jackwhite20.github.io/Comix/)

### Wiki
[Comix Wiki](https://github.com/JackWhite20/Comix/wiki)

### How it works
Players will connect to the Comix instance. The instance will then act like a tunnel between the selected target Server and the player. The next target Server is selected by an Round-Robin-Algorithm at the moment. Later on you can switch between Algorithms to fit your need. All players are connected through Comix so all your other Servers and Proxies are behind Comix (this is the Reverse Proxy task of Comix).
The only thing the players need to know is the domain which points to the ip where Comix is running.

### What you can do with it
If you have a large Minecraft Server Network (+400/800 players) it is a very good idea to setup a Multi-Proxy-System with BungeeCord. Instead of using a DNS based Balancing for the BungeeCord Server, you can easily use Comix. If one instance of Comix does not fit your needs, you can easily setup another instance and than use a simple DNS-RR-Balancing for the two or multiple Comix instances. Because Comix will now get all the ping requests, you can configure stuff like motd, sample text, max players etc. in a simple config file. It's as simple as it sounds. You can see some example configs in the Comix wiki above.

### Features
- IP-Blacklist
- Commands (help, reload, kickall, etc..)
- Whitelist (choose who is allowed to join, or allow all)
- Customizable Ping Response (MOTD, online players, max players and sample text)
- Colored MOTD
- Maintenance mode (custom MOTD, custom kick message, custom version string)
- Logging
- Online/Offline detection for servers (automatically removing and adding to the LB)
- Custom address and port binding for the Load-Balancer
- As many target servers as you want
- Load balancing with simple Round-Robin
- Based on Netty for high-performance
- Monitoring (in work)
- Block ip ranges (in work)

### License
Licensed under the GNU General Public License, Version 3.0.
