FROM jboss/wildfly:10.1.0.Final
MAINTAINER civilis@acme.com

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365 --silent

ADD alfresco-client.war /opt/jboss/wildfly/standalone/deployments/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
