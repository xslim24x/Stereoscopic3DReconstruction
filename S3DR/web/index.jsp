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
    <script src="js/three.min.js"></script>
    <script src="js/TrackballControls.js"></script>
    <script src="js/PLYLoader.js"></script>
    <script src="js/clientS3DR.js"></script>
    <link  rel="stylesheet" href="css/bootstrap.css" >
    <link  rel="stylesheet" href="css/bootstrap-theme.css" >
    <link rel="stylesheet" href="css/clientS3DR.css" >
  </head>
  <body>
  <div class="center-block" style="margin: 2em;text-align: center;">
    <h1>Stereoscopic 3D Reconstruction System</h1>
    <div id="stereofeed">
      <img id="camfeed" src="/a?cam=stereo" width="80%">
      <!--<img src="/a?cam=2">  -->
      <br><br>
    </div>
    <!--
    <div class="pull-left">
      <div style="float: left;margin: 1em"><img src="cam1.jpg"><p align="center">Camera 1</p></div>
      <div style="float: left;margin: 1em"><img src="cam2.jpg"><p align="center">Camera 2</p></div>
    </div>
    -->
    <div class="btn-toolbar" style="float:right;margin-right:20%;">
      <button class="btn btn-success btn-lg" onclick="StopCams();"><span class="glyphicon glyphicon-import"></span> Import</button>
      <button disabled="true" class="btn btn-success btn-lg"><span class="glyphicon glyphicon-export"></span> Export</button>
      <button class="btn btn-primary btn-lg" ><span class="glyphicon glyphicon-camera"></span> Capture</button>
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
  <script type="text/javascript">

    var _camera, _scene, _renderer, _trackball, _projector;

    var _entities = [];

    initGL();
    //loadstuff();
    //ply();
    animate();


    function loadstuff() {
      var request = new XMLHttpRequest();
      request.open("GET", "./arbor.json", false);
      request.send(null)
      var my_JSON_object = JSON.parse(request.responseText);
      createScene(my_JSON_object);
    }

    function ply(){
      var loader = new THREE.PLYLoader();
      loader.load( 'dragon.ply', function ( geometry ) {

        var material = new THREE.MeshPhongMaterial( { color: 0x0055ff, specular: 0x111111, shininess: 200 } );
        var mesh = new THREE.Mesh( geometry, material );

        mesh.position.set( 0, - 0.25, 0 );
        mesh.rotation.set( 0, - Math.PI / 2, 0 );

        mesh.castShadow = true;
        mesh.receiveShadow = true;
        _scene.add( mesh );

      } );
    }


    function ConvertClr(clr) {
      var bytes = [];

      bytes[0] = (clr >>> 24) & 0xFF; //R
      bytes[1] = (clr >>> 16) & 0xFF; //G
      bytes[2] = (clr >>> 8) & 0xFF;  //B
      bytes[3] = (clr >>> 0) & 0xFF;  //A

      return bytes[2] | (bytes[1] << 8) | (bytes[0] << 16);
    }

    function clearScene()
    {
      for (var i = 0; i < _entities.length; i++) {
        _scene.remove(_entities[i]);
      }

      _entities = [];
    }

    function createScene(meshDataList){

      clearScene();

      _camera.fov = 40;
      _camera.position.x = 0;
      _camera.position.y = 0;
      _camera.position.z = 30;

      var center = [0.0, 0.0, 0.0];

      var len = meshDataList.length;

      for (var meshIdx = 0; meshIdx < len; meshIdx++) {

        var meshData = meshDataList[meshIdx];

        var geometry = new THREE.Geometry();

        var vertexArray = [];

        //uncompress vertices array
        for (var i = 0; i < meshData.VertexIndices.length; i += 1) {

          var idx = 3 * meshData.VertexIndices[i];

          vertexArray[i] = new THREE.Vector3(
                  meshData.VertexCoords[idx],
                  meshData.VertexCoords[idx + 1],
                  meshData.VertexCoords[idx + 2]);
        }

        var normalArray = [];

        //uncompress normals array
        for (var i = 0; i < meshData.NormalIndices.length; i += 1) {

          var idx = 3 * meshData.NormalIndices[i];

          normalArray[i] = new THREE.Vector3(
                  meshData.Normals[idx],
                  meshData.Normals[idx + 1],
                  meshData.Normals[idx + 2]);
        }

        //Generate Faces
        for (var i = 0; i < vertexArray.length; i += 3) {

          geometry.vertices.push(vertexArray[i]);
          geometry.vertices.push(vertexArray[i + 1]);
          geometry.vertices.push(vertexArray[i + 2]);

          var face = new THREE.Face3(i, i + 1, i + 2)

          geometry.faces.push(face);

          face.vertexNormals.push(normalArray[i]);
          face.vertexNormals.push(normalArray[i + 1]);
          face.vertexNormals.push(normalArray[i + 2]);
        }

        center[0] += meshData.Center[0];
        center[1] += meshData.Center[1];
        center[2] += meshData.Center[2];

        var material = new THREE.MeshLambertMaterial(
                {
                  color: ConvertClr(meshData.Color[0]),
                  shading: THREE.SmoothShading
                });

        var body = new THREE.Mesh(geometry, material);

        body.doubleSided = false;

        body.geometry.dynamic = true;
        body.geometry.__dirtyVertices = true;
        body.geometry.__dirtyNormals = true;

        var entity = new THREE.Object3D();

        entity.add(body);

        _entities.push(entity);

        _scene.add(entity);
      }

      center[0] = center[0] / len;
      center[1] = center[1] / len;
      center[2] = center[2] / len;

      for (var i = 0; i < _entities.length; i++) {
        _entities[i].applyMatrix(new THREE.Matrix4().makeTranslation(
                -center[0],
                -center[1],
                -center[2]));
      }
    };

    function hasWebGL() {
      try {
        var canvas = document.createElement('canvas');
        var ret =
                !!(window.WebGLRenderingContext &&
                        (canvas.getContext('webgl') ||
                        canvas.getContext('experimental-webgl'))
                );
        return ret;
      }
      catch (e) {
        return false;
      };
    }

    function initGL() {

      var animateWithWebGL = hasWebGL();

      var container = document.getElementById("3DArea");

      _scene = new THREE.Scene();

      var width = 600;
      var height = 400;

      _camera = new THREE.PerspectiveCamera(40, width / height, 0.1, 500);

      _camera.position.x = 0;
      _camera.position.y = 0;
      _camera.position.z = 50;

      _scene.add(_camera);

      _trackball = new THREE.TrackballControls(_camera, container);
      _trackball.rotateSpeed = 8;
      _trackball.zoomSpeed = 7.0;
      _trackball.panSpeed = 4;
      _trackball.noZoom = false;
      _trackball.noPan = false;
      _trackball.staticMoving = true;
      _trackball.dynamicDampingFactor = 0.3;
      _trackball.minDistance = 1;
      _trackball.maxDistance = 100;
      _trackball.keys = [82, 90, 80]; // [r:rotate, z:zoom, p:pan]
      //_trackball.addEventListener('change', render);

      // create lights
      var light1 = new THREE.PointLight(0xFFFFFF);
      var light2 = new THREE.PointLight(0xFFFFFF);
      var light3 = new THREE.PointLight(0xFFFFFF);
      var light4 = new THREE.PointLight(0xFFFFFF);

      light1.position.x = 100;
      light1.position.y = 50;
      light1.position.z = 200;

      light2.position.x = -100;
      light2.position.y = 150;
      light2.position.z = -200;

      light3.position.x = 100;
      light3.position.y = -150;
      light3.position.z = -100;

      light4.position.x = -100;
      light4.position.y = -150;
      light4.position.z = 100;

      _scene.add(light1);
      _scene.add(light2);
      _scene.add(light3);
      _scene.add(light4);



      _renderer = new THREE.WebGLRenderer();  //CanvasRenderer();
      _renderer.setSize(width, height);
      _projector = new THREE.Projector();

      container.appendChild(_renderer.domElement);
      container.firstElementChild.setAttribute("id","3Drend");
      document.addEventListener('mousewheel', onDocumentMouseWheel, false);

      _renderer.domElement.addEventListener('mousedown', onDocumentMouseDown, false);

      var container = document.getElementById("3DArea");
    }

    //zoom
    function onDocumentMouseWheel(event) {
      _camera.fov -= event.wheelDeltaY * 0.05;

      if (_camera.fov < 10.0) {
        _camera.fov = 10.0;
      }

      if (_camera.fov > 180.0) {
        _camera.fov = 180.0;
      }

      _camera.updateProjectionMatrix();

      render();
    }

    //rotate
    function onDocumentMouseDown(event) {

      event.preventDefault();

      var container = document.getElementById("3DArea");

      var mouseX = (event.clientX / window.innerWidth) * 2 - 1;
      var mouseY = -(event.clientY / window.innerHeight) * 2 + 1;

      var vector = new THREE.Vector3(mouseX, mouseY, 0.5);

      var ray = new THREE.Ray(
              _camera.position,
              vector.subSelf(_camera.position).normalize());

      /*var vector = new THREE.Vector3(
       ((event.clientX - container.offsetLeft) / _scene.WIDTH) * 2 - 1,
       -((event.clientY - container.offsetTop) / _scene.HEIGHT) * 2 + 1,
       0.5);*/

      _projector.unprojectVector(vector, _camera);

      var intersects = ray.intersectObjects(_entities);

      if (intersects.length > 0) {

        //SELECTED = intersects[0].object;

        alert("Intersect: " + intersects.length)

      }
    }

    function animate() {
      requestAnimationFrame(animate);
      _trackball.update();
      render();
    }

    function render() {
      _renderer.render(_scene, _camera);
    }

  </script>
  </body>
</html>

