
package com.rustero.effects;


public class glEffectGlowingEdges extends glEffect {


	protected String getFragmentCode() {
		String result =
				"precision mediump float; \n" +
				"varying vec2 vTextureCoord; \n" +
				" " +
				"vec4 getPixel(vec2 aPosition) {" +
				"	return texture2D(sTexture, aPosition); " +
				"}" +
				" " +
				"float bw(vec2 coords) {" +
				"	vec4 lm = getPixel(coords) * vec4(0.21, 0.71, 0.07, 1);" +
				"	return lm.r+lm.g+lm.b;" +
				"} " +
				" " +
				"void main() {\n" +
				"	vec2 uv = vTextureCoord;" +
				"	vec2 of = vec2(1.0 / 128.0, 0);" +
				"	float bwColor = sqrt(abs(bw(uv) - bw(uv+of.xx)) + abs(bw(uv + of.xy) - bw(uv + of.yx)));" +
				"   gl_FragColor = vec4(bwColor); \n" +
				"}\n";

		return result;
	}


	public glEffectGlowingEdges() {
		super();
		name = NAME_GlowingEdges;
	}


}
