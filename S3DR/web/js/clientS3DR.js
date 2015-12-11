/**
 * Created by Slim on 11/17/2015.
 */
var mytimers = [];

function refresh(cam)
{
    var timeout = 300;
    (function startRefresh()
    {
        var address
        //sets a dummy get attribute to force reload
        if(cam.src.indexOf('&')>-1)
            address = cam.src.split('&')[0];
        else
            address = cam.src;
        cam.src = address+"&time="+new Date().getTime();
        var temp = setTimeout(startRefresh,timeout);
        mytimers.push(temp);
    })();
}

window.onload = function()
{
    //TODO: detect available sources, add left, right choice
    var leftcam = document.getElementById('lcam');
    var rightcam = document.getElementById('rcam');
    refresh(leftcam);
    refresh(rightcam);
}

function StopCams(){
    for (i = 0;i<mytimers.length;i++){
        clearTimeout(mytimers[i]);
    }
}