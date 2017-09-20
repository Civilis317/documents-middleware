#!/usr/bin/env sh
set -e

# Add extra library
apt-get update
apt-get install -y --no-install-recommends sudo curl rsync supervisor fontconfig libice6 libsm6 libxt6 libxrender1 libfontconfig1 libxinerama1 libglu1-mesa  libcups2 ghostscript imagemagick xvfb xfonts-base
apt-get clean
rm -rf /var/lib/apt/lists/*

# vars
ALF_HOME=/opt/alfresco
ALF_BIN=$(basename "${ALF_URL}")
export JAVA_HOME=${ALF_HOME}/java

# get alfresco installer
mkdir -p $ALF_HOME
cd /tmp
curl -O $ALF_URL
chmod +x $ALF_BIN

# install alfresco
./$ALF_BIN --mode unattended --prefix $ALF_HOME --alfresco_admin_password admin

# get rid of installer - makes image smaller
rm $ALF_BIN

# Move alf_data temporary
mv ${ALF_HOME}/alf_data ${ALF_HOME}/alf_data_org
mkdir ${ALF_HOME}/alf_data

# We copy the original setting for share to tune them after start
cp ${ALF_HOME}/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml ${ALF_HOME}/tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml.org

# We copy the original setting for alfresco to tune them after start
cp ${ALF_HOME}/tomcat/shared/classes/alfresco-global.properties ${ALF_HOME}/tomcat/shared/classes/alfresco-global.properties.org

# We copy the original setting for tomcat to tune them after start
cp ${ALF_HOME}/tomcat/conf/server.xml ${ALF_HOME}/tomcat/conf/server.xml.org
cp ${ALF_HOME}/tomcat/conf/logging.properties ${ALF_HOME}/tomcat/conf/logging.properties.org

# Add account
groupadd alfresco
useradd -s /bin/false -g alfresco -d /opt/alfresco alfresco
chown -R alfresco:alfresco /opt/alfresco

# Set sudo
echo "alfresco ALL=(ALL) NOPASSWD: /opt/alfresco/tomcat/bin/catalina.sh run" >> /etc/sudoers
