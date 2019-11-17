# Gateway benchmark

This is simple test for benchmark current stage of gateway

## Module

### resource server

Simple Http server write with Go

Port: 9000

### gateway

Demo module use spring cloud gateway

Port: 9100

### zuul

Demo module use Zuul 1 proxy

Port: 9200


### zuul 2

Demo module use Zuul 2 proxy

Port: 9300

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

## Result

### Gateway
  12 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    70.54ms   62.63ms 797.14ms   87.71%
    Req/Sec   530.79    239.09     1.08k    60.57%
  187072 requests in 30.10s, 41.39MB read
Requests/sec:   6215.66
Transfer/sec:      1.38MB

### Zuul
  12 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   172.24ms  228.76ms   2.00s    88.67%
    Req/Sec   245.14    115.50   620.00     67.72%
  84583 requests in 30.09s, 20.10MB read
  Socket errors: connect 0, read 0, write 0, timeout 346
Requests/sec:   2811.38
Transfer/sec:    684.06KB

### Zuul 2