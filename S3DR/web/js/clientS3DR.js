/**
 * Created by Slim on 11/17/2015.
 */
var camtimer;

$( "#capbtn" ).click(function() {
    clearTimeout(camtimer);
    $("#expbtn").enable();
    //TODO ajax get output links, send 3dmodel and obj url
    var request = new XMLHttpRequest();
    request.open("get", "./a", false);
});

function refresh(cam)
{
    var timeout = 100;
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


function ClickFile(){
    clearTimeout(camtimer);
    $('#files').click();
}