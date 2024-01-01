# Hotswap Agent Plugin for Forge 1.8.9 (and similar versions)

Dispatches Forge events for classes that are being reloaded by DCEVM via a Hotswap Agent Plugin.

## Usage

This mod does not touch any minecraft code at all, so as long as the forge event loop and the launchwrapper is
compatible this mod can be used in any minecraft version. It was primarily tested in 1.8.9.

First install DCEVM and Hotswap Agent like normally.

> [!WARNING]
> DCEVM and Hotswap Agent are two separate installations.
> Check your logs for
> ```
> Loading Hotswap agent {VERSION} - unlimited runtime class redefinition.
> ```
> If it is not present, and you can still hotswap classes, you have only installed DCEVM, not hotswap agent.

First download hotswap agent into your classpath. Loading as a mod in your mods folder *might* work, but I cannot
give any guarantees on that.

```kts
repositories {
    maven("https://repo.nea.moe/releases")
}

dependencies {
    // Can be remapped or not. Does not matter, since it does not touch MC code
    // Check for the latest version at https://repo.nea.moe/#/releases/moe/nea/hotswapagent-forge
    implementation("moe.nea:hotswapagent-forge:1.0.0")
}
```

Next create a `src/main/resources/hotswap-agent.properties`
([Official documentation](http://hotswapagent.org/mydoc_configuration.html)):

```properties
pluginPackages=moe.nea.hotswapagentforge.plugin
```

Now launch using DCEVM with Hotswap Agent and check your console. If you can find a line loading the plugin you are
ready.

```
INFO (org.hotswap.agent.config.PluginRegistry) - Discovered plugins: [Forge, PotentiallyOtherPlugins]
```

You can also check `HotswapEvent#isReady()` and co to see if the plugin and tweaker were loaded successfully. Now just
subscribe to `HotswapEvent`s child classes on the normal `MinecraftForge.EVENT_BUS`. You might only want to register
the classes pertaining to hotswap in a development environment, since shipping Hotswap Agent Forge is advised against.
You can check `Launch.blackboard.get("fml.deobfuscatedEnvironment")` to see if you are in a development environment.

Loading Hotswap Agent Forge without Hotswap Agent present is just a pure noop, and should not cause any problems.

## Architecture

> [!IMPORTANT]
> This is a very rough architectural overview and is slightlyinaccurate in favor of being understandable in parts.

### DCEVM

DCEVM is a JVM that allows for enhanced class redefinition. This means you can reload method bodies, add new methods,
delete methods, add fields, delete fields, and more. This is commonly called "hotswapping" a (set of) class(es).

### Hotswap Agent

Hotswap Agent is an extension to DCEVM that allows for the dynamic reloading of DCEVM to be translated to the
application level. For that Hotswap Agent has some built in plugins, but also allows user defined plugins. Some of the
builtin plugins allow for generic things like re-running static initialization, but other plugins exist for common
frameworks like spring.

### Hotswap Agent Forge

Hotswap Agent Forge consists of three parts, the plugin, the tweakers, and the events.

#### The Plugin

Understanding the daisy changing going on in this section is probably easier after reading about
[class loaders](#class-loaders).

The plugin is just a standard Hotswap Agent Plugin. It consists of a static component that changes the tweaker to load
the non-static plugin. This needs to be done, so we can obtain a reference to the correct class loader and tweaker.

Once the non-static plugin is loaded (which is located in the same class) it listens for all class definitions and
redefinitions and uses the scheduler and command apis of Hotswap Agent to call functions on the tweaker loaded in the
application class loaded.

#### The Tweakers

There is an IFMLLoadingPlugin here, but all that does is load the tweaker. The tweaker itself does three important
things.

First it registers a class transformer which does absolutely nothing to the classes, but informs the tweaker when two
important classes are loaded. The forge event bus, and the HotswapEvent we will fire. This informs us that the required
JARs are available in the launch class loader and have been used at least once.

The second thing it does that it serves as an injection point into the application class loader. The plugin needs to
call into the application and cannot work from the bootstrap class loader itself. So whenever the tweaker class is
loaded by the launchwrapper it calls into the hotswap agent plugin via `PluginManagerInvoker`. This call isn't naturally
present in the tweaker, but is injected by the plugin via a class transformation done by hotswap agent (as opposed to a
class transformation done by the launch class loader).
In theory this step could be done on any class in the application class loader, or even in the launch class loader
but using a tweaker allows us to stay very consistent by being in a known good class loader very quickly without having
to worry about class initialization order inside of the plugin.

The third thing the tweaker does is to function as an event handler for the plugin. Whenever the plugin queues and the
hotswap agent scheduler dispatches an event, it will call a corresponding function on the tweaker. The tweaker then
needs to daisy-chain through another class loader (the launch class loader), since it is most likely loaded from the
application class loader. From there it uses reflection to call into the forge event bus and dispatch events.

#### The events

The events are the part the end user actually touches. Those are just POJOs inside the launch class loader that can be
passed around freely, and are dispatched via the standard `MinecraftForge.EVENT_BUS`. They are dispatched on whatever
thread is deemed to be fit by the hotswap agent, so most likely not the main minecraft thread.

## Addendum

### Class Loaders

There are three main class loaders in play. Loading a class in the wrong class loader can lead to two class instances
existing which are not the same. Because your second loaded class will not incur changes in the first class in a
different class loader. Sometimes accessing a class from another class loader *can* be fine, but that is dependent on
the specific class loader, and it is almost always a unidirectional property.

- Application Class Loader

This is the main class loader used by java. It typically is an URLClassLoader and is used to load the launchwrapper and
early FML classes. It may or may not also load tweakers and IFMLLoadingPlugins, depending on how they are defined.

- LaunchClassLoader / Minecraft Class Loader

The LaunchClassLoader is the class loader used to load minecraft classes. It allows for class transformations and falls
back on the application class loader. Mods are loaded from this class loader, as well as forge. Tweakers are handed a
reference to this class loader so that they can register class transformations and load classes inside the minecraft
environment.

- "Bootstrap" Class Loader

While this isn't technically the bootstrap class loader in this version of Java (i think), this class loader is used
to load java agents (such as Hotswap Agent and its plugins) as well as the Java rt itself to some extent. Not willing to
further disambiguate in this document i will just use "bootstrap class loader/path" as a nebulous term for a class
loader that can't be accessed or modified by the application easily.

