precision mediump float;
varying mediump vec2 vTextureCoord;
uniform lowp mat4 uColorMatrix;
uniform lowp float uColorIntensity;
void main() {
    lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);
    lowp vec4 outputColor = textureColor * uColorMatrix;
    gl_FragColor = (uColorIntensity * outputColor) + ((1.0 - uColorIntensity) * textureColor);
}
