[hugo]: https://github.com/HugoCarvalho99
[hugo-pic]: https://github.com/HugoCarvalho99.png?size=120
[nelson]: https://github.com/nelsonmestevao
[nelson-pic]: https://github.com/nelsonmestevao.png?size=120
[pedro]: https://github.com/pedroribeiro22
[pedro-pic]: https://github.com/pedroribeiro22.png?size=120

# Anonymous Gateway

> Originator Anonymization Overlay Network

## :rocket: Getting Started

These instructions will get you a copy of the project up and running on your
local machine for development and testing purposes.

Start by filling out the environment variables defined in the `.env` file. Use
the `.env.sample` as a starting point.

```bash
cp .env.sample .env
```

After this, you must fill in the fields correctly and export them in your
environment. Checkout [direnv](https://direnv.net/) for your shell and
[EnvFile](https://github.com/Ashald/EnvFile) for IntelliJ.

### :inbox_tray: Prerequisites

The following software is required to be installed on your system:

- [Java SDK 11](https://openjdk.java.net/)
- [Maven](https://maven.apache.org/maven-features.html)

### :hammer: Development

Start a AnonGW instance. You need to pass a list of peers because it's different
for each instance.

```
bin/anongw "<peers...>" [<port> <host> <udp> <ip>]
```

You can use `netcat` as the target server or use our simple script to setup a
HTTP server.

```
bin/server [<port> <host>]
```

For testing purposes, you can connect to it through also with `curl`.

```
curl -i -X GET <AnonGW IP>:<port>
```

Running tests.

```
bin/test
```

Format the code accordingly to common guide lines.

```
bin/format
```

Lint your code with _checkstyle_.

```
bin/lint
```

### :microscope: Testing

For testing purposes use the CORE emulator with [this](resources/topology.imn)
topology in order to create a suitable test scenario.

![Topology](resources/images/network.png)

Run the HTTP server on `Serv1@10.3.3.1`. Setup at least 3 instances of AnonGW on
3 different nodes. The recommendation is to setup one on `Serv3@10.3.3.3`,
another on `Serv2@10.3.3.2` and one on `Zeus@10.4.4.2`. The client should 
connect from `Portatil1@10.1.1.1` to any one of them.

### :package: Deployment

Bundling the app into jar file.

```
mvn package
```

### :hammer_and_wrench: Tools

The recommended Integrated Development Environment (IDE) is [IntelliJ
IDEA](https://www.jetbrains.com/idea/).

For emulating a network use the Common Open Research Emulator (CORE). Setup
instructions for setting it up on a Virtual Machine with Ubuntu 20.04 are
available on our wiki [here](https://gitlab.com/mieiuminho/CC/anongw/-/wikis/Setup-CORE).

## :busts_in_silhouette: Team

| [![Hugo][hugo-pic]][hugo] | [![Nelson][nelson-pic]][nelson] | [![Pedro][pedro-pic]][pedro] |
| :-----------------------: | :-----------------------------: | :--------------------------: |
|   [Hugo Carvalho][hugo]   |    [Nelson Estevão][nelson]     |    [Pedro Ribeiro][pedro]    |
