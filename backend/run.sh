#!/bin/bash

set -e

docker compose build account-service
docker compose build agent-service
docker compose build aggregation-service
docker compose build normalisation-service
docker compose build log-analysis-service
docker compose build log-saving-service
docker compose build log-query-service
docker compose build alert-service
docker compose build notification-service

docker compose up -d
