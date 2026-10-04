/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

/**
 * @param {*} msg
 */
function logInfo(msg) {
	console.log("LoaderBootstrap: [INFO] " + msg);
}

/**
 * @param {*} msg
 */
function logWarn(msg) {
	console.log("LoaderBootstrap: [WARN] " + msg);
}

/**
 * @param {*} msg
 */
function logError(msg) {
	console.error("LoaderBootstrap: [ERROR] " + msg);
}

/** @type {function(string,number):ArrayBuffer|null} */
var decodeBase64Impl = null;

/**
 * @return {function(string,number):ArrayBuffer}
 */
function createBase64Decoder() {
	const revLookup = [];
	const code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	for (var i = 0, len = code.length; i < len; ++i) {
		revLookup[code.charCodeAt(i)] = i;
	}

	revLookup["-".charCodeAt(0)] = 62;
	revLookup["_".charCodeAt(0)] = 63;

	/**
	 * @param {string} b64
	 * @param {number} start
	 * @return {!Array<number>}
	 */
	function getLens(b64, start) {
		const len = b64.length - start;
		if (len % 4 > 0) {
			throw new Error("Invalid string. Length must be a multiple of 4");
		}
		var validLen = b64.indexOf("=", start);
		if (validLen === -1) {
			validLen = len;
		}else {
			validLen -= start;
		}
		const placeHoldersLen = validLen === len ? 0 : 4 - (validLen % 4);
		return [validLen, placeHoldersLen];
	}
	
	/**
	 * @param {string} b64
	 * @param {number} start
	 * @return {ArrayBuffer}
	 */
	function decodeImpl(b64, start) {
		var tmp;
		const lens = getLens(b64, start);
		const validLen = lens[0];
		const placeHoldersLen = lens[1];
		const arr = new Uint8Array(((validLen + placeHoldersLen) * 3 / 4) - placeHoldersLen);
		var curByte = 0;
		const len = (placeHoldersLen > 0 ? validLen - 4 : validLen) + start;
		var i;
		for (i = start; i < len; i += 4) {
			tmp = (revLookup[b64.charCodeAt(i)] << 18) |
				(revLookup[b64.charCodeAt(i + 1)] << 12) |
				(revLookup[b64.charCodeAt(i + 2)] << 6) |
				revLookup[b64.charCodeAt(i + 3)]
			arr[curByte++] = (tmp >> 16) & 0xFF
			arr[curByte++] = (tmp >> 8) & 0xFF
			arr[curByte++] = tmp & 0xFF
		}
		if (placeHoldersLen === 2) {
			tmp = (revLookup[b64.charCodeAt(i)] << 2) |
				(revLookup[b64.charCodeAt(i + 1)] >> 4)
			arr[curByte++] = tmp & 0xFF
		}else if (placeHoldersLen === 1) {
			tmp = (revLookup[b64.charCodeAt(i)] << 10) |
				(revLookup[b64.charCodeAt(i + 1)] << 4) |
				(revLookup[b64.charCodeAt(i + 2)] >> 2)
			arr[curByte++] = (tmp >> 8) & 0xFF
			arr[curByte++] = tmp & 0xFF
		}
		return arr.buffer;
	}
	
	return decodeImpl;
}

/**
 * @param {string} url
 * @param {number} start
 * @return {ArrayBuffer}
 */
function decodeBase64(url, start) {
	if(!decodeBase64Impl) {
		decodeBase64Impl = createBase64Decoder();
	}
	return decodeBase64Impl(url, start);
}

/**
 * @param {number} ms
 * @return {!Promise}
 */
function asyncSleep(ms) {
	return new Promise(function(resolve) {
		setTimeout(resolve, ms);
	});
}

/**
 * @param {string} url
 * @param {number} ms
 * @return {!Promise}
 */
function preloadImage(url, ms) {
	return new Promise(function(resolve) {
		const imgObj = new Image();
		imgObj.addEventListener("load", resolve);
		imgObj.addEventListener("error", function() {
			logWarn("Failed to preload image: " + url);
			resolve();
		});
		imgObj.src = url;
		setTimeout(resolve, ms);
	});
}

/**
 * @param {string} url
 * @return {!Promise<ArrayBuffer>}
 */
function downloadURL(url) {
	return new Promise(function(resolve) {
		fetch(url, { "cache": "force-cache" })
			.then(function(res) {
				return res.arrayBuffer();
			})
			.then(resolve)
			.catch(function(ex) {
				logError("Failed to fetch URL! " + ex);
				resolve(null);
			});
	});
}

/**
 * @param {string} url
 * @return {!Promise<ArrayBuffer>}
 */
function downloadDataURL(url) {
	if(!url.startsWith("data:application/octet-stream;base64,")) {
		return downloadURL(url);
	}else {
		return new Promise(function(resolve) {
			downloadURL(url).then(function(res) {
				if(res) {
					resolve(res);
				}else {
					logWarn("Failed to decode base64 via fetch, doing it the slow way instead...");
					try {
						resolve(decodeBase64(url, 37));
					}catch(ex) {
						logError("Failed to decode base64! " + ex);
						resolve(null);
					}
				}
			});
		});
	}
}

/**
 * @param {HTMLElement} rootElement
 * @param {string} msg
 */
function displayInvalidEPW(rootElement, msg) {
	const downloadFailureMsg = /** @type {HTMLElement} */ (document.createElement("h2"));
	downloadFailureMsg.style.color = "#AA0000";
	downloadFailureMsg.style.padding = "25px";
	downloadFailureMsg.style.fontFamily = "sans-serif";
	downloadFailureMsg.style["marginBlock"] = "0px";
	downloadFailureMsg.appendChild(document.createTextNode(msg));
	rootElement.appendChild(downloadFailureMsg);
	const downloadFailureMsg2 = /** @type {HTMLElement} */ (document.createElement("h4"));
	downloadFailureMsg2.style.color = "#AA0000";
	downloadFailureMsg2.style.padding = "25px";
	downloadFailureMsg2.style.fontFamily = "sans-serif";
	downloadFailureMsg2.style["marginBlock"] = "0px";
	downloadFailureMsg2.appendChild(document.createTextNode("Try again later"));
	rootElement.style.backgroundColor = "white";
	rootElement.appendChild(downloadFailureMsg2);
}

window.main = async function() {
	if(typeof window.eaglercraftXOpts === "undefined") {
		const msg = "window.eaglercraftXOpts is not defined!";
		logError(msg);
		alert(msg);
		return;
	}
	
	const containerId = window.eaglercraftXOpts.container;
	if(typeof containerId !== "string") {
		const msg = "window.eaglercraftXOpts.container is not a string!";
		logError(msg);
		alert(msg);
		return;
	}
	
	var assetsURI = window.eaglercraftXOpts.assetsURI;
	if(typeof assetsURI !== "string") {
		if((typeof assetsURI === "object") && (typeof assetsURI[0] === "object") && (typeof assetsURI[0]["url"] === "string")) {
			assetsURI = assetsURI[0]["url"];
		}else {
			const msg = "window.eaglercraftXOpts.assetsURI is not a string!";
			logError(msg);
			alert(msg);
			return;
		}
	}
	
	if(assetsURI.startsWith("data:")) {
		delete window.eaglercraftXOpts.assetsURI;
	}
	
	const rootElement = /** @type {HTMLElement} */ (document.getElementById(containerId));
	
	if(!rootElement) {
		const msg = "window.eaglercraftXOpts.container \"" + containerId + "\" is not a known element id!";
		logError(msg);
		alert(msg);
		return;
	}
	
	var node;
	while(node = rootElement.lastChild) {
		rootElement.removeChild(node);
	}
	
	const splashElement = /** @type {HTMLElement} */ (document.createElement("div"));
	splashElement.style.width = "100%";
	splashElement.style.height = "100%";
	splashElement.style.setProperty("image-rendering", "pixelated");
	splashElement.style.background = "center / contain no-repeat url(\"data:image/png;base64,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\") white";
	rootElement.appendChild(splashElement);
	
	// allow the screen to update
	await asyncSleep(20);
	
	/** @type {ArrayBuffer} */
	var theEPWFileBuffer;
	if(assetsURI.startsWith("data:")) {
		logInfo("Downloading EPW file \"<data: " + assetsURI.length + " chars>\"...");
		theEPWFileBuffer = await downloadDataURL(assetsURI);
	}else {
		logInfo("Downloading EPW file \"" + assetsURI + "\"...");
		theEPWFileBuffer = await downloadURL(assetsURI);
	}
	
	var isInvalid = false;
	if(!theEPWFileBuffer) {
		isInvalid = true;
	}else if(theEPWFileBuffer.byteLength < 384) {
		logError("The EPW file is too short");
		isInvalid = true;
	}
	
	if(isInvalid) {
		rootElement.removeChild(splashElement);
		const msg = "Failed to download EPW file!";
		displayInvalidEPW(rootElement, msg);
		logError(msg);
		return;
	}
	
	const dataView = new DataView(theEPWFileBuffer);
	
	if(dataView.getUint32(0, true) !== 608649541 || dataView.getUint32(4, true) !== 1297301847) {
		logError("The file is not an EPW file");
		isInvalid = true;
	}
	
	const phileLength = theEPWFileBuffer.byteLength;
	if(dataView.getUint32(8, true) !== phileLength) {
		logError("The EPW file is the wrong length");
		isInvalid = true;
	}

	if(isInvalid) {
		rootElement.removeChild(splashElement);
		const msg = "EPW file is invalid!";
		displayInvalidEPW(rootElement, msg);
		logError(msg);
		return;
	}

	const textDecoder = new TextDecoder("utf-8");

	const splashDataOffset = dataView.getUint32(100, true);
	const splashDataLength = dataView.getUint32(104, true);
	const splashMIMEOffset = dataView.getUint32(108, true);
	const splashMIMELength = dataView.getUint32(112, true);
	
	if(splashDataOffset < 0 || splashDataOffset + splashDataLength > phileLength
			|| splashMIMEOffset < 0 || splashMIMEOffset + splashMIMELength > phileLength) {
		logError("The EPW file contains an invalid offset (component: splash)");
		isInvalid = true;
	}

	if(isInvalid) {
		rootElement.removeChild(splashElement);
		const msg = "EPW file is invalid!";
		displayInvalidEPW(rootElement, msg);
		logError(msg);
		return;
	}

	const splashBinSlice = new Uint8Array(theEPWFileBuffer, splashDataOffset, splashDataLength);
	const splashMIMESlice = new Uint8Array(theEPWFileBuffer, splashMIMEOffset, splashMIMELength);
	const splashURL = URL.createObjectURL(new Blob([ splashBinSlice ], { "type": textDecoder.decode(splashMIMESlice) }));

	await preloadImage(splashURL, 50);

	logInfo("Loaded splash img: " + splashURL);
	splashElement.style.background = "center / contain no-repeat url(\"" + splashURL + "\"), 0px 0px / 1000000% 1000000% no-repeat url(\"" + splashURL + "\") white";

	// allow the screen to update
	await asyncSleep(20);

	const loaderJSOffset = dataView.getUint32(164, true);
	const loaderJSLength = dataView.getUint32(168, true);
	const loaderWASMOffset = dataView.getUint32(180, true);
	const loaderWASMLength = dataView.getUint32(184, true);
	
	if(loaderJSOffset < 0 || loaderJSOffset + loaderJSLength > phileLength
			|| loaderWASMOffset < 0 || loaderWASMOffset + loaderWASMLength > phileLength) {
		logError("The EPW file contains an invalid offset (component: loader)");
		isInvalid = true;
	}

	if(isInvalid) {
		rootElement.removeChild(splashElement);
		const msg = "EPW file is invalid!";
		displayInvalidEPW(rootElement, msg);
		logError(msg);
		return;
	}

	const loaderJSSlice = new Uint8Array(theEPWFileBuffer, loaderJSOffset, loaderJSLength);
	const loaderJSURL = URL.createObjectURL(new Blob([ loaderJSSlice ], { "type": "text/javascript;charset=utf-8" }));
	logInfo("Loaded loader.js: " + splashURL);
	const loaderWASMSlice = new Uint8Array(theEPWFileBuffer, loaderWASMOffset, loaderWASMLength);
	const loaderWASMURL = URL.createObjectURL(new Blob([ loaderWASMSlice ], { "type": "application/wasm" }));
	logInfo("Loaded loader.wasm: " + loaderWASMURL);

	const optsObj = {};
	for(const [key, value] of Object.entries(window.eaglercraftXOpts)) {
		if(key !== "container" && key !== "assetsURI") {
			optsObj[key] = value;
		}
	}

	window.__eaglercraftXLoaderContextPre = {
		"rootElement": rootElement,
		"eaglercraftXOpts": optsObj,
		"theEPWFileBuffer": theEPWFileBuffer,
		"loaderWASMURL": loaderWASMURL,
		"splashURL": splashURL
	};

	logInfo("Appending loader.js to document...");

	const scriptElement = /** @type {HTMLScriptElement} */ (document.createElement("script"));
	scriptElement.type = "text/javascript";
	scriptElement.src = loaderJSURL;
	document.head.appendChild(scriptElement);

};

