#!/bin/bash

build_image () {
  cd "$1" || exit
  mvn clean package || exit
  docker build . -t "$1" -f Dockerfile || exit
  cd ..
}

build_image api-gateway
build_image identity-service
build_image db-migrator

docker compose up