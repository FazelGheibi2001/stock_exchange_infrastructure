FROM eclipse-temurin:25.0.3_9-jdk-noble AS builder

WORKDIR /workspace

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        unzip \
    && rm -rf /var/lib/apt/lists/*

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -ntp -DskipTests clean package \
    && cp $(find target -maxdepth 1 -name "*.jar" ! -name "*.original" | head -1) /workspace/app.jar


FROM eclipse-temurin:25.0.3_9-jre-noble AS runtime

ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Dfile.encoding=UTF-8" \
    TMPDIR=/tmp

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        tesseract-ocr \
        tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/* \
    && tesseract --version \
    && tesseract --list-langs | grep -Fx eng

RUN groupadd --system --gid 10001 app \
    && useradd --system \
       --uid 10001 \
       --gid app \
       --create-home \
       --home-dir /home/app \
       --shell /usr/sbin/nologin \
       app

WORKDIR /app

COPY --from=builder \
     --chown=app:app \
     /workspace/app.jar \
     /app/app.jar

USER app

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "/app/app.jar"]