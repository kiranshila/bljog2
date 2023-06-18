function highlight_code() {
  if (typeof Worker === undefined) return false;
  document.querySelectorAll("pre code").forEach((node) => {
    var worker = new Worker("/worker.js");
    worker.onmessage = (event) => {
      node.innerHTML = event.data;
    };
    worker.postMessage(node.textContent);
  });
}
