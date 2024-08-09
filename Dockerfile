#FROM openjdk:17-jdk-alpine
#ADD target/apic-nibss-notify.jar apic-nibss-notify.jar
#RUN mkdir -p ./logs \
#&& chmod 775 ./logs \
#&& ln -sf /dev/stdout ./logs/apicNibssNotify-info.log
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","/apic-nibss-notify.jar"]

# Define base image
FROM openjdk:8-jdk-alpine

# Define user properties
ARG USERNAME=redboxadm
ARG USER_UID=1000
ARG USER_GID=$USER_UID
ARG BUILD_ID

ENV BUILD_ID_ENV=${BUILD_ID}

# Add the jar file
ADD target/integration-channel-bua-collection-service.jar integration-channel-bua-collection-service.jar

# Needed so that /bin/sh: will run on alpine
RUN apk add --no-cache shadow=4.5-r2

# Create the user
RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && apk update \
    && apk add --no-cache sudo \
    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME \
&& mkdir -p ./logs \
&& chmod 775 ./logs \
&& ln -sf /dev/stdout ./logs/integration-channel-bua-collection-service-info.log

# Set the port the service is to be exposed on. Port 8080 should be 8080 for all deployments to Openshift except
# in the case of special needs.
EXPOSE 8080

# Set the User to run the service
USER $USERNAME

# Set the entry point into the container
ENTRYPOINT ["java","-jar","/integration-channel-bua-collection-service.jar", "$ARGS"]
