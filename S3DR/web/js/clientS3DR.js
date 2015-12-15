/**
 * Created by Slim on 11/17/2015.
 */
    //timer for refreshing camera feed pics
var camtimer;

//input btn code
function InputBtn() {
    var uploader = $("#fileInput");
    uploader.click();
}

//function to handle new ply file, send to scene and render
function newFile(){
    var file = document.getElementById("fileInput");
    if (FileReader && file.files && file.files.length) {
        var fr = new FileReader();
        fr.onload = function () {
            myfile = fr.result;
            plyopen(myfile)
        }
        fr.readAsDataURL(file.files[0]);
    }
}

//cap btn code
function CaptureBtn(){
    clearTimeout(camtimer);
    var request = new XMLHttpRequest();
    request.open("GET", "/a?cam=disp", true);
    request.send(null)
}

//set up refreshing
function refresh(cam)
{
    var timeout = 200;//ms between new frame req
    (function startRefresh()
    {
        var address
        //sets a dummy get attribute to force reload
        if(cam.src.indexOf('&')>-1)
            address = cam.src.split('&')[0];
        else
            address = cam.src;
        cam.src = address+"&time="+new Date().getTime();
        camtimer = setTimeout(startRefresh,timeout);

    })();
}

window.onload = function()
{
    //TODO: detect available sources, add left, right choice
    var cam = document.getElementById('camfeed');
    refresh(cam);
}
