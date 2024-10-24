function formatBytes(a,b=2){if(!+a)return"0 Bytes";const c=0>b?0:b,d=Math.floor(Math.log(a)/Math.log(1024));return`${parseFloat((a/Math.pow(1024,d)).toFixed(c))} ${["Bytes","KiB","MiB","GiB","TiB","PiB","EiB","ZiB","YiB"][d]}`}


const statusMessage = document.getElementById('statusMessage');
function updateStatusMessage(text) {
  statusMessage.textContent = text;
}

function dragOverHandler(event) {
  event.preventDefault();
}

function dropHandler(event) {
    event.preventDefault();

    uploadFiles(event.dataTransfer.files);
}

function uploadFiles(files) {

  const formData = new FormData();
  for (const file of files) {
    formData.append('file', file);
  }

  const xhr = new XMLHttpRequest();

  const fileText = `${files.length} file${files.length > 1 ? 's' : ''}`
  xhr.addEventListener('loadend', () => {
    if (xhr.status === 200) {
      updateStatusMessage(`✅ Upload of ${fileText} successful`);
      setTimeout(() => location.reload(), 2000);
    } else {
      updateStatusMessage(`❌ Upload error - HTTP error code: ${xhr.status}`);
    }
  });

  xhr.upload.addEventListener('progress', event => {
     updateStatusMessage(`⏳ Uploading ${fileText} - ${formatBytes(event.loaded)} of ${formatBytes(event.total)} bytes`);
  });

  xhr.open('post', '/upload');
  xhr.send(formData);
}

console.log("JS initialized");
