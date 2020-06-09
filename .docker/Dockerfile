FROM registry-vpc.cn-beijing.aliyuncs.com/wendal/jdk8:1.0.0
#RUN apt update & apt install -y ffmpeg && rm -r /var/cache/
COPY . /opt/walnut/
WORKDIR /opt/walnut
CMD java -Dfile.encodig=UTF-8 -Djava.awt.headless=true -jar /opt/walnut/walnut.jar

