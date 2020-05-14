FROM postgres:latest
ENV container docker
ENV DEBIAN_FRONTEND noninteractive
RUN echo 'APT::Install-Recommends "0"; \n\
APT::Get::Assume-Yes "true"; \n\
APT::Get::force-yes "true"; \n\
APT::Install-Suggests "0";' > /etc/apt/apt.conf.d/01buildconfig && \
    apt-get update && apt-get install wget gnupg ca-certificates && \
    apt-get update && \
    apt-get install \
        less \
        bind9-host \
        net-tools \
        iputils-ping \
        sudo \
        vim \
        telnet \
        openssh-server \
        faketime \
        iptables && \
    rm -rf /var/run && \
    ln -s /dev/shm /var/run


COPY ./boostrap-pg.sql /docker-entrypoint-initdb.d/
