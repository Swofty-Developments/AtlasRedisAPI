# Atlas Redis API
![badge](https://img.shields.io/github/v/release/Swofty-Developments/AtlasRedisAPI)
[![badge](https://jitpack.io/v/Swofty-Developments/AtlasRedisAPI.svg)](https://jitpack.io/#Swofty-Developments/AtlasRedisAPI)
![badge](https://img.shields.io/github/downloads/Swofty-Developments/AtlasRedisAPI/total)
![badge](https://img.shields.io/github/last-commit/Swofty-Developments/AtlasRedisAPI)
[![badge](https://img.shields.io/discord/830345347867476000?label=discord)](https://discord.gg/atlasmc)
[![badge](https://img.shields.io/github/license/Swofty-Developments/AtlasRedisAPI)](https://github.com/Swofty-Developments/AtlasRedisAPI/blob/master/LICENSE.txt)

**[JavaDoc 1.0.2](https://swofty-developments.github.io/AtlasRedisAPI/)**

Used by Atlas Network. Simple but blazingly fast all-purpose Redis API. Perfect for use in JSP, Minecraft, Server Backends or just about anything else!

## Table of contents

* [Getting started](#getting-started)
* [Connecting to Redis Server](#connecting-to-redis-server)
* [Subscribing to a channel](#subscribing-to-a-channel)
* [Sending messages to a specific server](#subscribing-to-a-specific-server)
* [Publishing messages](#publishing-messages)
* [Events & incoming messages](#events--incoming-messages)
* [License](#license)

## Getting started

This API is intended for stand-alone usage, meaning that you do not need to run any extra spigot-plugins to use this library.

### Add AtlasRedisAPI to your project 

[![badge](https://jitpack.io/v/Swofty-Developments/AtlasRedisAPI.svg)](https://jitpack.io/#Swofty-Developments/AtlasRedisAPI)

First, you need to setup the dependency on the AtlasRedisAPI. Replace **VERSION** with the version of the release.

<details>
    <summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Swofty-Developments</groupId>
        <artifactId>AtlasRedisAPI</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```
</details>

<details>
    <summary>Gradle</summary>

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Swofty-Developments:AtlasRedisAPI:VERSION'
}
```
</details>

## Connecting to Redis Server

Before doing anything on the API, it is necessary to generate an instance of the RedisAPI class to connect to your Redis server.

```java
// 
// The standard way of connecting to an instance
// Pass through a redis uri, must follow valid redis schema
// Inside of the URI, you are able to pass through the IP, PORT, USERNAME and PASSWORD
//
RedisAPI.generateInstance("redis://localhost:6379");
```

## Subscribing to a channel

To receive data from your Redis server, you need to subscribe selected channels. You can do it simply just by calling:

```java
//
// Note that you can register one class to more than one channel name, making it so that one listener class handles multiple channels.
//
RedisAPI.getInstance().registerChannel(
    "cove", // This is the name of the channel
    ExampleListener.class // Your listener class, see more about listening to Redis messages below.
);
```

## Sending messages to a specific server

Due to the nature of this API - There are probably going to be situations in which you will probably want to send a message to a specific pool listening to a channel. To do this, you need to add a Filter ID to your RedisAPI instance, this Filter ID is then checked against whenever you send a message from a different connection, check 'Publishing Messages' for more information on how to do that.
```java
RedisAPI.getInstance().setFilterID("bungee"); // This RedisAPI instance will now block out any messages that do not have this filter id passed through with it.
```

## Publishing messages

You can easily publish messages to the RedisAPI instance. It is not required to subscribe channel before you publish a message:

```java
// For sending a message to every single instance of the Redis pool listening to the channel
RedisAPI.getInstance().publishMessage(
    ChannelRegistry.getFromName("cove"), // The name that you pass through here is the same as the name you pass through when registering a channel, look at 'Subscribing to a channel' for more information
    "examplemessage" // The message that you want to pass through the channel
);

// For sending a message to a specific instance of the Redis pool listening to a channel
RedisAPI.getInstance().publishMessage(
    "bungeecordserver", // This is the filter ID for the message, meaning that only Redis pools that have their filter code set to this value will recieve the message
    ChannelRegistry.getFromName("cove"), // The name that you pass through here is the same as the name you pass through when registering a channel, look at 'Subscribing to a channel' for more information
    "examplemessage" // The message that you want to pass through the channel
);
```

## Events & Incoming messages

Using AtlasRedisAPI you can retrieve data from Redis using bukkit's (bungee's) Listeners. **But make sure the correct
Event is chosen as the names are same for Bungee and Spigot!**

```java
//
// Only messages that are passed through the channel name given when registering the channel class will be passed onto this event.
// So if this class is registered using RedisAPI.getInstance().registerChannel("cove", ExampleListener.class) then this class will only listen to messages coming through the "cove" channel.
//
public class ExampleListener implements RedisMessagingReceiveEvent {
    @Override
    public void onMessage(String channel, String message) {
        System.out.println("test");
    }
}

```

## License
AtlasRedisAPI is licensed under the permissive MIT license. Please see [`LICENSE.txt`](https://github.com/Swofty-Developments/AtlasRedisAPI/blob/master/LICENSE.txt) for more information.
