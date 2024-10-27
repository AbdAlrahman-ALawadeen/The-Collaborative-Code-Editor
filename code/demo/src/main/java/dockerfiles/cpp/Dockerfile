FROM gcc:latest
COPY code.cpp /app/code.cpp
COPY input.txt /app/input.txt
WORKDIR /app
RUN g++ code.cpp -o code
CMD ["bash", "-c", "./code < /app/input.txt"]
