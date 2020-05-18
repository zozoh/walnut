#!/bin/bash

image="registry.cn-beijing.aliyuncs.com/wendal/walnut"  
  
#get timestamp for the tag  
timestamp=$(date +%Y%m%d%H%M%S)  
  
tag=$image:$timestamp  
latest=$image:latest  


#./gradlew clean lessc wtar
cp .docker/* build/wzip
cd build/wzip
docker build . -t $tag
docker tag $tag $latest
docker push $tag
docker push $latest

