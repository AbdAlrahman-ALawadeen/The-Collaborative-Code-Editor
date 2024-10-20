FROM gcc:latest
COPY code.cpp /app/code.cpp
WORKDIR /app
RUN g++ code.cpp -o code
CMD ["./code"]
