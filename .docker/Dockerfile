#FROM registry-vpc.cn-beijing.aliyuncs.com/wendal/jdk8:1.0.0
#FROM openjdk:15-jdk-alpine
# 切换到 JDK17，因为 Mongo 的 Driver 版本提升了
FROM openjdk:17
#RUN apt update & apt install -y ffmpeg && rm -r /var/cache/
#RUN apk add --no-cache fontconfig ttf-dejavu
COPY . /opt/
WORKDIR /opt/walnut
CMD java -Djava.awt.headless=true -jar /opt/walnut/walnut.jar
