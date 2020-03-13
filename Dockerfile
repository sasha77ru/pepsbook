#JAR IS BUILDED WHEN IMAGE IS BUILDED..
FROM maven
COPY ./ /root/pepsbook
WORKDIR /root/pepsbook
RUN apt-get update && apt-get install -y npm \
    && npm install \
    && npm run build \
    && mvn package -DskipTests
VOLUME /root/.m2
VOLUME /root/node_modules
CMD java -jar /root/pepsbook/target/pepsbook.jar  --spring.profiles.active=prod
