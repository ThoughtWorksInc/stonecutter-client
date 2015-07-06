# stonecutter-client

[![Build Status](https://snap-ci.com/ThoughtWorksInc/stonecutter-client/branch/master/build_image)](https://snap-ci.com/ThoughtWorksInc/stonecutter-client/branch/master)

Stub app for the stonecutter oath application

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Development VM

You can also develop and run the application in a VM.  You will need [Vagrant][] installed.

navigate to the ops/ directory of the project and run:
    
    cd ops
    vagrant up

When the VM has started, access the virtual machine by running:

	vagrant ssh

The source folder will be located at /var/stonecutter-client


[Vagrant]: https://www.vagrantup.com

## Running
Before starting the server, build the views by running:

    gulp build

To start a web server for the application in development mode, run:

    lein ring server-headless
    
NB: running the application like this will save users into an in memory cache that will be destroyed as soon as the app is shutdown.

To start a web server with users persisted to mongodb, ensure you have mongo running locally and run:

    lein run


## Running the static frontend

### Getting started

First install [brew](http://brew.sh/)

```
brew install node
npm install
```

You also require gulp to be installed globally.

```
npm install -g gulp 
```

Depending on system privileges you may need to install it globally with sudo:
 
```
sudo npm install -g gulp 
```
 
### Running the prototype

####Simply type
```
gulp server
```

## Running the client app as a test-client

### Getting started

First start [Stone Cutter](www.github.com/ThoughtWorksInc/stonecutter) auth server in memory

```
lein ring server
```

It will register a test user and client account
then, log INFO of "TEST USER DETAILS" and "TEST CLIENT DETAILS"
export the **client-id** and **client-secret** in the sesion where you intend to start your Stonecutter-client

```
export CLIENT_ID=<insert :client-id string here>
export CLIENT_SECRET=<:client-secret here>
```

You can also optionally set the path of your OAuth server, (this currently defaults to `http://localhost:3000`
```
export SCAUTH_PATH=<insert auth server url here>
```

### Running the test client

After exporting the correct environment variables, you should be able to demo the Auth flow via stonecutter-client by typing

```
lein ring server
```

#### Available routes
refer to [routes.txt](https://github.com/ThoughtWorksInc/stonecutter-client/blob/master/resources/routes.txt)
