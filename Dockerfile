FROM maven
COPY ./ /root/pepsbook
WORKDIR /root/pepsbook
VOLUME /root/.m2
VOLUME /root/node_modules
RUN apt-get update && apt-get install -y npm \
    && npm install \
    && npm run build \
    && mvn package -DskipTests
CMD java -jar /root/pepsbook/target/pepsbook.jar  --spring.profiles.active=prod
