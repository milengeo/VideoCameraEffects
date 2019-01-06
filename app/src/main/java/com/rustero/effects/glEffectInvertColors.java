
package com.rustero.effects;


public class glEffectInvertColors extends glEffect {


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
				"	vec4 pixel = getPixel(vTextureCoord);" +
				"   gl_FragColor = vec4(vec4((1.0 - pixel.rgb), pixel.a)); \n" +
				"}\n";

		return result;
	}


	public glEffectInvertColors() {
		super();
		name = NAME_InvertColors;
	}


}
