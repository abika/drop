function formatBytes(a,b=2){if(!+a)return"0 Bytes";const c=0>b?0:b,d=Math.floor(Math.log(a)/Math.log(1024));return`${parseFloat((a/Math.pow(1024,d)).toFixed(c))} ${["Bytes","KiB","MiB","GiB","TiB","PiB","EiB","ZiB","YiB"][d]}`}


const statusMessage = document.getElementById('status_message');
function updateStatusMessage(text) {
  statusMessage.textContent = text;
}

const dropZone = document.getElementById('drop_zone');
function dragOverHandler(event) {
  event.preventDefault();
}

function dragEnterHandler(event) {
  if (event.dataTransfer.items.length > 0 &&
    event.dataTransfer.items[0].kind === 'file') {
    dropZone.style.backgroundColor = 'honeydew'
  } else {
    dropZone.style.backgroundColor = 'lavenderblush'
  }
}

function dragLeaveHandler(event) {
  dropZone.style.backgroundColor = 'aliceblue'
}

function dropHandler(event) {
  event.preventDefault();

  uploadFiles(event.dataTransfer.files);
}

function submitFormHandler(event, form) {
  event.preventDefault();

  const filesInput = document.getElementById('files');
  uploadFiles(filesInput.files);

  form.reset();
}

function uploadFiles(files) {
  if (files.length === 0) {
    updateStatusMessage('No files to upload!?');
    return;
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

  const formData = new FormData();
  for (const file of files) {
    formData.append('file', file);
  }
  xhr.send(formData);
}

const filesForm = document.getElementById("files_form");
filesForm.addEventListener("submit", (event) => {
  submitFormHandler(event, filesForm);
});

console.log("JS initialized");
