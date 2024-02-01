run: jar
    java -jar ./kt-lox/build/libs/kt-lox-1.0-SNAPSHOT.jar

jar:
    cd kt-lox && ./gradlew jar
