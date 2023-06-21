# HCsCR
**H**aram**C**lient**s**ide**C**rystal**R**emover.

*Remove your end crystals before the server even knows you hit 'em!*  
This Fabric mod removes end crystals client-side on hit unlike vanilla client waiting for an "EntityDestroy" packet from the server.

## Download
- [ModRinth](https://modrinth.com/mod/hcscr)
- [GitHub](https://github.com/FemboyPvE/HCsCR/releases/latest)

## Building
You will need:

- Java JDK 17 or higher. (e.g. [Temurin](https://adoptium.net/))
- 3 GB of available RAM.
- A bit of storage.

How to:
- Ensure your JDK is set up properly. (i.e. JDK path is in `JAVA_HOME` environment variable)
- Clone this repo or download it. (e.g. via `git clone https://github.com/FemboyPvE/HCsCR`)
- Open the terminal (command prompt) there.
- Run `./gradlew build`.
- Grab JARs from `/fabric-1.[your_version]/build/libs/`

## License
This project is licensed under [Apache License 2.0](https://github.com/FemboyPvE/HCsCR/blob/master/LICENSE).

## FAQ
**Q**: I don't understand [insert something here].  
**A**: [Discord](https://dsc.gg/femboypve).

**Q**: How to download?  
**A**: Releases are available [on ModRinth](https://modrinth.com/mod/hcscr), [on GitHub Releases](https://github.com/FemboyPvE/HCsCR/releases/latest), snapshots are available [on GitHub Actions](https://github.com/FemboyPvE/HCsCR/actions) if you have a GitHub account.

**Q**: Is that a hack?  
**A**: Not by design, but can be considered so and can be blocked by server. Please check your server rules before using this mod.

**Q**: Is this mod client-side or server-side?  
**A**: Client-side only, can be blocked by server.

**Q**: How to block this mod as a server owner?  
**A**: Read more [here](https://github.com/FemboyPvE/HCsCR/blob/master/HOW_TO_BLOCK.md).

**Q**: Your mod's name is offensive.  
**A**: Accept it, report it or fork it. IDC unless some moderators tell me about it.

**Q**: Forge?  
**A**: No.

**Q**: Quilt?  
**A**: Should already work, but I won't support it if it doesn't work.

**Q**: 1.17.1 or older?  
**A**: No.
