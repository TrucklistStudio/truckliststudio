/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.image;

/**
 * A Filter to invert the alpha channel of an image. This is really only useful for inverting selections, where we only use the alpha channel.
 */
public class InvertAlphaFilter extends PointFilter {

	public InvertAlphaFilter() {
		canFilterIndexColorModel = true;
	}

        @Override
	public int filterRGB(int x, int y, int rgb) {
		return rgb ^ 0xff000000;
	}

        @Override
	public String toString() {
		return "Alpha/Invert";
	}

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
