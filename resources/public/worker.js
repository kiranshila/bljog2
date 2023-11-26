onmessage = function (event) {
  importScripts(
    "//cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.8.0/build/highlight.min.js",
  );
  importScripts(
    "//cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.8.0/build/languages/julia.min.js",
  );
  importScripts(
    "//cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.8.0/build/languages/julia-repl.min.js",
  );
  importScripts(
    "//cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.8.0/build/languages/clojure.min.js",
  );
  importScripts(
    "//cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.8.0/build/languages/matlab.min.js",
  );
  importScripts(
    "//cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.8.0/build/languages/openscad.min.js",
  );

  self.hljs.configure({ tabReplace: 4 });
  var result = self.hljs.highlightAuto(event.data);
  postMessage(result.value);
  close();
};
