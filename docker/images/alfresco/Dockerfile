FROM ubuntu:16.04
MAINTAINER civilis@acme.com

#Alfresco version
ENV ALF_URL=http://dl.alfresco.com/release/community/201612-build-00014/alfresco-community-installer-201612-linux-x64.bin
ENV ALF_HOME=/opt/alfresco

RUN mkdir -p /app/assets

# install alfresco
COPY assets/setup/install_alfresco.sh /app/assets/install_alfresco.sh
RUN chmod +x /app/assets/install_alfresco.sh
RUN /app/assets/install_alfresco.sh

# Logrotate
COPY assets/setup/logrotate-alfresco.conf /etc/logrotate.d/alfresco

# init scripts
COPY assets/init.py /app/
COPY assets/run.sh /app/
RUN chmod +x /app/*
COPY assets/setup/supervisord-alfresco.conf /etc/supervisor/conf.d/
COPY assets/setup/supervisord-postgresql.conf /etc/supervisor/conf.d/

# CLEAN APT
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

VOLUME ["${ALF_HOME}/alf_data"]

EXPOSE 21 137 138 139 445 7070 8009 8080

CMD /app/run.sh
