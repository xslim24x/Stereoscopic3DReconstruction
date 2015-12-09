<%--
  Created by IntelliJ IDEA.
  User: Slim
  Date: 11/5/2015
  Time: 9:21 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Stereoscopic 3D Reconstruction System</title>
      <script src="js/jquery-1.11.3.min.js"></script>
      <script src="js/bootstrap.js"></script>
      <script src="jquery.contextmenu.js"></script>
      <script src="js/clientS3DR.js"></script>

    <link  rel="stylesheet" href="css/bootstrap.css" />
    <link  rel="stylesheet" href="css/bootstrap-theme.css" />
    <link rel="stylesheet" href="css/clientS3DR.css" />
  </head>
  <body>
  <div class="center-block" style="margin: 2em;text-align: center;max-width: 1400px">
    <h1>Stereoscopic 3D Reconstruction System</h1>
    <div>
      <img id="cam1" src="/a?cam=0">
      <img id="cam2" src="/a?cam=1">
      <!--<img src="/a?cam=2">  -->
    </div>
    <!--
    <div class="pull-left">
      <div style="float: left;margin: 1em"><img src="cam1.jpg"><p align="center">Camera 1</p></div>
      <div style="float: left;margin: 1em"><img src="cam2.jpg"><p align="center">Camera 2</p></div>
    </div>
    -->
    <div style="text-align: justify;width: 100%;"><button class="btn btn-primary btn-lg" >Capture</button></div>
    <div style="margin: 5em;clear:left">

      <img src="3d.jpg">
      <img src="xyz-rot.png" width="100" style="margin-top: 15em">
      <button class="btn btn-success btn-lg" style="inline-box-align: 3;margin-bottom: 15em">Import</button>
      <button class="btn btn-success btn-lg" style="inline-box-align: 3;margin-bottom: 15em">Export</button>

    </div>
    <div class="hide" id="rmenu">
      <ul>
        <li>
          <h1>Right</h1>
        </li>

        <li>
          <a href="http://localhost:8080/login">Calibration</a>
        </li>

        <li>
          <a href="C:\"></a>
        </li>
      </ul>
    </div>
    <div style="margin: 5em">

    </div>
  </div>
  </body>
</html>

