# fetch basic image
FROM openjdk:11
WORKDIR "/opt/"

ENTRYPOINT ["java", "-jar", "/opt/HomeFinder-1.0-SNAPSHOT.jar"]