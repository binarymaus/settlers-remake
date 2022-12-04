/*******************************************************************************
 * Copyright (c) 2015 - 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package go.graphics.swing.sound;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import go.graphics.sound.ISoundDataRetriever;
import go.graphics.sound.SoundHandle;
import go.graphics.sound.SoundPlayer;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;

public class SwingSoundPlayer implements SoundPlayer {

	private final ISoundSettingsProvider soundSettingsProvider;
	private ISoundDataRetriever soundDataRetriever;
	private final long device;
	private final long context;
	private final List<Integer> sources = new ArrayList<>();
	private final Map<Integer, Integer> sounds = new HashMap<>();

	public SwingSoundPlayer(ISoundSettingsProvider soundSettingsProvider) {
		this.soundSettingsProvider = soundSettingsProvider;
		device = ALC10.alcOpenDevice((String)null);

		if(device == 0) {
			context = 0;
			System.err.println("Could not initialize OpenAL: Audio is not going to work!");
			return;

		}

		context = ALC10.alcCreateContext(device, (IntBuffer)null);

		if(context == 0) {
			System.err.println("Could not create an OpenAL context: Audio is not going to work!");
			return;
		}

		ALC10.alcMakeContextCurrent(context);
		AL.createCapabilities(ALC.createCapabilities(device));
	}
	private int getAvailableSource() {
		for(int source : sources) {
			if(AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) return source;
		}

		int newSource = AL10.alGenSources();
		sources.add(newSource);
		return newSource;
	}

	private int createSoundBuffer(int soundIndex) {
		int buffer = AL10.alGenBuffers();
		try {
			short[] data = soundDataRetriever.getSoundData(soundIndex);
			AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, data, 20500);
		} catch (IOException e) {
			System.err.println("Could not create sound buffer for: " + soundIndex);
			e.printStackTrace();
		}
		return buffer;
	}

	@Override
	public void playSound(int soundStart, float leftVolume, float rightVolume) {
		if(context == 0) return;

		float gameVolume = soundSettingsProvider.getVolume();

		int source = getAvailableSource();

		int buffer = sounds.computeIfAbsent(soundStart, this::createSoundBuffer);

		float localVolume = (leftVolume+rightVolume)/2;
		float globalVolume = gameVolume * localVolume;
		AL10.alSourceRewind(source);
		AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
		AL10.alSourcef(source, AL10.AL_GAIN, globalVolume);
		AL10.alSource3f(source, AL10.AL_POSITION, (rightVolume-leftVolume)/localVolume, 0f, 0f);
		AL10.alSourcePlay(source);
	}

	public void close() {
		if(device != 0) {
			if(context != 0) {
				sources.forEach(AL10::alDeleteSources);
				sounds.values().forEach(AL10::alDeleteBuffers);
				ALC10.alcDestroyContext(context);
			}
			ALC10.alcCloseDevice(device);
		}
	}

	@Override
	public void setSoundDataRetriever(ISoundDataRetriever soundDataRetriever) {
		this.soundDataRetriever = soundDataRetriever;
	}

	@Override
	public SoundHandle openSound(File musicFile) {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(musicFile));

			return new SwingSoundHandle(clip);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
}
