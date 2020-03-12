#JAR IS BUILDED WHEN IMAGE IS BUILDED..
FROM maven
COPY ./ /root/pepsbook
WORKDIR /root/pepsbook
RUN mvn package -DskipTests
CMD java -jar /root/pepsbook/target/pepsbook.jar  --spring.profiles.active=prod
VOLUME /root/.m2