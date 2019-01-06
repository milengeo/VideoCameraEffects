
package com.rustero.effects;


public class glEffectPosterize extends glEffect {


	protected String getFragmentCode() {
		String result =
				"precision mediump float; \n" +
				"varying vec2 vTextureCoord; \n" +
				" " +
				"vec4 getPixel(vec2 aPosition) {" +
				"	return texture2D(sTexture, aPosition); " +
				"}" +
				" " +
				"void main() {\n" +
				"	vec4 textureColor = getPixel(vTextureCoord); " +
				"	vec4 pixel = floor((textureColor * 10.0) + vec4(0.5)) / 10.0; " +
				"	gl_FragColor = pixel; \n" +
				"}\n";

		return result;
	}


	public glEffectPosterize() {
		super();
		name = NAME_Posterize;
	}


}
