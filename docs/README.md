<img alt src=hcscr.png>

# HCsCR

Remove your end crystals before the server even knows you hit 'em!

## Language

- **English** 🇬🇧 🇺🇸
- [Русский 🇷🇺](README_ru.md)

## Downloads

- [GitHub Releases](https://github.com/VidTu/HCsCR/releases)

## Dependencies

- Fabric, Forge, NeoForge, or Quilt
- Minecraft ([Supported versions](#versions))
- **Fabric/Quilt only**: [Fabric API](https://modrinth.com/mod/fabric-api)
  or [QFAPI/QSL](https://modrinth.com/mod/qsl) (*Required*)
- **Fabric/Quilt only**: [Mod Menu](https://modrinth.com/mod/modmenu)
  (*Optional*)

## About

Crystal PvP ("cpvp") has become a pretty popular combat mode in Minecraft.
Crystals themselves were never designed to be used in combat. Because of this, a
client ping (latency) plays a significant role in a crystal PvP fight, affecting
how fast you can spam. This mod helps to reduce (but does NOT fully remove) the
ping factor from crystal PvP fights. This is commonly called a *client-side
crystal optimizer* mod. It also removes the ping factor for PvP with anchors,
though the ping doesn't play a big role in anchor fights, and it might even be
beneficial to have high ping in these fights. This is not the only crystal
optimizer mod available, but it's the most configurable out there.

*NOTE*: A server-side crystal optimizer plugin (not this mod) is much more
effective. Consider checking the `/fastcrystals` command or similar commands
on your server. Ask the admins to install one if the command doesn't exist.

## Versions

| Support              | Versions                                                                                                | Note                                                                                                                                        |
|----------------------|---------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Beta&nbsp;&#x1F195;  | 26.2                                                                                                    | Newly supported versions. May be unstable, but *active* feature development happens here, bugs are *actively* fixed.                        |
| Active&nbsp;&#x2705; | 26.1.2, 1.21.11, 1.21.1, 1.20.1                                                                         | Actively supported versions. Well tested, features are *often* backported and bugs are *actively* fixed.                                    |
| Legacy&nbsp;&#x2753; | 1.21.10, 1.21.8, 1.21.5, 1.21.4, 1.21.3, 1.20.6, 1.20.4, 1.20.2, 1.19.4, 1.19.2, 1.18.2, 1.17.1, 1.16.5 | Versions supported on a best-effort basis. Features are backported *sometimes*. Bugs are fixed *often*. Critical bugs are *actively* fixed. |
| Ended&nbsp;&#x274C;  | 1.21.9, 1.21.7, 1.21.6, 1.21.2, 1.19.3                                                                  | No support. Outdated. **Use at your own risk.**                                                                                             |

## FAQ

### For Players

**Q**: I need help, have some questions, or have some other feedback.  
**A**: You can join the [Discord server](https://discord.gg/Q6saSVSuYQ).

**Q**: Where can I download this mod?  
**A**: [GitHub Releases](https://github.com/VidTu/HCsCR/releases).
You can also find unstable builds at
[GitHub Actions](https://github.com/VidTu/HCsCR/actions).
You'll need a GitHub account to download these.

**Q**: Which mod loaders are supported?  
**A**: Fabric, Forge, and NeoForge are supported. Quilt should work too.

**Q**: Which Minecraft versions are/were supported?  
**A**: See the "[Versions](#versions)" section.

**Q**: Why support so many Minecraft versions?  
**A**: Because I can.

**Q**: Do I need Fabric API or Quilt Standard Libraries?  
**A**: Yes, you'll need Fabric API for Fabric and QFAPI/QSL for Quilt.
Obviously, you don't need them for Forge or NeoForge.

**Q**: Is this mod client-side or server-side?  
**A**: This mod works on the client side. There is no server-side version.

**Q**: Is this a hack?  
**A**: It depends on your opinion on what's a hack and what's not. This mod
reduces the role that ping (latency) plays in combat with crystals.
Specifically, it does affect how fast the crystals are removed. Crystal
placement is NOT affected. It cannot be affected in a way that's
considered legitimate.

**Q**: Is this designed to be a hack?  
**A**: No.

**Q**: I've found a bug.  
**A**: Report it [here](https://github.com/VidTu/HCsCR/issues). If you are not
sure whether this is a bug or a simple question, you can join the
[Discord](https://discord.gg/Q6saSVSuYQ). Report security vulnerabilities
[here](https://github.com/VidTu/HCsCR/security).

**Q**: Can I use this in my modpack?  
**A**: Sure. Credit (e.g., a link to the mod's GitHub page) is appreciated but
is not required. Monetization and redistribution are allowed as per the
[Apache 2.0 License](https://github.com/VidTu/HCsCR/blob/main/LICENSE).
*BEWARE*: This mod may be considered a hack by some people.

**Q**: Why is this mod not on Modrinth or CurseForge?  
**A**: Modrinth
[says](https://github.com/user-attachments/assets/437df1a1-3331-499c-ac49-6ec114494bd4)
it violates their [rules](https://modrinth.com/legal/rules). CurseForge has a
terrible API and I don't want to deal with it.

**Q**: Why does this mod exist, when there are server-side crystal optimizers?  
**A**: Firstly, this mod was created in 2023 before the server-side crystal
optimizers. Secondly, not every server has a server-side crystal optimizer.
Thirdly, I updated it in 2025 because I needed to learn the
[Stonecutter](https://stonecutter.kikugie.dev/) preprocessor.
Now it's not hard to maintain, so why not.

**Q**: How fast is it?  
**A**: It should be pretty fast. No precise numbers,
it depends on the ping, server MSPT, etc.

<details>
<summary>Totally Real™ Reviews From Happy Users</summary>
<img alt="Grandma is happy with the crystal optimizer" src=totally_legit_review_en.png>
</details>

### For Developers

**Q**: Is this mod open source?  
**A**: [Yes.](https://github.com/VidTu/HCsCR) (Licensed
under the [Apache 2.0 License](https://github.com/VidTu/HCsCR/blob/main/LICENSE))

**Q**: I want to block this mod as a server owner, can I do this?  
**A**: A
[plugin channel](https://minecraft.wiki/w/Java_Edition_protocol/Plugin_channels)
called `hcscr:imhere` is registered by this mod. Older versions used a different
channel, but the namespace was always `hcscr`. *BEWARE*: Other mods similar to
this one don't offer a way to block them. Consider investing your time/resources
into an anti-cheat plugin if you want to block all of them.

**Q**: How to compile for only one Minecraft version?
I can't stand waiting hours for the project to initialize.  
**A**: Run the `./compileone <version>-<loader>` script with a desired version,
for example: `./compileone 1.16.5-fabric`. Note that due to the Stonecutter
requirements, the latest version may still be initialized because it is the
[VCS Version](https://stonecutter.kikugie.dev/wiki/glossary#vcs-version)
of HCsCR by design.

**Q**: Why so much yapping in this README?  
**A**: ~~I paid for the whole LLM, I'm going to use the whole LLM.~~
Because writing READMEs is easier than writing actual code.

**Q**: Do you use AI/LLM/Code Generation/Copilot/etc.?  
**A**: [Perhaps](https://github.com/VidTu/HCsCR/commit/1fd405d66e447c2dbdb775a39e3ca066b20e2fc4),
but with [consequences](https://github.com/VidTu/HCsCR/commit/1898f1dc49a3b9e154cc89b40652a5cb30ef8459).
Mostly, no, the code is 99.67% human-written, because AI is generating dumb
stuff, especially for Minecraft. If you (or some contributors) will use AI, and
it will magically® work™, good for you. I don't promote AI nor am I against it.

**Q**: Does HCsCR have a public API?  
**A**: Nope. Except for the plugin channel mentioned above, there's no
public-facing API in this mod. All classes/packages are marked as
[@ApiStatus.Internal](https://javadoc.io/static/org.jetbrains/annotations/26.1.0/org/jetbrains/annotations/ApiStatus.Internal.html)
for that reason.

**Q**: Can I still *link*/compile against to the mod? What about
the [SemVer](https://semver.org/) versioning used by the mod?  
**A**: You can, at your own risk. SemVer-compatible versioning is
used by HCsCR for ease of use, but it is used arbitrarily. This mod
does not declare a public API, therefore, breaking source/binary
changes may and will occur even between minor and patch versions.

## License

This project is provided under the Apache 2.0 License.
Check out [NOTICE](https://github.com/VidTu/HCsCR/blob/main/NOTICE) and
[LICENSE](https://github.com/VidTu/HCsCR/blob/main/LICENSE) for more information.

## Credits

This mod is developed primarily by [VidTu](https://github.com/VidTu),
but it wouldn't be possible without:

- [Contributors](https://github.com/VidTu/HCsCR/graphs/contributors).
- [Stonecutter](https://codeberg.org/stonecutter/stonecutter) by
  [KikuGie](https://codeberg.org/KikuGie). (and contributors)
- [Blossom](https://github.com/KyoriPowered/blossom) by
  [Kyori](https://github.com/KyoriPowered). (and contributors)
- [Fabric Loader](https://github.com/FabricMC/fabric-loader),
  [Fabric API](https://github.com/FabricMC/fabric), and
  [Fabric Loom](https://github.com/FabricMC/fabric-loom) by
  [FabricMC](https://github.com/FabricMC). (and contributors)
- [NeoForge](https://github.com/neoforged/NeoForge),
  [NeoGradle](https://github.com/neoforged/NeoGradle), and
  [ModDevGradle](https://github.com/neoforged/ModDevGradle) by
  [NeoForged](https://github.com/neoforged). (and contributors)
- [Forge](https://github.com/MinecraftForge/MinecraftForge),
  [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle), and
  [renamer](https://github.com/MinecraftForge/renamer) by
  [Minecraft Forge](https://github.com/MinecraftForge). (and contributors)
- [Mod Menu](https://github.com/TerraformersMC/ModMenu) by
  [TerraformersMC](https://github.com/TerraformersMC). (and contributors)
- [Mixin](https://github.com/SpongePowered/Mixin) by
  [SpongePowered](https://github.com/SpongePowered). (and contributors)
- [MixinExtras](https://github.com/LlamaLad7/MixinExtras) by
  [LlamaLad7](https://github.com/LlamaLad7). (and contributors)
- [Minecraft](https://minecraft.net/) by
  [Mojang](https://mojang.com/).

It also uses [Gradle](https://gradle.org/) and [Java](https://java.com/).

## Development

### Building (Compiling)

#### All Versions

> [!IMPORTANT]
> This is the slowest compilation method, not recommended for
> general usage, unless *all* artifacts for all supported
> versions (including "Legacy" ones) are required.
>
> On slow devices (e.g., GitHub CI),
> it might take **about 2 hours**.

To compile all [Beta, Active and Legacy](#supported-versions)
supported versions of the mod from the source code:

1. Have 8 GB of free RAM, 30 GB of free disk space,
   and an active internet connection.
2. Install Java 25 (for Gradle; you'll also need 8, 16, 17, and 21 for the
   compilation, download either of those, the others will be automatically
   downloaded via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Run `./compileall --legacy` from the terminal/PowerShell.
4. Grab the JARs from the `./build/libs/` folder.

#### Supported Versions

> [!NOTE]
> This is the normal compilation method, used during
> normal development and testing. However, it will
> exclude the "Legacy" versions from compilation.
>
> On slow devices (e.g., GitHub CI),
> it usually takes about 30 to 60 minutes.

To compile all [Beta and Active](#supported-versions)
supported versions of the mod from the source code:

1. Have 6 GB of free RAM, 20 GB of free disk space,
   and an active internet connection.
2. Install Java 25 (for Gradle; you'll also need 17 and 21 for the compilation,
   download either of those, the others will be automatically downloaded
   via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Run `./compileall` from the terminal/PowerShell.
4. Grab the JARs from the `./build/libs/` folder.

#### One Version

> [!TIP]
> This is the fastest compilation method. It might
> be useful if you want to compile mod for just
> one Minecraft version for personal usage.
>
> On slow devices (e.g., GitHub CI),
> it usually takes about 15 to 30 minutes.

To compile one specific Minecraft version of the mod from the source code:

1. Have 4 GB of free RAM, 10 GB of free disk space,
   and an active internet connection.
2. Install Java 25 (for Gradle; you may also need 8, 16, 17, or 21 for the
   compilation, download either of those, the other will be automatically
   downloaded via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Run `./compileone <version>-<loader>` from the terminal/PowerShell.
   (for example: `./compileone 1.16.5-fabric`)
4. Grab the JAR from the `./build/libs/` folder.

### Developing/Debugging

Run the `./launch <version>` (e.g. `./launch 1.16.5-fabric`) command to
launch the game client. You can attach a debugger to that process.
Hotswap is supported. "Enhanced" hotswap (class redefinition) and
hotswap agent will work if supported by your JVM.

Switch the current active Stonecutter version by using `./switch <version>`
command. It is discouraged to modify code commented out by the preprocessor,
switch to the required version instead.

Reset to the VCS Stonecutter version before committing
changes via `./reset` command to avoid a diff mess.

Running the client via generated tasks (e.g., for IntelliJ IDEA) may work, but
you might need to make some adjustments. Launching the game directly
(without Gradle) might also work, but it is also not supported.

The development environment has stricter preconditions: Mixin checks,
Netty detector, Java assertions, etc. Code with bugs might (and probably will)
fail faster here than in a production environment.

The recommended IDE for development is IntelliJ IDEA (Community or Ultimate)
with the Minecraft Development plugin. This is not a strict requirement,
however. Any IDE/editor should work just fine.
