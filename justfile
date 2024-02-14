_cargo := 'cd rs-lox && cargo'

test:
    {{ _cargo }} test

run:
    {{ _cargo }} run

run-kt FILE="": jar
    java -jar ./kt-lox/build/libs/kt-lox-1.0-SNAPSHOT.jar {{ FILE }}

jar:
    cd kt-lox && ./gradlew jar
