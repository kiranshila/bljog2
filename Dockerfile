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

# Build the CSS
FROM node:18-alpine as css
WORKDIR /build
COPY src /build/src
COPY package.json /build
COPY tailwind.config.js /build
COPY resources/public/main.css /build
RUN npm install
RUN npx tailwindcss -i main.css -o out.css

# Fetch blog posts
FROM alpine/git as content
RUN git clone --depth=1 -b main https://github.com/kiranshila/blog_posts /posts

# Build prod image
FROM scratch
COPY --from=graal /build/target/bljog /bljog
COPY --from=content /posts /posts
COPY resources /resources
COPY --from=css /build/out.css /resources/public
EXPOSE 8080
CMD ["/bljog", "/posts"]
