# fetch basic image
FROM openjdk:11
WORKDIR "/opt/"

ENTRYPOINT ["java", "-jar", "/opt/HomeFinder-1.1-SNAPSHOT.jar"]