mvn clean package -DskipTests &&  \
./resource-server/server & \
java -jar ./zuul/target/zuul-0.0.1-SNAPSHOT.jar & \
java -jar ./gateway/target/gateway-0.0.1-SNAPSHOT.jar &
