var gl;
var canvas;
var gridSizeX =1024, gridSizeY =1024, windowOffsetX =40, windowOffsetY =40;
var windowWidth, windowHeight, viewAngle, viewHeight;
var transform = [1, 0, 0, 1, 0, 0];
var renderTextures = [];
var brightness;
var renderer = {};

// from DragObject.java
const MT_CHARGED = 1;
const MT_CONDUCTING = 2;
const MT_DIELECTRIC = 3;

    function getShader(gl, id, prefix) {
        var shaderScript = document.getElementById(id);
        if (!shaderScript) {
            return null;
        }

        var str = "";
        var k = shaderScript.firstChild;
        while (k) {
            if (k.nodeType == 3) {
                str += k.textContent;
            }
            k = k.nextSibling;
        }

        var shader;
        if (shaderScript.type == "x-shader/x-fragment") {
            shader = gl.createShader(gl.FRAGMENT_SHADER);
        } else if (shaderScript.type == "x-shader/x-vertex") {
            shader = gl.createShader(gl.VERTEX_SHADER);
        } else {
            return null;
        }

        if (prefix)
        	str = prefix + str;
        gl.shaderSource(shader, str);
        gl.compileShader(shader);

        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
            alert(gl.getShaderInfoLog(shader));
            return null;
        }

        return shader;
    }


    var shaderProgramMain, shaderProgramRelax, shaderProgramAcoustic, shaderProgramDraw, shaderProgramMode;
    var shaderProgramCopyRG, shaderProgramResidual, shaderProgramViewCharge, shaderProgramCalcCharge, shaderProgramSum;
    var shaderProgramCopyRGB, shaderProgramScalarField;

    function initShader(fs, vs, prefix) {
        var fragmentShader = getShader(gl, fs, prefix);
        var vertexShader = getShader(gl, vs, prefix);

        var shaderProgram = gl.createProgram();
        gl.attachShader(shaderProgram, vertexShader);
        gl.attachShader(shaderProgram, fragmentShader);
        gl.linkProgram(shaderProgram);

        if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
	    console.log(gl.getProgramInfoLog(shaderProgram));
        	debugger;
            alert("Could not initialise shaders");
        }

        gl.useProgram(shaderProgram);

        shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
        shaderProgram.textureCoordAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
        shaderProgram.colorAttribute = gl.getAttribLocation(shaderProgram, "aColor");

        shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
        shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
        shaderProgram.samplerUniform = gl.getUniformLocation(shaderProgram, "uSampler");
        shaderProgram.sourceTextureUniform = gl.getUniformLocation(shaderProgram, "uSourceTexture");
        shaderProgram.rightSideTextureUniform = gl.getUniformLocation(shaderProgram, "uRightSideTexture");

        return shaderProgram;
    }

    function initShaders() {
    	shaderProgramMain = initShader("shader-display-fs", "shader-vs", null);
    	shaderProgramMain.brightnessUniform = gl.getUniformLocation(shaderProgramMain, "brightness");
    	shaderProgramMain.colorsUniform = gl.getUniformLocation(shaderProgramMain, "colors");
        shaderProgramMain.rightSideTextureUniform = gl.getUniformLocation(shaderProgramMain, "uRightSideTexture");

    	shaderProgramScalarField = initShader("shader-display-scalar-field-fs", "shader-vs", null);
    	shaderProgramScalarField.brightnessUniform = gl.getUniformLocation(shaderProgramScalarField, "brightness");
    	shaderProgramScalarField.colorsUniform = gl.getUniformLocation(shaderProgramScalarField, "colors");
        shaderProgramScalarField.rightSideTextureUniform = gl.getUniformLocation(shaderProgramScalarField, "uRightSideTexture");
    	shaderProgramScalarField.stepSizeXUniform = gl.getUniformLocation(shaderProgramScalarField, "stepSizeX");
    	shaderProgramScalarField.stepSizeYUniform = gl.getUniformLocation(shaderProgramScalarField, "stepSizeY");
    	shaderProgramScalarField.multsUniform = gl.getUniformLocation(shaderProgramScalarField, "mults");

    	shaderProgram3D = initShader("shader-3d-fs", "shader-3d-vs", null);
    	shaderProgram3D.brightnessUniform = gl.getUniformLocation(shaderProgram3D, "brightness");
    	shaderProgram3D.equipMultUniform = gl.getUniformLocation(shaderProgram3D, "equipMult");
    	shaderProgram3D.colorsUniform = gl.getUniformLocation(shaderProgram3D, "colors");
    	shaderProgram3D.xOffsetUniform = gl.getUniformLocation(shaderProgram3D, "xOffset");
    	shaderProgram3D.normalMatrixUniform = gl.getUniformLocation(shaderProgram3D, "uNormalMatrix");
    	shaderProgram3D.stepSizeXUniform = gl.getUniformLocation(shaderProgram3D, "stepSizeX");
    	shaderProgram3D.stepSizeYUniform = gl.getUniformLocation(shaderProgram3D, "stepSizeY");
        shaderProgram3D.rightSideTextureUniform = gl.getUniformLocation(shaderProgram3D, "uRightSideTexture");

    	shaderProgramRelax = initShader("shader-relax-fs", "shader-vs", null);
    	shaderProgramRelax.stepSizeXUniform = gl.getUniformLocation(shaderProgramRelax, "stepSizeX");
    	shaderProgramRelax.stepSizeYUniform = gl.getUniformLocation(shaderProgramRelax, "stepSizeY");

    	shaderProgramResidual = initShader("shader-residual-fs", "shader-vs", null);
    	shaderProgramResidual.stepSizeXUniform = gl.getUniformLocation(shaderProgramResidual, "stepSizeX");
    	shaderProgramResidual.stepSizeYUniform = gl.getUniformLocation(shaderProgramResidual, "stepSizeY");

    	shaderProgramSum = initShader("shader-sum-fs", "shader-vs", null);
    	shaderProgramSum.stepSizeXUniform = gl.getUniformLocation(shaderProgramSum, "stepSizeX");
    	shaderProgramSum.stepSizeYUniform = gl.getUniformLocation(shaderProgramSum, "stepSizeY");

    	shaderProgramAdd = initShader("shader-add-mult-fs", "shader-vs", null);
    	shaderProgramAdd.multUniform = gl.getUniformLocation(shaderProgramAdd, "mult");

    	//shaderProgramSubtract = initShader("shader-subtract-fs", "shader-vs", null);
    	shaderProgramCopyRG = initShader("shader-copy-rg-fs", "shader-vs", null);
    	shaderProgramCopyRGB = initShader("shader-copy-rgb-fs", "shader-vs", null);

    	shaderProgramDraw = initShader("shader-draw-fs", "shader-draw-vs");

    	shaderProgramViewCharge = initShader("shader-view-charge-fs", "shader-view-charge-vs");
    	shaderProgramViewCharge.brightnessUniform = gl.getUniformLocation(shaderProgramViewCharge, "brightness");
    	shaderProgramViewCharge.textureMatrixUniform = gl.getUniformLocation(shaderProgramViewCharge, "uTextureMatrix");

    	shaderProgramCalcCharge = initShader("shader-calc-charge-fs", "shader-calc-charge-vs");
    	shaderProgramCalcCharge.textureMatrixUniform  = gl.getUniformLocation(shaderProgramCalcCharge, "uTextureMatrix");

    	shaderProgramEquip = initShader("shader-equipotential-fs", "shader-vs", null);
    	shaderProgramEquip.stepSizeXUniform = gl.getUniformLocation(shaderProgramEquip, "stepSizeX");
    	shaderProgramEquip.stepSizeYUniform = gl.getUniformLocation(shaderProgramEquip, "stepSizeY");
    	shaderProgramEquip.brightnessUniform = gl.getUniformLocation(shaderProgramEquip, "brightness");
        shaderProgramEquip.rightSideTextureUniform = gl.getUniformLocation(shaderProgramEquip, "uRightSideTexture");

    	shaderProgramFieldVector = initShader("shader-field-vector-fs", "shader-field-vector-vs", null);
    	shaderProgramFieldVector.stepSizeXUniform = gl.getUniformLocation(shaderProgramFieldVector, "stepSizeX");
    	shaderProgramFieldVector.stepSizeYUniform = gl.getUniformLocation(shaderProgramFieldVector, "stepSizeY");
    	shaderProgramFieldVector.brightnessUniform = gl.getUniformLocation(shaderProgramFieldVector, "brightness");
    	shaderProgramFieldVector.arrowTextureUniform = gl.getUniformLocation(shaderProgramFieldVector, "uArrowTexture");
    	shaderProgramFieldVector.textureMatrixUniform = gl.getUniformLocation(shaderProgramFieldVector, "uTextureMatrix");
    	shaderProgramFieldVector.eMultUniform = gl.getUniformLocation(shaderProgramFieldVector, "uEMult");
    	shaderProgramFieldVector.pMultUniform = gl.getUniformLocation(shaderProgramFieldVector, "uPMult");
    	shaderProgramFieldVector.pointSizeUniform = gl.getUniformLocation(shaderProgramFieldVector, "pointSize");
    }

    var arrowTexture;

    function initTextures() {
    }


    var mvMatrix = mat4.create();
    var mvMatrixStack = [];
    var pMatrix = mat4.create();
    var matrix3d = mat4.create();
    var zoom3d = 1;
    
    function mvPushMatrix() {
        var copy = mat4.create();
        mat4.set(mvMatrix, copy);
        mvMatrixStack.push(copy);
    }

    function mvPopMatrix() {
        if (mvMatrixStack.length == 0) {
            throw "Invalid popMatrix!";
        }
        mvMatrix = mvMatrixStack.pop();
    }

    function setMatrixUniforms(shaderProgram) {
        gl.uniformMatrix4fv(shaderProgram.pMatrixUniform, false, pMatrix);
        gl.uniformMatrix4fv(shaderProgram.mvMatrixUniform, false, mvMatrix);

        var normalMatrix = mat3.create();
        mat4.toInverseMat3(mvMatrix, normalMatrix);
        mat3.transpose(normalMatrix);
        gl.uniformMatrix3fv(shaderProgram.nMatrixUniform, false, normalMatrix);
    }

    function degToRad(degrees) {
        return degrees * Math.PI / 180;
    }


//
// Initialize a texture and load an image.
// When the image finished loading copy it into the texture.
//
function loadTexture(gl, url) {
  const texture = gl.createTexture();
  gl.bindTexture(gl.TEXTURE_2D, texture);

  // Because images have to be download over the internet
  // they might take a moment until they are ready.
  // Until then put a single pixel in the texture so we can
  // use it immediately. When the image has finished downloading
  // we'll update the texture with the contents of the image.
  const level = 0;
  const internalFormat = gl.RGBA;
  const width = 1;
  const height = 1;
  const border = 0;
  const srcFormat = gl.RGBA;
  const srcType = gl.UNSIGNED_BYTE;
  const pixel = new Uint8Array([0, 0, 255, 255]);  // opaque blue
  gl.texImage2D(gl.TEXTURE_2D, level, internalFormat,
                width, height, border, srcFormat, srcType,
                pixel);

  const image = new Image();
  image.onload = function() {
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texImage2D(gl.TEXTURE_2D, level, internalFormat,
                  srcFormat, srcType, image);

    // WebGL1 has different requirements for power of 2 images
    // vs non power of 2 images so check if the image is a
    // power of 2 in both dimensions.
    if (isPowerOf2(image.width) && isPowerOf2(image.height)) {
       // Yes, it's a power of 2. Generate mips.
       gl.generateMipmap(gl.TEXTURE_2D);
    } else {
       // No, it's not a power of 2. Turn of mips and set
       // wrapping to clamp to edge
       gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
       gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
       gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    }
  };
  image.src = url;

  return texture;
}

function isPowerOf2(value) {
  return (value & (value - 1)) == 0;
}



    var fbType;

    function initTextureFramebuffer(sz) {
    	var rttFramebuffer = gl.createFramebuffer();
    	gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    	rttFramebuffer.width = sz;
    	rttFramebuffer.height = sz;
    	//console.log("makgin framebuffer of size " + sz);

    	var rttTexture = gl.createTexture();
    	gl.bindTexture(gl.TEXTURE_2D, rttTexture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    	//gl.generateMipmap(gl.TEXTURE_2D);

    	//gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, rttFramebuffer.width, rttFramebuffer.height, 0, gl.RGBA, gl.UNSIGNED_BYTE, null);
    	gl.HALF_FLOAT_OES = 0x8D61;
    	
    	if (fbType == 0) {
    		// this works on android
    		gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, rttFramebuffer.width, rttFramebuffer.height, 0, gl.RGBA, gl.FLOAT, null);
    	} else {
    		// for ios
    		gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGB, rttFramebuffer.width, rttFramebuffer.height, 0, gl.RGB, gl.HALF_FLOAT_OES, null);
    	}

    	var renderbuffer = gl.createRenderbuffer();
    	gl.bindRenderbuffer(gl.RENDERBUFFER, renderbuffer);
//    	gl.renderbufferStorage(gl.RENDERBUFFER, gl.DEPTH_COMPONENT16, rttFramebuffer.width, rttFramebuffer.height);

    	gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, rttTexture, 0);
//    	gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_ATTACHMENT, gl.RENDERBUFFER, renderbuffer);

        var status = gl.checkFramebufferStatus(gl.FRAMEBUFFER);
        if (status !== gl.FRAMEBUFFER_COMPLETE) {
          console.log("failed to create framebuffer");
          return null;
        }

        var pixels = new Float32Array(4);
        gl.readPixels(0, 0, 1, 1, gl.RGBA, gl.FLOAT, pixels);
        if (gl.getError() != gl.NO_ERROR)
            console.log("readPixels failed");
        else
            renderer.readPixelsWorks = true;

    	gl.bindTexture(gl.TEXTURE_2D, null);
    	gl.bindRenderbuffer(gl.RENDERBUFFER, null);
    	gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    	return {framebuffer:rttFramebuffer, texture:rttTexture};
    }

    function deleteRenderTexture(rt) {
    	if (!rt)
    		return;
    	gl.deleteTexture(rt.texture);
    	gl.deleteFramebuffer(rt.framebuffer);
    }

    var fullScreenVertexPositionBuffer;
    var fullScreenVertexTextureCoordBuffer;
    var screen3DTextureBuffer;
    var simVertexPositionBuffer;
    var simVertexTextureCoordBuffer;
    var simVertexBuffer;
    
    var simPosition = [];
    var simTextureCoord = [];
    var srcCoords = [
                     -.26, 0, -.25, 0
                     ];
    var gridSize3D = 128;
    var gridRange;

    function initBuffers() {
    	if (!fullScreenVertexPositionBuffer)
    		fullScreenVertexPositionBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexPositionBuffer);
    	vertices = [
    	            -1, +1,
    	            +1, +1,
    	            -1, -1,
    	            +1, -1,
    	            ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
    	fullScreenVertexPositionBuffer.itemSize = 2;
    	fullScreenVertexPositionBuffer.numItems = 4;

    	if (!fullScreenVertexTextureCoordBuffer)
    		fullScreenVertexTextureCoordBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexTextureCoordBuffer);
    	var textureCoords = [
    	                     windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY
    	                     ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords), gl.STATIC_DRAW);
    	fullScreenVertexTextureCoordBuffer.itemSize = 2;
    	fullScreenVertexTextureCoordBuffer.numItems = 4;

    	if (!sourceBuffer)
    		sourceBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
    	sourceBuffer.itemSize = 2;
    	sourceBuffer.numItems = 2;

    	if (!colorBuffer)
    		colorBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
    	colorBuffer.itemSize = 4;
    	colorBuffer.numItems = 2;

    	if (!screen3DTextureBuffer)
    		screen3DTextureBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, screen3DTextureBuffer);
    	screen3DTextureBuffer.itemSize = 2;
    	var texture3D = [];
    	gridRange = textureCoords[2]-textureCoords[0];
    	for (i = 0; i <= gridSize3D; i++) {
    		texture3D.push(textureCoords[0],
    					   textureCoords[0]+gridRange*i/gridSize3D,
    					   textureCoords[0]+gridRange/gridSize3D,
    					   textureCoords[0]+gridRange*i/gridSize3D);
    	}
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(texture3D), gl.STATIC_DRAW);
    	screen3DTextureBuffer.numItems = texture3D.length / 2;
    	
    	simPosition = [];
    	simTextureCoord = [];
    	
    	setPosRect(1, 1, gridSizeX-2, gridSizeY-2, gridSizeX);

    	if (!simVertexPositionBuffer)
    		simVertexPositionBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
    	simVertexPositionBuffer.itemSize = 2;
    	simVertexPositionBuffer.numItems = simPosition.length/2;

    	if (!simVertexTextureCoordBuffer)
    		simVertexTextureCoordBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
    	simVertexTextureCoordBuffer.itemSize = 2;
    	simVertexTextureCoordBuffer.numItems = simPosition.length/2;

    	indexBuffer = gl.createBuffer();
    }

    // create coordinates for a rectangular portion of the grid
    function setPosRect(x1, y1, x2, y2, gx) {
    	var points = [ x2, y1, x1, y1, x2, y2, x1, y1, x2, y2, x1, y2 ];
    	var i;
    	for (i = 0; i != 6; i++) {
    		var xi = points[i*2];
    		var yi = points[i*2+1];
    		simPosition.push(-1+2*xi/gx, -1+2*yi/gx);
    		simTextureCoord.push(xi/gx, yi/gx);
    	}
    }

    var sourceBuffer;
    var colorBuffer;
    var indexBuffer;
    var colors;
    var chargeColors;
    var destHeight;
    var minFeatureWidth;

    renderer.setDestination = function (rtnum) {
    	var rt = renderTextures[rtnum];
    	var rttFramebuffer = rt.framebuffer;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
        destHeight = rttFramebuffer.height;

        //console.log("setting destination to " + rtnum + ", height = " + destHeight);
	// minimum width/height of anything drawn, should always be at least one pixel width
        minFeatureWidth = (windowWidth / rttFramebuffer.width) * 1.5;
        renderer.minFeatureWidth = minFeatureWidth;
    }
    
    // we do this to work around apparent Javascript bug on Safari
    renderer.getMinFeatureWidth = function () { return minFeatureWidth; }

    renderer.runRelax = function (srcnum, rsnum, resid) {
    	var sourceRT = renderTextures[srcnum];
    	var rightSideRT = renderTextures[rsnum];
        var prog = resid ? shaderProgramResidual : shaderProgramRelax;
        gl.useProgram(prog);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.uniform1i(prog.sourceTextureUniform, 0);
        gl.uniform1f(prog.stepSizeXUniform, 1/sourceRT.framebuffer.width);
        gl.uniform1f(prog.stepSizeYUniform, 1/sourceRT.framebuffer.height);

        gl.activeTexture(gl.TEXTURE1);
        gl.bindTexture(gl.TEXTURE_2D, rightSideRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.uniform1i(prog.rightSideTextureUniform, 1);
 
        setMatrixUniforms(prog);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    renderer.copyRG = function (srcnum) {
    	var sourceRT = renderTextures[srcnum];
        var prog = shaderProgramCopyRG;
        gl.useProgram(prog);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.uniform1i(prog.sourceTextureUniform, 0);
 
        setMatrixUniforms(prog);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    renderer.copyRGB = function (srcnum) {
    	var sourceRT = renderTextures[srcnum];
        var prog = shaderProgramCopyRGB;
        gl.useProgram(prog);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.uniform1i(prog.sourceTextureUniform, 0);
 
        setMatrixUniforms(prog);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    renderer.addMult = function (srcnum, rsnum, mult) {
    	var sourceRT = renderTextures[srcnum];
    	var rightSideRT = renderTextures[rsnum];
        var prog = shaderProgramAdd;
        gl.useProgram(prog);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.uniform1i(prog.sourceTextureUniform, 0);

        gl.activeTexture(gl.TEXTURE1);
        gl.bindTexture(gl.TEXTURE_2D, rightSideRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.uniform1i(prog.rightSideTextureUniform, 1);
        gl.uniform4fv(prog.multUniform, mult);
 
        setMatrixUniforms(prog);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    renderer.calcDifference = function (src1, src2) {
    	var sourceRT = renderTextures[src1];
    	var rightSideRT = renderTextures[src2];
        var prog = shaderProgramAdd;
        gl.useProgram(prog);
        //gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.uniform1i(prog.sourceTextureUniform, 0);

        gl.activeTexture(gl.TEXTURE1);
        gl.bindTexture(gl.TEXTURE_2D, rightSideRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.uniform1i(prog.rightSideTextureUniform, 1);
 
        setMatrixUniforms(prog);
        gl.uniform4f(prog.multUniform, -1, 0, 0, 0);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);

        var pixels = new Float32Array(4*destHeight*destHeight);
        gl.readPixels(0, 0, destHeight, destHeight, gl.RGBA, gl.FLOAT, pixels);
        var i;
        var tot = 0;
        for (i = 0; i != pixels.length; i += 4)
            tot += Math.abs(pixels[i]);
        console.log("total diff = " + tot);
        return tot;
    }

    renderer.sum = function (srcnum) {
    	var sourceRT = renderTextures[srcnum];
        var prog = shaderProgramSum;
        gl.useProgram(prog);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.uniform1i(prog.sourceTextureUniform, 0);
        gl.uniform1f(prog.stepSizeXUniform, 1/sourceRT.framebuffer.width);
        gl.uniform1f(prog.stepSizeYUniform, 1/sourceRT.framebuffer.height);

        setMatrixUniforms(prog);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    renderer.writeCharge = function (x, y, f) {
        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0.0, f, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = srcCoords[2] = x;
        srcCoords[1] = y;
        var off = gridSizeY / destHeight;
        srcCoords[3] = srcCoords[1]+off;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

//        gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexTextureCoordBuffer);
//        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, fullScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
	gl.colorMask(false, true, false, false);
        gl.drawArrays(gl.LINES, 0, 2);
	gl.colorMask(true, true, true, true);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        //mvPopMatrix();
    }

    renderer.drawHandle = function (x, y) {
        gl.useProgram(shaderProgramDraw);
        if (renderer.drawingSelection >= 0) {
        	renderer.drawSelectedHandle(x, y);
        	return;
        }
        if (renderer.drawingSelection < 0)
        	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1, 1.0, 1.0, 1.0);
        else 
        	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, renderer.drawingSelection,
        			renderer.drawingSelection, 0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = -1+2*(x+.5)/windowWidth;
        var cy = +1-2*(y+.5)/windowHeight;
        var ox = .01;
        var oy = .01;
        var coords = [ cx-ox, cy-oy, cx+ox, cy-oy, cx+ox, cy+oy, cx-ox, cy+oy ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        mat4.identity(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(renderer.drawingSelection < 0 ? 1 : 2);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.LINE_LOOP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    // display draggable charge
    renderer.drawChargeObject = function (x, y, r, value) {
        gl.useProgram(shaderProgramDraw);
        if (value > 0)
          gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1, 1, 0, 1);
        else
          gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0, 0, 1, 1);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = -1+2*(x+.5)/windowWidth;
        var cy = +1-2*(y+.5)/windowHeight;
        var coords = [cx, cy];
        r *= 2/windowWidth;
        var i;
        for (i = 0; i <= 20; i++) {
          var ang = Math.PI*i/10;
          coords.push(cx+r*Math.cos(ang), cy+r*Math.sin(ang));
        }
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        mat4.identity(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(renderer.drawingSelection < 0 ? 1 : 2);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.TRIANGLE_FAN, 0, coords.length/2);

        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0, 0, 0, 1);
        const d1 = .025;
        const d2 = .005;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([cx-d1, cy-d2, cx+d1, cy-d2, cx-d1, cy+d2, cx+d1, cy+d2]), gl.STATIC_DRAW);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        if (value > 0) {
          gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([cx-d2, cy-d1, cx+d2, cy-d1, cx-d2, cy+d1, cx+d2, cy+d1]), gl.STATIC_DRAW);
          gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        }
        
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    renderer.drawSelectedHandle = function (x, y) {
       	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, renderer.drawingSelection, renderer.drawingSelection, 0, 0.5);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = -1+2*(x+.5)/windowWidth;
        var cy = +1-2*(y+.5)/windowHeight;
        var ox = .012;
        var oy = .012;
        var coords = [ cx-ox, cy-oy, cx+ox, cy-oy, cx-ox, cy+oy, cx+ox, cy+oy ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        mat4.identity(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(renderer.drawingSelection < 0 ? 1 : 2);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    renderer.drawFocus = function (x, y) {
        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1, 1.0, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = x; // -1+2*(x+.5)/windowWidth;
        var cy = y; // +1-2*(y+.5)/windowHeight;
        var ox = 3;
        var oy = 3;
        var coords = [ cx-ox, cy, cx+ox, cy, cx, cy+oy, cx, cy-oy ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.LINES, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        //mvPopMatrix();
    }

    renderer.drawLineSource = function (x, y, x2, y2, f) {
        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, f, 0.0, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = x;
        srcCoords[1] = y;
        srcCoords[2] = x2;
        srcCoords[3] = y2;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.LINES, 0, 2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    function loadMatrix(mtx) {
    	mat4.identity(mtx);
    	if (renderer.drawingSelection > 0) {
    		// drawing on screen
        	mtx[0] = +2/windowWidth;
        	mtx[5] = -2/windowHeight;
        	mtx[12] = -1 + .5*mtx[0];
        	mtx[13] = +1 + .5*mtx[5];
    	} else {
    		// drawing walls into render texture
        	mtx[0] = +2/gridSizeX;
        	mtx[5] = -2/gridSizeY;
        	mtx[12] = -1 + (.5+windowOffsetX)*mtx[0];
        	mtx[13] = +1 + (.5+windowOffsetY)*mtx[5];
    	}
    	mat4.multiply(mtx, [transform[0], transform[3], 0, 0,
    	                    transform[1], transform[4], 0, 0,
    	                    0,0,1,0,
    	                    transform[2], transform[5], 0, 1], mtx);
    }

    function setupForDrawing(v) {
        gl.useProgram(shaderProgramDraw);
        if (renderer.drawingSelection > 0) {
    		gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, renderer.drawingSelection,
    				renderer.drawingSelection, 0, 1.0);
        } else {
    		//var rttFramebuffer = renderTexture1.framebuffer;
    		//gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		//gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
            
            // blue channel used for walls and media
    		gl.colorMask(false, true, true, false);
    		gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0.0, 0.0, v, 1.0);
    	}
    }
    
    // gl.lineWidth does not work on Chrome, so we need this workaround to draw lines as
    // triangle strips instead
    function thickLinePoints(arr, thick) {
    	var i;
    	var result = [];
    	var ax = 0, ay = 0;
    	for (i = 0; i < arr.length-2; i += 2) {
    		var dx = arr[i+2] - arr[i];
    		var dy = arr[i+3] - arr[i+1];
    		var dl = Math.hypot(dx, dy);
    		if (dl > 0) {
    			var mult = thick/dl;
    			ax =  mult*dy;
    			ay = -mult*dx;
    		}	
    		result.push(arr[i]+ax, arr[i+1]+ay, arr[i]-ax, arr[i+1]-ay);
    	}
    	result.push(arr[i]+ax, arr[i+1]+ay, arr[i]-ax, arr[i+1]-ay);
    	return result;
    }
    
    function drawWall(x, y, x2, y2, pot) {
    	setupForDrawing(1);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords = thickLinePoints([x, y, x2, y2, x, y], renderer.drawingSelection == 1 ? .5 : renderer.drawingSelection > 0 ? 1.5 : Math.max(renderer.minFeatureWidth, 1.5));
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(3);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.drawArrays(gl.LINE_STRIP, 0, 3);

	if (renderer.drawingSelection < 0)
          gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, pot, 0.0, 0.0, 1.0);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 6);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.lineWidth(1);

		gl.colorMask(true, true, true, true);
		//gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    renderer.drawEllipse = function (cx, cy, xr, yr) {
    	setupForDrawing(0);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var coords = [];
        var i;
        for (i = -xr; i <= xr; i++) {
        	coords.push(cx-i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        for (i = xr-1; i >= -xr; i--) {
        	coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        coords.push(coords[0], coords[1]);
//        console.log("coords for ellipse: " + coords);
        coords = thickLinePoints(coords, renderer.drawingSelection == 1 ? .5 : 1.5);
//        gl.lineWidth(4);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.lineWidth(1);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    renderer.drawLens = function (x1, y1, w, h, m) {
    	setupForDrawing(m);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var i;
        var w2 = w/2;
        var coords = [x1+w2, y1+h];
        var ym = h/(Math.sqrt(2)-1);
        for (i = 0; i <= w; i++) {
        	var x = (i-w2)/w2;
        	var y = ym*(Math.sqrt(1+x*x)-1);
        	coords.push(x1+i, y1+y);
        }
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_FAN, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }
    
    renderer.writeShape = function (contours) {
        var tess = Tess2.tesselate({
           contours: contours,
           polySize: 3 // output triangles
        });

        var med = renderer.permittivity;
        var pot = renderer.residual ? 0 : renderer.potential;
        if (med == undefined) {
            gl.colorMask(false, true, false, false);
            med = 0;
        } else
            gl.colorMask(false, med == 0, true, false);
        gl.useProgram(shaderProgramDraw);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(tess.vertices), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(tess.elements), gl.STATIC_DRAW);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0, pot, med, 1.0);
        gl.drawElements(gl.TRIANGLES, tess.elements.length, gl.UNSIGNED_SHORT, 0);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        gl.colorMask(true, true, true, true);
    }

    renderer.displayCharge = function (contours) {
        var tess = Tess2.tesselate({
           contours: contours,
           polySize: 3 // output triangles
        });

        gl.useProgram(shaderProgramViewCharge);
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramViewCharge);
	
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(tess.vertices), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramViewCharge.vertexPositionAttribute, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramViewCharge.vertexPositionAttribute);

        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(tess.elements), gl.STATIC_DRAW);

        // grid containing calculated charge
    	var sourceRT = renderTextures[renderer.chargeSource];
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.uniform1i(shaderProgramViewCharge.sourceTextureUniform, 0);

        var matx = [1/gridSizeX,0,0,0, 0,-1/gridSizeY,0,0, 0,0,1,0, (windowOffsetX+.5)/gridSizeX,1-(windowOffsetY+.5)/gridSizeY,0,1];
        mat4.multiply(matx, [transform[0], transform[3], 0, 0,
                            transform[1], transform[4], 0, 0,
                            0,0,1,0,
                            transform[2], transform[5], 0, 1], matx);
	gl.uniformMatrix4fv(shaderProgramViewCharge.textureMatrixUniform, false, matx);

        gl.uniform1f(shaderProgramViewCharge.brightnessUniform, brightness*2);
        gl.drawElements(gl.TRIANGLES, tess.elements.length, gl.UNSIGNED_SHORT, 0);

        gl.disableVertexAttribArray(shaderProgramViewCharge.vertexPositionAttribute);
    }

    renderer.calcCharge = function (contours) {
        var tess = Tess2.tesselate({
           contours: contours,
           polySize: 3 // output triangles
        });

        gl.useProgram(shaderProgramCalcCharge);
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramCalcCharge);
	
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(tess.vertices), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramCalcCharge.vertexPositionAttribute, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramCalcCharge.vertexPositionAttribute);

        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(tess.elements), gl.STATIC_DRAW);

        // potential grid to calculate the charge from
    	var sourceRT = renderTextures[renderer.chargeSource];
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.uniform1i(shaderProgramCalcCharge.sourceTextureUniform, 0);

        var matx = [1/gridSizeX,0,0,0, 0,-1/gridSizeY,0,0, 0,0,1,0, (windowOffsetX+.5)/gridSizeX,1-(windowOffsetY+.5)/gridSizeY,0,1];
        mat4.multiply(matx, [transform[0], transform[3], 0, 0,
                            transform[1], transform[4], 0, 0,
                            0,0,1,0,
                            transform[2], transform[5], 0, 1], matx);
	gl.uniformMatrix4fv(shaderProgramCalcCharge.textureMatrixUniform, false, matx);

        gl.drawElements(gl.TRIANGLES, tess.elements.length, gl.UNSIGNED_SHORT, 0);
        gl.disableVertexAttribArray(shaderProgramCalcCharge.vertexPositionAttribute);
    }

    renderer.getProbeValue = function (x, y) {
        var pixels = new Float32Array(4*9);
        gl.readPixels(windowOffsetX+x-1, gridSizeY-windowOffsetY-y-2, 3, 3, gl.RGBA, gl.FLOAT, pixels);
        return [pixels[4*4], pixels[1*4], pixels[7*4], pixels[3*4], pixels[5*4]];
    }

    renderer.getCharge = function () {
        var pixels = new Float32Array(4*destHeight*destHeight);
        gl.readPixels(0, 0, destHeight, destHeight, gl.RGBA, gl.FLOAT, pixels);
        var i;
        var charge = 0;
        for (i = 0; i != pixels.length; i += 4)
            charge += pixels[i];
        return charge;
    }

    renderer.clearDisplay = function () {
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
	gl.clearColor(0, 0, 0, 1);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)
    }

    renderer.setBrightness = function (bright) {
        brightness = bright;
    }

    renderer.displayScalar = function (s, rs, bright, pot) {
        gl.useProgram(shaderProgramMain);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
        brightness = bright;

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
	gl.clearColor(0, 0, 0, 1);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        // draw result
        gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgramMain.vertexPositionAttribute, fullScreenVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramMain.textureCoordAttribute, fullScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[s].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);

        gl.activeTexture(gl.TEXTURE2);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[rs].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        gl.uniform1i(shaderProgramMain.samplerUniform, 0);
        gl.uniform1i(shaderProgramMain.rightSideTextureUniform, 2);
        gl.uniform1f(shaderProgramMain.brightnessUniform, bright);
        if (pot)
          gl.uniform3fv(shaderProgramMain.colorsUniform, colors);
        else
          gl.uniform3fv(shaderProgramMain.colorsUniform, chargeColors);

        setMatrixUniforms(shaderProgramMain);
        gl.enableVertexAttribArray(shaderProgramMain.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramMain.textureCoordAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, fullScreenVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(shaderProgramMain.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramMain.textureCoordAttribute);

        mvPopMatrix();
    }

    renderer.displayScalarField = function (s, rs, mults) {
        var prog = shaderProgramScalarField;
        gl.useProgram(prog);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
	gl.clearColor(0, 0, 0, 1);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        // draw result
        gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexPositionBuffer);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, fullScreenVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(prog.textureCoordAttribute, fullScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[s].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);

        gl.activeTexture(gl.TEXTURE2);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[rs].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        gl.uniform1i(prog.samplerUniform, 0);
        gl.uniform1i(prog.rightSideTextureUniform, 2);
        gl.uniform1f(prog.stepSizeXUniform, 1/gridSizeX);
        gl.uniform1f(prog.stepSizeYUniform, 1/gridSizeY);
        gl.uniform1fv(prog.multsUniform, mults);
        gl.uniform3fv(prog.colorsUniform, colors);

        setMatrixUniforms(prog);
        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, fullScreenVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);

        mvPopMatrix();
    }

    renderer.drawSceneEquip = function(s, rs, bright) {
        if (bright == 0)
          return;
        gl.useProgram(shaderProgramEquip);
/*
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)
*/

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        // draw result
        gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgramEquip.vertexPositionAttribute, fullScreenVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, fullScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramEquip.textureCoordAttribute, fullScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[s].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        gl.activeTexture(gl.TEXTURE2);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[rs].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        gl.uniform1i(shaderProgramEquip.samplerUniform, 0);
        gl.uniform1i(shaderProgramEquip.rightSideTextureUniform, 2);
        gl.uniform1f(shaderProgramEquip.brightnessUniform, bright);
        gl.uniform1f(shaderProgramEquip.stepSizeXUniform, .5/gl.viewportWidth);
        gl.uniform1f(shaderProgramEquip.stepSizeYUniform, .5/gl.viewportHeight);
        gl.uniform3fv(shaderProgramEquip.colorsUniform, colors);

        setMatrixUniforms(shaderProgramEquip);
        gl.enableVertexAttribArray(shaderProgramEquip.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramEquip.textureCoordAttribute);
        gl.enable(gl.BLEND);
	gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, fullScreenVertexPositionBuffer.numItems);
        gl.disable(gl.BLEND);
        gl.disableVertexAttribArray(shaderProgramEquip.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramEquip.textureCoordAttribute);

        mvPopMatrix();
    }

    renderer.fetchPotentialPixels = function(s) {
        renderer.setDestination(s);
        potPixels = new Float32Array(4*windowWidth*windowHeight);
        gl.readPixels(windowOffsetX, windowOffsetY, windowWidth, windowHeight, gl.RGBA, gl.FLOAT, potPixels);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
        var i; 
        for (i = 3; i < potPixels.length; i += 4)
          potPixels[i] = 0;
    }

    renderer.freePotentialPixels = function() {
        potPixels = null;
    }

    renderer.drawFieldLine = function (x, y, dir) {
        var i, j;
        y = windowHeight-1-y;
        x += .5; y += .5;
        var coords = [x, y];
        var xi = Math.floor(x);
        var yi = Math.floor(y);
        // check to see if we already drew a field line nearby
        for (i = -2; i <= 2; i++)
          for (j = -2; j <= 2; j++) {
            if (i*i+j*j > 4)
              continue;
            if (potPixels[4*(xi+i+(yi+j)*windowWidth)+3] > 0)
              return;
          }
        for (i = 0; i != 800; i++) {
            xi = Math.floor(x);
            yi = Math.floor(y);
            if (xi <= 0 || yi <= 0 || xi >= windowWidth-1 || yi >= windowHeight-1) break;
            var ind = 4*(xi+yi*windowWidth);
            var r0 = potPixels[ind];
            // mark where we've drawn field lines already.  but avoid marking for field lines that are just stubs.
            if (i > 5)
              potPixels[ind+3] = 1;
            //if (Math.abs(r0) > 80) break;
            var ru = potPixels[ind-windowWidth*4];
            var rd = potPixels[ind+windowWidth*4];
            var rl = potPixels[ind-4];
            var rr = potPixels[ind+4];
            var dx = rr-rl;
            var dy = rd-ru;
            var dl = Math.hypot(dx, dy);
            if (dl == 0) break;
            if (dl*brightness < .05) break;
            dl *= dir;
            x += dx/dl;
            y += dy/dl;
            coords.push(x, y);
        }
  
        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1, 1, 1, 1);
  
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);
  
      	mat4.identity(pMatrix);
        pMatrix[0] = +2/windowWidth;
        pMatrix[5] = +2/windowHeight;
        pMatrix[12] = -1 + .5*pMatrix[0];
        pMatrix[13] = -1 + .5*pMatrix[5];
        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.LINE_STRIP, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    renderer.drawFieldLinesShape = function (bounds) {
        var i = 0;
        // only do outside
        var poly = bounds[0];
        renderer.transformBoundary([poly]);
        var sep = 15;
        var dist = 0;
        while (true) {
          var x = poly[i];
          var y = poly[i+1];
          var dx = poly[(i+2) % poly.length]-x;
          var dy = poly[(i+3) % poly.length]-y;
          var dl = Math.hypot(dx, dy);
          if (dist > dl) {
            dist -= dl;
            i += 2;
            if (i == poly.length)
              break;
            continue;
          }
          renderer.drawFieldLine(x+dx*dist/dl, y+dy*dist/dl, 1);
          renderer.drawFieldLine(x+dx*dist/dl, y+dy*dist/dl, -1);
          dist += sep;
        }
    }

    renderer.displayField = function (s, rs, bright, emult, pmult, count) {
        gl.useProgram(shaderProgramFieldVector);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
        brightness = bright;

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
	gl.clearColor(0, 0, 0, 1);

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[s].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        gl.activeTexture(gl.TEXTURE1);
        gl.bindTexture(gl.TEXTURE_2D, arrowTexture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);

        var coords = [];
        var i, j;
        for (i = 0; i != count; i++)
          for (j = 0; j != count; j++) {
            coords.push(-1+(i+.5)/(count/2), -1+(j+.5)/(count/2));
          }
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.DYNAMIC_DRAW);
        gl.vertexAttribPointer(shaderProgramFieldVector.vertexPositionAttribute, 2, gl.FLOAT, false, 0, 0);

        gl.uniform1i(shaderProgramFieldVector.samplerUniform, 0);
        gl.uniform1i(shaderProgramFieldVector.arrowTextureUniform, 1);
        gl.uniform1i(shaderProgramFieldVector.rightSideTextureUniform, 2);
        gl.uniform1f(shaderProgramFieldVector.brightnessUniform, bright);
        gl.uniform1f(shaderProgramFieldVector.stepSizeXUniform, .5/gl.viewportWidth);
        gl.uniform1f(shaderProgramFieldVector.stepSizeYUniform, .5/gl.viewportHeight);
        gl.uniform1f(shaderProgramFieldVector.eMultUniform, emult);
        gl.uniform1f(shaderProgramFieldVector.pMultUniform, pmult);
        gl.uniform1f(shaderProgramFieldVector.pointSizeUniform, gl.viewportWidth/count);
        var matx = [.5*(gridSizeX-windowOffsetX*2)/gridSizeX, 0, 0, 0, .5*(gridSizeY-windowOffsetY*2)/gridSizeY, 0, 0, 0, 1];
        matx[6] = windowOffsetX/gridSizeX + matx[0];
        matx[7] = windowOffsetY/gridSizeY + matx[4];
	gl.uniformMatrix3fv(shaderProgramFieldVector.textureMatrixUniform, false, matx);

        gl.enableVertexAttribArray(shaderProgramEquip.vertexPositionAttribute);
        gl.enable(gl.BLEND);
        // canvas background color must be black for this to work (otherwise white may show through)
	gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
        gl.drawArrays(gl.POINTS, 0, coords.length/2);
        gl.disable(gl.BLEND);
        gl.disableVertexAttribArray(shaderProgramEquip.vertexPositionAttribute);
    }

    renderer.checkIntersection = function (bounds1, bounds2) {
      var i;
      // use tesselator to calculate intersection.  calculate union.
      // if it's a single shape, the polygons intersect
      var res = Tess2.tesselate({
          contours: [bounds1, bounds2],
          elementType: Tess2.BOUNDARY_CONTOURS,
          windingRule: Tess2.WINDING_POSITIVE
      });
      return res.elementCount == 1;
    }

    renderer.transformBoundary = function (bounds) {
      var i;
      var j;
      for (j = 0; j != bounds.length; j++) {
        var arr = bounds[j];
        for (i = 0; i < arr.length; i += 2) {
          var x = arr[i];
          var y = arr[i+1];
          arr[i]   = transform[0]*x + transform[1]*y + transform[2];
          arr[i+1] = transform[3]*x + transform[4]*y + transform[5];
        }
      }
    }

    renderer.drawScene3D = function (s, rs, bright, equipMult) {
        gl.useProgram(shaderProgram3D);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
        brightness = bright;

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        mat4.perspective(45, gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
        mat4.translate(mvMatrix, [0, 0, -3.2]);
        mat4.multiply(mvMatrix, matrix3d, mvMatrix);
        mat4.scale(mvMatrix, [zoom3d, zoom3d, zoom3d]);
        
	// draw result
        gl.bindBuffer(gl.ARRAY_BUFFER, screen3DTextureBuffer);
        gl.vertexAttribPointer(shaderProgram3D.textureCoordAttribute, screen3DTextureBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[s].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        gl.activeTexture(gl.TEXTURE2);
        gl.bindTexture(gl.TEXTURE_2D, renderTextures[rs].texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);

        gl.uniform1i(shaderProgram3D.samplerUniform, 0);
        gl.uniform1f(shaderProgram3D.brightnessUniform, bright);
        gl.uniform1f(shaderProgram3D.equipMultUniform, equipMult);
        gl.uniform3fv(shaderProgram3D.colorsUniform, colors);
        gl.uniform1i(shaderProgram3D.rightSideTextureUniform, 2);
        gl.uniform1f(shaderProgram3D.stepSizeXUniform, .5/gl.viewportWidth);
        gl.uniform1f(shaderProgram3D.stepSizeYUniform, .5/gl.viewportHeight);
	gl.uniformMatrix4fv(shaderProgram3D.normalMatrixUniform, false, matrix3d);

        setMatrixUniforms(shaderProgram3D);
        gl.enableVertexAttribArray(shaderProgram3D.textureCoordAttribute);
        gl.enable(gl.DEPTH_TEST);
        var i;
        for (i = 0; i != gridSize3D; i++) {
            gl.uniform1f(shaderProgram3D.xOffsetUniform, gridRange*i/gridSize3D);
        	gl.drawArrays(gl.TRIANGLE_STRIP, 0, screen3DTextureBuffer.numItems);
        }
        gl.disable(gl.DEPTH_TEST);
        gl.disableVertexAttribArray(shaderProgram3D.textureCoordAttribute);

        mvPopMatrix();
    }

    var lastTime = 0;

    document.passCanvas = function passCanvas (cv) {
    	canvas = cv;
    	gl = cv.getContext("experimental-webgl");
        arrowTexture = loadTexture(gl, 'arrow.png');

    	console.log("got gl context " + gl + " " + cv.width + " " + cv.height);
    	var float_texture_ext = gl.getExtension('OES_texture_float');
    	var float_texture_linear_ext = gl.getExtension('OES_texture_float_linear');
    	var half_float_texture_ext = gl.getExtension('OES_texture_half_float');
	gl.getExtension('EXT_float_blend');

    	fbType = 0;
    	var renderTexture2 = initTextureFramebuffer(64);
    	if (!renderTexture2) {
    		// float didn't work, try half float
    		fbType = 1;
        	renderTexture2 = initTextureFramebuffer(64);
        	if (!renderTexture2) {
        		alert("Couldn't create frame buffer, try javascript version");
        		return;
        	}
    	}
    	deleteRenderTexture(renderTexture2);
    	initShaders();
    	initTextures();
        mat4.identity(matrix3d);
	mat4.rotateX(matrix3d, -Math.PI/3);

    	gl.clearColor(0.0, 0.0, 1.0, 1.0);

    	renderer.acoustic = false;
    	renderer.readPixelsWorks = false;
    	renderer.setResolution = function (x, y, wx, wy) {
    		gridSizeX = x;
    		gridSizeY = y;
    		windowOffsetX = wx;
    		windowOffsetY = wy;
    		console.log("setres " + gridSizeX + " " + windowOffsetX);
    		windowWidth  = gridSizeX-windowOffsetX*2;
    		windowHeight = gridSizeY-windowOffsetY*2;
    		for (var i = 0; i != renderTextures.length; i++)
    			deleteRenderTexture(renderTextures[i]);
    		renderTextures = [];
    		var sz = 8;

                // create 3 of each size, doubling each time
    		while (1) {
    			console.log("creating buffers size " + sz + " at " + renderTextures.length);
    			renderTextures.push(initTextureFramebuffer(sz));
    			renderTextures.push(initTextureFramebuffer(sz));
    			renderTextures.push(initTextureFramebuffer(sz));
    			if (sz >= gridSizeX)
    				break;
    			sz *= 2;
    		}

    		// create an extra one full size to store result
    		renderTextures.push(initTextureFramebuffer(sz));
    		initBuffers();
    	}
    	renderer.getRenderTextureCount = function () { return renderTextures.length-1; }
    	renderer.drawWall = function (x, y, x2, y2, pot) { drawWall(x, y, x2, y2, pot); }
    	renderer.clearWall = function (x, y, x2, y2) { drawWall(x, y, x2, y2, 1); }
    	renderer.setTransform = function (a, b, c, d, e, f) {
    		transform[0] = a; transform[1] = b;
    		transform[2] = c; transform[3] = d;
    		transform[4] = e; transform[5] = f;
    	}
    	renderer.clearDestination = function () {
        	gl.clearColor(0.0, 0.0, 1.0, 1.0);
    		gl.clear(gl.COLOR_BUFFER_BIT);
    	}
    	renderer.set3dViewAngle = function (x, y) {
    	    var mtemp = mat4.create();
    	    mat4.identity(mtemp);
    		mat4.rotateY(mtemp, x/100);
    		mat4.rotateX(mtemp, y/100);
    		mat4.multiply(mtemp, matrix3d, matrix3d);
    	}
    	renderer.set3dViewZoom = function (z) {
    		zoom3d = z;
    	}
	renderer.setColors = function () {
		colors = [];
		chargeColors = [];
		for(var i = 0; i < arguments.length; i++) {
			var arg = arguments[i];
                        if (i < 4)
			    colors.push(((arg>>16)&0xff)/255, ((arg>>8)&0xff)/255, (arg&0xff)/255);
                        if (i < 2 || i > 3)
			    chargeColors.push(((arg>>16)&0xff)/255, ((arg>>8)&0xff)/255, (arg&0xff)/255);
		}
	}
	renderer.drawingSelection = -1;
    mat4.identity(pMatrix);
    mat4.identity(mvMatrix);
    return renderer;
}


