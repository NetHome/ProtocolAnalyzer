/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu).
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.impl.ProtocolSampler;

/**
 * User: Stefan
 * Date: 2013-04-13
 * Time: 14:58
 */
public class SignalInverter implements ProtocolSampler {

    private ProtocolSampler sink;
    private int invert = 1;

    public SignalInverter(ProtocolSampler sink) {
        this.sink = sink;
    }

    public void addSample(int sample) {
        sink.addSample(sample * invert);
    }

    public void setSampleRate(int frequency) {
        sink.setSampleRate(frequency);
    }

    public void setInverted(boolean inverted) {
        invert = inverted ? -1 : 1;
    }

    public boolean isInverted() {
        return invert == -1;
    }
}
