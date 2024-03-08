ARG PROJECT_ARTIFACT_ID=kypo-rest-training
############ BUILD STAGE ############
FROM maven:3.8.4-openjdk-17-slim AS build
ARG PROJECT_ARTIFACT_ID
WORKDIR /app
## default link to proprietary repository, e.g., Nexus repository
ARG PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO
COPY pom.xml /app/pom.xml
COPY kypo-api-training /app/kypo-api-training
COPY kypo-elasticsearch-training /app/kypo-elasticsearch-training
COPY kypo-persistence-training /app/kypo-persistence-training
COPY kypo-service-training /app/kypo-service-training
COPY $PROJECT_ARTIFACT_ID /app/$PROJECT_ARTIFACT_ID
# Build JAR file
RUN mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL && \
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/$PROJECT_ARTIFACT_ID.jar

############ RUNNABLE STAGE ############
FROM eclipse-temurin:17-jre-jammy
ARG PROJECT_ARTIFACT_ID
WORKDIR /app
COPY /etc/training.properties /app/etc/training.properties
COPY entrypoint.sh /app/entrypoint.sh
COPY --from=build /app/$PROJECT_ARTIFACT_ID.jar ./
RUN apt-get update && \
    # Required to use nc command in the wait for it function, see entrypoint.sh
    apt-get install -y netcat && \
    # Make a file executable
    chmod a+x entrypoint.sh
EXPOSE 8083
ENTRYPOINT ["./entrypoint.sh"]