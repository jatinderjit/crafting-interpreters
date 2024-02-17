_cargo := 'cd rs-lox && cargo'

test:
    {{ _cargo }} test

run:
    {{ _cargo }} run

run-kt FILE="": jar
    cd kt-lox && java -jar ./build/libs/kt-lox-1.0-SNAPSHOT.jar {{ FILE }}

jar:
    cd kt-lox && ./gradlew jar
