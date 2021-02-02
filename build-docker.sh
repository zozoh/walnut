#!/bin/bash

image="registry-vpc.cn-beijing.aliyuncs.com/wendal/walnut"  
  
#get timestamp for the tag  
timestamp=$(date +%Y%m)  
  
tag=$image:$timestamp  
latest=$image:latest  


#./gradlew clean lessc wtar
mkdir build/docker/
mkdir build/docker/walnut/
mkdir build/docker/titanium/
cp .docker/* build/docker/
cp -rf build/wzip/* build/docker/walnut/
cp .docker/web_local.properties build/docker/walnut/
rm -fr build/docker/walnut/libs/
rm -fr build/docker/walnut/classes/
cp -rf /root/repo/titanium/src build/docker/titanium/

cd build/docker/

docker build . -t $tag
docker tag $tag $latest
docker push $tag
docker push $latest

