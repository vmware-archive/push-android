#!/bin/bash
curl -v --header "Content-Type: application/json" -X PUT --data @- http://cfms-push-service-staging.one.pepsi.cf-app.com/v1/registration/02abc36e-1026-4a82-a54e-db0443759b4e <<POST_BODY
{"device_alias":"android_test_device","device_manufacturer":"LGE","device_model":"Nexus 5","os_version":"4.4.2","registration_token":"APA91bEWpf7ZrX89uReL_G4j-8IRWpsCgLFto8ONC1Cx_ah1E4PUUd-W52eYeZgfCbJkWucBYDFjw8DFAmAg1rnLckTg-PNXxqXXeTSiSSz4zhtBvblbLIwnBHPyf1lZlrftmwPRf_DAo8QWEyBJmgaOGlipxi97zPuzzofqSAj63zWWvCRUlD4","secret":"d1335dab-32b8-4957-8aa2-9139716e74a1","variant_uuid":"7d588b87-4af8-4783-8ce9-50bee8890f24"}
POST_BODY
