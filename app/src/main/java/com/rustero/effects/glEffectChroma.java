
package com.rustero.effects;


import android.opengl.GLES20;

import com.rustero.egl.glCore;


public class glEffectChroma extends glEffect {


	protected String getFragmentCode() {
		String result =
				"precision mediump float;\n" +
						"varying vec2 vTextureCoord;\n" +
						"void main() {\n" +
						"	vec4 pixel = texture2D(sTexture, vTextureCoord); " +
						"	vec2 offset = vec2(.04,.0); " +
						"	pixel.r = texture2D(sTexture, vTextureCoord + offset.xy).r; " +
						"	pixel.g = texture2D(sTexture, vTextureCoord).g; " +
						"	pixel.b = texture2D(sTexture, vTextureCoord + offset.xy).b; " +
						"   gl_FragColor = pixel; \n" +
						"}\n";

		return result;
	}


	public glEffectChroma() {
		super();
		name = NAME_GlowingEdges;
	}


}
