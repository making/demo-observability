# How deploy Zipkin on Cloud Foundry

```
SPACE_NAME=playground
APP_NAME=zipkin
APPS_DOMAIN=apps.pcfone.io
APP_PORT=9411

cf create-route ${SPACE_NAME} ${APPS_DOMAIN} --hostname ${APP_NAME}
cf push ${APP_NAME} --docker-image openzipkin/zipkin --no-route --no-start -m 512m

APP_GUID=$(cf app ${APP_NAME} --guid)
HTTP_ROUTE_GUID=$(cf curl /v2/routes?q=host:${APP_NAME} | jq -r .resources[0].metadata.guid)
cf curl /v2/apps/${APP_GUID} -X PUT -d "{\"ports\": [${APP_PORT}]}"
cf curl /v2/route_mappings -X POST -d "{\"app_guid\": \"${APP_GUID}\", \"route_guid\": \"${HTTP_ROUTE_GUID}\", \"app_port\": ${APP_PORT}}"

cf start ${APP_NAME}
```