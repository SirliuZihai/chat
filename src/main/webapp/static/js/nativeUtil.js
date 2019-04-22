/* eslint-disable */
function errorCallback(error) {
  alert("ERROR: " + error.code)
}
function writeLogFile(data) {
  var type = window.PERSISTENT;
  var size = 5*1024*1024;
  window.requestFileSystem(type, size, successCallback, errorCallback)
  function successCallback(fs) {
    savefile(fs,data,'log.txt')
  }
}

//*******************************沙箱外存储*********************//
function savefile(dirEntry,fileData, fileName){
  dirEntry.getFile(
    fileName, {
      create: true,
      exclusive: false
    },
    function (fileEntry) {
      writeFile(fileEntry, fileData, true);

    }, onErrorCreateFile);
}



function onErrorCreateFile(e) {
  console.log('Failed create file: ' + e.toString());
};

function onErrorGetDir(error){
  console.log("文件夹创建失败！")
}
/**
 * cordova.file.externalRootDirectory ok
 * */
function writeLogFile2(data){
  var url2 =cordova.file.externalRootDirectory
  window.resolveLocalFileSystemURL(url2, function (dirEntry) {
    savefile(dirEntry,data+'/n','appZihailog2.txt')
  }, null);
}
function writeFile(fileEntry, dataObj, isAppend) {
  // Create a FileWriter object for our FileEntry (log.txt).
  fileEntry.createWriter(function (fileWriter) {
    fileWriter.onwriteend = function() {
     // readFile(fileEntry)
    };
    fileWriter.onerror = function (e) {
      alert("Failed file read: " + e.toString());
    };
    // If we are appending data to file, go to the end of the file.
    if (isAppend) {
      try {
        fileWriter.seek(fileWriter.length);
      }
      catch (e) {
        alert.log("file doesn't exist!");
      }
    }
    fileWriter.write(dataObj);
  });
}
function readFile(fileEntry) {
  fileEntry.file(function (file) {
    var reader = new FileReader();
    reader.onloadend = function() {
     alert(fileEntry.fullPath + "===" + this.result);
    };
    reader.readAsText(file);
  }, onErrorReadFile);
}
function sendPicture (ws) {
  navigator.camera.getPicture((dataUrl) => {
      alert(dataUrl)
      if (dataUrl)ws.send('[image]:' + dataUrl)
    }, null,
    { quality: 50, destinationType: navigator.camera.DestinationType.DATA_URL, sourceType: navigator.camera.PictureSourceType.PHOTOLIBRARY })

}
export default {
  writeLogFile2,
  sendPicture
}
