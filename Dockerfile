# fetch basic image
FROM openjdk:11
WORKDIR "/opt/"
ENTRYPOINT ["java", "-jar", "/opt/HomeFinder.jar"]