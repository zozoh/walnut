FROM alpine:latest

MAINTAINER wendal <wendal1985@gmail.com>

RUN apk update && \
    apk add bash openssh python fuse && \
    mkdir -p ~root/.ssh && chmod 700 ~root/.ssh/ && \
    echo -e "Port 22\n" >> /etc/ssh/sshd_config && \
    cp -a /etc/ssh /etc/ssh.cache && \
    rm -rf /var/cache/apk/*

RUN ssh-keygen -A

EXPOSE 22

RUN mkdir -p /opt/walnut && mkdir -p /opt/fuse && echo -e "PermitRootLogin yes\n" >> /etc/ssh/sshd_config
COPY entry.sh /entry.sh
COPY ess.py /opt/walnut/ess.py
COPY fuse.py /opt/walnut/fuse.py
COPY requests /opt/walnut/requests

ENV WALNUT_HOST wendal.ngrok.wendal.cn
ENV WALNUT_PORT 80
ENV WALNUT_USERNAME wendal
ENV WALNUT_SEID GODKEY

ENTRYPOINT ["/entry.sh"]

#CMD ["/usr/sbin/sshd", "-D", "-f", "/etc/ssh/sshd_config"]
