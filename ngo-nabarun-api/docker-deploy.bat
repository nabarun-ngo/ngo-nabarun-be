@echo off
mvnw clean package
docker build . -t souvik98/nabarunapp:v1 
docker push souvik98/nabarunapp:v1