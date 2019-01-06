
package com.rustero.effects;


public class glEffectSwirl extends glEffect {




	protected String getFragmentCode() {
		String result =
			"precision mediump float;\n" +
			"varying vec2 vTextureCoord;" +
			"void main() {" +
			"   vec2 position = vTextureCoord;" +
			"	float radius = 0.5;" +
			"	float angle = 0.6;" +
			"vec2 center = vec2(0.5, 0.5);" +
			"vec2 tc = (position) - center;" +
			"float dist = length(tc);" +
			"if( dist < radius )" +
			"{" +
			"	float percent = (radius - dist) / radius;" +
			"	float theta = percent * percent * angle * 8.0;" +
			"	float s = sin(theta);" +
			"	float c = cos(theta);" +
			"	tc = vec2(dot(tc, vec2(c, -s)), dot(tc, vec2(s, c)));" +
			"}" +
			"tc += center;" +
			"   vec4 pixel = texture2D(sTexture, tc);" +
			"   gl_FragColor = pixel * vec4(1.0, 1.0, 1.0, 1.0);" +
           	"}";
		return result;
	}



    public glEffectSwirl() {
        super();
		name = NAME_Swirl;
    }





}
