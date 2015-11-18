/**
 * Created by Slim on 11/17/2015.
 */


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
        setTimeout(startRefresh,timeout);
    })();
}

window.onload = function()
{
    //TODO: detect available sources, add left, right choice
    var cam1 = document.getElementById('cam1');
    var cam2 = document.getElementById('cam2');
    refresh(cam1);
    refresh(cam2);
}