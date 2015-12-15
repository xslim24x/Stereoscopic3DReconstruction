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
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Stereoscopic 3D Reconstruction System</title>
    <script src="js/jquery-1.11.3.min.js"></script>
    <script src="js/bootstrap.js"></script>
    <script src="jquery.contextmenu.js"></script>
    <%--<script src="js/three.min.js"></script>--%>
    <script src="js/three.js"></script>
    <script src="js/TrackballControls.js"></script>
    <script src="js/PLYLoader.js"></script>
    <script src="js/OBJLoader.js"></script>
    <script src="js/clientS3DR.js"></script>
    <link  rel="stylesheet" href="css/bootstrap.css" >
    <link  rel="stylesheet" href="css/bootstrap-theme.css" >
    <link rel="stylesheet" href="css/clientS3DR.css" >
  </head>
  <body>
  <div class="center-block" style="margin: 2em;text-align: center;">
    <h1>Stereoscopic 3D Reconstruction System</h1>
    <!--
    <div class="pull-left">
      <div style="float: left;margin: 1em"><img src="cam1.jpg"><p align="center">Camera 1</p></div>
      <div style="float: left;margin: 1em"><img src="cam2.jpg"><p align="center">Camera 2</p></div>
    </div>
    -->
    <div id="stereofeed">
      <img id="camfeed" src="/a?cam=stereo" width="80%">
      <!--<img src="/a?cam=2">  -->
      <br><br>
    </div>
    <div class="btn-toolbar" style="float:right;margin-right:20%;">
      <!-- <input type="file" class="show" id="files" name="file" /> -->
      <div style="height:0px;overflow:hidden">
        <input type="file" id="fileInput" name="fileInput" accept=".ply" onchange="newFile();" />
      </div>
      <button id="impbtn" class="btn btn-success btn-lg" onclick="InputBtn();"><span class="glyphicon glyphicon-import"></span> Import</button>
      <button id="expbtn" type="submit" disabled="true" class="btn btn-success btn-lg"><span class="glyphicon glyphicon-export"></span> Export</button>
      <button id="capbtn" class="btn btn-primary btn-lg" onclick="CaptureBtn();" ><span class="glyphicon glyphicon-camera"></span> Capture</button>
    </div>
    <div style="margin: 5em;clear:left">
      <div id="3DArea" />
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

  <script>

      //function to handle ply files containing point clouds with no faces and only vertices
      function plyopen(thisfile){
        var loader = new THREE.PLYLoader();
        var group = new THREE.Object3D();
        loader.load(thisfile, function (geometry) {
          var material = new THREE.ParticleBasicMaterial({
            size: 0.01,
            opacity: 1.0,
            transparent: true,
            blending: THREE.AdditiveBlending
          });

          group = new THREE.ParticleSystem(geometry, material);
          group.sortParticles = true;

          console.log(group);
          scene.add(group);
        });
      }



      // create a scene, cam and setup webglrendering
      var scene = new THREE.Scene();
      var camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
      var webGLRenderer = new THREE.WebGLRenderer();
      webGLRenderer.setClearColor(new THREE.Color(0x000, 1.0));
      webGLRenderer.setSize(600, 400);
      webGLRenderer.shadowMapEnabled = true;

      //camera initial position
      camera.fov = 40
      camera.position.x = 10;
      camera.position.y = 10;
      camera.position.z = 10;
      camera.lookAt(new THREE.Vector3(0, -2, 0));


      var container = document.getElementById("3DArea");

      //setup mouse trackball to change perspectve
      trackball = new THREE.TrackballControls(camera, container);
      trackball.rotateSpeed = 8;
      trackball.zoomSpeed = 7.0;
      trackball.panSpeed = 4;
      trackball.noZoom = false;
      trackball.noPan = false;
      trackball.staticMoving = true;
      trackball.dynamicDampingFactor = 0.3;
      trackball.minDistance = 1;
      trackball.maxDistance = 100;
      trackball.keys = [82, 90, 80]; // [r:rotate, z:zoom, p:pan]

      var spotLight = new THREE.SpotLight(0xffffff);
      spotLight.position.set(20, 20, 20);
      scene.add(spotLight);

      $("#3DArea").append(webGLRenderer.domElement);

      var group;
      animate();

      function onDocumentMouseWheel(event) {
        camera.fov -= event.wheelDeltaY * 0.05;

        if (camera.fov < 10.0) {
          camera.fov = 10.0;
        }

        if (camera.fov > 180.0) {
          camera.fov = 180.0;
        }

        camera.updateProjectionMatrix();
        render();
      }

      //rotate
      function onDocumentMouseDown(event) {

        event.preventDefault();

        var container = document.getElementById("3DArea");

        var mouseX = (event.clientX / 600) * 2 - 1;
        var mouseY = -(event.clientY / 400) * 2 + 1;

        var vector = new THREE.Vector3(mouseX, mouseY, 0.5);

        var ray = new THREE.Ray(
                camera.position,
                vector.sub(camera.position).normalize());


        projector.unprojectVector(vector, camera);
      }

      function animate() {
        requestAnimationFrame(animate);
        trackball.update();
        render();
      }

      function render() {
        webGLRenderer.render(scene, camera);
      }



  </script>
  </body>
</html>

