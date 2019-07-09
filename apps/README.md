Deploy [Syslog to Elasticsearch](https://github.com/making/syslog-to-elasticsearch) first.

```
cf create-user-provided-service syslog-to-elasticsearch -l syslog://tcp.apps.pcfone.io:10014

./build-and-push-all.sh
```
