/**
 * Created by Slim on 11/17/2015.
 */
var camtimer;

function refresh(cam)
{
    var timeout = 150;
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

function StopCams(){
    for (i = 0;i<mytimers.length;i++){
        clearTimeout(mytimers[i]);
    }
}