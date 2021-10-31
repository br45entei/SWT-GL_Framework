/*******************************************************************************
 * 
 * Copyright © 2021 Brian_Entei (br45entei@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 *******************************************************************************/
package com.gmail.br45entei.audio;

import com.gmail.br45entei.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

/** @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class FlacData implements PCMProcessor {
	
	/** @param args Program command line arguments */
	public static final void main(String[] args) {
		String path = StringUtil.stringArrayToString(args, ' ');
		File in = new File(path);
		if(!in.isFile()) {
			System.err.println("Usage: java.exe -classpath /bin com.gmail.br45entei.audio.FlacData path/to/flac/file.flac");
			return;
		}
		int i = 0;
		File out = new File(path.concat(".raw"));
		while(out.exists()) {
			out = new File(path.concat("_").concat(Integer.toString(i++, 10)).concat(".raw"));
		}
		FlacData data = new FlacData();
		try(FileInputStream fIn = new FileInputStream(in)) {
			FLACDecoder decoder = new FLACDecoder(fIn);
			decoder.addPCMProcessor(data);
			decoder.decode();
		} catch(IOException e) {
			e.printStackTrace();
		}
		try(FileOutputStream fOut = new FileOutputStream(out)) {
			fOut.write(data.getData());
			fOut.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private volatile ByteArrayOutputStream output = new ByteArrayOutputStream();
	
	@Override
	public void processStreamInfo(StreamInfo info) {
	}
	
	@Override
	public void processPCM(ByteData pcm) {
		this.output.write(pcm.getData(), 0, pcm.getLen());
	}
	
	/** @return The actual FLAC data */
	public final byte[] getData() {
		return this.output == null ? new byte[0] : this.output.toByteArray();
	}
	
	/** @return The actual FLAC data, in a direct byte buffer */
	public final ByteBuffer getDirectBuffer() {
		byte[] data = this.getData();
		return ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder()).put(data).rewind();
	}
	
	/** Release resources */
	public final void dispose() {
		this.output = null;
		System.gc();
	}
	
}
