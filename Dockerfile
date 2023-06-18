# Build Clojure Uberjar
FROM clojure:temurin-20-tools-deps as clojure
RUN mkdir -p /build
WORKDIR /build
COPY deps.edn build.clj /build/
COPY src /build/src
RUN clojure -T:build uber

# Graal the uberjar into native
FROM ghcr.io/graalvm/native-image:ol9-java17-22.3.2 as graal
WORKDIR /build
COPY --from=clojure /build/target/bljog.jar /build/target/bljog.jar
COPY build_native.sh /build/
RUN ./build_native.sh

# Fetch blog posts
FROM alpine/git as content
RUN git clone --depth=1 -b main https://github.com/kiranshila/blog_posts /posts

# Build prod image
FROM scratch
COPY --from=graal /build/target/bljog /bljog
COPY --from=content /posts /posts
COPY resources /resources
EXPOSE 8080
CMD ["/bljog", "/posts"]
