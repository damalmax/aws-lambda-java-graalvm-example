## AWS Lambda example written in Java and compiled with GraalVM Native Image

## Prerequisites
- Install Docker
- Prepare Docker GraalVM Native Image based on AmazonLinux v2
```shell
$ cd docker
$ docker build -t native-image .
```

## How to build
```shell
$ ./gradlew clean awsLambda
```
