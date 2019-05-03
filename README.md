[![Build Status](https://travis-ci.com/spine-examples/blog.svg?branch=master)](https://travis-ci.com/spine-examples/examples-java) &nbsp;
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)


# Blog Example

This example shows a simple Blog Context application.

## Running

A local gRPC server receiving commands and queries for the Blog Context can be started with: 
```sh
./gradlew :server:run
```

By default, it listens on the port 50051. To start it on a different port, use:
```sh
/gradlew :server:run -Dport=PORT_NUMBER
```
