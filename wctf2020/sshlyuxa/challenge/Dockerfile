FROM maven:3-jdk-8 as builder

COPY task /code
WORKDIR /code
RUN mvn package


FROM openjdk:8
ARG flag

RUN mkdir /code
WORKDIR /code
COPY --from=builder /code/target/challenge-1.0-SNAPSHOT-all.jar /code/challenge.jar
COPY run.sh /
RUN chmod +x /run.sh

RUN useradd challenge
RUN chown challenge /code

RUN cp /bin/cat /readflag; chmod u+s /readflag
RUN echo $flag > /FLAG
RUN chmod 400 /FLAG && chown root:root /FLAG

USER challenge

CMD ["/run.sh"]

