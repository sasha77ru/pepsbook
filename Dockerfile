FROM sasha77ru/pepsbook:prep
COPY ./ /root/pepsbook
WORKDIR /root/pepsbook
RUN npm install \
    && npm run build \
    && mvn package -DskipTests \
    && rm -rf node_modules \
    && rm -rf /root/.m2/repository
CMD java -jar /root/pepsbook/target/pepsbook.jar  --spring.profiles.active=prod
