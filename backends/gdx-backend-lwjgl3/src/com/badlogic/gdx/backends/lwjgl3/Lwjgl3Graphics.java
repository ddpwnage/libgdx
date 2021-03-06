/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.lwjgl3;

import java.awt.Toolkit;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;

public class Lwjgl3Graphics implements Graphics {
	private final Lwjgl3Window window;
	private final GL20 gl20;
	private final GL30 gl30;
	private volatile int frameBufferWidth;
	private volatile int frameBufferHeight;
	private volatile int logicalWidth;
	private volatile int logicalHeight;
	private boolean isFullscreen;
	private BufferFormat bufferFormat;
	private long lastFrameTime = -1;
	private float deltaTime;
	private long frameId;
	private long frameCounterStart = 0;
	private int frames;
	private int fps;
	
	IntBuffer tmpBuffer = BufferUtils.createIntBuffer(1);
	IntBuffer tmpBuffer2 = BufferUtils.createIntBuffer(1);
	
	private GLFWFramebufferSizeCallback resizeCallback = new GLFWFramebufferSizeCallback() {
		@Override
		public void invoke(long windowHandle, final int width, final int height) {
			Lwjgl3Graphics.this.frameBufferWidth = width;
			Lwjgl3Graphics.this.frameBufferHeight = height;
			GLFW.glfwGetWindowSize(windowHandle, tmpBuffer, tmpBuffer2);
			Lwjgl3Graphics.this.logicalWidth = tmpBuffer.get(0);
			Lwjgl3Graphics.this.logicalHeight = tmpBuffer2.get(0);
			if(!window.isListenerInitialized()) {
				return;
			}
			GLFW.glfwMakeContextCurrent(windowHandle);
			window.getListener().resize(getWidth(), getHeight());
			window.getListener().render();
			GLFW.glfwSwapBuffers(windowHandle);			
		}
	};
	
	public Lwjgl3Graphics(Lwjgl3Window window) {
		this.window = window;
		this.gl20 = new Lwjgl3GL20();
		this.gl30 = null;
		updateFramebufferInfo();
		GLFW.glfwSetFramebufferSizeCallback(window.getWindowHandle(), resizeCallback);		
	}
	
	private void updateFramebufferInfo() {
		GLFW.glfwGetFramebufferSize(window.getWindowHandle(), tmpBuffer, tmpBuffer2);
		this.frameBufferWidth = tmpBuffer.get(0);
		this.frameBufferHeight = tmpBuffer2.get(0);
		GLFW.glfwGetWindowSize(window.getWindowHandle(), tmpBuffer, tmpBuffer2);
		Lwjgl3Graphics.this.logicalWidth = tmpBuffer.get(0);
		Lwjgl3Graphics.this.logicalHeight = tmpBuffer2.get(0);
		this.isFullscreen = GLFW.glfwGetWindowMonitor(window.getWindowHandle()) != 0;
		Lwjgl3ApplicationConfiguration config = window.getConfig();
		bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.samples, false);
	}
	
	void update() {
		long time = System.nanoTime();
		if (lastFrameTime == -1) lastFrameTime = time;
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;

		if (time - frameCounterStart >= 1000000000) {
			fps = frames;
			frames = 0;
			frameCounterStart = time;
		}
		frames++;
		frameId++;				
	}
	
	@Override
	public boolean isGL30Available() {
		return gl30 != null;
	}

	@Override
	public GL20 getGL20() {
		return gl20;
	}

	@Override
	public GL30 getGL30() {
		return gl30;
	}

	@Override
	public int getWidth() {
		if(window.getConfig().useHDPI) {
			return frameBufferWidth;
		} else {
			return logicalWidth;
		}
	}

	@Override
	public int getHeight() {
		if(window.getConfig().useHDPI) {
			return frameBufferHeight;
		} else {
			return logicalHeight;
		}
	}
	
	@Override
	public int getBackBufferWidth() {
		return frameBufferWidth;
	}

	@Override
	public int getBackBufferHeight() {
		return frameBufferHeight;
	}
	
	public int getLogicalWidth() {
		return logicalWidth;
	}
	
	public int getLogicalHeight() {
		return logicalHeight;
	}

	@Override
	public long getFrameId() {
		return frameId;
	}

	@Override
	public float getDeltaTime() {
		return deltaTime;
	}

	@Override
	public float getRawDeltaTime() {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

	@Override
	public GraphicsType getType() {
		return GraphicsType.LWJGL3;
	}

	@Override
	public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpcX () {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f;
	}

	@Override
	public float getPpcY () {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f;
	}

	@Override
	public float getDensity () {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 160f;
	}
	
	@Override
	public boolean supportsDisplayModeChange() {
		return true;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return Lwjgl3ApplicationConfiguration.getDisplayModes();
	}

	@Override
	public DisplayMode getDesktopDisplayMode() {
		return Lwjgl3ApplicationConfiguration.getDesktopDisplayMode();
	}

	@Override
	public boolean setDisplayMode(DisplayMode displayMode) {
		
		return false;
	}

	@Override
	public boolean setDisplayMode(int width, int height, boolean fullscreen) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTitle(String title) {
		if(title == null) {
			title = "";
		}
		GLFW.glfwSetWindowTitle(window.getWindowHandle(), title);
	}

	@Override
	public void setVSync(boolean vsync) {
		GLFW.glfwSwapInterval(vsync? 1: 0);
	}

	@Override
	public BufferFormat getBufferFormat() {
		return bufferFormat;
	}

	@Override
	public boolean supportsExtension(String extension) {
		return GLFW.glfwExtensionSupported(extension) == GLFW.GLFW_TRUE;
	}

	@Override
	public void setContinuousRendering(boolean isContinuous) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isContinuousRendering() {
		return true;
	}

	@Override
	public void requestRendering() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isFullscreen() {
		return isFullscreen;
	}

	@Override
	public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCursor(Cursor cursor) {
		// TODO Auto-generated method stub
	}
	
	public static class Lwjgl3DisplayMode extends DisplayMode {
		Lwjgl3DisplayMode(int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}
}
