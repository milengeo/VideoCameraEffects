
package com.rustero.effects;


public class glEffectPixelation extends glEffect {


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
				"	float factor = 0.01;" +
				"	vec2 coord = vec2(factor * floor(vTextureCoord.x / factor), factor * floor(vTextureCoord.y / factor));" +
				"	vec3 tc = getPixel(coord).xyz;" +
				"   gl_FragColor = vec4(tc, 1.0); \n" +
				"}\n";

		return result;
	}


	public glEffectPixelation() {
		super();
		name = NAME_Pixelation;
	}


}
