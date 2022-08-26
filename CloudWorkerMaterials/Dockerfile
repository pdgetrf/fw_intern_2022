FROM golang:1.12.0-alpine3.9

RUN apk add --no-cache \
        python3 \
        py3-pip \
    && pip3 install --upgrade pip \
    && pip3 install --no-cache-dir \
        awscli \
    && rm -rf /var/cache/apk/*

RUN aws --version

RUN mkdir /test-app
ADD . /test-app

RUN mv /test-app/.aws ~/
WORKDIR /test-app

RUN go build FetchData.go
CMD ["/test-app"] 
