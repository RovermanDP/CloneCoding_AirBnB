const notifyButton = document.getElementById("notify-button");
const toast = document.getElementById("toast");

function showToast() {
  toast.classList.add("show");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => {
    toast.classList.remove("show");
  }, 2400);
}

notifyButton?.addEventListener("click", showToast);
