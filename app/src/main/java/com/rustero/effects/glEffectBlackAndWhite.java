
package com.rustero.effects;


public class glEffectBlackAndWhite extends glEffect {


    protected String getFragmentCode() {
        String result =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
            "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
            "    gl_FragColor = vec4(color, color, color, 1.0);\n" +
            "}\n";
        return result;
    }


    public glEffectBlackAndWhite() {
        super();
        name = NAME_BlackAndWhite;
    }





}
