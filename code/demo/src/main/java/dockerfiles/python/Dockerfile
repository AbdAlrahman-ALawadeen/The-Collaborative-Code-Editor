FROM python:3.9
COPY code.py /app/code.py
COPY input.txt /app/input.txt
WORKDIR /app
CMD ["bash", "-c", "python /app/code.py < /app/input.txt"]
