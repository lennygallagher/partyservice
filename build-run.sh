#!/usr/bin/env bash

mvn clean install
docker build -t maria/partyservice:${1} .
docker stop partyservice
docker rm partyservice
docker run -d --name partyservice --net=hackathon -p 8081:8080 maria/partyservice:${1}
