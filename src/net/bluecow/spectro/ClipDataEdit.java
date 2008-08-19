/*
 * Created on Aug 14, 2008
 *
 * Spectro-Edit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spectro-Edit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package net.bluecow.spectro;

import java.awt.Rectangle;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Captures the necessary state and behaviour to undo and redo some change
 * to a rectangular region of spectral data.
 */
public class ClipDataEdit extends AbstractUndoableEdit {

    private final Clip clip;
    private final int firstFrame;
    private final int firstFreqIndex;
    private final double[][] oldData;
    private double[][] newData;
    
    /**
     * @param clip The clip to capture data from and apply undo/redo operations to.
     * @param firstFrame The index of the first frame to capture.
     * @param firstFreqIndex The first frequency index to capture.
     * @param nFrames The number of frames (starting at and including firstFrame) to capture.
     * @param nFreqs The number of frequency slots (starting at and including firstFreqIndex) to capture.
     */
    public ClipDataEdit(
            Clip clip,
            int firstFrame,
            int firstFreqIndex,
            int nFrames,
            int nFreqs) {
        if (nFrames == 0) {
            throw new IllegalArgumentException("Data area to capture is empty (nFrames == 0)");
        }
        if (nFreqs == 0) {
            throw new IllegalArgumentException("Data area to capture is empty (nFreqs == 0)");
        }
        this.clip = clip;
        this.firstFrame = firstFrame;
        this.firstFreqIndex = firstFreqIndex;
        oldData = new double[nFrames][nFreqs];
        capture(oldData);
    }
    
    /**
     * Copies the current contents of the same clip region that was captured
     * during the constructor invocation. This will be the REDO data.
     */
    public void captureNewData() {
        if (newData != null) {
            throw new IllegalStateException("Already captured new data");
        }
        newData = new double[oldData.length][oldData[0].length];
        capture(newData);
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        apply(oldData);
        clip.regionChanged(getRegion());
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        apply(newData);
        clip.regionChanged(getRegion());
    }
    
    /**
     * Applies the given data into the frames of {@link #clip}.
     * 
     * @param data The data to copy into clip.
     */
    private void apply(double[][] data) {
        for (int i = 0; i < data.length; i++) {
            Frame f = clip.getFrame(i + firstFrame);
            for (int j = 0; j < data[0].length; j++) {
                f.setReal(j + firstFreqIndex, data[i][j]);
            }
        }
    }

    /**
     * Copies data from the clip into the given arrays.
     * 
     * @param data The arrays to store the clip data into.
     */
    private void capture(double[][] data) {
        for (int i = 0; i < data.length; i++) {
            Frame f = clip.getFrame(i + firstFrame);
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = f.getReal(j + firstFreqIndex);
            }
        }
    }

    /**
     * Returns the data region for this edit.
     * 
     * @return A rectangle with (x, y, w, h) == (firstFrame, firstFreqIndex, nFrames, nFreqs).
     */
    public Rectangle getRegion() {
        return new Rectangle(firstFrame, firstFreqIndex, oldData.length, oldData[0].length);
    }

}