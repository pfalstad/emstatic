
var gl;
var canvas;
var gridSizeX =1024, gridSizeY =1024, windowOffsetX =40, windowOffsetY =40;
var windowWidth, windowHeight, viewAngle, viewHeight;
var sim;
var transform = [1, 0, 0, 1, 0, 0];
var renderTextures = [];
var minFeatureWidth;
var brightness;

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


    var shaderProgramMain, shaderProgramFixed, shaderProgramAcoustic, shaderProgramDraw, shaderProgramMode;
    var shaderProgramCopy, shaderProgramResidual, shaderProgramEdgeCharge;

    function initShader(fs, vs, prefix) {
        var fragmentShader = getShader(gl, fs, prefix);
//        var vs = (fs == "shader-draw-fs" || fs == "shader-mode-fs") ? "shader-draw-vs" : "shader-vs";
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
        shaderProgram.dampingAttribute = gl.getAttribLocation(shaderProgram, "aDamping");
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

    	shaderProgram3D = initShader("shader-3d-fs", "shader-3d-vs", null);
    	shaderProgram3D.brightnessUniform = gl.getUniformLocation(shaderProgram3D, "brightness");
    	shaderProgram3D.equipMultUniform = gl.getUniformLocation(shaderProgram3D, "equipMult");
    	shaderProgram3D.colorsUniform = gl.getUniformLocation(shaderProgram3D, "colors");
    	shaderProgram3D.xOffsetUniform = gl.getUniformLocation(shaderProgram3D, "xOffset");
    	shaderProgram3D.normalMatrixUniform = gl.getUniformLocation(shaderProgram3D, "uNormalMatrix");
    	shaderProgram3D.stepSizeXUniform = gl.getUniformLocation(shaderProgram3D, "stepSizeX");
    	shaderProgram3D.stepSizeYUniform = gl.getUniformLocation(shaderProgram3D, "stepSizeY");
        shaderProgram3D.rightSideTextureUniform = gl.getUniformLocation(shaderProgram3D, "uRightSideTexture");

    	shaderProgramFixed = initShader("shader-simulate-fs", "shader-vs", null);
    	shaderProgramFixed.stepSizeXUniform = gl.getUniformLocation(shaderProgramFixed, "stepSizeX");
    	shaderProgramFixed.stepSizeYUniform = gl.getUniformLocation(shaderProgramFixed, "stepSizeY");

    	shaderProgramResidual = initShader("shader-residual-fs", "shader-vs", null);
    	shaderProgramResidual.stepSizeXUniform = gl.getUniformLocation(shaderProgramResidual, "stepSizeX");
    	shaderProgramResidual.stepSizeYUniform = gl.getUniformLocation(shaderProgramResidual, "stepSizeY");

    	shaderProgramAdd = initShader("shader-add-fs", "shader-vs", null);
    	shaderProgramCopy = initShader("shader-copy-fs", "shader-vs", null);

    	shaderProgramAcoustic = initShader("shader-simulate-fs", "shader-vs", "#define ACOUSTIC 1\n");
    	shaderProgramAcoustic.stepSizeXUniform = gl.getUniformLocation(shaderProgramAcoustic, "stepSizeX");
    	shaderProgramAcoustic.stepSizeYUniform = gl.getUniformLocation(shaderProgramAcoustic, "stepSizeY");

    	shaderProgramDraw = initShader("shader-draw-fs", "shader-draw-vs");
    	shaderProgramMode = initShader("shader-mode-fs", "shader-draw-vs");

    	shaderProgramEdgeCharge = initShader("shader-edge-charge-fs", "shader-edge-charge-vs");
    	shaderProgramEdgeCharge.brightnessUniform = gl.getUniformLocation(shaderProgramEdgeCharge, "brightness");
    	shaderProgramEdgeCharge.textureMatrixUniform = gl.getUniformLocation(shaderProgramEdgeCharge, "uTextureMatrix");

    	shaderProgramEquip = initShader("shader-equipotential-fs", "shader-vs", null);
    	shaderProgramEquip.stepSizeXUniform = gl.getUniformLocation(shaderProgramEquip, "stepSizeX");
    	shaderProgramEquip.stepSizeYUniform = gl.getUniformLocation(shaderProgramEquip, "stepSizeY");
    	shaderProgramEquip.brightnessUniform = gl.getUniformLocation(shaderProgramEquip, "brightness");
        shaderProgramEquip.rightSideTextureUniform = gl.getUniformLocation(shaderProgramEquip, "uRightSideTexture");

    	shaderProgramField = initShader("shader-field-fs", "shader-field-vs", null);
    	shaderProgramField.stepSizeXUniform = gl.getUniformLocation(shaderProgramField, "stepSizeX");
    	shaderProgramField.stepSizeYUniform = gl.getUniformLocation(shaderProgramField, "stepSizeY");
    	shaderProgramField.brightnessUniform = gl.getUniformLocation(shaderProgramField, "brightness");
    	shaderProgramField.arrowTextureUniform = gl.getUniformLocation(shaderProgramField, "uArrowTexture");
    	shaderProgramField.textureMatrixUniform = gl.getUniformLocation(shaderProgramField, "uTextureMatrix");
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



    var renderTexture1, renderTexture2;
    var fbType;

    function initTextureFramebuffer(sz) {
    	var rttFramebuffer = gl.createFramebuffer();
    	gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    	rttFramebuffer.width = sz;
    	rttFramebuffer.height = sz;
    	console.log("makgin framebuffer of size " + sz);

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
          return null;
        }

        var pixels = new Float32Array(4);
        gl.readPixels(0, 0, 1, 1, gl.RGBA, gl.FLOAT, pixels);
        if (gl.getError() != gl.NO_ERROR)
            console.log("readPixels failed");
        else
            sim.readPixelsWorks = true;

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

    var laptopScreenVertexPositionBuffer;
    var laptopScreenVertexTextureCoordBuffer;
    var screen3DTextureBuffer;
    var simVertexPositionBuffer;
    var simVertexTextureCoordBuffer;
    var simVertexBuffer;
    var simVertexDampingBuffer;
    
    var simPosition = [];
    var simTextureCoord = [];
    var simDamping = [];
    var srcCoords = [
                     -.26, 0, -.25, 0
                     ];
    var gridSize3D = 128;
    var gridRange;

    function initBuffers() {
    	if (!laptopScreenVertexPositionBuffer)
    		laptopScreenVertexPositionBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexPositionBuffer);
    	vertices = [
    	            -1, +1,
    	            +1, +1,
    	            -1, -1,
    	            +1, -1,
    	            ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
    	laptopScreenVertexPositionBuffer.itemSize = 2;
    	laptopScreenVertexPositionBuffer.numItems = 4;

    	if (!laptopScreenVertexTextureCoordBuffer)
    		laptopScreenVertexTextureCoordBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
    	var textureCoords = [
    	                     windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY
    	                     ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords), gl.STATIC_DRAW);
    	laptopScreenVertexTextureCoordBuffer.itemSize = 2;
    	laptopScreenVertexTextureCoordBuffer.numItems = 4;

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
    	simDamping = [];
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

    	if (!simVertexDampingBuffer)
    		simVertexDampingBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexDampingBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simDamping), gl.STATIC_DRAW);
    	simVertexDampingBuffer.itemSize = 1;
    	simVertexDampingBuffer.numItems = simDamping.length;
    }

    // create coordinates for a rectangular portion of the grid, making sure to set the damping attribute
    // appropriately (1 for visible area, slightly less for offscreen area used to avoid reflections at edges)
    function setPosRect(x1, y1, x2, y2, gx) {
    	var points = [ x2, y1, x1, y1, x2, y2, x1, y1, x2, y2, x1, y2 ];
    	var i;
    	for (i = 0; i != 6; i++) {
    		var xi = points[i*2];
    		var yi = points[i*2+1];
    		simPosition.push(-1+2*xi/gx, -1+2*yi/gx);
    		simTextureCoord.push(xi/gx, yi/gx);
    		var damp = 1;
    		if (xi == 1 || yi == 1 || xi == gx-2 || yi == gx-2)
    			damp = .999-8*.01; // was 20
    			simDamping.push(damp);
    	}
    }

    var sourceBuffer;
    var colorBuffer;
    var colors;
    var destHeight;

    function setDestination(rtnum) {
    	var rt = renderTextures[rtnum];
    	var rttFramebuffer = rt.framebuffer;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
        destHeight = rttFramebuffer.height;

	// minimum width/height of anything drawn, should always be at least one pixel width
        minFeatureWidth = (windowWidth / rttFramebuffer.width) * 1.5;
    }
    
    function simulate(srcnum, rsnum, resid) {
    	var sourceRT = renderTextures[srcnum];
    	var rightSideRT = renderTextures[rsnum];
        var prog = resid ? shaderProgramResidual : shaderProgramFixed;
        gl.useProgram(prog);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simDamping = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.dampingAttribute);
        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexDampingBuffer);
        gl.vertexAttribPointer(prog.dampingAttribute, simVertexDampingBuffer.itemSize, gl.FLOAT, false, 0, 0);

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
        gl.disableVertexAttribArray(prog.dampingAttribute);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    function copy(srcnum) {
    	var sourceRT = renderTextures[srcnum];
        var prog = shaderProgramCopy;
        gl.useProgram(prog);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

    	simPosition = [];
    	simDamping = [];
    	simTextureCoord = [];

    	setPosRect(1, 1, destHeight-1, destHeight-1, destHeight);
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.dampingAttribute);
        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexDampingBuffer);
        gl.vertexAttribPointer(prog.dampingAttribute, simVertexDampingBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, sourceRT.texture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.uniform1i(prog.sourceTextureUniform, 0);
 
        setMatrixUniforms(prog);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.dampingAttribute);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    function add(srcnum, rsnum) {
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

        gl.enableVertexAttribArray(prog.dampingAttribute);
        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexDampingBuffer);
        gl.vertexAttribPointer(prog.dampingAttribute, simVertexDampingBuffer.itemSize, gl.FLOAT, false, 0, 0);

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
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.dampingAttribute);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    function drawSource(x, y, f) {
        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, f, 0.0, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = srcCoords[2] = x;
        srcCoords[1] = y;
        var off = gridSizeY / destHeight;
        srcCoords[3] = srcCoords[1]+off;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

//        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
//        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
		gl.colorMask(true, false, false, false);
        console.log("pre " + gl.getError());
        gl.drawArrays(gl.LINES, 0, 2);
		gl.colorMask(true, true, true, true);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        //mvPopMatrix();
    }

    function drawHandle(x, y) {
        gl.useProgram(shaderProgramDraw);
        if (sim.drawingSelection >= 0) {
        	drawSelectedHandle(x, y);
        	return;
        }
        if (sim.drawingSelection < 0)
        	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1, 1.0, 1.0, 1.0);
        else 
        	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, sim.drawingSelection,
        			sim.drawingSelection, 0, 1.0);

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
//        gl.lineWidth(sim.drawingSelection < 0 ? 1 : 2);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.LINE_LOOP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    function drawSelectedHandle(x, y) {
       	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, sim.drawingSelection, sim.drawingSelection, 0, 0.5);

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
//        gl.lineWidth(sim.drawingSelection < 0 ? 1 : 2);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    function drawFocus(x, y) {
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

    function drawLineSource(x, y, x2, y2, f) {
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

    function drawPhasedArray(x, y, x2, y2, f1, f2) {
        var rttFramebuffer = renderTexture1.framebuffer;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
        gl.colorMask(true, true, false, false);
        gl.useProgram(shaderProgramMode);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = x;
        srcCoords[1] = y;
        srcCoords[2] = x2;
        srcCoords[3] = y2;
        var colors = [f1, Math.PI/2, 0, 0, f2, Math.PI/2, 0, 0];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramMode);
        gl.enableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramMode.colorAttribute);
        gl.drawArrays(gl.LINES, 0, 2);
        gl.disableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramMode.colorAttribute);

        gl.colorMask(true, true, true, true);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

        //mvPopMatrix();
    }

    function loadMatrix(mtx) {
    	mat4.identity(mtx);
    	if (sim.drawingSelection > 0) {
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
        if (sim.drawingSelection > 0) {
    		gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, sim.drawingSelection,
    				sim.drawingSelection, 0, 1.0);
        } else {
    		//var rttFramebuffer = renderTexture1.framebuffer;
    		//gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		//gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
            
            // blue channel used for walls and media
    		gl.colorMask(true, false, true, false);
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
        srcCoords = thickLinePoints([x, y, x2, y2, x, y], sim.drawingSelection == 1 ? .5 : sim.drawingSelection > 0 ? 1.5 : Math.max(minFeatureWidth, 1.5));
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(3);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.drawArrays(gl.LINE_STRIP, 0, 3);

	if (sim.drawingSelection < 0)
          gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, pot, 0.0, 0.0, 1.0);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 6);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.lineWidth(1);

		gl.colorMask(true, true, true, true);
		//gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawPoke(x, y) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(true, true, false, false);

        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1.0, 0.0, 0.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var verts = [x, y];
        var colors = [1,0,0,1];
        var steps = 8;
        var i;
        var r = 6;
        for (i = 0; i != steps+1; i++) {
        	var ang = Math.PI*2*i/steps;
        	verts.push(x+r*Math.cos(ang), y+r*Math.sin(ang));
        	colors.push(0,0,0,0);
        }
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(verts), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        
        gl.enable(gl.BLEND);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE);
        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.colorAttribute, 4, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramDraw.colorAttribute);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_FAN, 0, 2+steps);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramDraw.colorAttribute);

		gl.colorMask(true, true, true, true);
		gl.disable(gl.BLEND);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawEllipse(cx, cy, xr, yr) {
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
        coords = thickLinePoints(coords, sim.drawingSelection == 1 ? .5 : 1.5);
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

    function drawParabola(x1, y1, w, h) {
    	setupForDrawing(0);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var coords = [];
        var i;
        var w2 = w/2;
        var a = h/(w2*w2);
        for (i = 0; i <= w; i++) {
        	var x0 = i-w2;
        	coords.push(x1+i, y1+h-a*x0*x0);
        }
        coords = thickLinePoints(coords, 1.5);
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

    function drawLens(x1, y1, w, h, m) {
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
    
    function drawSolidEllipse(cx, cy, xr, yr, med, pot) {
	if (med == undefined) {
	    gl.colorMask(true, false, false, false);
	    med = 0;
	} else
	    gl.colorMask(med == 0, false, true, false);
        gl.useProgram(shaderProgramDraw);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var coords = [cx, cy];
        var i;
	xr = Math.max(xr, minFeatureWidth);
	yr = Math.max(yr, minFeatureWidth);
        for (i = -xr; i <= xr; i++) {
        	coords.push(cx-i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        for (i = xr-1; i >= -xr; i--) {
        	coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, pot, 0.0, med, 1.0);
        gl.drawArrays(gl.TRIANGLE_FAN, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

		gl.colorMask(true, true, true, true);
    }

    function displayEllipseCharge(cx, cy, xr, yr) {
        var coords = [], tcoords = [];
        var i;
        var insetMultX = (xr-2)/xr;
        var insetMultY = (yr-2)/yr;
        var outMultX = (xr+2)/xr;
        var outMultY = (yr+2)/yr;
        for (i = -xr; i <= xr; i++) {
        	coords.push(cx-i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
        	coords.push(cx-i*insetMultX, cy-yr*insetMultY*Math.sqrt(1-i*i/(xr*xr)));
        	tcoords.push(cx-i*outMultX, cy-yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
        	tcoords.push(cx-i*outMultX, cy-yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
        }
        for (i = xr-1; i >= -xr; i--) {
        	coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
        	coords.push(cx-i*insetMultX, cy+yr*insetMultY*Math.sqrt(1-i*i/(xr*xr)));
        	tcoords.push(cx-i*outMultX, cy+yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
        	tcoords.push(cx-i*outMultX, cy+yr*outMultY*Math.sqrt(1-i*i/(xr*xr)));
        }

        gl.useProgram(shaderProgramEdgeCharge);
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramEdgeCharge);
	
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramEdgeCharge.vertexPositionAttribute, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramEdgeCharge.vertexPositionAttribute);

	gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(tcoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramEdgeCharge.textureCoordAttribute, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramEdgeCharge.textureCoordAttribute);

        var matx = [1/gridSizeX,0,0, 0,-1/gridSizeY,0, windowOffsetX/gridSizeX,1-windowOffsetY/gridSizeY,1];
	gl.uniformMatrix3fv(shaderProgramEdgeCharge.textureMatrixUniform, false, matx);

        gl.uniform1f(shaderProgramEdgeCharge.brightnessUniform, brightness);

        gl.drawArrays(gl.TRIANGLE_STRIP, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramEdgeCharge.textureCoordAttribute);
        gl.disableVertexAttribArray(shaderProgramEdgeCharge.vertexPositionAttribute);
    }

    function drawMedium(x, y, x2, y2, x3, y3, x4, y4, m1, pot) {
	gl.colorMask(m1 == 0, false, true, false);
        gl.useProgram(shaderProgramDraw);

        if (x2-x < minFeatureWidth)
		x2 = x4 = x+minFeatureWidth;
	if (y3-y < minFeatureWidth)
		y3 = y4 = y+minFeatureWidth;
        var medCoords = [x, y, x2, y2, x3, y3, x4, y4];
        //var colors = [ pot,0,m1,1, pot,0,m1,1, pot,0,m1,1, pot,0,m1,1 ];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        //gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        //gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        //gl.vertexAttribPointer(shaderProgramDraw.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        //gl.enableVertexAttribArray(shaderProgramDraw.colorAttribute);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, pot, 0.0, m1, 1.0);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        //gl.disableVertexAttribArray(shaderProgramDraw.colorAttribute);

		gl.colorMask(true, true, true, true);
    }

    function displayBoxCharge(x, y, x2, y2, x3, y3, x4, y4) {
        // double some of the coordinates to get thickLinePoints to work right on right angles
        var thick = 2;
        var coords = [x, y, x+thick, y+thick, x2, y2,
                      x+thick, y+thick, x2, y2, x2-thick, y2+thick,
                      x2, y2, x2-thick, y2+thick, x4, y4,
                      x2-thick, y2+thick, x4, y4, x4-thick, y4-thick,
                      x4, y4, x4-thick, y4-thick, x3, y3,
                      x4-thick, y4-thick, x3, y3, x3+thick, y3-thick,
                      x3, y3, x3+thick, y3-thick, x, y,
                      x3+thick, y3-thick, x, y, x+thick, y+thick];
        var tcoords = [x, y-2, x+thick, y-2, x2, y2-2,
                       x+thick, y-2, x2, y2-2, x2-thick, y2-2,
                       x2+2, y2, x2+2, y2+thick, x4+2, y4,
                       x2+2, y2+thick, x4+2, y4, x4+2, y4-thick,
                       x4, y4+2, x4-thick, y4+2, x3, y3+2,
                       x4-thick, y4+2, x3, y3+2, x3+thick, y3+2,
                       x3-2, y3, x3-2, y3-thick, x-2, y,
                       x3-2, y3-thick, x-2, y, x-2, y+thick];

        gl.useProgram(shaderProgramEdgeCharge);
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramEdgeCharge);
	
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramEdgeCharge.vertexPositionAttribute, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramEdgeCharge.vertexPositionAttribute);

	gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(tcoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramEdgeCharge.textureCoordAttribute, 2, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramEdgeCharge.textureCoordAttribute);

        var matx = [1/gridSizeX,0,0, 0,-1/gridSizeY,0, windowOffsetX/gridSizeX,1-windowOffsetY/gridSizeY,1];
	gl.uniformMatrix3fv(shaderProgramEdgeCharge.textureMatrixUniform, false, matx);

        gl.uniform1f(shaderProgramEdgeCharge.brightnessUniform, brightness);

        gl.drawArrays(gl.TRIANGLES, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramEdgeCharge.textureCoordAttribute);
        gl.disableVertexAttribArray(shaderProgramEdgeCharge.vertexPositionAttribute);
    }

    function drawChargedBox(x, y, x2, y2, x3, y3, x4, y4, chg) {
	gl.colorMask(true, false, false, false);
        gl.useProgram(shaderProgramDraw);

        if (x2-x < minFeatureWidth)
		x2 = x4 = x+minFeatureWidth;
	if (y3-y < minFeatureWidth)
		y3 = y4 = y+minFeatureWidth;
        var medCoords = [x, y, x2, y2, x3, y3, x4, y4];
        //var colors = [ pot,0,m1,1, pot,0,m1,1, pot,0,m1,1, pot,0,m1,1 ];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        //gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        //gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        //gl.vertexAttribPointer(shaderProgramDraw.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        //gl.enableVertexAttribArray(shaderProgramDraw.colorAttribute);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, chg, 0.0, 0.0, 1.0);
        //gl.enable(gl.BLEND);
	//gl.blendFunc(gl.ONE, gl.ONE);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        //gl.disable(gl.BLEND);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        //gl.disableVertexAttribArray(shaderProgramDraw.colorAttribute);

		gl.colorMask(true, true, true, true);
    }

    function drawModes(x, y, x2, y2, a, b, c, d) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(true, true, false, false);
//		gl.clear(gl.COLOR_BUFFER_BIT);
        gl.useProgram(shaderProgramMode);
        var z = 0;
        var z2 = 0;
        if (sim.acoustic) {
        	z = Math.PI/2;
        	a += z;
        	b += z;
        	if (c || d) {
        		z2 = z;
        		c += z;
        		d += z;
        	}
    	}

        var medCoords = [x, y, x, y2, x2, y, x2, y2];
        var colors = [ z,z,z2,z2, z,b,z2,d, a,z,c,z2, a,b,c,d ];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramMode);
        gl.enableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramMode.colorAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramMode.colorAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    
    function drawTriangle(x, y, x2, y2, x3, y3, m) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(false, false, true, false);
//		gl.clear(gl.COLOR_BUFFER_BIT);

        gl.useProgram(shaderProgramDraw);
//        console("draw triangle " + m);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0.0, 0.0, m, 1.0);

        var medCoords = [x, y, x2, y2, x3, y3];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 3);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function getProbeValue(x, y) {
        var rttFramebuffer = renderTexture1.framebuffer;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
        var pixels = new Float32Array(4*9);
        gl.readPixels(windowOffsetX+x-1, gridSizeY-windowOffsetY-y-2, 3, 3, gl.RGBA, gl.FLOAT, pixels);
        return [pixels[4*4], pixels[1*4], pixels[7*4], pixels[3*4], pixels[5*4]];
    }

    function drawScenePotential(s, rs, bright, equipMult) {
        gl.useProgram(shaderProgramMain);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        // draw result
        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgramMain.vertexPositionAttribute, laptopScreenVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramMain.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

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
        gl.uniform3fv(shaderProgramMain.colorsUniform, colors);

        setMatrixUniforms(shaderProgramMain);
        gl.enableVertexAttribArray(shaderProgramMain.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramMain.textureCoordAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, laptopScreenVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(shaderProgramMain.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramMain.textureCoordAttribute);

        mvPopMatrix();
	drawSceneEquip(s, rs, equipMult);
    }

    function drawSceneEquip(s, rs, bright) {
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
        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgramEquip.vertexPositionAttribute, laptopScreenVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramEquip.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

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
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, laptopScreenVertexPositionBuffer.numItems);
        gl.disable(gl.BLEND);
        gl.disableVertexAttribArray(shaderProgramEquip.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramEquip.textureCoordAttribute);

        mvPopMatrix();
    }

    function drawSceneField(s, rs, bright, equipMult) {
        gl.useProgram(shaderProgramField);
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
        //gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexPositionBuffer);
        //gl.vertexAttribPointer(shaderProgramEquip.vertexPositionAttribute, laptopScreenVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        //gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        //gl.vertexAttribPointer(shaderProgramField.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

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
	var count = 60; // was 80
        for (i = 0; i != count; i++)
          for (j = 0; j != count; j++) {
            coords.push(-1+(i+.5)/(count/2), -1+(j+.5)/(count/2));
          }
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.DYNAMIC_DRAW);
        gl.vertexAttribPointer(shaderProgramField.vertexPositionAttribute, 2, gl.FLOAT, false, 0, 0);

        gl.uniform1i(shaderProgramField.samplerUniform, 0);
        gl.uniform1i(shaderProgramField.arrowTextureUniform, 1);
        gl.uniform1i(shaderProgramField.rightSideTextureUniform, 2);
        gl.uniform1f(shaderProgramField.brightnessUniform, bright);
        gl.uniform1f(shaderProgramField.stepSizeXUniform, .5/gl.viewportWidth);
        gl.uniform1f(shaderProgramField.stepSizeYUniform, .5/gl.viewportHeight);
        var matx = [.5*(gridSizeX-windowOffsetX*2)/gridSizeX, 0, 0, 0, .5*(gridSizeY-windowOffsetY*2)/gridSizeY, 0, 0, 0, 1];
        matx[6] = windowOffsetX/gridSizeX + matx[0];
        matx[7] = windowOffsetY/gridSizeY + matx[4];
	gl.uniformMatrix3fv(shaderProgramField.textureMatrixUniform, false, matx);
        //gl.uniform3fv(shaderProgramEquip.colorsUniform, colors);

        //setMatrixUniforms(shaderProgramEquip);
        gl.enableVertexAttribArray(shaderProgramEquip.vertexPositionAttribute);
        gl.drawArrays(gl.POINTS, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramEquip.vertexPositionAttribute);
        //gl.disableVertexAttribArray(shaderProgramEquip.textureCoordAttribute);

        //mvPopMatrix();

	drawSceneEquip(s, rs, equipMult);
    }

    function display(s, rs, bright, equipMult, type) {
      brightness = bright;
      if (type == 2)
        drawScene3D(s, rs, bright, equipMult);
      else if (type == 1)
        drawScenePotential(s, rs, bright, equipMult);
      else
        drawSceneField(s, rs, bright, equipMult);
    }

    function drawScene3D(s, rs, bright, equipMult) {
        gl.useProgram(shaderProgram3D);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

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

    document.passCanvas = function passCanvas (cv, sim_) {
    	canvas = cv;
    	sim = sim_;
    	gl = cv.getContext("experimental-webgl");
        arrowTexture = loadTexture(gl, 'arrow.png');

    	console.log("got gl context " + gl + " " + cv.width + " " + cv.height);
    	var float_texture_ext = gl.getExtension('OES_texture_float');
    	var float_texture_linear_ext = gl.getExtension('OES_texture_float_linear');
    	var half_float_texture_ext = gl.getExtension('OES_texture_half_float');
	gl.getExtension('EXT_float_blend');

//    	gridSizeX = gridSizeY = 1024;
//    	windowOffsetX = windowOffsetY = 40;
    	fbType = 0;
    	renderTexture2 = initTextureFramebuffer(64);
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
//    	renderTexture1 = initTextureFramebuffer();
    	initShaders();
//    	initBuffers();
    	initTextures();
        mat4.identity(matrix3d);
	mat4.rotateX(matrix3d, -Math.PI/3);
    	//loadLaptop();

//    	drawWalls(renderTexture1);

    	gl.clearColor(0.0, 0.0, 1.0, 1.0);

    	sim.acoustic = false;
    	sim.readPixelsWorks = false;
    	sim.display = display;
    	sim.runRelax = simulate;
    	sim.add = function (s, b) { add(s, b); }
    	sim.copy = function (s) { copy(s); }
    	sim.setResolution = function (x, y, wx, wy) {
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
    		while (1) {
    			console.log("creating buffers size " + sz + " at " + renderTextures.length);
    			renderTextures.push(initTextureFramebuffer(sz));
    			renderTextures.push(initTextureFramebuffer(sz));
    			renderTextures.push(initTextureFramebuffer(sz));
    			if (sz >= gridSizeX)
    				break;
    			sz *= 2;
    		}
    		renderTexture1 = renderTextures[renderTextures.length-2];
    		renderTexture2 = renderTextures[renderTextures.length-1];
    		initBuffers();
    	}
    	sim.getRenderTextureCount = function () { return renderTextures.length; }
    	sim.drawSource = function (x, y, f) { drawSource(x, y, f); }
    	sim.drawLineSource = function (x, y, x2, y2, f) { drawLineSource(x, y, x2, y2, f); }
    	sim.drawPhasedArray = function (x, y, x2, y2, f1, f2) { drawPhasedArray(x, y, x2, y2, f1, f2); }
    	sim.drawHandle = function (x, y) { drawHandle(x, y); }
    	sim.drawFocus = function (x, y) { drawFocus(x, y); }
    	sim.drawPoke = function (x, y) { drawPoke(x, y); }
    	sim.drawWall = function (x, y, x2, y2, pot) { drawWall(x, y, x2, y2, pot); }
    	sim.clearWall = function (x, y, x2, y2) { drawWall(x, y, x2, y2, 1); }
    	sim.drawParabola = function (x, y, w, h) { drawParabola(x, y, w, h); }
    	sim.drawLens = function (x, y, w, h, m) { drawLens(x, y, w, h, m); }
    	sim.drawEllipse = function (x, y, x2, y2, m) { drawEllipse(x, y, x2, y2); }
    	sim.drawSolidEllipse = function (x, y, x2, y2, m, p) { drawSolidEllipse(x, y, x2, y2, m, p); }
    	sim.drawMedium = function (x, y, x2, y2, x3, y3, x4, y4, m, m2) { drawMedium(x, y, x2, y2, x3, y3, x4, y4, m, m2); }
    	sim.drawChargedBox = function (x, y, x2, y2, x3, y3, x4, y4, chg) { drawChargedBox(x, y, x2, y2, x3, y3, x4, y4, chg); }
    	sim.displayBoxCharge = displayBoxCharge;
        sim.displayEllipseCharge = displayEllipseCharge;
    	sim.drawTriangle = function (x, y, x2, y2, x3, y3, m) { drawTriangle(x, y, x2, y2, x3, y3, m); }
    	sim.drawModes = function (x, y, x2, y2, a, b, c, d) { drawModes(x, y, x2, y2, a, b, c, d); }
    	sim.setTransform = function (a, b, c, d, e, f) {
    		transform[0] = a; transform[1] = b;
    		transform[2] = c; transform[3] = d;
    		transform[4] = e; transform[5] = f;
    	}
	sim.getProbeValue = function (x, y) { return getProbeValue(x, y); }
    	sim.setDestination = function (a) { setDestination(a); }
    	sim.clearDestination = function () {
        	gl.clearColor(0.0, 0.0, 1.0, 1.0);
    		gl.clear(gl.COLOR_BUFFER_BIT);
    	}
    	sim.doBlank = function () {
    		var rttFramebuffer = renderTexture1.framebuffer;
    		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
    		gl.colorMask(true, true, false, false);	
        	gl.clearColor(0.0, 0.0, 1.0, 1.0);
    		gl.clear(gl.COLOR_BUFFER_BIT);
    		gl.colorMask(true, true, true, true);
    		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    	}
    	sim.doBlankWalls = function () {
    		var rttFramebuffer = renderTexture1.framebuffer;
    		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
    		gl.colorMask(false, false, true, false);
        	gl.clearColor(0.0, 0.0, 1.0, 1.0);
    		gl.clear(gl.COLOR_BUFFER_BIT);
    		gl.colorMask(true, true, true, true);
    		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
	}
    	sim.set3dViewAngle = function (x, y) {
    	    var mtemp = mat4.create();
    	    mat4.identity(mtemp);
    		mat4.rotateY(mtemp, x/100);
    		mat4.rotateX(mtemp, y/100);
    		mat4.multiply(mtemp, matrix3d, matrix3d);
    	}
    	sim.set3dViewZoom = function (z) {
    		zoom3d = z;
    	}
	sim.setColors = function () {
		colors = [];
		for(var i = 0; i < arguments.length; i++) {
			var arg = arguments[i];
			colors.push(((arg>>16)&0xff)/255, ((arg>>8)&0xff)/255, (arg&0xff)/255);
		}
	}
	sim.drawingSelection = -1;
    mat4.identity(pMatrix);
    mat4.identity(mvMatrix);

}


