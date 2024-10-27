FROM openjdk:11
COPY code.java /app/code.java
COPY input.txt /app/input.txt
WORKDIR /app
RUN javac code.java
CMD ["bash", "-c", "cat /app/input.txt | java code"]
