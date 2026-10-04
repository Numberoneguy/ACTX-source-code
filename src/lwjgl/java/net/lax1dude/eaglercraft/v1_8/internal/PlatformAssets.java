/*
 * Copyright (c) 2022-2023 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.internal;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import net.lax1dude.eaglercraft.v1_8.EaglerInputStream;
import net.lax1dude.eaglercraft.v1_8.opengl.ImageData;

public class PlatformAssets {
	
	private static final boolean IS_RUNNING_IN_JAR;

	static {
		// Detects if the application is executing from inside a JAR package
		URL classUrl = PlatformAssets.class.getResource("PlatformAssets.class");
		IS_RUNNING_IN_JAR = classUrl != null && "jar".equalsIgnoreCase(classUrl.getProtocol());
	}

	public static boolean isRunningInJar() {
		return IS_RUNNING_IN_JAR;
	}
	
	static URL getDesktopResourceURL(String path) {
		String classpathLocation = path.startsWith("/") ? path : "/" + path;

		if (IS_RUNNING_IN_JAR) {
			return PlatformAssets.class.getResource(classpathLocation);
		}

		File f = new File("resources", path);
		if (f.isFile()) {
			try {
				return f.toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}

		// Fallback to classpath if not found on disk in workspace dev mode
		return PlatformAssets.class.getResource(classpathLocation);
	}
	
	public static boolean getResourceExists(String path) {
		String classpathLocation = path.startsWith("/") ? path : "/" + path;

		if (IS_RUNNING_IN_JAR) {
			return PlatformAssets.class.getResource(classpathLocation) != null;
		}

		if ((new File("resources", path)).isFile()) {
			return true;
		}

		return PlatformAssets.class.getResource(classpathLocation) != null;
	}
	
	public static byte[] getResourceBytes(String path) {
		String classpathLocation = path.startsWith("/") ? path : "/" + path;

		// 1. If running in JAR, bypass file system checks completely
		if (IS_RUNNING_IN_JAR) {
			return loadFromClasspath(classpathLocation);
		}

		// 2. If running in IDE / Gradle workspace runtime, try reading from disk
		File loadFile = new File("resources", path);
		if (loadFile.isFile()) {
			byte[] ret = new byte[(int) loadFile.length()];
			try (FileInputStream is = new FileInputStream(loadFile)) {
				int i, j = 0;
				while (j < ret.length && (i = is.read(ret, j, ret.length - j)) != -1) {
					j += i;
				}
				return ret;
			} catch (IOException ex) {
				// Fallthrough
			}
		}

		// Fallback to classpath
		return loadFromClasspath(classpathLocation);
	}

	private static byte[] loadFromClasspath(String classpathLocation) {
		try (InputStream is = PlatformAssets.class.getResourceAsStream(classpathLocation)) {
			if (is == null) {
				return null;
			}
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] data = new byte[8192];
			int nRead;
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException ex) {
			return null;
		}
	}

	public static ImageData loadImageFile(InputStream data) {
		return loadImageFile(data, "image/png");
	}

	public static ImageData loadImageFile(InputStream data, String mime) {
		try {
			BufferedImage img = ImageIO.read(data);
			if(img == null) {
				throw new IOException("Data is not a supported image format!");
			}
			int w = img.getWidth();
			int h = img.getHeight();
			boolean a = img.getColorModel().hasAlpha();
			int[] pixels = new int[w * h];
			img.getRGB(0, 0, w, h, pixels, 0, w);
			for(int i = 0; i < pixels.length; ++i) {
				int j = pixels[i];
				if(!a) {
					j = j | 0xFF000000;
				}
				pixels[i] = (j & 0xFF00FF00) | ((j & 0x00FF0000) >>> 16) |
						((j & 0x000000FF) << 16);
			}
			return new ImageData(w, h, pixels, a);
		}catch(IOException ex) {
			return null;
		}
	}

	public static ImageData loadImageFile(byte[] data) {
		return loadImageFile(new EaglerInputStream(data), "image/png");
	}

	public static ImageData loadImageFile(byte[] data, String mime) {
		return loadImageFile(new EaglerInputStream(data), mime);
	}

}