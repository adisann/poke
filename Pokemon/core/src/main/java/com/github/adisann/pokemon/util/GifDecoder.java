package com.github.adisann.pokemon.util;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to decode animated GIF files into LibGDX Animation objects.
 * 
 * Since LibGDX doesn't natively support animated GIFs, this class reads GIF files
 * and extracts each frame into TextureRegions that can be used with Animation.
 * 
 * Usage:
 *   GifDecoder.GifAnimation gifAnim = GifDecoder.loadGIF(Gdx.files.internal("pokemon.gif"));
 *   Animation<TextureRegion> animation = gifAnim.getAnimation();
 *   
 *   // In render loop:
 *   TextureRegion frame = animation.getKeyFrame(stateTime, true);
 *   batch.draw(frame, x, y);
 *   
 *   // When done:
 *   gifAnim.dispose();
 */
public class GifDecoder {
    
    /**
     * Container for a decoded GIF animation that manages texture disposal.
     */
    public static class GifAnimation implements Disposable {
        private Animation<TextureRegion> animation;
        private List<Texture> textures;
        
        public GifAnimation(Animation<TextureRegion> animation, List<Texture> textures) {
            this.animation = animation;
            this.textures = textures;
        }
        
        public Animation<TextureRegion> getAnimation() {
            return animation;
        }
        
        @Override
        public void dispose() {
            if (textures != null) {
                for (Texture t : textures) {
                    t.dispose();
                }
                textures.clear();
            }
        }
    }
    
    /**
     * Load a GIF file and return an Animation with all frames.
     * 
     * @param file The GIF file to load
     * @return GifAnimation containing the animation and textures (must be disposed when done)
     */
    public static GifAnimation loadGIF(FileHandle file) {
        try {
            InputStream is = file.read();
            GifReader reader = new GifReader();
            GifReader.GifData gifData = reader.read(is);
            is.close();
            
            if (gifData == null || gifData.frames.isEmpty()) {
                System.out.println("GifDecoder: Failed to load GIF or no frames: " + file.path());
                return createFallbackAnimation();
            }
            
            List<Texture> textures = new ArrayList<>();
            Array<TextureRegion> frames = new Array<>();
            
            // Default frame duration in seconds (GIF delay is in centiseconds)
            float defaultDelay = 0.1f;
            float totalDuration = 0f;
            
            for (GifReader.GifFrame frame : gifData.frames) {
                Pixmap pixmap = frame.pixmap;
                Texture texture = new Texture(pixmap);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                textures.add(texture);
                frames.add(new TextureRegion(texture));
                
                // Use frame delay if available
                float delay = frame.delay > 0 ? frame.delay / 100f : defaultDelay;
                totalDuration += delay;
                
                // Dispose pixmap after creating texture
                pixmap.dispose();
            }
            
            // Calculate average frame duration
            float frameDuration = textures.size() > 0 ? totalDuration / textures.size() : defaultDelay;
            
            Animation<TextureRegion> animation = new Animation<>(frameDuration, frames, PlayMode.LOOP);
            
            System.out.println("GifDecoder: Loaded " + file.name() + " with " + textures.size() + 
                             " frames, duration=" + frameDuration + "s per frame");
            
            return new GifAnimation(animation, textures);
            
        } catch (Exception e) {
            System.out.println("GifDecoder: Error loading GIF: " + file.path() + " - " + e.getMessage());
            e.printStackTrace();
            return createFallbackAnimation();
        }
    }
    
    /**
     * Create a fallback single-frame animation for error cases.
     */
    private static GifAnimation createFallbackAnimation() {
        // Create a 1x1 magenta pixel as fallback (visible error indicator)
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 0f, 1f, 1f); // Magenta
        pixmap.fill();
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        
        List<Texture> textures = new ArrayList<>();
        textures.add(texture);
        
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(texture));
        
        Animation<TextureRegion> animation = new Animation<>(1f, frames, PlayMode.LOOP);
        return new GifAnimation(animation, textures);
    }
    
    /**
     * Inner class to parse GIF file format.
     * Handles GIF87a and GIF89a formats.
     */
    private static class GifReader {
        
        static class GifFrame {
            Pixmap pixmap;
            int delay; // Delay in centiseconds (1/100 of a second)
            int disposalMethod;
        }
        
        static class GifData {
            int width;
            int height;
            List<GifFrame> frames = new ArrayList<>();
        }
        
        private static final int MAX_STACK_SIZE = 4096;
        
        private InputStream input;
        private int[] globalColorTable;
        private int bgColor;
        private int width, height;
        private boolean gctFlag;
        private int gctSize;
        
        // Current frame state
        private int[] localColorTable;
        private boolean lctFlag;
        private int lctSize;
        private int ix, iy, iw, ih;
        private boolean interlace;
        private int frameDelay;
        private int disposalMethod;
        private int transIndex;
        private boolean transparency;
        
        // Previous frame for disposal
        private int[] previousPixels;
        private int[] currentPixels;
        
        public GifData read(InputStream is) {
            this.input = is;
            GifData data = new GifData();
            
            try {
                // Read header
                byte[] header = new byte[6];
                readFully(header);
                String sig = new String(header);
                if (!sig.startsWith("GIF")) {
                    return null;
                }
                
                // Read logical screen descriptor
                width = readShort();
                height = readShort();
                data.width = width;
                data.height = height;
                
                int packed = readByte();
                gctFlag = (packed & 0x80) != 0;
                gctSize = 2 << (packed & 7);
                bgColor = readByte();
                readByte(); // Pixel aspect ratio (ignored)
                
                // Read global color table
                if (gctFlag) {
                    globalColorTable = readColorTable(gctSize);
                }
                
                // Initialize pixel buffers
                currentPixels = new int[width * height];
                previousPixels = new int[width * height];
                
                // Read frames
                boolean done = false;
                while (!done) {
                    int code = readByte();
                    switch (code) {
                        case 0x2C: // Image descriptor
                            GifFrame frame = readFrame();
                            if (frame != null) {
                                data.frames.add(frame);
                            }
                            break;
                        case 0x21: // Extension
                            readExtension();
                            break;
                        case 0x3B: // Terminator
                            done = true;
                            break;
                        case -1: // EOF
                            done = true;
                            break;
                        default:
                            // Skip unknown block
                            break;
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                return data.frames.isEmpty() ? null : data;
            }
            
            return data;
        }
        
        private GifFrame readFrame() throws Exception {
            // Read image descriptor
            ix = readShort();
            iy = readShort();
            iw = readShort();
            ih = readShort();
            
            int packed = readByte();
            lctFlag = (packed & 0x80) != 0;
            interlace = (packed & 0x40) != 0;
            lctSize = 2 << (packed & 7);
            
            // Read local color table if present
            if (lctFlag) {
                localColorTable = readColorTable(lctSize);
            }
            
            // Get active color table
            int[] colorTable = lctFlag ? localColorTable : globalColorTable;
            if (colorTable == null) {
                return null;
            }
            
            // Read image data
            int[] pixels = decodeImageData(colorTable);
            if (pixels == null) {
                return null;
            }
            
            // Handle disposal method before rendering new frame
            if (disposalMethod == 2) {
                // Restore to background
                for (int i = 0; i < currentPixels.length; i++) {
                    currentPixels[i] = 0;
                }
            } else if (disposalMethod == 3) {
                // Restore to previous
                System.arraycopy(previousPixels, 0, currentPixels, 0, currentPixels.length);
            }
            
            // Save previous state for next frame
            System.arraycopy(currentPixels, 0, previousPixels, 0, currentPixels.length);
            
            // Render frame onto current pixels
            for (int y = 0; y < ih; y++) {
                int destY = iy + y;
                if (destY < 0 || destY >= height) continue;
                
                for (int x = 0; x < iw; x++) {
                    int destX = ix + x;
                    if (destX < 0 || destX >= width) continue;
                    
                    int srcIdx = y * iw + x;
                    int pixel = pixels[srcIdx];
                    
                    // Check transparency
                    if (transparency && (pixel & 0xFF000000) == 0) {
                        continue;
                    }
                    
                    int destIdx = destY * width + destX;
                    currentPixels[destIdx] = pixel;
                }
            }
            
            // Create pixmap from current pixels
            Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = currentPixels[y * width + x];
                    // Convert ARGB to RGBA
                    int a = (pixel >> 24) & 0xFF;
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;
                    pixmap.drawPixel(x, y, (r << 24) | (g << 16) | (b << 8) | a);
                }
            }
            
            GifFrame frame = new GifFrame();
            frame.pixmap = pixmap;
            frame.delay = frameDelay;
            frame.disposalMethod = disposalMethod;
            
            // Reset frame-specific state
            transparency = false;
            frameDelay = 0;
            disposalMethod = 0;
            
            return frame;
        }
        
        private int[] decodeImageData(int[] colorTable) throws Exception {
            int dataSize = readByte();
            
            // LZW decode
            int clearCode = 1 << dataSize;
            int endCode = clearCode + 1;
            int available = clearCode + 2;
            int codeSize = dataSize + 1;
            int codeMask = (1 << codeSize) - 1;
            int oldCode = -1;
            
            short[] prefix = new short[MAX_STACK_SIZE];
            byte[] suffix = new byte[MAX_STACK_SIZE];
            byte[] pixelStack = new byte[MAX_STACK_SIZE + 1];
            
            for (int code = 0; code < clearCode; code++) {
                prefix[code] = 0;
                suffix[code] = (byte) code;
            }
            
            int[] pixels = new int[iw * ih];
            int pixelIndex = 0;
            int top = 0;
            int first = 0;
            
            int datum = 0;
            int bits = 0;
            int count = 0;
            byte[] block = new byte[256];
            int blockIndex = 0;
            
            while (pixelIndex < pixels.length) {
                // Read more bits if needed
                while (bits < codeSize) {
                    if (count == 0) {
                        count = readByte();
                        if (count <= 0) break;
                        readFully(block, 0, count);
                        blockIndex = 0;
                    }
                    datum |= (block[blockIndex++] & 0xFF) << bits;
                    bits += 8;
                    count--;
                }
                
                int code = datum & codeMask;
                datum >>= codeSize;
                bits -= codeSize;
                
                if (code == clearCode) {
                    codeSize = dataSize + 1;
                    codeMask = (1 << codeSize) - 1;
                    available = clearCode + 2;
                    oldCode = -1;
                    continue;
                }
                
                if (code == endCode) {
                    break;
                }
                
                if (oldCode == -1) {
                    int colorIndex = suffix[code] & 0xFF;
                    if (colorIndex < colorTable.length) {
                        pixels[pixelIndex++] = colorTable[colorIndex];
                    }
                    oldCode = code;
                    first = code;
                    continue;
                }
                
                int inCode = code;
                if (code >= available) {
                    pixelStack[top++] = (byte) first;
                    code = oldCode;
                }
                
                while (code >= clearCode) {
                    pixelStack[top++] = suffix[code];
                    code = prefix[code];
                }
                
                first = suffix[code] & 0xFF;
                pixelStack[top++] = (byte) first;
                
                if (available < MAX_STACK_SIZE) {
                    prefix[available] = (short) oldCode;
                    suffix[available] = (byte) first;
                    available++;
                    if ((available & codeMask) == 0 && available < MAX_STACK_SIZE) {
                        codeSize++;
                        codeMask = (1 << codeSize) - 1;
                    }
                }
                
                oldCode = inCode;
                
                // Output pixels
                while (top > 0 && pixelIndex < pixels.length) {
                    int colorIndex = pixelStack[--top] & 0xFF;
                    if (colorIndex < colorTable.length) {
                        pixels[pixelIndex++] = colorTable[colorIndex];
                    } else {
                        pixels[pixelIndex++] = 0;
                    }
                }
            }
            
            // Skip remaining blocks
            while (true) {
                int blockSize = readByte();
                if (blockSize <= 0) break;
                skip(blockSize);
            }
            
            // Handle interlacing
            if (interlace) {
                pixels = deinterlace(pixels, iw, ih);
            }
            
            return pixels;
        }
        
        private int[] deinterlace(int[] pixels, int w, int h) {
            int[] result = new int[pixels.length];
            int srcIdx = 0;
            
            // Pass 1: every 8th row starting at 0
            for (int y = 0; y < h; y += 8) {
                System.arraycopy(pixels, srcIdx, result, y * w, w);
                srcIdx += w;
            }
            // Pass 2: every 8th row starting at 4
            for (int y = 4; y < h; y += 8) {
                System.arraycopy(pixels, srcIdx, result, y * w, w);
                srcIdx += w;
            }
            // Pass 3: every 4th row starting at 2
            for (int y = 2; y < h; y += 4) {
                System.arraycopy(pixels, srcIdx, result, y * w, w);
                srcIdx += w;
            }
            // Pass 4: every 2nd row starting at 1
            for (int y = 1; y < h; y += 2) {
                System.arraycopy(pixels, srcIdx, result, y * w, w);
                srcIdx += w;
            }
            
            return result;
        }
        
        private void readExtension() throws Exception {
            int extCode = readByte();
            
            switch (extCode) {
                case 0xF9: // Graphic Control Extension
                    readByte(); // Block size (always 4)
                    int packed = readByte();
                    disposalMethod = (packed & 0x1C) >> 2;
                    transparency = (packed & 1) != 0;
                    frameDelay = readShort();
                    transIndex = readByte();
                    readByte(); // Block terminator
                    break;
                    
                default:
                    // Skip other extensions
                    skipBlocks();
                    break;
            }
        }
        
        private void skipBlocks() throws Exception {
            int blockSize;
            while ((blockSize = readByte()) > 0) {
                skip(blockSize);
            }
        }
        
        private int[] readColorTable(int size) throws Exception {
            int[] table = new int[size];
            byte[] rgb = new byte[size * 3];
            readFully(rgb);
            
            for (int i = 0; i < size; i++) {
                int r = rgb[i * 3] & 0xFF;
                int g = rgb[i * 3 + 1] & 0xFF;
                int b = rgb[i * 3 + 2] & 0xFF;
                table[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
            
            return table;
        }
        
        private int readByte() throws Exception {
            return input.read();
        }
        
        private int readShort() throws Exception {
            int lo = input.read();
            int hi = input.read();
            return (hi << 8) | lo;
        }
        
        private void readFully(byte[] buffer) throws Exception {
            readFully(buffer, 0, buffer.length);
        }
        
        private void readFully(byte[] buffer, int offset, int length) throws Exception {
            int remaining = length;
            while (remaining > 0) {
                int read = input.read(buffer, offset + (length - remaining), remaining);
                if (read <= 0) break;
                remaining -= read;
            }
        }
        
        private void skip(int n) throws Exception {
            input.skip(n);
        }
    }
}
