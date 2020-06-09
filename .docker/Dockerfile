#FROM registry-vpc.cn-beijing.aliyuncs.com/wendal/jdk8:1.0.0
FROM openjdk:8-alpine
#RUN apt update & apt install -y ffmpeg && rm -r /var/cache/
RUN apk add --no-cache fontconfig ttf-dejavu
COPY . /opt/walnut/
WORKDIR /opt/walnut
CMD java -Djava.awt.headless=true -jar /opt/walnut/walnut.jar

