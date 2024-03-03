_cargo := 'cd rs-lox && cargo'

run-c *ARGS: build-c
    cd clox && ./lox {{ ARGS }}

build-c:
    cd clox && make all

run-rs:
    {{ _cargo }} run

test-rs:
    {{ _cargo }} test

run-kt FILE="": jar
    cd kt-lox && java -jar ./build/libs/kt-lox-1.0-SNAPSHOT.jar {{ FILE }}

jar:
    cd kt-lox && ./gradlew jar
