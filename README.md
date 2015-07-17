## Comix
Comix is a Load Balancing Reverse Proxy Server build on top of Netty for Minecraft Networks.

![ScreenShot](http://www.ibm.com/developerworks/websphere/library/techarticles/1308_gupta/images/fig01.jpg)

### How it works
Users/Clients will connect to the Comix instance. The instance will then act like a Tunnel between the selected target Server and the User/Client. The next target Server is selected by an Round-Robin-Algorithm at the moment. Later on you can switch between Algorithms to fit your need.

### What you can do with it
If you have a large Minecraft Server Network (+400/800 players) it is a very good idea to setup and Multi-Proxy-System with BungeeCord. Instead of using a DNS based Balancing for the BungeeCord Server, you can easily use Comix. If one instance of Comix does not fit your needs, you can easily setup another instance and than use a simple DNS-RR-Balancing for the two or multiple Comix instances.

### Features
- IP-Blacklist
- Customizable Ping Response (MOTD, online players, max players, etc.)
- Colored MOTD
- Maintenance mode (Custom MOTD, custom kick message, custom version string)
- Logging (Planned)
- Custom Address and Port binding for the Load-Balancer
- As many target Server as you want
- Load Balancing with simple Round-Robin
- Based on Netty for high performance
- Monitoring (Planned)

### Wiki
[Wiki](https://github.com/JackWhite20/Comix/wiki)


### License
Licensed under the GNU General Public License, Version 3.0.
