# Gateway benchmark

This is simple test for benchmark current stage of gateway

## Module

### gateway

Demo module use spring cloud gateway
Port: 9100

### zuul

Demo module use Zuul 1 proxy
Port: 9200

### resource server

Simple Http server write with Go
Port: 9000

## Install benchmarking tool

[Prepare wrk](https://github.com/wg/wrk)

```shell script
sudo apt install wrk
```

## Build & Run service

Build and start

```shell script
bash ./start
```


Stop 

```shell script
bash ./stop.sh
```

Run docker wrk

```shell script
bash docker-compose.yml
```

Run bench mark test

```shell script
./test.sh

```
