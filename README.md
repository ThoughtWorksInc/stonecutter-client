# stonecutter-client
Stub app for the stonecutter oath application


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
