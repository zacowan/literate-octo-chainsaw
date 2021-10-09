# Used for local testing
FROM openjdk:16

COPY ./src /usr/src/app

COPY ./test/*.cfg /usr/src/app

WORKDIR /usr/src/app

RUN javac *.java

CMD ["java", "peerProcess", "0"]
