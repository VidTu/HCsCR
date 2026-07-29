<img alt src=hcscr_dev.png>

# HCsCR (Dev's Corner)

This is the page with various technical information for the HCsCR mod.

**Check out the [main page](https://github.com/VidTu/HCsCR/blob/main/README.md)
if you are not a technical person/not a developer. (and not a nerd)**

## Language

- **English**
- [Русский](https://github.com/VidTu/HCsCR/blob/main/CONTRIBUTING_ru.md)

## Developer FAQ

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

**Q**: Why so much yapping in the README.md and CONTRIBUTING.md?  
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

## Building (Compiling)

### One Version

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
2. Install Java 25 (for Gradle; you may also need 8, 17, or 21 for the
   compilation, download either of those, the other will be automatically
   downloaded via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Clone or download the repository. (`.git` folder is *not* required)
4. Run `./compileone <version>-<loader>` from the terminal/PowerShell
   from within the downloaded repository folder.
   (for example: `./compileone 1.16.5-fabric`)
5. Grab the JAR from the `./build/libs/` folder.

### Supported Versions

> [!NOTE]
> This is the normal compilation method, used during
> normal development and testing. However, it will
> exclude the "Legacy" versions from compilation.
>
> On slow devices (e.g., GitHub CI),
> it usually takes about 30 to 60 minutes.

To compile all [Beta and Active](#versions)
supported versions of the mod from the source code:

1. Have 6 GB of free RAM, 20 GB of free disk space,
   and an active internet connection.
2. Install Java 25 (for Gradle; you'll also need 21 for the compilation,
   download either of those, the other will be automatically downloaded
   via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Clone or download the repository. (`.git` folder is *not* required)
4. Run `./compileall` from the terminal/PowerShell
   from within the downloaded repository folder.
5. Grab the JARs from the `./build/libs/` folder.

### All Versions

> [!IMPORTANT]
> This is the slowest compilation method, not recommended for
> general usage, unless *all* artifacts for all supported
> versions (including "Legacy" ones) are required.
>
> On slow devices (e.g., GitHub CI),
> it might take **about 2 hours**.

To compile all [Beta, Active and Legacy](#versions)
supported versions of the mod from the source code:

1. Have 8 GB of free RAM, 30 GB of free disk space,
   and an active internet connection.
2. Install Java 25 (for Gradle; you'll also need 8, 17, and 21 for the
   compilation, download either of those, the others will be automatically
   downloaded via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Clone or download the repository. (`.git` folder is *not* required)
4. Run `./compileall --legacy` from the terminal/PowerShell
   from within the downloaded repository folder.
5. Grab the JARs from the `./build/libs/` folder.

## Developing/Debugging

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

### Debug JARs

The `ru.vidtu.hcscr.debug` boolean Gradle property allows producing
more debuggable JARs. It controls the following sub-properties:

- `ru.vidtu.hcscr.debug.javac`: Tell `javac` to emit `-parameters`
  data for reflection. Reflective data includes method parameter flags
  and names, which might be useful for debugging or decompilation.
- `ru.vidtu.hcscr.debug.metadata`: Don't use the custom post-processor
  (see the `buildSrc` folder) to strip annotations and other metadata
  from class-files like `SourceFile` attributes and `@deprecated` tags.
- `ru.vidtu.hcscr.debug.asserts`: Keep `assert` statements after
  compilation for better debuggability. Note that assertions must
  be enabled using `-enableassertions` VM flag on most JVMs.
- `ru.vidtu.hcscr.debug.logs`: Change logging calls:
  - Enable `debug` and `trace` logging calls and create loggers for them
    in some classes where loggers are not created during normal execution.
  - Add a `MOD_HCSCR` marker to every logging call.
  - Always produce stack-traces for exceptions.
  - Produce more logging details. (more context, more parameters, etc.)
- `ru.vidtu.hcscr.debug.resources`: Don't minify resource files that are
  *not* Java classes, such as `.json`, `.toml`, `.mcmeta` files and others.
- `ru.vidtu.hcscr.debug.package`: Don't strip `package-info.class` files.

More specific debug properties will override the global one.
All debug properties are *disabled* by default. See the
[Gradle documentation](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties)
for more information on how to set Gradle properties.

For example, you can compile the "JAR with all debug properties"
using `./gradlew -Pru.vidtu.hcscr.debug=true assemble`.

### Slim JARs

> [!WARNING]
> **Note**: This option *will* make it harder to know what parts of the mod are
> causing issues. Do *not* enable this option unless you know what you're doing!

If you want to produce extra-small JARs at the cost of debuggability, you
can set the `ru.vidtu.hcscr.slim` boolean Gradle property to `true`.

This property is incompatible with the Debug JARs options.
