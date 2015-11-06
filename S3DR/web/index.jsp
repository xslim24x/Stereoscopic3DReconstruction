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
  </head>
  <body>
  <div class="center-block" style="margin: 2em">
    <h1>Stereoscopic 3D Reconstruction System</h1>
    <div class="pull-left">
      <div style="float: left;margin: 1em"><img src="cam1.jpg"><p align="center">Camera 1</p></div>
      <div style="float: left;margin: 1em"><img src="cam2.jpg"><p align="center">Camera 2</p></div>
    </div>
    <div style="clear:left"><button class="btn btn-primary btn-lg" >Capture</button></div>
    <div style="margin: 5em;clear:left">

      <img src="3d.jpg">
      <img src="xyz-rot.png" width="100" style="margin-top: 15em">
      <button class="btn btn-success btn-lg" style="inline-box-align: 3;margin-bottom: 15em">Import</button>
      <button class="btn btn-success btn-lg" style="inline-box-align: 3;margin-bottom: 15em">Export</button>

    </div>
    <div style="margin: 5em">

    </div>
  $END$
  </body>
</html>

