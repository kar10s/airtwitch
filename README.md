[![Build Status](https://travis-ci.org/mdreier/airtwitch.svg?branch=master)](https://travis-ci.org/mdreier/airtwitch)
[![codecov](https://codecov.io/gh/mdreier/airtwitch/branch/master/graph/badge.svg)](https://codecov.io/gh/mdreier/airtwitch)

# AirTwitch
A library and clients to stream Twitch live streams to an Airplay device.

## Usage
There is only one client provided, an CLI application. A graphiccal UI is planned.

### Using the library
The AirTwitch library (project `airtwitch-core`) provides two entry points.

To get devices, simply start the service discovery and register a listener.

````java
AirPlayServiceDiscovery.registerListener(device -> System.out.println("Found device: " + device.getName());
````
The listener will be called every time a new device is dicovered. Remember to shut down the discovery when 
ending the application. It starts a daemon thread which will prevent the JVM from terminating.
````java
AirPlayServiceDiscovery.close();
````

The Twitch API provides a slim section of the full API, just enough to get the live streams of a channel. Remember 
to get the channel token before accessing the stream list.
````java
Twitch twitch = new Twitch();
List<Channel> channels = twitch.searchChannels("stream name");
//or
Channel channel = twitch.getChannelById(12345);

channel.requestChannelToken();
List<LiveStream> streams = channel.getLiveStreams();
````

To start playback, simply pass the stream URI to the device:
````java
StreamControl streamControl = device.createStream(stream.getStreamUri());
streamControl.play();
//wait...
streamControl.stop();
````

### The command line client
Start the client by running the JAR. Type `help` to print help information.

Select a device by going to the device cubment (enter `d` or `device`) and select a device from the list. Type `exit` 
to return to the main menu.

Search for a stream by going to the stream menu (enter `s` or `stream`) and search for a channel using the `search`
command (e.g. `search mychannel`). Select the channel and stream you want to watch and type `exit` 
to return to the main menu.

Finally you can start the stream using the `play` command. Press the enter key to terminate the stream and `exit` to 
leave the application.

## Twitch client ID
To run, the application needs a [Twitch Client ID](https://dev.twitch.tv/docs/v5/guides/using-the-twitch-api/)
for calls to the Twitch API. You can create it on the [connections page](https://www.twitch.tv/settings/connections) 
of your Twitch account. Register a new developer application on the bottom of that page.

To provide the ID to the application, you have four options:

1. Provide it as Java system property `twitchClientId`
2. Provide it as environment variable `TWITCH_CLIENT_ID`
3. Provide it in a file named `twitch_client_id` in the classpath of the application.

You can also include it in the built application, see the build section below.

# Build
Clone the repository, then run `mvn clean package` to build the library and the clients.

## Setting the Twitch Client ID
To include the Twitch Client ID in the final application, simply  set the environment variable `TWITCH_CLIENT_ID` 
before starting the build. It will be automatically included.
