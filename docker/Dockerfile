FROM amazonlinux:2
RUN yum install tar gzip gcc zlib-devel glibc-static libstdc++-static -y
RUN true \
    && mkdir /app \
    && curl -sL https://get.graalvm.org/jdk -o /tmp/graalvm-installer.sh \
    && chmod +x /tmp/graalvm-installer.sh \
    && /tmp/graalvm-installer.sh --to "/opt" graalvm-ce-java11-22.3.1
ENV JAVA_HOME="/opt/graalvm-ce-java11-22.3.1"
ENV PATH="$JAVA_HOME/bin:$PATH"
WORKDIR /app
ENTRYPOINT ["native-image"]
