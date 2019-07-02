# How deploy prometheus-rsocket-proxy on Cloud Foundry

**TCP Router is required**.

https://github.com/micrometer-metrics/prometheus-rsocket-proxy

```
SPACE_NAME=playground
APP_NAME=prometheus-rsocket-proxy
APPS_DOMAIN=apps.pcfone.io
TCP_DOMAIN=tcp.apps.pcfone.io
TCP_PORT=10002
APP_PORT=7001

cf create-route ${SPACE_NAME} ${APPS_DOMAIN} --hostname ${APP_NAME}
cf create-route ${SPACE_NAME} ${TCP_DOMAIN} --port ${TCP_PORT}
cf push ${APP_NAME} --docker-image micrometermetrics/prometheus-rsocket-proxy:latest --no-route

APP_GUID=$(cf app ${APP_NAME} --guid)
HTTP_ROUTE_GUID=$(cf curl /v2/routes?q=host:${APP_NAME} | jq -r .resources[0].metadata.guid)
TCP_ROUTE_GUID=$(cf curl /v2/routes?q=port:${TCP_PORT} | jq -r .resources[0].metadata.guid)

cf curl /v2/apps/${APP_GUID} -X PUT -d "{\"ports\": [8080, ${APP_PORT}]}"

cf curl /v2/route_mappings -X POST -d "{\"app_guid\": \"${APP_GUID}\", \"route_guid\": \"${HTTP_ROUTE_GUID}\", \"app_port\": 8080}"
cf curl /v2/route_mappings -X POST -d "{\"app_guid\": \"${APP_GUID}\", \"route_guid\": \"${TCP_ROUTE_GUID}\", \"app_port\": ${APP_PORT}}"
```